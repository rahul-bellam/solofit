package com.solofit.app.sol

/**
 * Determines how reliable a nutrition estimate is for a given food.
 * Returns HIGH, MEDIUM, or LOW as a string so UI can map to its own enum.
 */
object NutritionConfidenceEngine {

    private val brandedKeywords = setOf(
        "protein", "whey", "casein", "bar", "shake", "powder", "greek", "yogurt",
        "milk", "bread", "pasta", "rice", "chicken breast", "egg", "eggs",
        "banana", "apple", "orange", "almond", "peanut butter", "oat", "oats",
        "tuna", "salmon", "tofu", "paneer"
    )

    private val mixedKeywords = setOf(
        "curry", "gravy", "stew", "soup", "casserole", "biryani", "pilaf",
        "pulao", "dal", "dahl", "sabzi", "bhaji", "masala", "tikka", "korma",
        "kebab", "roll", "wrap", "sandwich", "burger", "pizza", "pasta",
        "salad", "bowl", "stir fry", "fried rice", "noodle", "ramen",
        "mixed", "homemade", "restaurant", "takeaway", "takeout"
    )

    enum class Level { HIGH, MEDIUM, LOW }

    fun fromSource(isBarcode: Boolean, name: String): Level {
        val lower = name.lowercase()

        if (isBarcode) return Level.HIGH

        val containsMixed = mixedKeywords.any { lower.contains(it) }
        if (containsMixed) return Level.LOW

        val containsBranded = brandedKeywords.any { lower.contains(it) }
        if (containsBranded) return Level.HIGH

        return Level.MEDIUM
    }
}
