package com.solofit.app.sol

import com.solofit.app.core.FitnessMath
import com.solofit.app.core.StreakCalculator
import com.solofit.app.data.local.entity.DailyMetricEntity
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.data.local.entity.GratitudeEntryEntity
import com.solofit.app.data.local.entity.BodyMeasurementEntity
import com.solofit.app.domain.model.MacroTotals
import java.time.LocalDate
import kotlin.math.roundToInt

object UserTwinBuilder {

    fun build(
        profile: UserProfileEntity?,
        metric: DailyMetricEntity?,
        prevMetric: DailyMetricEntity?,
        totals: MacroTotals,
        weeklyMetrics: List<DailyMetricEntity>,
        weeklyTotals: List<MacroTotals>,
        history: List<com.solofit.app.data.local.relation.SessionWithSets>,
        measurements: List<BodyMeasurementEntity>,
        wellness: com.solofit.app.data.local.UserPreferences.DailyWellness,
        yesterdayWellness: com.solofit.app.data.local.UserPreferences.DailyWellness,
        waterMlToday: Int,
        waterGoalMl: Int,
        recentGratitude: List<GratitudeEntryEntity>,
        stepGoal: Int,
        // ── Derived engines already computed by SolViewModel ──
        fullBaseline: UserBaseline?,
        dailyPriority: DailyPriorityResult,
        momentum: MomentumState,
        lifestyleMode: LifestyleMode,
        microWins: List<MicroWin>,
        identityMessage: IdentityMessage?,
        setbackMessages: List<SetbackRecoveryMessage>,
        patternDiscoveries: List<PatternDiscovery>,
        burnout: BurnoutAssessment?,
        setbackPrediction: SetbackPrediction?,
        confidence: String,
        daysTracked: Int
    ): UserTwin {
        val now = LocalDate.now()
        val dates = history.map { it.session.date }
        val todayStr = now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)

        val recoveryScore = metric?.let {
            FitnessMath.recoveryScore(
                sleepHours = it.sleepHours, steps = it.steps,
                workoutDone = dates.contains(todayStr), waterMl = null,
                waterGoalMl = waterGoalMl, energyScore = it.energyScore
            )
        }
        val previousRecovery = prevMetric?.let {
            FitnessMath.recoveryScore(
                sleepHours = it.sleepHours, steps = it.steps,
                workoutDone = null, waterMl = null,
                waterGoalMl = waterGoalMl, energyScore = it.energyScore
            )
        }
        val weeklyRecovery = weeklyMetrics.mapNotNull { m ->
            FitnessMath.recoveryScore(
                sleepHours = m.sleepHours, steps = m.steps,
                workoutDone = dates.contains(m.date), waterMl = null,
                waterGoalMl = waterGoalMl, energyScore = m.energyScore
            )
        }
        val targetProtein = profile?.targetProtein ?: 150
        val weeklySteps = weeklyMetrics.mapNotNull { it.steps }
        val weeklySleep = weeklyMetrics.mapNotNull { it.sleepHours }
        val weeklyProteinPct = weeklyTotals.map { t ->
            if (targetProtein > 0) t.proteinG / targetProtein else 0.0
        }

        val signals = buildList {
            add(ModuleSignal("sleep", "Sleep Duration", metric?.sleepHours, prevMetric?.sleepHours, "hours",
                TrendDirection.STABLE, if ((metric?.sleepHours ?: 0.0) >= 7.0) SignalStatus.GOOD else SignalStatus.LOW,
                if ((metric?.sleepHours ?: 0.0) < 7.0) "Sleep is below the 7-hour recovery threshold" else ""
            ))
            add(ModuleSignal("movement", "Daily Steps", metric?.steps?.toDouble(), prevMetric?.steps?.toDouble(), "steps",
                TrendDirection.STABLE, if ((metric?.steps ?: 0) >= stepGoal) SignalStatus.GOOD else SignalStatus.ON_TRACK,
                if ((metric?.steps ?: 0) < stepGoal) "Steps are below your daily target" else ""
            ))
            add(ModuleSignal("workouts", "Workouts This Week", StreakCalculator.daysActiveInWindow(dates, now, 7).toDouble(), null, "sessions",
                TrendDirection.STABLE, if (StreakCalculator.daysActiveInWindow(dates, now, 7) >= 3) SignalStatus.GOOD else SignalStatus.ON_TRACK
            ))
            add(ModuleSignal("nutrition", "Protein Adherence", if (targetProtein > 0) (totals.proteinG / targetProtein * 100) else null, null, "%",
                TrendDirection.STABLE, if (targetProtein > 0 && totals.proteinG >= targetProtein) SignalStatus.GOOD else SignalStatus.ON_TRACK
            ))
            add(ModuleSignal("water", "Water Intake", waterMlToday.toDouble(), null, "ml",
                TrendDirection.STABLE, if (waterMlToday >= waterGoalMl) SignalStatus.GOOD else SignalStatus.ON_TRACK
            ))
            wellness.meditationMinutes?.let {
                add(ModuleSignal("meditation", "Meditation", it.toDouble(), yesterdayWellness.meditationMinutes?.toDouble(), "min",
                    TrendDirection.STABLE, if (it > 0) SignalStatus.GOOD else SignalStatus.LOW
                ))
            }
            wellness.stressLevel?.let {
                add(ModuleSignal("stress", "Stress Level", it.toDouble(), yesterdayWellness.stressLevel?.toDouble(), "/10",
                    TrendDirection.STABLE, if (it <= 4) SignalStatus.GOOD else if (it <= 7) SignalStatus.ON_TRACK else SignalStatus.LOW
                ))
            }
        }

        val trends = buildList<WellnessTrend> {
            if (weeklySteps.size >= 2) {
                val first = weeklySteps.first().toDouble()
                val last = weeklySteps.last().toDouble()
                val change = if (first > 0) ((last - first) / first * 100).roundToInt() else 0
                add(WellnessTrend("Steps Trend", if (change > 5) TrendDirection.UP else if (change < -5) TrendDirection.DOWN else TrendDirection.STABLE,
                    change, 7, if (change > 0) SignalStatus.GOOD else SignalStatus.LOW))
            }
            if (weeklyRecovery.size >= 2) {
                val first = weeklyRecovery.first().toDouble()
                val last = weeklyRecovery.last().toDouble()
                val change = if (first > 0) ((last - first) / first * 100).roundToInt() else 0
                add(WellnessTrend("Recovery Trend", if (change > 5) TrendDirection.UP else if (change < -5) TrendDirection.DOWN else TrendDirection.STABLE,
                    change, 7, if (change >= 0) SignalStatus.GOOD else SignalStatus.LOW))
            }
        }

        val risks = buildList<RiskFactor> {
            if ((metric?.sleepHours ?: 0.0) < 6.0) {
                add(RiskFactor("sleep", 0.7f, "Chronic sleep deprivation", TrendDirection.DOWN))
            }
            if (wellness.stressLevel != null && wellness.stressLevel >= 7) {
                add(RiskFactor("stress", 0.6f, "Elevated stress levels", TrendDirection.UP))
            }
            if (weeklySteps.isNotEmpty() && weeklySteps.average() < 3000) {
                add(RiskFactor("movement", 0.5f, "Consistently low step count", TrendDirection.STABLE))
            }
            burnout?.let { b ->
                if (b.level == BurnoutLevel.ELEVATED || b.level == BurnoutLevel.HIGH) {
                    add(RiskFactor("burnout", b.score.toFloat() / 100f, "Burnout indicators detected", TrendDirection.UP))
                }
            }
        }

        val strengths = buildList<Strength> {
            if (StreakCalculator.daysActiveInWindow(dates, now, 7) >= 3) {
                add(Strength("workouts", "Workout Consistency", "${StreakCalculator.currentStreak(dates, now)} day streak", "Keeping your training routine builds long-term results"))
            }
            if (recentGratitude.size >= 3) {
                add(Strength("journal", "Journaling Habit", "${recentGratitude.size} entries this week", "Regular reflection supports mental clarity"))
            }
            if (microWins.isNotEmpty()) {
                add(Strength("habits", "Daily Micro-Wins", "${microWins.size} today", "Small wins compound into lasting change"))
            }
        }

        val daysActiveThisWeek = StreakCalculator.daysActiveInWindow(dates, now, 7)
        val streakDays = StreakCalculator.currentStreak(dates, now)
        val weeklyProteinDaysCount = weeklyTotals.count { t ->
            targetProtein > 0 && t.proteinG >= targetProtein
        }

        val overallConsistency = listOf(
            daysActiveThisWeek.toFloat() / 7f,
            if (targetProtein > 0) weeklyProteinPct.filter { it >= 0.8 }.size.toFloat() / weeklyProteinPct.size.coerceAtLeast(1) else 0f,
            if (weeklySteps.isNotEmpty()) weeklySteps.count { it >= stepGoal / 2 }.toFloat() / weeklySteps.size else 0f,
            if (weeklySleep.isNotEmpty()) weeklySleep.count { it >= 6.0 }.toFloat() / weeklySleep.size else 0f,
            recentGratitude.size.toFloat() / 7f
        )
        val consistencyScore = overallConsistency.average().toFloat()

        return UserTwin(
            profile = ProfileState(
                name = profile?.name ?: "",
                activityLevel = profile?.activityLevel,
                fitnessGoal = profile?.goal,
                targetCalories = profile?.targetCalories ?: 2000,
                targetProtein = targetProtein,
                stepGoal = stepGoal
            ),
            movement = MovementState(
                todaySteps = metric?.steps,
                yesterdaySteps = prevMetric?.steps,
                weeklySteps = weeklySteps,
                weeklyWalkingTrend = ""
            ),
            workouts = WorkoutState(
                sessionsThisWeek = daysActiveThisWeek,
                workoutToday = dates.contains(todayStr),
                strengthIncreasing = weeklyProteinDaysCount > 0,
                volumeIncreasing = false,
                weeklyRecovery = weeklyRecovery
            ),
            recovery = RecoveryState(score = recoveryScore, previousScore = previousRecovery),
            sleep = SleepState(hours = metric?.sleepHours, previousHours = prevMetric?.sleepHours, weeklyHours = weeklySleep),
            stress = StressState(level = wellness.stressLevel, previousLevel = yesterdayWellness.stressLevel),
            nutrition = NutritionState(
                consumedCalories = totals.calories.roundToInt(),
                consumedProtein = totals.proteinG.roundToInt(),
                weeklyProteinPct = weeklyProteinPct
            ),
            water = WaterState(mlToday = waterMlToday, goalMl = waterGoalMl),
            meditation = MeditationState(minutesToday = wellness.meditationMinutes, previousMinutes = yesterdayWellness.meditationMinutes),
            journal = JournalState(
                daysThisWeek = recentGratitude.size,
                sentiment = JournalSentimentEngine.classify(recentGratitude.map { it.text })
                    ?.takeUnless { it == JournalSentiment.NEUTRAL },
                recentEntryCount = recentGratitude.size
            ),
            bodyRecomp = BodyRecompState(
                measurementImproving = measurements.mapNotNull { it.waistCm }.let { waists ->
                    if (waists.size >= 2) waists.last() < waists.first() else null
                }
            ),
            habits = HabitsState(streakDays = streakDays, daysActiveThisWeek = daysActiveThisWeek, daysTracked = daysTracked),
            reflections = ReflectionState(identityMessage = identityMessage, setbackMessages = setbackMessages, patternDiscoveries = patternDiscoveries),
            signals = signals,
            currentState = when {
                daysTracked < 3 -> WellnessState.INSUFFICIENT_DATA
                consistencyScore >= 0.7f && recoveryScore != null && recoveryScore >= 60 -> WellnessState.THRIVING
                consistencyScore >= 0.4f -> WellnessState.MAINTAINING
                risks.any { it.severity >= 0.6f } -> WellnessState.AT_RISK
                else -> WellnessState.STRUGGLING
            },
            trends = trends,
            risks = risks,
            strengths = strengths,
            momentum = momentum,
            burnoutRisk = burnout,
            setbackPrediction = setbackPrediction,
            consistency = ConsistencyProfile(
                workoutConsistency = daysActiveThisWeek.toFloat() / 7f,
                nutritionConsistency = if (targetProtein > 0) weeklyProteinPct.count { it >= 0.8 }.toFloat() / weeklyProteinPct.size.coerceAtLeast(1) else 0f,
                movementConsistency = if (weeklySteps.isNotEmpty()) weeklySteps.count { it >= stepGoal / 2 }.toFloat() / weeklySteps.size else 0f,
                meditationConsistency = 0f,
                journalConsistency = recentGratitude.size.toFloat() / 7f,
                sleepConsistency = if (weeklySleep.isNotEmpty()) weeklySleep.count { it >= 6.0 }.toFloat() / weeklySleep.size else 0f,
                waterConsistency = 0f,
                overallScore = consistencyScore
            ),
            historicalPatterns = patternDiscoveries.map { p ->
                HistoricalPattern(
                    pattern = p.observation,
                    confidence = 0.5f,
                    frequency = p.category,
                    suggestion = p.insight
                )
            },
            lifestyleMode = lifestyleMode,
            dailyPriority = dailyPriority.priority,
            microWins = microWins,
            daysTracked = daysTracked,
            confidence = confidence
        )
    }
}
