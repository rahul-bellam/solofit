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
    val previousMeditationMinutes: Int?,
    val journalDays: Int,
    val journalSentiment: JournalSentiment?,
    val measurementImproving: Boolean?,
    val strengthIncreasing: Boolean?,
    val phaseDay: Int,
    val phaseTargetDays: Int,
    val historySessionCount: Int,
    val recentTrainingVolumeIncrease: Boolean,
    val daysTracked: Int = 0,
    val weeklySteps: List<Int> = emptyList(),
    val weeklyRecovery: List<Int> = emptyList(),
    val weeklyProteinPct: List<Double> = emptyList(),
    val weeklySleep: List<Double> = emptyList(),
    val baseline: UserBaseline? = null
)

enum class JournalSentiment {
    POSITIVE, NEUTRAL, CHALLENGING
}

enum class DayLabel(val displayName: String, val description: String) {
    RECOVERY_FOCUS("Recovery Focus Day", "Prioritise rest and recovery today"),
    PERFORMANCE("Performance Day", "Your body is ready for a strong session"),
    NUTRITION_FOCUS("Nutrition Focus Day", "Fuel and hydration deserve extra attention today"),
    CONSISTENCY("Consistency Day", "Small actions compound over time"),
    MINDFULNESS("Mindfulness Day", "Mental recovery and reflection take centre stage"),
    BALANCED("Balanced Day", "Everything is within a healthy range")
}

enum class SignalStatus { GOOD, ON_TRACK, LOW }

data class SignalSummary(
    val label: String,
    val status: SignalStatus,
    val detail: String
)

data class TrendSummary(
    val label: String,
    val direction: TrendDirection,
    val percentage: Int,
    val values: List<Int>,
    val status: SignalStatus
)

enum class TrendDirection(val arrow: String) {
    UP("↑"),
    DOWN("↓"),
    STABLE("→")
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

data class Script(
    val headline: String,
    val detail: String,
    val recommendation: String,
    val reasoning: List<String>,
    val recommendations: List<String>,
    val voiceLine: String,
    val type: InsightType
)

data class SolBriefing(
    val greeting: String,
    val primary: Script,
    val supplementary: List<Script>,
    val dayLabel: DayLabel,
    val signals: List<SignalSummary>,
    val trends: List<TrendSummary> = emptyList(),
    val hasSufficientData: Boolean = true,
    val daysTracked: Int = 0,
    val emptyMessage: String = ""
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
    CONSISTENCY,
    EMPTY_STATE
}

enum class VoicePersonality(val displayName: String) {
    COACH("Coach"),
    COMPANION("Companion"),
    MINIMAL("Minimal")
}
