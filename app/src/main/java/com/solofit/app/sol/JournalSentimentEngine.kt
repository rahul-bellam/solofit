package com.solofit.app.sol

/**
 * Local keyword-based journal sentiment — no GPT, no network. The *trend* across entries
 * matters more than any single entry, so callers usually classify a batch.
 */
object JournalSentimentEngine {

    private val positiveWords = setOf(
        "great", "happy", "productive", "excited", "energized", "energised",
        "grateful", "good", "thankful", "love", "enjoy", "blessed", "wonderful",
        "amazing", "peaceful", "calm", "strong", "focused", "rested", "motivated"
    )

    private val negativeWords = setOf(
        "stressed", "overwhelmed", "exhausted", "burned out", "burnout", "burntout",
        "tired", "frustrated", "struggle", "hard", "sad", "anxious", "stress",
        "difficult", "challenging", "worried", "drained", "fatigued", "sleepless"
    )

    /** Net sentiment of a single piece of text (positive hits minus negative hits). */
    fun score(text: String): Int {
        val t = text.lowercase()
        val pos = positiveWords.count { it in t }
        val neg = negativeWords.count { it in t }
        return pos - neg
    }

    /** Classify a batch of entries; null/empty input → null (not enough signal). */
    fun classify(texts: List<String>): JournalSentiment? {
        if (texts.isEmpty()) return null
        val net = texts.sumOf { score(it) }
        return when {
            net > 0 -> JournalSentiment.POSITIVE
            net < 0 -> JournalSentiment.CHALLENGING
            else -> JournalSentiment.NEUTRAL
        }
    }
}
