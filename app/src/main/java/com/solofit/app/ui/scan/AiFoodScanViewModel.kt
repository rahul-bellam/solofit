package com.solofit.app.ui.scan

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.BuildConfig
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.data.remote.GeminiInlineData
import com.solofit.app.data.remote.GeminiPart
import com.solofit.app.data.remote.GeminiContent
import com.solofit.app.data.remote.GeminiRequest
import com.solofit.app.data.remote.GeminiService
import com.solofit.app.domain.model.MealCategory
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.time.LocalTime
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
Analyze this food image. Return ONLY valid JSON with no markdown formatting, no code fences:
{"name": "food name", "caloriesPer100g": number, "proteinPer100g": number, "carbsPer100g": number, "fatsPer100g": number, "fiberPer100g": number, "estimatedGrams": number}
Focus on proteins, carbs, fats, and fibers. Default estimatedGrams to 200 if unsure.
""".trimIndent()

private fun bitmapToBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
    return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
}

private fun inferMealCategory(): MealCategory {
    val hour = LocalTime.now().hour
    return when {
        hour in 5..10 -> MealCategory.BREAKFAST
        hour in 11..13 -> MealCategory.LUNCH
        hour in 14..16 -> MealCategory.SNACKS
        hour in 17..20 -> MealCategory.DINNER
        else -> MealCategory.SNACKS
    }
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

    private val requestTimestamps = mutableListOf<Long>()
    private val minIntervalMs = 5_000L
    private val maxRequestsPerMinute = 5L
    private val windowMs = 60_000L

    private fun checkRateLimit(): Boolean {
        val now = System.currentTimeMillis()
        requestTimestamps.removeAll { now - it > windowMs }
        if (requestTimestamps.size >= maxRequestsPerMinute.toInt()) return true
        if (requestTimestamps.isNotEmpty() && now - requestTimestamps.last() < minIntervalMs) return true
        requestTimestamps.add(now)
        return false
    }

    fun analyzeAndLog(bitmap: Bitmap) {
        if (_isScanning.value) return
        if (checkRateLimit()) {
            _scanResult.tryEmit(
                AiScanResult.Error("Rate limited — max $maxRequestsPerMinute scans per minute, ${minIntervalMs / 1000}s between scans.")
            )
            return
        }

        if (BuildConfig.GEMINI_API_KEY.isBlank() ||
            BuildConfig.GEMINI_API_KEY == "YOUR_GEMINI_API_KEY"
        ) {
            _scanResult.tryEmit(
                AiScanResult.Error("Set GEMINI_API_KEY in app/build.gradle.kts")
            )
            return
        }

        viewModelScope.launch {
            _isScanning.value = true
            try {
                val base64 = bitmapToBase64(bitmap)
                val response = geminiService.generateContent(
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
                        )
                    )
                )

                val text = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()
                    ?.text?.trim()
                    ?: throw Exception("No response from AI")

                val cleaned = text
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val json = Json { ignoreUnknownKeys = true }
                val aiFood = json.decodeFromString<AiFoodJson>(cleaned)

                val foodId = foodRepository.addCustomFood(
                    FoodItemEntity(
                        name = aiFood.name.trim(),
                        category = "AI Scan",
                        caloriesPer100g = aiFood.caloriesPer100g,
                        proteinPer100g = aiFood.proteinPer100g,
                        carbsPer100g = aiFood.carbsPer100g,
                        fatsPer100g = aiFood.fatsPer100g,
                        fiberPer100g = aiFood.fiberPer100g,
                        isCustom = true
                    )
                )

                dailyLogRepository.logFood(
                    DailyLogEntity(
                        date = DateUtils.today(),
                        foodId = foodId,
                        gramsConsumed = aiFood.estimatedGrams.coerceAtLeast(10.0),
                        mealCategory = inferMealCategory().name
                    )
                )

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
            } catch (e: java.net.SocketTimeoutException) {
                _scanResult.tryEmit(AiScanResult.Error("Request timed out. Check your internet."))
            } catch (e: Exception) {
                _scanResult.tryEmit(
                    AiScanResult.Error(e.message ?: "AI scan failed. Try again.")
                )
            } finally {
                _isScanning.value = false
            }
        }
    }
}
