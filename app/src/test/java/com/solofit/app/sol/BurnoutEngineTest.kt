package com.solofit.app.sol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BurnoutEngineTest {

    @Test
    fun `stable healthy week is low risk with no insight`() {
        val result = BurnoutEngine.assess(
            BurnoutInput(
                weeklySleep = listOf(7.5, 7.5, 7.0, 7.5, 7.0, 7.5, 7.0),
                weeklyRecovery = listOf(70, 72, 71, 73, 70, 74, 72),
                weeklySteps = listOf(8000, 8200, 8100, 8300, 8000, 8400, 8200),
                recoveryScore = 72,
                sleepHours = 7.0,
                stressLevel = 2,
                journalSentiment = JournalSentiment.POSITIVE
            )
        )
        assertEquals(BurnoutLevel.LOW, result.level)
        assertNull(result.insight)
        assertEquals("Maintain", result.recommendedFocus)
    }

    @Test
    fun `declining sleep and recovery with short-sleep streak triggers recovery spiral`() {
        val result = BurnoutEngine.assess(
            BurnoutInput(
                weeklySleep = listOf(7.0, 6.5, 6.0, 5.5, 5.0, 4.5, 4.5),
                weeklyRecovery = listOf(65, 60, 55, 48, 42, 38, 34),
                weeklySteps = listOf(7000, 6500, 6000, 5500, 5000, 4500, 4000),
                recoveryScore = 34,
                sleepHours = 4.5,
                stressLevel = 4,
                journalSentiment = JournalSentiment.CHALLENGING
            )
        )
        assertTrue(result.score >= 65)
        assertEquals(BurnoutLevel.HIGH, result.level)
        assertNotNull(result.insight)
        assertEquals("Recovery Is Slipping", result.insight!!.title)
        assertEquals("Recovery", result.recommendedFocus)
        assertTrue(result.contributors.any { it.label == "Sleep" })
    }

    @Test
    fun `rising training load with falling sleep flags workload imbalance`() {
        val result = BurnoutEngine.assess(
            BurnoutInput(
                weeklySleep = listOf(7.5, 7.0, 6.8, 6.5, 6.4, 6.3, 6.2),
                weeklyRecovery = listOf(70, 68, 66, 64, 62, 60, 58),
                weeklySteps = listOf(8000, 8200, 8400, 8600, 8800, 9000, 9200),
                recoveryScore = 58,
                sleepHours = 6.2,
                stressLevel = 3,
                trainingLoadIncreasing = true
            )
        )
        assertEquals("Training Load Is Outpacing Recovery", result.insight?.title)
    }

    @Test
    fun `high stress, low recovery, negative journal flags stress accumulation`() {
        val result = BurnoutEngine.assess(
            BurnoutInput(
                weeklySleep = listOf(6.5, 6.6, 6.5, 6.6, 6.5, 6.6, 6.5), // flat — not a spiral
                weeklyRecovery = listOf(48, 47, 46, 47, 46, 45, 46),
                weeklySteps = listOf(6000, 6100, 6000, 6100, 6000, 6100, 6000),
                recoveryScore = 46,
                sleepHours = 6.5,
                stressLevel = 5,
                journalSentiment = JournalSentiment.CHALLENGING
            )
        )
        assertEquals("Stress Has Been Elevated Recently", result.insight?.title)
        assertTrue(result.contributors.any { it.label == "Stress" })
    }

    @Test
    fun `empty input does not crash and is low risk`() {
        val result = BurnoutEngine.assess(BurnoutInput())
        assertEquals(BurnoutLevel.LOW, result.level)
        assertTrue(result.score in 0..100)
    }
}
