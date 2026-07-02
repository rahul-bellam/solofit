package com.solofit.app.sol

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.domain.model.VoiceMode
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.domain.repository.BodyRepository
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.JournalRepository
import com.solofit.app.domain.repository.FriendRepository
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WeightRepository
import com.solofit.app.domain.repository.WorkoutRepository
import com.solofit.app.core.FitnessMath
import com.solofit.app.core.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

data class SolUiState(
    val visible: Boolean = true,
    val userName: String = "",
    val briefingHeader: String = "Today's Focus",
    val greeting: String = "",
    val headline: String = "",
    val detail: String = "",
    val reasoning: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val voiceLine: String = "",
    val type: InsightType = InsightType.MORNING_GREETING,
    val dayLabel: DayLabel = DayLabel.BALANCED,
    // ── Daily Narrative (Point 5) ──
    val todayTheme: DayTheme = DayTheme.BALANCED,
    val themeReason: String = "",
    // ── Causal explanation (Point 7) ──
    val causalExplanation: String = "",
    // ── Current Screen (Point 1 — context-aware Sol) ──
    val currentScreen: String = "",
    // ── Life Context (Point 6) ──
    val lifeContext: LifeContext = LifeContext(),
    val signals: List<SignalSummary> = emptyList(),
    val trends: List<TrendSummary> = emptyList(),
    val supplementaryHeadlines: List<String> = emptyList(),
    val isSpeaking: Boolean = false,
    val personality: VoicePersonality = VoicePersonality.COMPANION,
    val hasStreakMilestone: Boolean = false,
    val streakMilestone: Int = 0,
    val isSunday: Boolean = false,
    val weeklyWorkoutCount: Int = 0,
    val weeklyProteinDays: Int = 0,
    val weeklyWalkingTrend: String = "",
    val hasSufficientData: Boolean = true,
    val daysTracked: Int = 0,
    val emptyMessage: String = "",
    // ── Adaptive Lifestyle Engine ──
    val dailyPriority: DailyPriority = DailyPriority.CONSISTENCY,
    val priorityReason: String = "",
    val priorityAction: String = "",
    val momentum: MomentumState = MomentumState(MomentumLevel.BUILDING, "", TrendDirection.STABLE, emptyList()),
    val lifestyleMode: LifestyleMode = LifestyleMode.REBUILDING,
    val movementRisk: MovementRisk? = null,
    val microWins: List<MicroWin> = emptyList(),
    val journalDays: Int = 0,
    val identityMessage: IdentityMessage? = null,
    val setbackMessages: List<SetbackRecoveryMessage> = emptyList(),
    val confidence: String = "Low",
    val patternDiscoveries: List<PatternDiscovery> = emptyList(),
    val setbackPrediction: SetbackPrediction? = null,
    val burnout: BurnoutAssessment? = null,
    val voiceMode: VoiceMode = VoiceMode.AUTO_WHEN_OPENED,
    val transcript: String = "",
    val userTwin: UserTwin? = null,
    val friendActivitySummary: String = "",
    val circleHealth: String = "",
    val mutualMomentum: String = "",
    val circleMomentumLabel: String = "",
    val activeCircleCount: Int = 0,
    val encouragementNeeded: List<String> = emptyList()
)

private val BRIEFING_HEADERS = listOf(
    "Today's Focus",
    "Daily Briefing",
    "Morning Check-In",
    "Recovery Update",
    "Wellness Summary",
    "Today's Overview"
)

@HiltViewModel
class SolViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val profileRepository: ProfileRepository,
    private val bodyRepository: BodyRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val weightRepository: WeightRepository,
    private val workoutRepository: WorkoutRepository,
    private val journalRepository: JournalRepository,
    private val friendRepository: FriendRepository,
    private val insightEngine: InsightEngine,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SolUiState())
    val state: StateFlow<SolUiState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val now = LocalDate.now()
            val todayStr = DateUtils.today()
            val profile = profileRepository.observeProfile().first()
            val totals = dailyLogRepository.observeTotalsForDate(todayStr).first()
            val history = workoutRepository.observeHistory().first()
            val metric = bodyRepository.observeMetric(todayStr).first()
            val yesterdayStr = now.minusDays(1).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val prevMetric = runCatching { bodyRepository.observeMetric(yesterdayStr).first() }.getOrNull()
            val measurements = bodyRepository.observeMeasurements().first()
            val setRows = workoutRepository.observeCompletedSetRows().first()
            val recentEntries = journalRepository.observeRecentGratitude(7).first()
            val wellness = prefs.dailyWellness(todayStr).first()
            val yesterdayWellness = prefs.dailyWellness(yesterdayStr).first()
            val waterMlToday = prefs.waterMl(todayStr).first()
            val personality = prefs.voicePersonality.first()
            val voiceMode = prefs.voiceMode.first()
            val lifeContext = prefs.lifeContext.first()


            val dates = history.map { it.session.date }

            val previousRecovery = prevMetric?.let {
                FitnessMath.recoveryScore(
                    sleepHours = it.sleepHours, steps = it.steps,
                    workoutDone = null, waterMl = null,
                    waterGoalMl = WellnessThresholds.WATER_DEFAULT_GOAL_ML, energyScore = it.energyScore
                )
            }
            val recoveryScore = metric?.let {
                FitnessMath.recoveryScore(
                    sleepHours = it.sleepHours, steps = it.steps,
                    workoutDone = dates.contains(todayStr), waterMl = null,
                    waterGoalMl = WellnessThresholds.WATER_DEFAULT_GOAL_ML, energyScore = it.energyScore
                )
            }

            val last3 = history.count { h ->
                runCatching {
                    ChronoUnit.DAYS.between(LocalDate.parse(h.session.date), now).toInt()
                }.getOrNull()?.let { it in 0..3 } == true
            }
            val prev3 = history.count { h ->
                runCatching {
                    ChronoUnit.DAYS.between(LocalDate.parse(h.session.date), now).toInt()
                }.getOrNull()?.let { it in 4..7 } == true
            }
            val volumeIncrease = last3 > prev3 && last3 >= 2

            val waists = measurements.mapNotNull { it.waistCm }
            val measurementImproving = if (waists.size >= 2) waists.last() < waists.first() else null
            val strengthIncreasing = setRows.isNotEmpty()

            val yesterdayMetric = prevMetric
            val prevSleep = yesterdayMetric?.sleepHours
            val prevSteps = yesterdayMetric?.steps

            val journalSentiment: JournalSentiment? =
                JournalSentimentEngine.classify(recentEntries.map { it.text })
                    ?.takeUnless { it == JournalSentiment.NEUTRAL }

            // ── Weekly data aggregation (batch queries, not N+1) ──
            val weekStart = now.minusDays(6)
            val weekStartStr = weekStart.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val daysInWeek = (0..6).map { weekStart.plusDays(it.toLong()) }
            val dateStrings = daysInWeek.map { it.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) }

            val weeklyMetricsAll = runCatching {
                bodyRepository.observeMetricsSince(weekStartStr).first()
            }.getOrDefault(emptyList())
            val weeklyMetrics = dateStrings.mapNotNull { date ->
                weeklyMetricsAll.find { it.date == date }
            }
            val weeklyTotalsAll = runCatching {
                dailyLogRepository.getDailyTotalsSince(weekStartStr)
            }.getOrDefault(emptyList())
            val weeklyTotals = dateStrings.mapNotNull { date ->
                weeklyTotalsAll.find { it.first == date }?.second
            }

            val weeklySteps = weeklyMetrics.mapNotNull { it.steps }
            val weeklyRecovery = weeklyMetrics.mapNotNull { m ->
                FitnessMath.recoveryScore(
                    sleepHours = m.sleepHours, steps = m.steps,
                    workoutDone = dates.contains(m.date), waterMl = null,
                    waterGoalMl = WellnessThresholds.WATER_DEFAULT_GOAL_ML, energyScore = m.energyScore
                )
            }
            val targetProtein = profile?.targetProtein ?: 150
            val weeklyProteinPct = weeklyTotals.map { t ->
                if (targetProtein > 0) t.proteinG / targetProtein else 0.0
            }

            val daysTracked = weeklyMetrics.size

            // ── Count days in last 7 where protein >= target ──
            val weeklyProteinDays = weeklyTotals.count { t ->
                targetProtein > 0 && t.proteinG >= targetProtein
            }

            // ── Calculate weekly step trend (batch query, not N+1) ──
            val prevWeekStart = weekStart.minusDays(7)
            val prevWeekStartStr = prevWeekStart.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val prevWeekDates = (0..6).map { prevWeekStart.plusDays(it.toLong()) }
            val prevWeekDateStrings = prevWeekDates.map { it.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) }
            val prevWeekMetricsAll = runCatching {
                bodyRepository.observeMetricsSince(prevWeekStartStr).first()
            }.getOrDefault(emptyList())
            val prevWeekMetrics = prevWeekDateStrings.mapNotNull { date ->
                prevWeekMetricsAll.find { it.date == date }
            }
            val currentWeekTotalSteps = weeklySteps.sum()
            val prevWeekTotalSteps = prevWeekMetrics.sumOf { it.steps ?: 0 }

            val walkingTrend = if (prevWeekTotalSteps > 0) {
                val pctChange = ((currentWeekTotalSteps - prevWeekTotalSteps).toDouble() / prevWeekTotalSteps * 100).roundToInt()
                if (pctChange > 0) "Walking increased $pctChange%"
                else if (pctChange < 0) "Walking decreased ${kotlin.math.abs(pctChange)}%"
                else "Walking consistent with last week"
            } else if (currentWeekTotalSteps > 0) {
                "Walking tracked this week"
            } else ""

            val input = SolInput(
                recoveryScore = recoveryScore,
                previousRecoveryScore = previousRecovery,
                streakDays = StreakCalculator.currentStreak(dates, now),
                daysActiveThisWeek = StreakCalculator.daysActiveInWindow(dates, now, 7),
                workoutToday = dates.contains(todayStr),
                consumedCalories = totals.calories.roundToInt(),
                consumedProtein = totals.proteinG.roundToInt(),
                targetCalories = profile?.targetCalories ?: 2000,
                targetProtein = targetProtein,
                sleepHours = metric?.sleepHours,
                previousSleepHours = prevSleep,
                steps = metric?.steps,
                previousSteps = prevSteps,
                waterMl = waterMlToday,
                waterGoalMl = WellnessThresholds.WATER_DEFAULT_GOAL_ML,
                energyScore = metric?.energyScore,
                stressLevel = wellness.stressLevel,
                previousStressLevel = yesterdayWellness.stressLevel,
                moodScore = metric?.moodScore,
                meditationMinutes = wellness.meditationMinutes,
                previousMeditationMinutes = prevMetric?.let { prefs.dailyWellness(yesterdayStr).first().meditationMinutes },
                journalDays = recentEntries.size,
                journalSentiment = journalSentiment,
                measurementImproving = measurementImproving,
                strengthIncreasing = strengthIncreasing,
                phaseDay = 1,
                phaseTargetDays = 365,
                historySessionCount = history.size,
                recentTrainingVolumeIncrease = volumeIncrease,
                daysTracked = daysTracked,
                weeklySteps = weeklySteps,
                weeklyRecovery = weeklyRecovery,
                weeklyProteinPct = weeklyProteinPct,
                weeklySleep = weeklyMetrics.mapNotNull { it.sleepHours }
            )

            val weeklyRecoveryMap = weeklyMetrics.associate { m ->
                m.date to (FitnessMath.recoveryScore(
                    sleepHours = m.sleepHours, steps = m.steps,
                    workoutDone = dates.contains(m.date), waterMl = null,
                    waterGoalMl = WellnessThresholds.WATER_DEFAULT_GOAL_ML, energyScore = m.energyScore
                ) ?: 0)
            }
            val weeklyProteinMap = dateStrings.zip(weeklyTotals).associate { (date, totals) ->
                date to (if (targetProtein > 0) totals.proteinG / targetProtein else 0.0)
            }
            val memoryEngine = SolMemoryEngine()
            val memory = memoryEngine.compute(
                historySessions = dates,
                weeklyRecovery = weeklyRecoveryMap,
                weeklyProtein = weeklyProteinMap
            )
            val briefing = insightEngine.computeBriefing(input, memory)
            val transformedVoice = VoicePersonalityTransformer.transform(briefing.primary.voiceLine, personality)

            // ── Adaptive Lifestyle Engine ──
            val baseline = BaselineCalculator.compute(
                weeklySteps = weeklySteps,
                weeklyRecovery = weeklyRecovery,
                weeklyProteinPct = weeklyProteinPct,
                weeklySleep = weeklyMetrics.mapNotNull { it.sleepHours },
                historyCount = history.size,
                daysTracked = daysTracked,
                meditationMinutes = wellness.meditationMinutes,
                profile = profile
            )
            val dailyPriority = DailyPriorityEngine.determine(
                recoveryScore = recoveryScore,
                sleepHours = metric?.sleepHours,
                baseline = baseline,
                todaySteps = metric?.steps,
                todayProtein = if (targetProtein > 0) totals.proteinG / targetProtein else 0.0,
                daysActiveThisWeek = StreakCalculator.daysActiveInWindow(dates, now, 7),
                meditationToday = wellness.meditationMinutes > 0,
                stressLevel = wellness.stressLevel
            )
            val momentum = MomentumCalculator.compute(
                daysActiveThisWeek = StreakCalculator.daysActiveInWindow(dates, now, 7),
                recoveryScore = recoveryScore,
                weeklySteps = weeklySteps,
                weeklyProteinPct = weeklyProteinPct,
                sleepHours = metric?.sleepHours,
                streakDays = StreakCalculator.currentStreak(dates, now),
                baseline = baseline
            )
            val lifestyleMode = LifestyleModeDetector.detect(
                recoveryScore = recoveryScore,
                sleepHours = metric?.sleepHours,
                weeklySleep = weeklyMetrics.mapNotNull { it.sleepHours },
                steps = metric?.steps,
                weeklySteps = weeklySteps,
                daysActiveThisWeek = StreakCalculator.daysActiveInWindow(dates, now, 7),
            )
            val stepGoal = prefs.stepGoal.first()
            val movementRisk = SedentaryIntelligence.assess(
                todaySteps = metric?.steps,
                weeklySteps = weeklySteps,
                stepGoal = stepGoal
            )
            val microWins = MicroWinEngine.detect(
                todaySteps = metric?.steps,
                yesterdaySteps = prevMetric?.steps,
                todaySleep = metric?.sleepHours,
                yesterdaySleep = prevMetric?.sleepHours,
                todayProtein = if (targetProtein > 0) totals.proteinG / targetProtein else null,
                yesterdayProtein = null,
                todayRecovery = recoveryScore,
                yesterdayRecovery = previousRecovery,
                todayMeditation = wellness.meditationMinutes,
                yesterdayMeditation = prevMetric?.let { prefs.dailyWellness(yesterdayStr).first().meditationMinutes }
            )

            // ── Identity Engine (max 2-3x per week) ──
            val activeMetrics = buildList {
                if (dates.contains(todayStr)) add("workout")
                if (weeklyProteinDays > 0) add("nutrition")
                if (recoveryScore != null && recoveryScore >= 50) add("recovery")
                if ((metric?.steps ?: 0) >= WellnessThresholds.MODERATE_MOVEMENT_STEPS) add("movement")
                if (wellness.meditationMinutes > 0) add("meditation")
                if (recentEntries.size >= 3) add("journal")
            }
            val recentStatements = emptyList<String>()
            val identityMessage = IdentityEngine.select(activeMetrics, recentStatements)

            // ── Setback Recovery Engine ──
            val workoutYesterday = dates.contains(yesterdayStr)
            val ateWellToday = targetProtein > 0 && totals.proteinG >= targetProtein * 0.8
            val yesterdayTotals = runCatching { dailyLogRepository.observeTotalsForDate(yesterdayStr).first() }.getOrNull()
            val ateWellYesterday = yesterdayTotals != null && targetProtein > 0 && yesterdayTotals.proteinG >= targetProtein * 0.8
            val movedEnough = (metric?.steps ?: 0) >= WellnessThresholds.MODERATE_MOVEMENT_STEPS
            val prevStepsVal = prevMetric?.steps ?: 0
            val movedYesterday = prevStepsVal >= WellnessThresholds.MODERATE_MOVEMENT_STEPS
            val setbackMessages = SetbackRecoveryEngine.detect(
                workoutToday = dates.contains(todayStr),
                ateWellToday = ateWellToday,
                movedEnough = movedEnough,
                meditatedToday = wellness.meditationMinutes > 0,
                journaledToday = recentEntries.isNotEmpty(),
                workoutYesterday = workoutYesterday,
                ateWellYesterday = ateWellYesterday,
                movedYesterday = movedYesterday,
                meditatedYesterday = (prevMetric?.let { prefs.dailyWellness(yesterdayStr).first().meditationMinutes } ?: 0) > 0,
                journaledYesterday = runCatching { journalRepository.observeRecentGratitude(7).first().any { it.date == yesterdayStr } }.getOrDefault(false),
                daysActiveThisWeek = StreakCalculator.daysActiveInWindow(dates, now, 7),
                momentum = momentum.level
            )

            // ── 30d / 90d Data Windows ──
            val thirtyDaysAgo = now.minusDays(29).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val ninetyDaysAgo = now.minusDays(89).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val metrics30d = runCatching { bodyRepository.observeMetricsSince(thirtyDaysAgo).first() }.getOrDefault(emptyList())
            val metrics90d = runCatching { bodyRepository.observeMetricsSince(ninetyDaysAgo).first() }.getOrDefault(emptyList())
            val totals30d = runCatching { dailyLogRepository.getDailyTotalsSince(thirtyDaysAgo) }.getOrDefault(emptyList())
            val totals90d = runCatching { dailyLogRepository.getDailyTotalsSince(ninetyDaysAgo) }.getOrDefault(emptyList())
            val weight30d = runCatching { weightRepository.getEntriesSince(thirtyDaysAgo) }.getOrDefault(emptyList())
            val weight90d = runCatching { weightRepository.getEntriesSince(ninetyDaysAgo) }.getOrDefault(emptyList())
            val allHistory = runCatching { workoutRepository.observeHistory().first() }.getOrDefault(emptyList())
            val workouts30d = allHistory.count { h ->
                runCatching { LocalDate.parse(h.session.date) >= now.minusDays(29) }.getOrDefault(false)
            }
            val workouts90d = allHistory.count { h ->
                runCatching { LocalDate.parse(h.session.date) >= now.minusDays(89) }.getOrDefault(false)
            }

            val steps30d = metrics30d.mapNotNull { it.steps?.toInt() }
            val steps90d = metrics90d.mapNotNull { it.steps?.toInt() }
            val sleep30d = metrics30d.mapNotNull { it.sleepHours }
            val sleep90d = metrics90d.mapNotNull { it.sleepHours }
            val recovery30d = metrics30d.mapNotNull { m ->
                FitnessMath.recoveryScore(
                    sleepHours = m.sleepHours, steps = m.steps?.toInt(),
                    workoutDone = null, waterMl = null,
                    waterGoalMl = WellnessThresholds.WATER_DEFAULT_GOAL_ML, energyScore = m.energyScore
                )
            }
            val recovery90d = metrics90d.mapNotNull { m ->
                FitnessMath.recoveryScore(
                    sleepHours = m.sleepHours, steps = m.steps?.toInt(),
                    workoutDone = null, waterMl = null,
                    waterGoalMl = WellnessThresholds.WATER_DEFAULT_GOAL_ML, energyScore = m.energyScore
                )
            }
            val protein30d = if (targetProtein > 0) totals30d.map { it.second.proteinG / targetProtein } else emptyList()
            val protein90d = if (targetProtein > 0) totals90d.map { it.second.proteinG / targetProtein } else emptyList()
            val meditation30dTotal = metrics30d.sumOf { runCatching { prefs.dailyWellness(it.date).first().meditationMinutes }.getOrDefault(0) }
            val meditation90dTotal = metrics90d.sumOf { runCatching { prefs.dailyWellness(it.date).first().meditationMinutes }.getOrDefault(0) }
            val bodyWeight30d = weight30d.map { it.weightKg }
            val bodyWeight90d = weight90d.map { it.weightKg }

            // ── Extended Baseline ──
            val fullBaseline = BaselineCalculator.compute(
                weeklySteps = weeklySteps,
                weeklyRecovery = weeklyRecovery,
                weeklyProteinPct = weeklyProteinPct,
                weeklySleep = weeklyMetrics.mapNotNull { it.sleepHours },
                historyCount = history.size,
                daysTracked = daysTracked,
                meditationMinutes = wellness.meditationMinutes,
                profile = profile,
                steps30d = steps30d, steps90d = steps90d,
                recovery30d = recovery30d, recovery90d = recovery90d,
                protein30d = protein30d, protein90d = protein90d,
                sleep30d = sleep30d, sleep90d = sleep90d,
                meditation30dTotal = meditation30dTotal, meditation90dTotal = meditation90dTotal,
                daysTracked30d = metrics30d.size, daysTracked90d = metrics90d.size,
                workouts30d = workouts30d, workouts90d = workouts90d,
                bodyWeight30d = bodyWeight30d, bodyWeight90d = bodyWeight90d
            )

            // ── Pattern Discovery Engine ──
            val workoutDaysOfWeek = history.mapNotNull { h ->
                runCatching { LocalDate.parse(h.session.date).dayOfWeek }.getOrNull()
            }
            val stepsByDay = weeklyMetrics.mapNotNull { m ->
                runCatching { LocalDate.parse(m.date).dayOfWeek to (m.steps?.toInt() ?: 0) }.getOrNull()
            }.toMap()
            val patternDiscoveries = PatternDiscoveryEngine.discover(
                trends = briefing.trends,
                signals = briefing.signals,
                baseline = fullBaseline,
                weeklyWorkoutDays = workoutDaysOfWeek,
                stepsByDay = stepsByDay,
                daysTracked = daysTracked
            )

            // ── Confidence System ──
            val confidence = when {
                daysTracked >= 90 -> "High"
                daysTracked >= 30 -> "Medium"
                daysTracked >= 7 -> "Low"
                else -> "Minimal"
            }

            val streakMilestone = when (input.streakDays) {
                7 -> 7; 30 -> 30; 100 -> 100; else -> 0
            }

            val headerIndex = (now.dayOfYear % BRIEFING_HEADERS.size).coerceIn(0, BRIEFING_HEADERS.size - 1)

            val isSunday = now.dayOfWeek == DayOfWeek.SUNDAY

            val weeklyWorkoutCount = StreakCalculator.daysActiveInWindow(dates, now, 7)

            // ── Setback Predictor (logistic regression) ──
            val savedWeightsStr = prefs.setbackPredictorWeights.first()
            val loadedWeights = if (savedWeightsStr.isNotEmpty()) {
                SetbackPredictor.deserializeWeights(savedWeightsStr) ?: SetbackPredictor.defaultWeights()
            } else SetbackPredictor.defaultWeights()
            val loadedBias = prefs.setbackPredictorBias.first().toDouble()
            val daysSinceLastWorkout = dates.lastOrNull()?.let {
                ChronoUnit.DAYS.between(runCatching { LocalDate.parse(it) }.getOrNull() ?: now, now)
            }?.toInt()?.coerceAtLeast(0) ?: 14
            val avgSleep3d = weeklyMetrics.takeLast(3).mapNotNull { it.sleepHours }.let {
                if (it.isNotEmpty()) it.average() else null
            }
            val avgSteps3d = weeklyMetrics.takeLast(3).mapNotNull { it.steps?.toInt() }.let {
                if (it.isNotEmpty()) it.average().toInt() else null
            }
            val momentumDirection = when {
                last3 > prev3 && last3 >= 2 -> 2
                last3 >= prev3 -> 1
                else -> 0
            }
            val features = SetbackPredictor.extractFeatures(
                daysSinceLastWorkout = daysSinceLastWorkout,
                avgRecovery7d = weeklyRecovery.average().let { if (it.isNaN()) null else it.toInt() },
                avgSleep3d = avgSleep3d,
                avgProteinAdherence7d = weeklyProteinPct.average().let { if (it.isNaN()) null else it },
                avgSteps3d = avgSteps3d,
                meditatedAny7d = weeklyMetrics.any { m ->
                    runCatching { prefs.dailyWellness(m.date).first().meditationMinutes > 0 }.getOrDefault(false)
                },
                journaledAny7d = recentEntries.isNotEmpty(),
                isWeekend = now.dayOfWeek == DayOfWeek.SATURDAY || now.dayOfWeek == DayOfWeek.SUNDAY,
                momentumDirection = momentumDirection
            )
            val prob = SetbackPredictor.predict(loadedWeights, loadedBias, features)
            val riskLevel = SetbackPredictor.probabilityToRisk(prob)
            val topDriver = SetbackPredictor.topDriver(loadedWeights, features)
            val setbackPrediction = SetbackPrediction(
                probability = prob,
                riskLevel = riskLevel,
                topDriver = topDriver
            )

            // ── Burnout Engine (knowledge-worker early-warning) ──
            val burnout = BurnoutEngine.assess(
                BurnoutInput(
                    weeklySleep = weeklyMetrics.mapNotNull { it.sleepHours },
                    weeklyRecovery = weeklyRecovery,
                    weeklySteps = weeklySteps,
                    recoveryScore = recoveryScore,
                    sleepHours = metric?.sleepHours,
                    stressLevel = wellness.stressLevel,
                    journalSentiment = journalSentiment,
                    usualWeeklyWorkouts = (workouts30d / 4.0).roundToInt(),
                    recentWeeklyWorkouts = weeklyWorkoutCount,
                    trainingLoadIncreasing = volumeIncrease
                )
            )

            // ── Online training (one example per day, after outcome is known) ──
            val lastTrainedDate = prefs.setbackLastTrainedDate.first()
            if (lastTrainedDate != yesterdayStr) {
                val trainedYesterday = dates.contains(yesterdayStr)
                val yTotals = runCatching { dailyLogRepository.observeTotalsForDate(yesterdayStr).first() }.getOrNull()
                val yMetric = prevMetric
                val yWellness = if (yMetric != null) runCatching { prefs.dailyWellness(yesterdayStr).first() }.getOrNull() else null
                val outcomeWorkout = trainedYesterday
                val outcomNutrition = yTotals != null && targetProtein > 0 && yTotals.proteinG >= targetProtein * 0.8
                val outcomeMovement = (yMetric?.steps ?: 0) >= WellnessThresholds.MODERATE_MOVEMENT_STEPS
                val outcomeMeditation = (yWellness?.meditationMinutes ?: 0) > 0
                val outcomeJournal = runCatching { journalRepository.observeRecentGratitude(7).first().any { it.date == yesterdayStr } }.getOrDefault(false)
                val positiveOutcomes = listOf(outcomeWorkout, outcomNutrition, outcomeMovement, outcomeMeditation, outcomeJournal).count { it }
                val label = if (positiveOutcomes >= 3) 0 else 1
                val yDates = history.map { it.session.date }
                val yesDate = LocalDate.parse(yesterdayStr)
                val yDaysSinceWorkout = yDates.lastOrNull()?.let {
                    ChronoUnit.DAYS.between(runCatching { LocalDate.parse(it) }.getOrNull() ?: yesDate, yesDate)
                }?.toInt()?.coerceAtLeast(0) ?: 14
                val yWeeklyMetrics = (0..6).map { now.minusDays(it.toLong() + 1) }.mapNotNull { d ->
                    runCatching { bodyRepository.getMetric(d.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)) }.getOrNull()
                }
                val yRecoveryList = yWeeklyMetrics.mapNotNull { m ->
                    FitnessMath.recoveryScore(
                        sleepHours = m.sleepHours, steps = m.steps?.toInt(),
                        workoutDone = yDates.contains(m.date), waterMl = null,
                        waterGoalMl = WellnessThresholds.WATER_DEFAULT_GOAL_ML, energyScore = m.energyScore
                    )
                }
                val yProteinList = (0..6).map { now.minusDays(it.toLong() + 1) }.mapNotNull { d ->
                    runCatching { dailyLogRepository.observeTotalsForDate(d.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)).first() }.getOrNull()
                }
                val yFeatures = SetbackPredictor.extractFeatures(
                    daysSinceLastWorkout = yDaysSinceWorkout,
                    avgRecovery7d = yRecoveryList.average().let { if (it.isNaN()) null else it.toInt() },
                    avgSleep3d = yWeeklyMetrics.takeLast(3).mapNotNull { it.sleepHours }.let {
                        if (it.isNotEmpty()) it.average() else null
                    },
                    avgProteinAdherence7d = yProteinList.map { if (targetProtein > 0) it.proteinG / targetProtein else 0.0 }.let {
                        if (it.isNotEmpty()) it.average() else null
                    },
                    avgSteps3d = yWeeklyMetrics.takeLast(3).mapNotNull { it.steps?.toInt() }.let {
                        if (it.isNotEmpty()) it.average().toInt() else null
                    },
                    meditatedAny7d = yWeeklyMetrics.any { m ->
                        runCatching { prefs.dailyWellness(m.date).first().meditationMinutes > 0 }.getOrDefault(false)
                    },
                    journaledAny7d = runCatching { journalRepository.observeRecentGratitude(7).first().any { it.date == yesterdayStr } }.getOrDefault(false),
                    isWeekend = LocalDate.parse(yesterdayStr).dayOfWeek == DayOfWeek.SATURDAY ||
                        LocalDate.parse(yesterdayStr).dayOfWeek == DayOfWeek.SUNDAY,
                    momentumDirection = yDates.count { h ->
                        runCatching {
                            val d = LocalDate.parse(h)
                            ChronoUnit.DAYS.between(d, yesDate).toInt() in 0..3
                        }.getOrDefault(false)
                    }.let { recent ->
                        val prev = yDates.count { h ->
                            runCatching {
                                val d = LocalDate.parse(h)
                                ChronoUnit.DAYS.between(d, yesDate).toInt() in 4..7
                            }.getOrDefault(false)
                        }
                        when {
                            recent > prev && recent >= 2 -> 2
                            recent >= prev -> 1
                            else -> 0
                        }
                    }
                )
                val example = TrainingExample(yFeatures, label)
                val (newWeights, newBias) = SetbackPredictor.train(loadedWeights, loadedBias, listOf(example))
                prefs.setSetbackPredictorWeights(SetbackPredictor.serializeWeights(newWeights))
                prefs.setSetbackPredictorBias(newBias.toFloat())
                prefs.setSetbackLastTrainedDate(yesterdayStr)
            }

            val waterGoalMl = WellnessThresholds.WATER_DEFAULT_GOAL_ML

            val community = runCatching {
                val friends = friendRepository.observeAccepted().first()
                val friendEvents = friends.associate { f ->
                    f.id to friendRepository.observeEvents(f.id).first()
                }
                FriendIntelligenceEngine.compute(friends, friendEvents, daysTracked)
            }.getOrDefault(CommunityState())
            val userTwin = UserTwinBuilder.build(
                profile = profile,
                metric = metric,
                prevMetric = prevMetric,
                totals = totals,
                weeklyMetrics = weeklyMetrics,
                weeklyTotals = weeklyTotals,
                history = history,
                measurements = measurements,
                wellness = wellness,
                yesterdayWellness = yesterdayWellness,
                waterMlToday = waterMlToday,
                waterGoalMl = waterGoalMl,
                recentGratitude = recentEntries,
                stepGoal = stepGoal,
                fullBaseline = fullBaseline,
                dailyPriority = dailyPriority,
                momentum = momentum,
                lifestyleMode = lifestyleMode,
                microWins = microWins,
                identityMessage = identityMessage,
                setbackMessages = setbackMessages,
                patternDiscoveries = patternDiscoveries,
                burnout = burnout,
                setbackPrediction = setbackPrediction,
                confidence = confidence,
                daysTracked = daysTracked
            )

            _state.value = SolUiState(
                visible = true,
                userName = profile?.name ?: "",
                briefingHeader = BRIEFING_HEADERS[headerIndex],
                greeting = briefing.greeting,
                headline = briefing.primary.headline,
                detail = briefing.primary.detail,
                reasoning = briefing.primary.reasoning,
                recommendations = briefing.primary.recommendations,
                voiceLine = transformedVoice,
                type = briefing.primary.type,
                dayLabel = briefing.dayLabel,
                todayTheme = briefing.todayTheme,
                themeReason = briefing.themeReason,
                causalExplanation = briefing.causalExplanation,
                signals = briefing.signals,
                trends = briefing.trends,
                supplementaryHeadlines = briefing.supplementary.map { s -> s.headline },
                personality = personality,
                hasStreakMilestone = streakMilestone > 0,
                streakMilestone = streakMilestone,
                isSunday = isSunday,
                weeklyWorkoutCount = weeklyWorkoutCount,
                weeklyProteinDays = weeklyProteinDays,
                weeklyWalkingTrend = walkingTrend,
                hasSufficientData = briefing.hasSufficientData,
                daysTracked = daysTracked,
                emptyMessage = briefing.emptyMessage,
                dailyPriority = dailyPriority.priority,
                priorityReason = dailyPriority.reason,
                priorityAction = dailyPriority.action,
                momentum = momentum,
                lifestyleMode = lifestyleMode,
                movementRisk = movementRisk,
                microWins = microWins,
                journalDays = recentEntries.size,
                identityMessage = identityMessage,
                setbackMessages = setbackMessages,
                confidence = confidence,
                setbackPrediction = setbackPrediction,
                burnout = burnout,
                voiceMode = voiceMode,
                lifeContext = lifeContext,
                transcript = transformedVoice,
                userTwin = userTwin.copy(community = community),
                friendActivitySummary = if (community.friends.isNotEmpty()) "${community.activeCircleCount} active in your circle" else "",
                circleHealth = community.circleHealth,
                mutualMomentum = community.mutualMomentum,
                circleMomentumLabel = community.circleMomentumLabel,
                activeCircleCount = community.activeCircleCount,
                encouragementNeeded = community.encouragementNeeded
            )
        }
    }

    fun setVoiceMode(mode: VoiceMode) {
        _state.value = _state.value.copy(voiceMode = mode)
        viewModelScope.launch { prefs.setVoiceMode(mode) }
    }

    fun onScreenChanged(screen: String) {
        _state.value = _state.value.copy(currentScreen = screen)
    }

    fun setLifeContext(context: LifeContext) {
        _state.value = _state.value.copy(lifeContext = context)
        viewModelScope.launch { prefs.setLifeContext(context) }
    }

    fun clearLifeContext() {
        _state.value = _state.value.copy(lifeContext = LifeContext())
        viewModelScope.launch { prefs.setLifeContext(LifeContext()) }
    }

    fun stopSpeaking() {
        tts?.stop()
        _state.value = _state.value.copy(isSpeaking = false)
    }

    fun speak() {
        val text = _state.value.voiceLine.ifEmpty { return }
        speakText(text)
    }

    fun speakLine(line: String) {
        val transformed = VoicePersonalityTransformer.transform(line, _state.value.personality)
        speakText(transformed)
    }

    private fun speakText(text: String) {
        if (tts == null) {
            tts = TextToSpeech(ctx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    this@SolViewModel.tts?.language = Locale.US
                    doSpeak(text)
                }
            }
        } else {
            doSpeak(text)
        }
    }

    private fun doSpeak(text: String) {
        _state.value = _state.value.copy(isSpeaking = true)
        val engine = tts ?: return
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                viewModelScope.launch {
                    _state.value = _state.value.copy(isSpeaking = false)
                }
            }
            override fun onError(utteranceId: String?) {
                viewModelScope.launch {
                    _state.value = _state.value.copy(isSpeaking = false)
                }
            }
        })
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sol")
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
