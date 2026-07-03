package com.solofit.app.ui.scan

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.BuildConfig
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.data.remote.GeminiGenerationConfig
import com.solofit.app.data.remote.GeminiInlineData
import com.solofit.app.data.remote.GeminiPart
import com.solofit.app.data.remote.GeminiContent
import com.solofit.app.data.remote.GeminiRequest
import com.solofit.app.data.remote.GeminiService
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

sealed interface AiScanResult {
    data class Success(
        val name: String,
        val caloriesPer100g: Double,
        val proteinPer100g: Double,
        val carbsPer100g: Double,
        val fatsPer100g: Double,
        val fiberPer100g: Double,
        val estimatedGrams: Double
    ) : AiScanResult

    data class Error(val message: String) : AiScanResult
}

@Serializable
data class AiFoodJson(
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatsPer100g: Double,
    val fiberPer100g: Double = 0.0,
    val estimatedGrams: Double = 200.0
)

private val AI_PROMPT = """
You are a nutrition estimator. Identify the food in the image and estimate its nutrition PER 100 GRAMS.
Use realistic values for common foods. If the dish is mixed (e.g. curry, biryani, salad, sandwich),
give a sensible overall estimate for the whole dish.
Return a JSON object with exactly these keys:
{"name": string, "caloriesPer100g": number, "proteinPer100g": number, "carbsPer100g": number, "fatsPer100g": number, "fiberPer100g": number, "estimatedGrams": number}
"estimatedGrams" is the approximate total grams of food visible (use 200 if unsure).
If you cannot recognize any food, set "name" to "" and all numbers to 0.
""".trimIndent()

private fun bitmapToBase64(bitmap: Bitmap): String {
    val maxDimension = 1024
    val scale = minOf(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height, 1f)
    val scaled = if (scale < 1f) {
        Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
    } else bitmap
    val stream = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
    if (scaled != bitmap) scaled.recycle()
    return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
}

private val json = Json { ignoreUnknownKeys = true }

/**
 * Pulls the JSON object out of a model reply. With JSON mode this is usually already
 * clean, but this also survives stray markdown fences or prose by taking the substring
 * between the first '{' and the last '}'.
 */
private fun extractJsonObject(raw: String): String {
    val start = raw.indexOf('{')
    val end = raw.lastIndexOf('}')
    return if (start in 0 until end) raw.substring(start, end + 1) else raw.trim()
}

@HiltViewModel
class AiFoodScanViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val foodRepository: FoodRepository,
    private val dailyLogRepository: DailyLogRepository
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanResult = MutableSharedFlow<AiScanResult>(extraBufferCapacity = 1)
    val scanResult = _scanResult.asSharedFlow()

    private val mutex = Mutex()
    private val requestTimestamps = mutableListOf<Long>()
    private val minIntervalMs = 5_000L
    private val maxRequestsPerMinute = 5L
    private val windowMs = 60_000L

    private suspend fun checkRateLimit(): Boolean = mutex.withLock {
        val now = System.currentTimeMillis()
        requestTimestamps.removeAll { now - it > windowMs }
        if (requestTimestamps.size >= maxRequestsPerMinute.toInt()) return@withLock true
        if (requestTimestamps.isNotEmpty() && now - requestTimestamps.last() < minIntervalMs) return@withLock true
        requestTimestamps.add(now)
        return@withLock false
    }

    /**
     * Analyzes [bitmap] and recycles it when done. This ViewModel takes sole
     * ownership of the bitmap on EVERY code path (early guard, rate-limit,
     * key checks, success, and error) — callers must NOT recycle it, or they'd
     * race the async encode/upload and hit "Can't compress a recycled bitmap".
     */
    fun analyzeFood(bitmap: Bitmap) {
        if (_isScanning.value) {
            bitmap.recycle()
            return
        }
        _isScanning.value = true
        viewModelScope.launch {
            if (checkRateLimit()) {
                _scanResult.tryEmit(
                    AiScanResult.Error("Rate limited — max $maxRequestsPerMinute scans per minute, ${minIntervalMs / 1000}s between scans.")
                )
                _isScanning.value = false
                bitmap.recycle()
                return@launch
            }

            if (BuildConfig.GEMINI_API_KEY.isBlank() ||
                BuildConfig.GEMINI_API_KEY == "YOUR_GEMINI_API_KEY"
            ) {
                _scanResult.tryEmit(
                    AiScanResult.Error("Set GEMINI_API_KEY in app/build.gradle.kts")
                )
                _isScanning.value = false
                bitmap.recycle()
                return@launch
            }
            if (!BuildConfig.GEMINI_API_KEY.startsWith("AIza")) {
                _scanResult.tryEmit(
                    AiScanResult.Error("Invalid API key format. Get a valid Gemini API key from aistudio.google.com.")
                )
                _isScanning.value = false
                bitmap.recycle()
                return@launch
            }
            try {
                val base64 = withContext(Dispatchers.IO) { bitmapToBase64(bitmap) }
                val response = withContext(Dispatchers.IO) {
                    geminiService.generateContent(
                        request = GeminiRequest(
                            contents = listOf(
                                GeminiContent(
                                    parts = listOf(
                                        GeminiPart(text = AI_PROMPT),
                                        GeminiPart(
                                            inlineData = GeminiInlineData(
                                                mimeType = "image/jpeg",
                                                data = base64
                                            )
                                        )
                                    )
                                )
                            ),
                            generationConfig = GeminiGenerationConfig(
                                responseMimeType = "application/json",
                                temperature = 0.2
                            )
                        )
                    )
                }

                // The model can decline an image for safety reasons → no usable candidate.
                response.promptFeedback?.blockReason?.let {
                    throw Exception("No response from AI")
                }

                val text = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()
                    ?.text?.trim()
                    ?: throw Exception("No response from AI")

                val aiFood = withContext(Dispatchers.IO) {
                    json.decodeFromString<AiFoodJson>(extractJsonObject(text))
                }

                if (aiFood.name.isBlank() || aiFood.caloriesPer100g <= 0.0) {
                    _scanResult.tryEmit(
                        AiScanResult.Error("Couldn't recognize a food. Try a clearer, closer photo.")
                    )
                    return@launch
                }

                _scanResult.tryEmit(
                    AiScanResult.Success(
                        name = aiFood.name.trim(),
                        caloriesPer100g = aiFood.caloriesPer100g,
                        proteinPer100g = aiFood.proteinPer100g,
                        carbsPer100g = aiFood.carbsPer100g,
                        fatsPer100g = aiFood.fatsPer100g,
                        fiberPer100g = aiFood.fiberPer100g,
                        estimatedGrams = aiFood.estimatedGrams
                    )
                )
            } catch (e: HttpException) {
                val code = e.code()
                val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                val msg = when {
                    code == 400 && body?.contains("API_KEY_INVALID", ignoreCase = true) == true ->
                        "Invalid API key. Check the GEMINI_API_KEY in your .env file."
                    code == 400 || code == 403 ->
                        "AI service rejected the request (HTTP $code). Check your API key."
                    code == 429 ->
                        "AI service rate limit exceeded. Wait a moment and try again."
                    code in 500..599 ->
                        "AI service temporarily unavailable (HTTP $code). Try again later."
                    else ->
                        "AI service error (HTTP $code). Try again."
                }
                _scanResult.tryEmit(AiScanResult.Error(msg))
            } catch (e: IOException) {
                val msg = when {
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Request timed out. Check your internet connection."
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                        e.message?.contains("network", ignoreCase = true) == true ->
                        "Network connection lost. Check your internet."
                    else ->
                        "Unable to upload image. Check your connection."
                }
                _scanResult.tryEmit(AiScanResult.Error(msg))
            } catch (e: kotlinx.serialization.SerializationException) {
                _scanResult.tryEmit(AiScanResult.Error("Food could not be identified. Try a clearer photo."))
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("No response", ignoreCase = true) == true ->
                        "AI could not analyze this image. Try a different angle or lighting."
                    e.message?.contains("parse", ignoreCase = true) == true ||
                        e.message?.contains("JSON", ignoreCase = true) == true ->
                        "Food could not be identified. Try a clearer photo."
                    else ->
                        "AI scan failed. Try again."
                }
                _scanResult.tryEmit(AiScanResult.Error(msg))
            } finally {
                _isScanning.value = false
                bitmap.recycle()
            }
        }
    }
}
