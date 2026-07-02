package com.solofit.app.sol

import androidx.compose.ui.graphics.Color

// ── Today's Theme (Daily Narrative — Point 5) ──
enum class DayTheme(
    val displayName: String,
    val description: String
) {
    RECOVERY("Recovery Day", "Your best investment today is restoring energy"),
    MOVEMENT("Movement Day", "Staying active is today's anchor habit"),
    NUTRITION("Nutrition Day", "Fuel and hydration deserve extra attention today"),
    PERFORMANCE("Performance Day", "Your body is ready for a strong session"),
    MINDFULNESS("Mindfulness Day", "Mental recovery and reflection take centre stage"),
    CONSISTENCY("Consistency Day", "Small actions compound — just show up"),
    REST("Rest Day", "True progress sometimes means doing nothing"),
    BUILDING("Building Day", "Foundations are still forming — keep going"),
    BALANCED("Balanced Day", "Everything is within a healthy range")
}

fun DayTheme.themeColor(): Color = when (this) {
    DayTheme.RECOVERY -> Color(0xFF6E655B)   // Slate Bronze
    DayTheme.MOVEMENT -> Color(0xFF827A6E)   // Weathered Stone
    DayTheme.NUTRITION -> Color(0xFF7A6B4A)  // Olive Leather
    DayTheme.PERFORMANCE -> Color(0xFFB45F38) // Burnished Copper
    DayTheme.MINDFULNESS -> Color(0xFF8A7C72) // Smoked Taupe
    DayTheme.CONSISTENCY -> Color(0xFF8B7355) // Weathered Bronze
    DayTheme.REST -> Color(0xFF756F69)        // TextSecondary light
    DayTheme.BUILDING -> Color(0xFFC98A3D)    // SolGold
    DayTheme.BALANCED -> Color(0xFF8B7355)    // Weathered Bronze
}

// ── Life Context (Point 6) ──
data class LifeContext(
    val situation: String = "",
    val active: Boolean = false,
    val setAt: Long = 0L
) {
    companion object {
        val PRESETS = mapOf(
            "busy" to "Busy Week",
            "travelling" to "Travelling",
            "exams" to "Exams",
            "night_shifts" to "Night Shifts",
            "working_from_home" to "Working from Home",
            "vacation" to "Vacation",
            "injured" to "Injured",
            "sick" to "Sick"
        )
    }
}

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
    val type: InsightType,
    // ── Causal explanation (Point 7) — why something changed ──
    val causalExplanation: String = ""
)

data class SolBriefing(
    val greeting: String,
    val primary: Script,
    val supplementary: List<Script>,
    val dayLabel: DayLabel,
    // ── Daily Narrative (Point 5) ──
    val todayTheme: DayTheme = DayTheme.BALANCED,
    val themeReason: String = "",
    // ── Causal explanation for the primary signal change (Point 7) ──
    val causalExplanation: String = "",
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

// ──────────────────────────────────────────────────────────────────────────────
//  UserTwin — the central data model every module contributes to.
//  Sol reads from UserTwin.  UI screens never write to Sol directly.
// ──────────────────────────────────────────────────────────────────────────────

/** A structured signal contributed by a single module for a given window. */
data class ModuleSignal(
    val source: String,        // e.g. "sleep", "workouts", "nutrition"
    val label: String,         // e.g. "Sleep Duration", "Protein Adherence"
    val currentValue: Double?,
    val previousValue: Double?,
    val unit: String = "",
    val trend: TrendDirection = TrendDirection.STABLE,
    val status: SignalStatus = SignalStatus.ON_TRACK,
    val insight: String = ""   // What this signal means for the user right now
)

/** High-level assessment of where the user is today. */
enum class WellnessState {
    THRIVING, MAINTAINING, STRUGGLING, AT_RISK, INSUFFICIENT_DATA
}

/** A risk factor Sol monitors across modules. */
data class RiskFactor(
    val domain: String,
    val severity: Float,        // 0.0 – 1.0
    val signal: String,
    val trend: TrendDirection,
    val daysActive: Int = 0
)

/** Something the user is doing well — used by Sol for positive reinforcement. */
data class Strength(
    val domain: String,
    val label: String,
    val streakOrConsistency: String,
    val impact: String
)

/** Trends across 7d / 30d / 90d windows. */
data class WellnessTrend(
    val label: String,
    val direction: TrendDirection,
    val pctChange: Int,
    val windowDays: Int,         // 7, 30, or 90
    val status: SignalStatus
)

/** Consistency profile — how reliably the user shows up in each domain. */
data class ConsistencyProfile(
    val workoutConsistency: Float,   // 0.0 – 1.0
    val nutritionConsistency: Float,
    val movementConsistency: Float,
    val meditationConsistency: Float,
    val journalConsistency: Float,
    val sleepConsistency: Float,
    val waterConsistency: Float,
    val overallScore: Float
)

/** A repeating behavioural pattern Sol has discovered across time. */
data class HistoricalPattern(
    val pattern: String,        // "Steps drop after rest days"
    val confidence: Float,      // 0.0 – 1.0
    val frequency: String,      // "3 times in last 4 weeks"
    val suggestion: String
)

/** Per-module state blocks that together form the UserTwin. */
data class ProfileState(
    val name: String = "",
    val activityLevel: com.solofit.app.domain.model.ActivityLevel? = null,
    val fitnessGoal: com.solofit.app.domain.model.FitnessGoal? = null,
    val targetCalories: Int = 0,
    val targetProtein: Int = 0,
    val stepGoal: Int = 0
)

data class MovementState(
    val todaySteps: Int? = null,
    val yesterdaySteps: Int? = null,
    val weeklySteps: List<Int> = emptyList(),
    val weeklyWalkingTrend: String = ""
)

data class WorkoutState(
    val sessionsThisWeek: Int = 0,
    val workoutToday: Boolean = false,
    val strengthIncreasing: Boolean = false,
    val volumeIncreasing: Boolean = false,
    val weeklyRecovery: List<Int> = emptyList()
)

data class RecoveryState(
    val score: Int? = null,
    val previousScore: Int? = null
)

data class SleepState(
    val hours: Double? = null,
    val previousHours: Double? = null,
    val weeklyHours: List<Double> = emptyList()
)

data class StressState(
    val level: Int? = null,
    val previousLevel: Int? = null
)

data class NutritionState(
    val consumedCalories: Int = 0,
    val consumedProtein: Int = 0,
    val weeklyProteinPct: List<Double> = emptyList()
)

data class WaterState(
    val mlToday: Int = 0,
    val goalMl: Int = 0
)

data class MeditationState(
    val minutesToday: Int? = null,
    val previousMinutes: Int? = null
)

data class JournalState(
    val daysThisWeek: Int = 0,
    val sentiment: JournalSentiment? = null,
    val recentEntryCount: Int = 0
)

data class BodyRecompState(
    val measurementImproving: Boolean? = null
)

data class HabitsState(
    val streakDays: Int = 0,
    val daysActiveThisWeek: Int = 0,
    val daysTracked: Int = 0
)

data class ReflectionState(
    val identityMessage: IdentityMessage? = null,
    val setbackMessages: List<SetbackRecoveryMessage> = emptyList(),
    val patternDiscoveries: List<PatternDiscovery> = emptyList()
)

/** The central digital twin that every module contributes signals to.
 *  Sol never reads from UI — it reads from this model. */
data class UserTwin(
    val buildTimestamp: Long = System.currentTimeMillis(),
    // ── Module States ──
    val profile: ProfileState = ProfileState(),
    val movement: MovementState = MovementState(),
    val workouts: WorkoutState = WorkoutState(),
    val recovery: RecoveryState = RecoveryState(),
    val sleep: SleepState = SleepState(),
    val stress: StressState = StressState(),
    val nutrition: NutritionState = NutritionState(),
    val water: WaterState = WaterState(),
    val meditation: MeditationState = MeditationState(),
    val journal: JournalState = JournalState(),
    val bodyRecomp: BodyRecompState = BodyRecompState(),
    val habits: HabitsState = HabitsState(),
    val reflections: ReflectionState = ReflectionState(),
    // ── Raw Signals ──
    val signals: List<ModuleSignal> = emptyList(),
    // ── Derived Intelligence ──
    val currentState: WellnessState = WellnessState.INSUFFICIENT_DATA,
    val trends: List<WellnessTrend> = emptyList(),
    val risks: List<RiskFactor> = emptyList(),
    val strengths: List<Strength> = emptyList(),
    val momentum: MomentumState = MomentumState(MomentumLevel.BUILDING, "", TrendDirection.STABLE, emptyList()),
    val burnoutRisk: BurnoutAssessment? = null,
    val setbackPrediction: SetbackPrediction? = null,
    val consistency: ConsistencyProfile? = null,
    val historicalPatterns: List<HistoricalPattern> = emptyList(),
    // ── Lifestyle ──
    val lifestyleMode: LifestyleMode = LifestyleMode.REBUILDING,
    val dailyPriority: DailyPriority = DailyPriority.CONSISTENCY,
    val microWins: List<MicroWin> = emptyList(),
    // ── Community (Friends / Circle) ──
    val community: CommunityState = CommunityState(),
    // ── Data Quality ──
    val daysTracked: Int = 0,
    val confidence: String = "Low"
)
