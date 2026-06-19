package com.solofit.app.sol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JournalSentimentEngineTest {

    @Test
    fun `positive entry scores positive`() {
        assertEquals(JournalSentiment.POSITIVE, JournalSentimentEngine.classify(
            listOf("Had a great and productive day, feeling energized")
        ))
    }

    @Test
    fun `negative entry scores challenging`() {
        assertEquals(JournalSentiment.CHALLENGING, JournalSentimentEngine.classify(
            listOf("Completely exhausted and overwhelmed, burned out from the deadline")
        ))
    }

    @Test
    fun `multi-word burnout phrase is detected`() {
        assertEquals(-1, JournalSentimentEngine.score("I feel burned out today"))
    }

    @Test
    fun `trend across entries decides the batch`() {
        val entries = listOf(
            "stressed and tired",
            "still exhausted",
            "good day finally"
        )
        // two negative entries outweigh one positive
        assertEquals(JournalSentiment.CHALLENGING, JournalSentimentEngine.classify(entries))
    }

    @Test
    fun `empty input is null`() {
        assertNull(JournalSentimentEngine.classify(emptyList()))
    }
}
