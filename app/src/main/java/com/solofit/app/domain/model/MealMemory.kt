package com.solofit.app.domain.model

/**
 * Normalizes meal names so similar foods map together.
 *
 * Examples:
 *   "Chicken Curry"        -> "chicken curry"
 *   "chicken masala curry" -> "chicken masala curry"
 *   "Curry, Chicken"       -> "chicken curry"
 *   "  CHICKEN  CURRY  "   -> "chicken curry"
 */
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
