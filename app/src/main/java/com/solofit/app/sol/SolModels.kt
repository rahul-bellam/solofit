package com.solofit.app.sol

data class SolInput(
    val recoveryScore: Int?,
    val previousRecoveryScore: Int?,
    val streakDays: Int,
    val daysActiveThisWeek: Int,
    val workoutToday: Boolean,
    val consumedCalories: Int,
    val consumedProtein: Int,
    val targetCalories: Int,
    val targetProtein: Int,
    val sleepHours: Double?,
    val previousSleepHours: Double?,
    val steps: Int?,
    val previousSteps: Int?,
    val waterMl: Int,
    val waterGoalMl: Int,
    val energyScore: Int?,
    val stressLevel: Int?,
    val previousStressLevel: Int?,
    val moodScore: Int?,
    val meditationMinutes: Int?,
    val journalDays: Int,
    val journalSentiment: JournalSentiment?,
    val measurementImproving: Boolean?,
    val strengthIncreasing: Boolean?,
    val phaseDay: Int,
    val phaseTargetDays: Int,
    val historySessionCount: Int,
    val recentTrainingVolumeIncrease: Boolean
)

enum class JournalSentiment {
    POSITIVE, NEUTRAL, CHALLENGING
}

data class SolInsight(
    val greeting: String,
    val headline: String,
    val detail: String,
    val reasoning: List<String>,
    val recommendations: List<String>,
    val voiceLine: String,
    val type: InsightType
)

enum class InsightType {
    MORNING_GREETING,
    SLEEP,
    WORKOUT,
    OVERTRAINING,
    NUTRITION,
    BODY_RECOMP,
    WALKING,
    RECOVERY,
    MEDITATION,
    JOURNAL,
    STREAK_MILESTONE,
    WATER,
    EVENING,
    WELLNESS,
    CONSISTENCY
}

enum class VoicePersonality(val displayName: String) {
    COACH("Coach"),
    COMPANION("Companion"),
    MINIMAL("Minimal")
}
