package com.solofit.app.sol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

/**
 * Deterministic tests for [InsightEngine.computeBriefing].
 *
 * The engine's primary-insight selection is priority-based but a few candidates
 * are gated by time of day (morning/evening). Every test therefore pins the clock
 * to a fixed **afternoon** time (14:00) — neither morning (<12) nor evening (>=17) —
 * so only time-independent candidates compete and the winner is fully determined by
 * the priority constants in WellnessThresholds. This removes wall-clock flakiness.
 */
class InsightEngineTest {

    private val engine = InsightEngine()
    private val afternoon: LocalTime = LocalTime.of(14, 0)

    private fun briefing(input: SolInput) = engine.computeBriefing(input, now = afternoon)

    private val baseInput = SolInput(
        recoveryScore = 60,
        previousRecoveryScore = 55,
        streakDays = 0,
        daysActiveThisWeek = 0,
        workoutToday = false,
        consumedCalories = 0,
        consumedProtein = 0,
        targetCalories = 2000,
        targetProtein = 150,
        sleepHours = 7.0,
        previousSleepHours = null,
        steps = 0,
        previousSteps = null,
        waterMl = 0,
        waterGoalMl = 3000,
        energyScore = 5,
        stressLevel = 3,
        previousStressLevel = null,
        moodScore = 5,
        meditationMinutes = 0,
        previousMeditationMinutes = null,
        journalDays = 0,
        journalSentiment = null,
        measurementImproving = false,
        strengthIncreasing = false,
        phaseDay = 0,
        phaseTargetDays = 90,
        historySessionCount = 0,
        recentTrainingVolumeIncrease = false,
        daysTracked = 1
    )

    @Test
    fun `primary insight is fully populated when data is sufficient`() {
        val primary = briefing(baseInput).primary
        assertTrue(primary.headline.isNotBlank())
        assertTrue(primary.reasoning.isNotEmpty())
        assertTrue(primary.recommendations.isNotEmpty())
        assertTrue(primary.recommendations.all { it.isNotBlank() })
        assertTrue(primary.voiceLine.isNotBlank())
    }

    @Test
    fun `no tracked days yields an empty-state briefing`() {
        val result = engine.computeBriefing(baseInput.copy(daysTracked = 0), now = afternoon)
        assertFalse(result.hasSufficientData)
        assertTrue(result.emptyMessage.isNotBlank())
    }

    @Test
    fun `streak milestones surface as the primary insight`() {
        assertTrue(briefing(baseInput.copy(streakDays = 7)).primary.headline.contains("Seven", ignoreCase = true))
        assertTrue(briefing(baseInput.copy(streakDays = 30)).primary.headline.contains("Thirty", ignoreCase = true))
        assertTrue(briefing(baseInput.copy(streakDays = 100)).primary.headline.contains("Hundred", ignoreCase = true))
    }

    @Test
    fun `very low recovery outranks everything as overtraining`() {
        val primary = briefing(baseInput.copy(recoveryScore = 20)).primary
        assertEquals(InsightType.OVERTRAINING, primary.type)
    }

    @Test
    fun `protein goal met surfaces a nutrition insight`() {
        val primary = briefing(baseInput.copy(consumedProtein = 150, targetProtein = 150)).primary
        assertEquals(InsightType.NUTRITION, primary.type)
        assertTrue(primary.headline.contains("Protein", ignoreCase = true))
    }

    @Test
    fun `logging a workout surfaces a workout insight`() {
        val primary = briefing(baseInput.copy(workoutToday = true)).primary
        assertEquals(InsightType.WORKOUT, primary.type)
    }

    @Test
    fun `hitting the step goal surfaces a walking insight`() {
        val primary = briefing(baseInput.copy(steps = 10000, recoveryScore = 45)).primary
        assertEquals(InsightType.WALKING, primary.type)
    }

    @Test
    fun `improving recovery surfaces a recovery insight`() {
        val primary = briefing(baseInput.copy(recoveryScore = 45, previousRecoveryScore = 30)).primary
        assertEquals(InsightType.RECOVERY, primary.type)
        assertTrue(primary.headline.contains("Improving", ignoreCase = true))
    }

    @Test
    fun `consistent meditation surfaces a meditation insight`() {
        val primary = briefing(
            baseInput.copy(
                meditationMinutes = 5,
                daysActiveThisWeek = 5,
                recoveryScore = 45,
                previousRecoveryScore = null
            )
        ).primary
        assertEquals(InsightType.MEDITATION, primary.type)
    }

    @Test
    fun `journaling streak surfaces a journal insight`() {
        val primary = briefing(
            baseInput.copy(journalDays = 5, recoveryScore = 45, previousRecoveryScore = null)
        ).primary
        assertEquals(InsightType.JOURNAL, primary.type)
    }

    @Test
    fun `positive journal sentiment surfaces a journal insight`() {
        val primary = briefing(
            baseInput.copy(
                journalDays = 2,
                journalSentiment = JournalSentiment.POSITIVE,
                recoveryScore = 45,
                previousRecoveryScore = null
            )
        ).primary
        assertEquals(InsightType.JOURNAL, primary.type)
        assertTrue(primary.headline.contains("Positive", ignoreCase = true))
    }
}
