package com.solofit.app.sol

import java.time.LocalTime
import javax.inject.Inject

class InsightEngine @Inject constructor() {

    companion object {
        const val MIN_DAYS_FOR_TRENDS = 3
        const val MIN_DAYS_FOR_WEEKLY = 7
        const val MIN_DAYS_FOR_PATTERNS = 14
    }

    fun computeBriefing(input: SolInput, memory: SolMemoryData? = null): SolBriefing {
        val timeOfDay = LocalTime.now()
        val morning = timeOfDay.hour < 12
        val evening = timeOfDay.hour >= 17
        val recovery = input.recoveryScore
        val greeting = if (morning) greetingForRecovery(recovery) else ""

        val hasSufficientData = input.daysTracked > 0

        if (!hasSufficientData) {
            return SolBriefing(
                greeting = "Welcome.",
                primary = EMPTY_STATE_SCRIPT,
                supplementary = emptyList(),
                dayLabel = DayLabel.BALANCED,
                signals = emptyList(),
                trends = emptyList(),
                hasSufficientData = false,
                daysTracked = input.daysTracked,
                emptyMessage = "Complete a few days of tracking and I'll begin identifying trends."
            )
        }

        val candidates = mutableListOf<PrioritizedInsight>()
        populateCandidates(input, morning, evening, candidates)

        if (candidates.isEmpty()) {
            val fallback = if (morning) fallbackMorning(input) else if (evening) fallbackEvening(input) else fallbackMorning(input)
            candidates.add(PrioritizedInsight(fallback, 10))
        }

        val sorted = candidates.sortedByDescending { it.priority }
        val primary = sorted.first()
        val supplementary = sorted.drop(1).take(3)

        val dayLabel = classifyDay(input)
        val signals = buildSignals(input)
        val trends = buildTrends(input)

        val memoryLine = memory?.let { m ->
            buildString {
                if (m.strongestDay.isNotBlank()) append("You usually train on ${m.strongestDay}. ")
                if (m.bestRecoveryDay.isNotBlank()) append("Your best recovery days are ${m.bestRecoveryDay}. ")
            }.ifBlank { null }
        }

        val enrichedPrimary = if (memoryLine != null) {
            primary.insight.copy(
                reasoning = primary.insight.reasoning + memoryLine,
                voiceLine = primary.insight.voiceLine + " " + memoryLine
            )
        } else primary.insight

        return SolBriefing(
            greeting = greeting,
            primary = enrichedPrimary,
            supplementary = supplementary.map { it.insight },
            dayLabel = dayLabel,
            signals = signals,
            trends = trends,
            hasSufficientData = true,
            daysTracked = input.daysTracked
        )
    }

    private fun classifyDay(input: SolInput): DayLabel {
        val rec = input.recoveryScore ?: return DayLabel.BALANCED
        val sleep = input.sleepHours ?: WellnessThresholds.SLEEP_ADEQUATE
        val stress = input.stressLevel ?: 3
        val steps = input.steps ?: 0
        val proteinPct = if (input.targetProtein > 0) input.consumedProtein.toDouble() / input.targetProtein else 0.0

        val meditated = (input.meditationMinutes ?: 0) > 0
        val journaled = input.journalDays > 0

        return when {
            rec < WellnessThresholds.RECOVERY_LOW || input.recentTrainingVolumeIncrease || sleep < WellnessThresholds.SLEEP_VERY_POOR -> DayLabel.RECOVERY_FOCUS
            rec >= WellnessThresholds.RECOVERY_HIGH && input.workoutToday && sleep >= WellnessThresholds.SLEEP_OPTIMAL && steps >= WellnessThresholds.PERFORMANCE_STEPS -> DayLabel.PERFORMANCE
            proteinPct < WellnessThresholds.PROTEIN_LOW && input.workoutToday -> DayLabel.NUTRITION_FOCUS
            stress >= 4 && meditated || journaled -> DayLabel.MINDFULNESS
            input.daysActiveThisWeek >= WellnessThresholds.CONSISTENT_WEEK_DAYS || input.streakDays >= WellnessThresholds.STREAK_MILESTONE_7 -> DayLabel.CONSISTENCY
            else -> DayLabel.BALANCED
        }
    }

    private fun buildSignals(input: SolInput): List<SignalSummary> {
        val signals = mutableListOf<SignalSummary>()

        val rec = input.recoveryScore
        signals.add(
            when {
                rec == null -> SignalSummary("Recovery", SignalStatus.ON_TRACK, "No data")
                rec >= WellnessThresholds.RECOVERY_GOOD -> SignalSummary("Recovery", SignalStatus.GOOD, "In range")
                rec >= WellnessThresholds.RECOVERY_LOW -> SignalSummary("Recovery", SignalStatus.ON_TRACK, "Moderate")
                else -> SignalSummary("Recovery", SignalStatus.LOW, "Below normal")
            }
        )

        val proteinPct = if (input.targetProtein > 0) input.consumedProtein.toDouble() / input.targetProtein else 0.0
        signals.add(
            when {
                proteinPct >= WellnessThresholds.PROTEIN_GOOD -> SignalSummary("Nutrition", SignalStatus.GOOD, "Within range")
                proteinPct >= WellnessThresholds.PROTEIN_LOW -> SignalSummary("Nutrition", SignalStatus.ON_TRACK, "Building")
                else -> SignalSummary("Nutrition", SignalStatus.LOW, "Needs attention")
            }
        )

        val steps = input.steps
        signals.add(
            when {
                steps == null -> SignalSummary("Movement", SignalStatus.ON_TRACK, "No data")
                steps >= WellnessThresholds.DEFAULT_STEP_GOAL -> SignalSummary("Movement", SignalStatus.GOOD, "Goal reached")
                steps >= WellnessThresholds.MODERATE_MOVEMENT_STEPS -> SignalSummary("Movement", SignalStatus.ON_TRACK, "Moderate")
                else -> SignalSummary("Movement", SignalStatus.LOW, "Below goal")
            }
        )

        return signals
    }

    private fun buildTrends(input: SolInput): List<TrendSummary> {
        val trends = mutableListOf<TrendSummary>()
        if (input.daysTracked < MIN_DAYS_FOR_TRENDS) return trends

        if (input.weeklyRecovery.size >= 2) {
            val first = input.weeklyRecovery.first()
            val last = input.weeklyRecovery.last()
            val diff = last - first
            trends.add(
                TrendSummary("Recovery",
                    if (diff > 5) TrendDirection.UP else if (diff < -5) TrendDirection.DOWN else TrendDirection.STABLE,
                    if (first > 0) ((diff.toDouble() / first) * 100).toInt() else 0,
                    input.weeklyRecovery,
                    if (last >= WellnessThresholds.RECOVERY_GOOD) SignalStatus.GOOD else if (last >= WellnessThresholds.RECOVERY_LOW) SignalStatus.ON_TRACK else SignalStatus.LOW
                )
            )
        }

        if (input.weeklyProteinPct.size >= 2) {
            val first = input.weeklyProteinPct.first()
            val last = input.weeklyProteinPct.last()
            val diff = last - first
            trends.add(
                TrendSummary("Protein",
                    if (diff > WellnessThresholds.PROTEIN_MICRO_WIN_DELTA) TrendDirection.UP else if (diff < -WellnessThresholds.PROTEIN_MICRO_WIN_DELTA) TrendDirection.DOWN else TrendDirection.STABLE,
                    if (first > 0) ((diff / first) * 100).toInt() else 0,
                    input.weeklyProteinPct.map { (it * 100).toInt() },
                    if (last >= WellnessThresholds.PROTEIN_GOOD) SignalStatus.GOOD else if (last >= WellnessThresholds.PROTEIN_LOW) SignalStatus.ON_TRACK else SignalStatus.LOW
                )
            )
        }

        if (input.weeklySteps.size >= 2) {
            val first = input.weeklySteps.first()
            val last = input.weeklySteps.last()
            val diff = last - first
            trends.add(
                TrendSummary("Walking",
                    if (diff > 500) TrendDirection.UP else if (diff < -500) TrendDirection.DOWN else TrendDirection.STABLE,
                    if (first > 0) ((diff.toDouble() / first) * 100).toInt() else 0,
                    input.weeklySteps,
                    if (last >= WellnessThresholds.DEFAULT_STEP_GOAL) SignalStatus.GOOD else if (last >= WellnessThresholds.MODERATE_MOVEMENT_STEPS) SignalStatus.ON_TRACK else SignalStatus.LOW
                )
            )
        }

        return trends
    }

    private fun greetingForRecovery(recovery: Int?): String {
        if (recovery == null) return "Good morning."
        return when {
            recovery >= WellnessThresholds.RECOVERY_EXCELLENT -> "Good morning. Recovery looks strong today."
            recovery >= WellnessThresholds.RECOVERY_MODERATE -> "Good morning. Recovery is within your normal range."
            recovery >= WellnessThresholds.RECOVERY_VERY_LOW -> "Good morning. Recovery is lower than usual today."
            else -> "Good morning. Your body appears to be asking for extra recovery."
        }
    }

    private data class PrioritizedInsight(val insight: Script, val priority: Int)

    private fun pickPrimaryInsight(input: SolInput, morning: Boolean, evening: Boolean): PrioritizedInsight {
        val candidates = mutableListOf<PrioritizedInsight>()
        populateCandidates(input, morning, evening, candidates)
        if (candidates.isEmpty()) {
            val fallback = if (morning) fallbackMorning(input) else if (evening) fallbackEvening(input) else fallbackMorning(input)
            candidates.add(PrioritizedInsight(fallback, 10))
        }
        return candidates.maxByOrNull { it.priority } ?: PrioritizedInsight(fallbackMorning(input), 10)
    }

    private fun populateCandidates(input: SolInput, morning: Boolean, evening: Boolean, candidates: MutableList<PrioritizedInsight>) {
        val rec = input.recoveryScore
        val minData = input.daysTracked >= 1

        if (!minData) return

        if (input.streakDays == 100) candidates += PrioritizedInsight(STREAK_100, WellnessThresholds.PRIORITY_STREAK_100)
        if (input.streakDays == 30) candidates += PrioritizedInsight(STREAK_30, WellnessThresholds.PRIORITY_STREAK_30)
        if (input.streakDays == WellnessThresholds.STREAK_MILESTONE_7) candidates += PrioritizedInsight(STREAK_7, WellnessThresholds.PRIORITY_STREAK_7)

        if (rec != null && rec < WellnessThresholds.RECOVERY_VERY_LOW) candidates += PrioritizedInsight(
            Script("Very Low Recovery", "Your body appears to be asking for extra recovery.", "A lighter session or recovery day may be beneficial.",
                listOf("Recovery score is ${rec}%"), listOf("Consider a rest day", "Focus on sleep and hydration", "Light movement only"),
                "Your body appears to be asking for extra recovery. A lighter session or recovery day may be beneficial.", InsightType.OVERTRAINING), 700)

        if (input.recentTrainingVolumeIncrease && rec != null && rec < WellnessThresholds.RECOVERY_MODERATE) candidates += PrioritizedInsight(
            Script("Training Volume Increased", "Training volume has increased recently.", "Pay attention to recovery and energy levels over the next few days.",
                listOf("Training volume has increased", if (rec < 50) "Recovery is lower than optimal" else ""),
                listOf("Monitor your energy levels", "Consider a recovery day", "Prioritize sleep"),
                "Training volume has increased recently. Pay attention to recovery and energy levels.", InsightType.OVERTRAINING), 600)

        if (rec != null && rec < WellnessThresholds.RECOVERY_LOW && input.recentTrainingVolumeIncrease) candidates += PrioritizedInsight(
            Script("Recovery Declining with High Volume", "You've trained hard several days in a row.", "A recovery-focused day may help maintain performance.",
                listOf("Training volume is elevated", "Recovery is trending downward"), listOf("Take a recovery day", "Focus on sleep", "Light movement only"),
                "You've trained hard several days in a row. A recovery-focused day may help maintain performance.", InsightType.OVERTRAINING), 550)

        if (rec != null && rec in WellnessThresholds.RECOVERY_LOW..WellnessThresholds.RECOVERY_EARLY_WARNING_MAX && input.weeklyRecovery.size >= 3) {
            val declining = input.weeklyRecovery.takeLast(3).let { it[0] >= it[1] && it[1] >= it[2] }
            if (declining) candidates += PrioritizedInsight(RECOVERY_EARLY_WARNING, WellnessThresholds.PRIORITY_RECOVERY_EARLY_WARNING)
        }

        if (input.recentTrainingVolumeIncrease && input.sleepHours != null && input.sleepHours < WellnessThresholds.SLEEP_LOW) {
            candidates += PrioritizedInsight(WORKLOAD_BALANCE, WellnessThresholds.PRIORITY_WORKLOAD_BALANCE)
        }

        if (input.daysActiveThisWeek >= WellnessThresholds.CONSISTENT_WEEK_DAYS && rec != null && rec >= WellnessThresholds.RECOVERY_GOOD) {
            candidates += PrioritizedInsight(CONSISTENCY_PRAISE, WellnessThresholds.PRIORITY_CONSISTENCY_PRAISE)
        }

        val sleep = input.sleepHours
        val prevSleep = input.previousSleepHours
        if (sleep != null) {
            if (sleep < WellnessThresholds.SLEEP_VERY_POOR) candidates += PrioritizedInsight(POOR_SLEEP_MULTI, WellnessThresholds.PRIORITY_POOR_SLEEP)
            else if (sleep < WellnessThresholds.SLEEP_POOR) candidates += PrioritizedInsight(POOR_SLEEP, WellnessThresholds.PRIORITY_POOR_SLEEP)
            else if (prevSleep != null && sleep > prevSleep + WellnessThresholds.SLEEP_IMPROVEMENT_DELTA) candidates += PrioritizedInsight(SLEEP_IMPROVED, WellnessThresholds.PRIORITY_SLEEP_IMPROVED)
            else if (prevSleep != null && sleep >= WellnessThresholds.SLEEP_OPTIMAL && kotlin.math.abs(sleep - prevSleep) < WellnessThresholds.SLEEP_BASELINE_DELTA) candidates += PrioritizedInsight(SLEEP_CONSISTENT, WellnessThresholds.PRIORITY_SLEEP_CONSISTENT)
        }

        val proteinPct = if (input.targetProtein > 0) input.consumedProtein.toDouble() / input.targetProtein else 0.0
        if (proteinPct >= WellnessThresholds.PROTEIN_GOAL_MET) candidates += PrioritizedInsight(PROTEIN_GOAL_MET, WellnessThresholds.PRIORITY_PROTEIN_GOAL_MET)
        else if (proteinPct >= WellnessThresholds.PROTEIN_NEAR) candidates += PrioritizedInsight(NEAR_PROTEIN_GOAL, WellnessThresholds.PRIORITY_NEAR_PROTEIN_GOAL)
        else if (evening && proteinPct < WellnessThresholds.PROTEIN_LOW) candidates += PrioritizedInsight(LOW_PROTEIN, WellnessThresholds.PRIORITY_LOW_PROTEIN)

        if (input.weeklyProteinPct.size >= 3 && input.weeklyProteinPct.takeLast(3).all { it < WellnessThresholds.PROTEIN_SLIPPING_THRESHOLD }) {
            candidates += PrioritizedInsight(PROTEIN_SLIPPING, WellnessThresholds.PRIORITY_PROTEIN_SLIPPING)
        }

        val calPct = if (input.targetCalories > 0) input.consumedCalories.toDouble() / input.targetCalories else 0.0
        if (evening && calPct in 0.9..1.1) candidates += PrioritizedInsight(CALORIE_GOAL_MET, 370)

        if (input.measurementImproving == true && input.strengthIncreasing == true) candidates += PrioritizedInsight(POSITIVE_RECOMP, 360)
        else if (input.strengthIncreasing == true) candidates += PrioritizedInsight(MUSCLE_GAIN_TREND, 340)
        else if (input.measurementImproving == true) candidates += PrioritizedInsight(FAT_LOSS_TREND, 330)

        if (input.strengthIncreasing == true) candidates += PrioritizedInsight(PERSONAL_BEST, 350)

        if (input.workoutToday) {
            candidates += PrioritizedInsight(WORKOUT_COMPLETED, 300)
            if (input.daysActiveThisWeek >= 4) candidates += PrioritizedInsight(WORKOUT_STREAK, 310)
        } else if (morning && rec != null && rec >= WellnessThresholds.RECOVERY_MODERATE) candidates += PrioritizedInsight(MISSED_WORKOUT, WellnessThresholds.PRIORITY_MISSED_WORKOUT)

        val steps = input.steps
        val prevSteps = input.previousSteps
        if (steps != null && steps >= WellnessThresholds.DEFAULT_STEP_GOAL) candidates += PrioritizedInsight(WALKING_GOAL_MET, WellnessThresholds.PRIORITY_WALKING_GOAL)
        else if (steps != null && evening && steps < WellnessThresholds.SEDENTARY_STEPS) candidates += PrioritizedInsight(SEDENTARY_DAY, WellnessThresholds.PRIORITY_SEDENTARY_DAY)
        if (steps != null && prevSteps != null && prevSteps > 0 && steps > prevSteps * 1.2) candidates += PrioritizedInsight(WALKING_IMPROVED, WellnessThresholds.PRIORITY_WALKING_IMPROVED)

        if (input.weeklySteps.size >= 5) {
            val recent = input.weeklySteps.takeLast(5)
            val decline = recent.zipWithNext().all { (a, b) -> b <= a }
            if (decline && recent.last() < WellnessThresholds.MODERATE_MOVEMENT_STEPS) candidates += PrioritizedInsight(LOW_MOVEMENT_WEEK, WellnessThresholds.PRIORITY_LOW_MOVEMENT_WEEK)
        }

        val pr = input.previousRecoveryScore
        if (rec != null && pr != null) {
            if (rec > pr + 5) candidates += PrioritizedInsight(RECOVERY_IMPROVING, WellnessThresholds.PRIORITY_RECOVERY_IMPROVING)
            else if (rec < pr - 5) candidates += PrioritizedInsight(RECOVERY_DECLINING, WellnessThresholds.PRIORITY_RECOVERY_DECLINING)
            else candidates += PrioritizedInsight(RECOVERY_STABLE, WellnessThresholds.PRIORITY_RECOVERY_STABLE)
        }

        if (input.meditationMinutes != null && input.meditationMinutes > 0 && input.daysActiveThisWeek >= WellnessThresholds.ACTIVE_WEEK_DAYS) candidates += PrioritizedInsight(MEDITATION_STREAK, WellnessThresholds.PRIORITY_MEDITATION_STREAK)

        val stress = input.stressLevel
        val prevStress = input.previousStressLevel
        if (stress != null && stress >= 4) candidates += PrioritizedInsight(HIGH_STRESS, WellnessThresholds.PRIORITY_HIGH_STRESS)
        if (stress != null && prevStress != null && stress < prevStress - 1) candidates += PrioritizedInsight(STRESS_IMPROVED, WellnessThresholds.PRIORITY_STRESS_IMPROVED)

        if (input.journalDays >= 3) candidates += PrioritizedInsight(JOURNAL_STREAK, WellnessThresholds.PRIORITY_JOURNAL_STREAK)
        when (input.journalSentiment) {
            JournalSentiment.POSITIVE -> candidates += PrioritizedInsight(JOURNAL_POSITIVE, WellnessThresholds.PRIORITY_JOURNAL_POSITIVE)
            JournalSentiment.CHALLENGING -> candidates += PrioritizedInsight(JOURNAL_CHALLENGING, WellnessThresholds.PRIORITY_JOURNAL_CHALLENGING)
            JournalSentiment.NEUTRAL, null -> {}
        }

        val waterPct = if (input.waterGoalMl > 0) input.waterMl.toDouble() / input.waterGoalMl else 0.0
        if (evening && waterPct < WellnessThresholds.WATER_LOW) candidates += PrioritizedInsight(
            Script("Hydration", "Water intake is lower than your goal today.", "Staying hydrated supports recovery and energy.",
                listOf("You've logged ${input.waterMl}ml of ${input.waterGoalMl}ml"), listOf("Drink a glass of water now", "Keep water nearby"),
                "Water intake is lower than your goal today. Staying hydrated supports recovery and energy.", InsightType.WATER), WellnessThresholds.PRIORITY_WATER_LOW)
    }

    private fun fallbackMorning(input: SolInput): Script {
        val rec = input.recoveryScore
        return when {
            rec == null -> Script("", "", "", emptyList(), emptyList(), "", InsightType.MORNING_GREETING)
            rec >= WellnessThresholds.RECOVERY_EXCELLENT -> MORNING_EXCELLENT
            rec >= WellnessThresholds.RECOVERY_MODERATE -> MORNING_AVERAGE
            rec >= WellnessThresholds.RECOVERY_VERY_LOW -> MORNING_LOW
            else -> MORNING_VERY_LOW
        }
    }

    private fun fallbackEvening(input: SolInput): Script {
        val rec = input.recoveryScore
        return when {
            rec == null -> EVENING_STRONG
            rec >= WellnessThresholds.RECOVERY_MODERATE -> EVENING_STRONG
            rec >= WellnessThresholds.RECOVERY_VERY_LOW -> EVENING_BALANCED
            else -> EVENING_RECOVERY_FOCUS
        }
    }

    // ─── SCRIPT LIBRARY ───

    private val MORNING_EXCELLENT = Script("Excellent Recovery", "Your body appears ready for a normal or challenging training session.",
        "A regular training day should feel comfortable.",
        listOf("Recovery score is high", "Sleep and movement are supporting recovery"),
        listOf("Proceed with your planned workout", "Train according to plan if you feel ready"),
        "Recovery looks strong today. Your body appears ready for a normal or challenging training session.", InsightType.MORNING_GREETING)

    private val MORNING_AVERAGE = Script("Average Recovery", "A regular training day should feel comfortable.",
        "Pay attention to how your body feels during warm-up.",
        listOf("Recovery is within your normal range"),
        listOf("Proceed with your planned workout", "Adjust intensity based on how you feel"),
        "Recovery is within your normal range. A regular training day should feel comfortable.", InsightType.MORNING_GREETING)

    private val MORNING_LOW = Script("Low Recovery", "Pay attention to how your body feels and consider reducing intensity if needed.",
        "A moderate session may be more beneficial than pushing hard.",
        listOf("Recovery is lower than usual"),
        listOf("Reduce workout intensity", "Focus on form over weight", "Prioritize hydration"),
        "Recovery is lower than usual today. A moderate session may be more beneficial than pushing hard.", InsightType.MORNING_GREETING)

    private val MORNING_VERY_LOW = Script("Very Low Recovery", "A lighter session or recovery day may be beneficial.",
        "Listen to your body and prioritise recovery today.",
        listOf("Recovery score is well below your normal range"),
        listOf("Take a rest day", "Focus on sleep", "Light stretching or walking"),
        "Your body appears to be asking for extra recovery. A lighter session or recovery day may be beneficial.", InsightType.MORNING_GREETING)

    private val SLEEP_IMPROVED = Script("Sleep Improved", "Your sleep improved compared to yesterday.", "Recovery may benefit from the extra rest.",
        listOf("Sleep duration increased compared to the previous night"), listOf("Maintain a consistent bedtime"),
        "Your sleep improved compared to yesterday. Recovery may benefit from the extra rest.", InsightType.SLEEP)

    private val SLEEP_CONSISTENT = Script("Consistent Sleep", "Your sleep schedule has been consistent recently.", "Small habits like this often support long-term progress.",
        listOf("Sleep has been consistent for several days"), listOf("Keep your current sleep routine"),
        "Your sleep schedule has been consistent recently. Small habits like this often support long-term progress.", InsightType.SLEEP)

    private val POOR_SLEEP = Script("Sleep was shorter than usual", "Consider prioritising recovery and hydration today.", "Extra rest may help maintain energy levels.",
        listOf("Sleep was below your typical duration"), listOf("Prioritise hydration today", "Aim for an earlier bedtime"),
        "Sleep was shorter than your usual amount. Consider prioritising recovery and hydration today.", InsightType.SLEEP)

    private val POOR_SLEEP_MULTI = Script("Sleep has been below normal for several days", "Recovery may take longer until sleep improves.", "Focus on consistent sleep timing.",
        listOf("Sleep has been consistently low for multiple days"), listOf("Set a consistent bedtime", "Reduce screen time before bed", "Prioritise sleep above all else"),
        "Sleep has been below your normal range for several days. Recovery may take longer until sleep improves.", InsightType.SLEEP)

    private val WORKOUT_COMPLETED = Script("Workout Complete", "Consistency is one of the strongest predictors of progress.", "Keep building momentum.",
        listOf("You logged a workout today"), listOf("Stay hydrated", "Consider a light cool-down walk"),
        "Workout complete. Consistency is one of the strongest predictors of progress. Keep building momentum.", InsightType.WORKOUT)

    private val WORKOUT_STREAK = Script("Workout Streak", "You've trained consistently this week.", "Building momentum is often more valuable than a complete workout.",
        listOf("You've worked out at least 4 days this week"), listOf("Maintain your routine", "Consider a recovery day if needed"),
        "You've trained consistently this week. Building momentum is often more valuable than a complete workout.", InsightType.WORKOUT)

    private val PERSONAL_BEST = Script("New Personal Best", "Progress is moving in the right direction.", "Keep building on this momentum.",
        listOf("Strength is improving across your lifts"), listOf("Acknowledge this milestone", "Keep training consistently"),
        "You achieved a new personal best today. Progress is moving in the right direction.", InsightType.WORKOUT)

    private val MISSED_WORKOUT = Script("No Workout Logged Today", "A short walk or recovery session can still contribute to your goals.", "Movement of any kind counts.",
        listOf("Rest days are part of the process"), listOf("Consider a walk", "Stretch or do light mobility work"),
        "No workout was logged today. A short walk or recovery session can still contribute to your goals.", InsightType.WORKOUT)

    private val PROTEIN_GOAL_MET = Script("Protein Goal Reached", "Consistent nutrition supports consistent progress.", "Keep the momentum going.",
        listOf("You've hit your protein target today"), listOf("Maintain consistent protein intake"),
        "You've reached your protein goal today. Consistent nutrition supports consistent progress.", InsightType.NUTRITION)

    private val NEAR_PROTEIN_GOAL = Script("Close to Protein Goal", "One protein-rich meal or snack could complete today's target.", "A small addition can make the difference.",
        listOf("You're close to your protein target"), listOf("Add a protein-rich snack", "Consider Greek yoghurt, eggs, or a shake"),
        "You're close to your protein target. One protein-rich meal or snack could complete today's goal.", InsightType.NUTRITION)

    private val LOW_PROTEIN = Script("Protein Intake Lower Than Target", "Additional protein may support recovery and muscle maintenance.", "Try to include protein in your next meal.",
        listOf("Protein intake is below your daily target"), listOf("Add protein to your next meal", "Consider a protein shake"),
        "Protein intake is lower than your target today. Additional protein may support recovery and muscle maintenance.", InsightType.NUTRITION)

    private val CALORIE_GOAL_MET = Script("Calorie Goal Aligned", "Today's nutrition is aligned with your calorie target.", "Consistency matters more than perfection.",
        listOf("Your calorie intake is on target"), listOf("Continue with balanced meals"),
        "Today's nutrition is aligned with your calorie target. Consistency matters more than perfection.", InsightType.NUTRITION)

    private val POSITIVE_RECOMP = Script("Positive Body Recomposition", "Weight has remained stable while measurements are improving.", "This is a sign of effective training and nutrition.",
        listOf("Weight is stable", "Measurements are improving", "Strength is increasing"), listOf("Continue your current approach", "Stay consistent with nutrition"),
        "Weight has remained stable while measurements are improving. This may indicate positive body recomposition.", InsightType.BODY_RECOMP)

    private val MUSCLE_GAIN_TREND = Script("Muscle Gain Trend", "Strength and body weight are both trending upward.", "Progress appears consistent with a muscle-building phase.",
        listOf("Strength is increasing", "Body weight is trending up"), listOf("Ensure adequate protein intake", "Keep training progressively"),
        "Strength and body weight are both trending upward. Progress appears consistent with a muscle-building phase.", InsightType.BODY_RECOMP)

    private val FAT_LOSS_TREND = Script("Fat Loss Trend", "Measurements are trending downward while strength remains stable.", "Progress appears consistent with fat loss.",
        listOf("Measurements are improving", "Strength is maintained"), listOf("Continue with your nutrition plan", "Maintain training intensity"),
        "Measurements are trending downward while strength remains stable. Progress appears consistent with fat loss.", InsightType.BODY_RECOMP)

    private val WALKING_GOAL_MET = Script("Movement Goal Reached", "Regular movement supports both health and recovery.", "Well done on staying active.",
        listOf("You've reached your daily step goal"), listOf("Stay consistent with daily movement"),
        "You've reached your movement goal today. Regular movement supports both health and recovery.", InsightType.WALKING)

    private val WALKING_IMPROVED = Script("Walking Activity Increased", "Walking activity has increased this week.", "Small increases in movement can have meaningful benefits.",
        listOf("Your step count is up compared to last week"), listOf("Continue incorporating walks into your day"),
        "Walking activity has increased this week. Small increases in movement can have meaningful benefits.", InsightType.WALKING)

    private val SEDENTARY_DAY = Script("Movement Has Been Lower Today", "A short walk may help improve energy and recovery.", "Even 10 minutes of walking makes a difference.",
        listOf("Step count is below your usual level"), listOf("Take a short walk", "Stand up and stretch regularly"),
        "Movement has been lower than usual today. A short walk may help improve energy and recovery.", InsightType.WALKING)

    private val RECOVERY_IMPROVING = Script("Recovery Is Improving", "Recent habits appear to be supporting recovery.", "Continue focusing on consistency.",
        listOf("Recovery score has increased compared to earlier this week"), listOf("Keep up your current habits", "Maintain consistent sleep and hydration"),
        "Recovery has improved compared to earlier this week. Recent habits appear to be supporting recovery.", InsightType.RECOVERY)

    private val RECOVERY_STABLE = Script("Recovery Is Stable", "Continue focusing on consistency.", "Your current routine is working.",
        listOf("Recovery remains within your normal range"), listOf("Maintain your current habits"),
        "Recovery remains within your normal range. Continue focusing on consistency.", InsightType.RECOVERY)

    private val RECOVERY_DECLINING = Script("Recovery Is Trending Downward", "Extra rest may help prevent accumulated fatigue.", "Prioritising recovery now supports long-term progress.",
        listOf("Recovery score has decreased compared to earlier this week"), listOf("Consider a rest day", "Prioritise sleep", "Reduce training intensity"),
        "Recovery is trending downward. Extra rest may help prevent accumulated fatigue.", InsightType.RECOVERY)

    private val MEDITATION_STREAK = Script("Meditation Practice Consistent", "Consistency often matters more than session length.", "Your mindfulness habit is supporting your wellness.",
        listOf("You've maintained your meditation practice this week"), listOf("Keep your daily practice", "Even short sessions count"),
        "You've maintained your meditation practice this week. Consistency often matters more than session length.", InsightType.MEDITATION)

    private val STRESS_IMPROVED = Script("Stress Levels Improving", "Reported stress levels have improved recently.", "Your recovery habits may be contributing.",
        listOf("Stress has decreased compared to your previous report"), listOf("Continue with your current recovery habits"),
        "Reported stress levels have improved recently. Your recovery habits may be contributing.", InsightType.MEDITATION)

    private val HIGH_STRESS = Script("Stress Appears Higher Than Usual", "A short mindfulness session may help create some space.", "Prioritising recovery may help manage stress levels.",
        listOf("Your reported stress level is elevated"), listOf("Try a short meditation", "Take a walk", "Journal for a few minutes"),
        "Stress appears higher than usual today. A short mindfulness session may help create some space.", InsightType.MEDITATION)

    private val JOURNAL_STREAK = Script("Journaling Consistently", "Reflection can make patterns easier to recognise over time.", "Keep paying attention to what is working well.",
        listOf("You've been journaling regularly"), listOf("Continue your daily reflection practice"),
        "You've been journaling consistently. Reflection can make patterns easier to recognise over time.", InsightType.JOURNAL)

    private val JOURNAL_POSITIVE = Script("Positive Trend Noticed", "Recent journal entries suggest a positive trend in mood.", "Continue paying attention to what is working well.",
        listOf("Mood appears to be trending positively"), listOf("Reflect on what's contributing to this trend"),
        "Recent journal entries suggest a positive trend in mood. Continue paying attention to what is working well.", InsightType.JOURNAL)

    private val JOURNAL_CHALLENGING = Script("Challenging Period Noticed", "Recent entries suggest a more demanding period.", "Prioritizing recovery may be helpful.",
        listOf("Mood appears to be more challenging recently"), listOf("Prioritise rest and recovery", "Consider a light day", "Talk to someone you trust"),
        "Recent entries suggest a more demanding period. Prioritizing recovery may be helpful.", InsightType.JOURNAL)

    private val STREAK_7 = Script("Seven Days of Consistency", "Momentum is building.", "Keep showing up.",
        listOf("You've trained for 7 consecutive days"), listOf("Maintain your routine", "Consider a recovery day if needed"),
        "Seven days of consistency. Momentum is building.", InsightType.STREAK_MILESTONE)

    private val STREAK_30 = Script("Thirty Days Completed", "Your habits are becoming part of your routine.", "This level of consistency is impressive.",
        listOf("You've trained for 30 days"), listOf("Celebrate this milestone", "Keep building on your habits"),
        "Thirty days completed. Your habits are becoming part of your routine.", InsightType.STREAK_MILESTONE)

    private val STREAK_100 = Script("One Hundred Days of Consistency", "Progress like this is built one day at a time.", "This shows real consistency.",
        listOf("You've trained for 100 days"), listOf("Reflect on the habits you've built", "Maintain your current habits"),
        "One hundred days of consistency. Progress like this is built one day at a time.", InsightType.STREAK_MILESTONE)

    private val EVENING_STRONG = Script("Strong Day", "You completed your key wellness goals.", "Keep going.",
        listOf("You made meaningful progress today"), listOf("Prepare for tomorrow", "Get quality sleep"),
        "Today is nearly complete. You completed your key wellness goals. Keep going.", InsightType.EVENING)

    // ─── Proactive Early Warning Scripts ───
    private val RECOVERY_EARLY_WARNING = Script("Recovery Is Trending Down", "Recovery has been declining gradually over the past few days.", "A lighter session today may prevent accumulated fatigue.",
        listOf("Recovery has been decreasing for several days"), listOf("Reduce intensity today", "Prioritise sleep and hydration"),
        "Recovery has been declining gradually. A lighter session today may prevent accumulated fatigue.", InsightType.RECOVERY)

    private val WORKLOAD_BALANCE = Script("Training Load vs Recovery", "Training volume is increasing while sleep is limited.", "Progress comes from training and recovery. Respect both.",
        listOf("Training volume is elevated", "Sleep has been limited"), listOf("Consider a rest day", "Prioritise sleep tonight"),
        "Training volume is climbing while recovery is limited. Respect both training and rest.", InsightType.OVERTRAINING)

    private val PROTEIN_SLIPPING = Script("Protein Consistency Slipping", "Protein intake has been below target for several days.", "Prioritise protein at your next meal to protect progress.",
        listOf("Protein has been below target for multiple days"), listOf("Add protein to your next meal", "Consider a protein shake"),
        "Protein consistency is slipping. Prioritise protein at your next meal.", InsightType.NUTRITION)

    private val LOW_MOVEMENT_WEEK = Script("Movement Has Declined This Week", "Walking volume has been decreasing over the past several days.", "A short walk today may help restore momentum.",
        listOf("Step count has been declining for several days"), listOf("Take a 15 minute walk", "Stand up and stretch regularly"),
        "Walking volume has been declining this week. A short walk today may help restore momentum.", InsightType.WALKING)

    private val CONSISTENCY_PRAISE = Script("Consistent Week", "You have been active and recovering well this week.", "That consistency is building momentum.",
        listOf("You have trained at least 4 days this week", "Recovery is within a healthy range"),
        listOf("Keep showing up", "Maintain your current habits"),
        "You have been consistent this week. That consistency is building momentum.", InsightType.CONSISTENCY)

    private val EVENING_BALANCED = Script("Balanced Day", "Small consistent actions tend to compound over time.", "Tomorrow's performance often starts with tonight's habits.",
        listOf("You made meaningful progress today"), listOf("Prioritise sleep", "Set intentions for tomorrow"),
        "Today included meaningful progress. Small consistent actions tend to compound over time.", InsightType.EVENING)

    private val EVENING_RECOVERY_FOCUS = Script("Recovery Focus", "A recovery-focused evening may help reset for tomorrow.", "Prioritising sleep and relaxation can support recovery.",
        listOf("Recovery has been lower than usual"), listOf("Prioritise sleep", "Light stretching", "Reduce screen time before bed"),
        "Today may be a good opportunity to prioritise recovery. Tomorrow's performance often starts with tonight's habits.", InsightType.EVENING)

    private val EMPTY_STATE_SCRIPT = Script(
        "You're still building your wellness profile.",
        "Complete a few days of tracking and I'll begin identifying trends.",
        "Track your workouts, meals, and daily metrics to unlock personalised insights.",
        listOf("No tracking data available yet"),
        listOf("Log your first workout", "Track your meals", "Record your daily wellness"),
        "You're still building your wellness profile. Complete a few days of tracking and I'll begin identifying trends.",
        InsightType.EMPTY_STATE
    )
}
