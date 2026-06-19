package com.solofit.app.data.remote

import com.solofit.app.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiService {
    // gemini-2.5-flash: current multimodal (vision) Flash model with JSON-mode support.
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String = BuildConfig.GEMINI_API_KEY,
        @Body request: GeminiRequest
    ): GeminiResponse

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
    }
}

// --- Request DTOs ---

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    @SerialName("inlineData") val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

/**
 * Forces structured output. `responseMimeType = application/json` makes the model
 * return raw JSON (no markdown fences / prose), which the scan flow can parse directly.
 */
@Serializable
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Double? = null
)

// --- Response DTOs ---

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    @SerialName("promptFeedback") val promptFeedback: GeminiPromptFeedback? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    @SerialName("finishReason") val finishReason: String? = null
)

@Serializable
data class GeminiPromptFeedback(
    @SerialName("blockReason") val blockReason: String? = null
)
