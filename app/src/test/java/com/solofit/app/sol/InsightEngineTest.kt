package com.solofit.app.sol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightEngineTest {

    private val engine = InsightEngine()

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
    fun `returns SolInsight with all required fields`() {
        val result = engine.compute(baseInput)
        assertTrue(result.headline.isNotEmpty())
        assertTrue(result.reasoning.isNotEmpty())
        assertTrue(result.recommendations.isNotEmpty())
        assertTrue(result.voiceLine.isNotEmpty())
    }

    @Test
    fun `streak 7 produces milestone insight`() {
        val input = baseInput.copy(streakDays = 7)
        val result = engine.compute(input)
        assertTrue(result.headline.contains("Seven", ignoreCase = true) || result.headline.contains("7", ignoreCase = true))
    }

    @Test
    fun `streak 30 produces milestone insight`() {
        val input = baseInput.copy(streakDays = 30)
        val result = engine.compute(input)
        assertTrue(result.headline.contains("Thirty", ignoreCase = true) || result.headline.contains("30", ignoreCase = true))
    }

    @Test
    fun `streak 100 produces milestone insight`() {
        val input = baseInput.copy(streakDays = 100)
        val result = engine.compute(input)
        assertTrue(result.headline.contains("Hundred", ignoreCase = true) || result.headline.contains("100", ignoreCase = true))
    }

    @Test
    fun `very low recovery triggers overtraining insight`() {
        val input = baseInput.copy(recoveryScore = 20)
        val result = engine.compute(input)
        assertEquals(InsightType.OVERTRAINING, result.type)
    }

    @Test
    fun `poor sleep triggers sleep insight`() {
        val input = baseInput.copy(sleepHours = 5.0, recoveryScore = 50)
        val result = engine.compute(input)
        assertEquals(InsightType.SLEEP, result.type)
    }

    @Test
    fun `very poor sleep triggers multi-day sleep insight`() {
        val input = baseInput.copy(sleepHours = 4.0, recoveryScore = 50, workoutToday = true)
        val result = engine.compute(input)
        assertEquals(InsightType.SLEEP, result.type)
        assertTrue(result.headline.contains("several") || result.headline.contains("below"))
    }

    @Test
    fun `sleep improved triggers sleep insight`() {
        val input = baseInput.copy(sleepHours = 8.0, previousSleepHours = 6.5, recoveryScore = 45)
        val result = engine.compute(input)
        assertEquals(InsightType.SLEEP, result.type)
        assertTrue(result.headline.contains("Improved", ignoreCase = true))
    }

    @Test
    fun `consistent sleep triggers sleep insight`() {
        val input = baseInput.copy(sleepHours = 7.5, previousSleepHours = 7.2, recoveryScore = 45, previousRecoveryScore = null)
        val result = engine.compute(input)
        assertEquals(InsightType.SLEEP, result.type)
        assertTrue(result.headline.contains("Consistent", ignoreCase = true))
    }

    @Test
    fun `protein goal met triggers nutrition insight`() {
        val input = baseInput.copy(
            consumedProtein = 150,
            targetProtein = 150
        )
        val result = engine.compute(input)
        assertEquals(InsightType.NUTRITION, result.type)
        assertTrue(result.headline.contains("Protein", ignoreCase = true))
    }

    @Test
    fun `workout logged triggers workout insight`() {
        val input = baseInput.copy(
            workoutToday = true,
            recoveryScore = 50
        )
        val result = engine.compute(input)
        assertEquals(InsightType.WORKOUT, result.type)
        assertTrue(result.headline.contains("Workout", ignoreCase = true))
    }

    @Test
    fun `workout streak triggers workout insight`() {
        val input = baseInput.copy(
            workoutToday = true,
            daysActiveThisWeek = 4,
            recoveryScore = 50
        )
        val result = engine.compute(input)
        assertEquals(InsightType.WORKOUT, result.type)
        assertTrue(result.headline.contains("Streak", ignoreCase = true))
    }

    @Test
    fun `personal best triggers workout insight`() {
        val input = baseInput.copy(
            strengthIncreasing = true,
            recoveryScore = 50,
            workoutToday = true
        )
        val result = engine.compute(input)
        assertTrue(result.type == InsightType.WORKOUT)
    }

    @Test
    fun `high step count triggers walking insight`() {
        val input = baseInput.copy(steps = 10000, recoveryScore = 45)
        val result = engine.compute(input)
        assertEquals(InsightType.WALKING, result.type)
    }

    @Test
    fun `walking improved triggers walking insight`() {
        val input = baseInput.copy(steps = 7000, previousSteps = 5000, recoveryScore = 45)
        val result = engine.compute(input)
        assertEquals(InsightType.WALKING, result.type)
        assertTrue(result.headline.contains("Increased", ignoreCase = true))
    }

    @Test
    fun `recovery improving triggers recovery insight`() {
        val input = baseInput.copy(
            recoveryScore = 45,
            previousRecoveryScore = 30
        )
        val result = engine.compute(input)
        assertEquals(InsightType.RECOVERY, result.type)
    }

    @Test
    fun `recovery declining triggers recovery insight`() {
        val input = baseInput.copy(
            recoveryScore = 40,
            previousRecoveryScore = 60
        )
        val result = engine.compute(input)
        assertEquals(InsightType.RECOVERY, result.type)
    }

    @Test
    fun `positive recomposition triggers body recomposition insight`() {
        val input = baseInput.copy(
            measurementImproving = true,
            strengthIncreasing = true,
            recoveryScore = 50,
            previousRecoveryScore = null,
            sleepHours = 7.0,
            workoutToday = true
        )
        val result = engine.compute(input)
        assertEquals(InsightType.BODY_RECOMP, result.type)
    }

    @Test
    fun `stress improved triggers meditation insight`() {
        val input = baseInput.copy(
            stressLevel = 3,
            previousStressLevel = 5,
            meditationMinutes = 5,
            daysActiveThisWeek = 3,
            recoveryScore = 45,
            previousRecoveryScore = null,
            sleepHours = 7.0
        )
        val result = engine.compute(input)
        assertEquals(InsightType.MEDITATION, result.type)
        assertTrue(result.headline.contains("Improving", ignoreCase = true))
    }

    @Test
    fun `high stress with meditation triggers meditation insight`() {
        val input = baseInput.copy(
            stressLevel = 5,
            meditationMinutes = 5,
            daysActiveThisWeek = 3,
            recoveryScore = 45,
            previousRecoveryScore = null,
            sleepHours = 7.0
        )
        val result = engine.compute(input)
        assertEquals(InsightType.MEDITATION, result.type)
    }

    @Test
    fun `journal streak triggers journal insight`() {
        val input = baseInput.copy(
            journalDays = 5,
            recoveryScore = 45,
            previousRecoveryScore = null,
            sleepHours = 7.0
        )
        val result = engine.compute(input)
        assertEquals(InsightType.JOURNAL, result.type)
    }

    @Test
    fun `journal positive sentiment triggers positive trend insight`() {
        val input = baseInput.copy(
            journalDays = 2,
            journalSentiment = JournalSentiment.POSITIVE,
            recoveryScore = 45,
            previousRecoveryScore = null,
            sleepHours = 7.0
        )
        val result = engine.compute(input)
        assertEquals(InsightType.JOURNAL, result.type)
        assertTrue(result.headline.contains("Positive", ignoreCase = true))
    }

    @Test
    fun `journal challenging sentiment triggers challenging insight`() {
        val input = baseInput.copy(
            journalDays = 2,
            journalSentiment = JournalSentiment.CHALLENGING,
            recoveryScore = 45,
            previousRecoveryScore = null,
            sleepHours = 7.0
        )
        val result = engine.compute(input)
        assertEquals(InsightType.JOURNAL, result.type)
    }

    @Test
    fun `recommendations are always non-empty`() {
        val result = engine.compute(baseInput)
        assertTrue(result.recommendations.isNotEmpty())
        assertTrue(result.recommendations.all { it.isNotBlank() })
    }

    @Test
    fun `reasoning reflects input data`() {
        val result = engine.compute(baseInput)
        assertTrue(result.reasoning.isNotEmpty())
    }

    @Test
    fun `training volume increase with low recovery triggers overtraining`() {
        val input = baseInput.copy(
            recentTrainingVolumeIncrease = true,
            recoveryScore = 45
        )
        val result = engine.compute(input)
        assertEquals(InsightType.OVERTRAINING, result.type)
    }

    @Test
    fun `training volume increase with very low recovery triggers recovery warning`() {
        val input = baseInput.copy(
            recentTrainingVolumeIncrease = true,
            recoveryScore = 35
        )
        val result = engine.compute(input)
        assertEquals(InsightType.OVERTRAINING, result.type)
    }
}
