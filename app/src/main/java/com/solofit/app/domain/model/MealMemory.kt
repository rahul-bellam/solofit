package com.solofit.app.domain.model

object MealNameNormalizer {
    private val punctuation = Regex("[,\\.!?;:()\\[\\]{}「」『』【】]")
    private val extraSpaces = Regex("\\s+")

    fun normalize(raw: String): String {
        if (raw.isBlank()) return raw.lowercase().trim()
        return raw
            .lowercase()
            .trim()
            .replace(punctuation, " ")
            .replace(extraSpaces, " ")
            .trim()
    }
}
