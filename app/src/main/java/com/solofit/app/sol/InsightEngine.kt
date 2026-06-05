package com.solofit.app.sol

import java.time.LocalTime
import javax.inject.Inject

class InsightEngine @Inject constructor() {

    companion object {
        const val MIN_DAYS_FOR_TRENDS = 3
        const val MIN_DAYS_FOR_WEEKLY = 7
        const val MIN_DAYS_FOR_PATTERNS = 14
    }

    fun compute(input: SolInput): SolInsight {
        val timeOfDay = LocalTime.now()
        val morning = timeOfDay.hour < 12
        val recovery = input.recoveryScore
        val greeting = if (morning) greetingForRecovery(recovery) else ""

        val (insight, _) = pickPrimaryInsight(input, morning, timeOfDay.hour >= 17)

        return SolInsight(
            greeting = greeting,
            headline = insight.headline,
            detail = insight.detail,
            reasoning = insight.reasoning,
            recommendations = insight.recommendations,
            voiceLine = insight.voiceLine.ifEmpty { "$greeting ${insight.headline}".trim() },
            type = insight.type
        )
    }

    fun computeBriefing(input: SolInput): SolBriefing {
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

        return SolBriefing(
            greeting = greeting,
            primary = primary.insight,
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
        val sleep = input.sleepHours ?: 6.0
        val stress = input.stressLevel ?: 3
        val steps = input.steps ?: 0
        val proteinPct = if (input.targetProtein > 0) input.consumedProtein.toDouble() / input.targetProtein else 0.0

        val meditated = (input.meditationMinutes ?: 0) > 0
        val journaled = input.journalDays > 0

        return when {
            rec < 40 || input.recentTrainingVolumeIncrease || sleep < 5.0 -> DayLabel.RECOVERY_FOCUS
            rec >= 70 && input.workoutToday && sleep >= 7.0 && steps >= 6000 -> DayLabel.PERFORMANCE
            proteinPct < 0.5 && input.workoutToday -> DayLabel.NUTRITION_FOCUS
            stress >= 4 && meditated || journaled -> DayLabel.MINDFULNESS
            input.daysActiveThisWeek >= 4 || input.streakDays >= 7 -> DayLabel.CONSISTENCY
            else -> DayLabel.BALANCED
        }
    }

    private fun buildSignals(input: SolInput): List<SignalSummary> {
        val signals = mutableListOf<SignalSummary>()

        val rec = input.recoveryScore
        signals.add(
            when {
                rec == null -> SignalSummary("Recovery", SignalStatus.ON_TRACK, "No data")
                rec >= 60 -> SignalSummary("Recovery", SignalStatus.GOOD, "Looking good")
                rec >= 40 -> SignalSummary("Recovery", SignalStatus.ON_TRACK, "Moderate")
                else -> SignalSummary("Recovery", SignalStatus.LOW, "Below normal")
            }
        )

        val proteinPct = if (input.targetProtein > 0) input.consumedProtein.toDouble() / input.targetProtein else 0.0
        signals.add(
            when {
                proteinPct >= 0.9 -> SignalSummary("Nutrition", SignalStatus.GOOD, "On track")
                proteinPct >= 0.5 -> SignalSummary("Nutrition", SignalStatus.ON_TRACK, "Building")
                else -> SignalSummary("Nutrition", SignalStatus.LOW, "Below target")
            }
        )

        val steps = input.steps
        signals.add(
            when {
                steps == null -> SignalSummary("Movement", SignalStatus.ON_TRACK, "No data")
                steps >= 8000 -> SignalSummary("Movement", SignalStatus.GOOD, "Goal reached")
                steps >= 5000 -> SignalSummary("Movement", SignalStatus.ON_TRACK, "Moderate")
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
                    if (last >= 60) SignalStatus.GOOD else if (last >= 40) SignalStatus.ON_TRACK else SignalStatus.LOW
                )
            )
        }

        if (input.weeklyProteinPct.size >= 2) {
            val first = input.weeklyProteinPct.first()
            val last = input.weeklyProteinPct.last()
            val diff = last - first
            trends.add(
                TrendSummary("Protein",
                    if (diff > 0.1) TrendDirection.UP else if (diff < -0.1) TrendDirection.DOWN else TrendDirection.STABLE,
                    if (first > 0) ((diff / first) * 100).toInt() else 0,
                    input.weeklyProteinPct.map { (it * 100).toInt() },
                    if (last >= 0.9) SignalStatus.GOOD else if (last >= 0.5) SignalStatus.ON_TRACK else SignalStatus.LOW
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
                    if (last >= 8000) SignalStatus.GOOD else if (last >= 5000) SignalStatus.ON_TRACK else SignalStatus.LOW
                )
            )
        }

        return trends
    }

    private fun greetingForRecovery(recovery: Int?): String {
        if (recovery == null) return "Good morning."
        return when {
            recovery >= 80 -> "Good morning. Recovery looks strong today."
            recovery >= 50 -> "Good morning. Recovery is within your normal range."
            recovery >= 30 -> "Good morning. Recovery is lower than usual today."
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
        return candidates.maxByOrNull { it.priority }!!
    }

    private fun populateCandidates(input: SolInput, morning: Boolean, evening: Boolean, candidates: MutableList<PrioritizedInsight>) {
        val rec = input.recoveryScore
        val minData = input.daysTracked >= 1

        if (!minData) return

        if (input.streakDays == 100) candidates += PrioritizedInsight(STREAK_100, 1000)
        if (input.streakDays == 30) candidates += PrioritizedInsight(STREAK_30, 900)
        if (input.streakDays == 7) candidates += PrioritizedInsight(STREAK_7, 800)

        if (rec != null && rec < 30) candidates += PrioritizedInsight(
            Script("Very Low Recovery", "Your body appears to be asking for extra recovery.", "A lighter session or recovery day may be beneficial.",
                listOf("Recovery score is ${rec}%"), listOf("Consider a rest day", "Focus on sleep and hydration", "Light movement only"),
                "Your body appears to be asking for extra recovery. A lighter session or recovery day may be beneficial.", InsightType.OVERTRAINING), 700)

        if (input.recentTrainingVolumeIncrease && rec != null && rec < 50) candidates += PrioritizedInsight(
            Script("Training Volume Increased", "Training volume has increased recently.", "Pay attention to recovery and energy levels over the next few days.",
                listOf("Training volume has increased", if (rec < 50) "Recovery is lower than optimal" else ""),
                listOf("Monitor your energy levels", "Consider a recovery day", "Prioritize sleep"),
                "Training volume has increased recently. Pay attention to recovery and energy levels.", InsightType.OVERTRAINING), 600)

        if (rec != null && rec < 40 && input.recentTrainingVolumeIncrease) candidates += PrioritizedInsight(
            Script("Recovery Declining with High Volume", "You've trained hard several days in a row.", "A recovery-focused day may help maintain performance.",
                listOf("Training volume is elevated", "Recovery is trending downward"), listOf("Take a recovery day", "Focus on sleep", "Light movement only"),
                "You've trained hard several days in a row. A recovery-focused day may help maintain performance.", InsightType.OVERTRAINING), 550)

        val sleep = input.sleepHours
        val prevSleep = input.previousSleepHours
        if (sleep != null) {
            if (sleep < 5.0) candidates += PrioritizedInsight(POOR_SLEEP_MULTI, 500)
            else if (sleep < 6.0) candidates += PrioritizedInsight(POOR_SLEEP, 500)
            else if (prevSleep != null && sleep > prevSleep + 0.5) candidates += PrioritizedInsight(SLEEP_IMPROVED, 260)
            else if (prevSleep != null && sleep >= 7.0 && kotlin.math.abs(sleep - prevSleep) < 0.5) candidates += PrioritizedInsight(SLEEP_CONSISTENT, 220)
        }

        val proteinPct = if (input.targetProtein > 0) input.consumedProtein.toDouble() / input.targetProtein else 0.0
        if (proteinPct >= 1.0) candidates += PrioritizedInsight(PROTEIN_GOAL_MET, 400)
        else if (proteinPct >= 0.8) candidates += PrioritizedInsight(NEAR_PROTEIN_GOAL, 390)
        else if (evening && proteinPct < 0.5) candidates += PrioritizedInsight(LOW_PROTEIN, 380)

        val calPct = if (input.targetCalories > 0) input.consumedCalories.toDouble() / input.targetCalories else 0.0
        if (evening && calPct in 0.9..1.1) candidates += PrioritizedInsight(CALORIE_GOAL_MET, 370)

        if (input.measurementImproving == true && input.strengthIncreasing == true) candidates += PrioritizedInsight(POSITIVE_RECOMP, 360)
        else if (input.strengthIncreasing == true) candidates += PrioritizedInsight(MUSCLE_GAIN_TREND, 340)
        else if (input.measurementImproving == true) candidates += PrioritizedInsight(FAT_LOSS_TREND, 330)

        if (input.strengthIncreasing == true) candidates += PrioritizedInsight(PERSONAL_BEST, 350)

        if (input.workoutToday) {
            candidates += PrioritizedInsight(WORKOUT_COMPLETED, 300)
            if (input.daysActiveThisWeek >= 4) candidates += PrioritizedInsight(WORKOUT_STREAK, 310)
        } else if (morning && rec != null && rec >= 50) candidates += PrioritizedInsight(MISSED_WORKOUT, 290)

        val steps = input.steps
        val prevSteps = input.previousSteps
        if (steps != null && steps >= 8000) candidates += PrioritizedInsight(WALKING_GOAL_MET, 280)
        else if (steps != null && evening && steps < 3000) candidates += PrioritizedInsight(SEDENTARY_DAY, 270)
        if (steps != null && prevSteps != null && prevSteps > 0 && steps > prevSteps * 1.2) candidates += PrioritizedInsight(WALKING_IMPROVED, 260)

        val pr = input.previousRecoveryScore
        if (rec != null && pr != null) {
            if (rec > pr + 5) candidates += PrioritizedInsight(RECOVERY_IMPROVING, 250)
            else if (rec < pr - 5) candidates += PrioritizedInsight(RECOVERY_DECLINING, 240)
            else candidates += PrioritizedInsight(RECOVERY_STABLE, 230)
        }

        if (input.meditationMinutes != null && input.meditationMinutes > 0 && input.daysActiveThisWeek >= 3) candidates += PrioritizedInsight(MEDITATION_STREAK, 150)

        val stress = input.stressLevel
        val prevStress = input.previousStressLevel
        if (stress != null && stress >= 4) candidates += PrioritizedInsight(HIGH_STRESS, 140)
        if (stress != null && prevStress != null && stress < prevStress - 1) candidates += PrioritizedInsight(STRESS_IMPROVED, 160)

        if (input.journalDays >= 3) candidates += PrioritizedInsight(JOURNAL_STREAK, 100)
        when (input.journalSentiment) {
            JournalSentiment.POSITIVE -> candidates += PrioritizedInsight(JOURNAL_POSITIVE, 110)
            JournalSentiment.CHALLENGING -> candidates += PrioritizedInsight(JOURNAL_CHALLENGING, 110)
            JournalSentiment.NEUTRAL, null -> {}
        }

        val waterPct = if (input.waterGoalMl > 0) input.waterMl.toDouble() / input.waterGoalMl else 0.0
        if (evening && waterPct < 0.5) candidates += PrioritizedInsight(
            Script("Hydration", "Water intake is lower than your goal today.", "Staying hydrated supports recovery and energy.",
                listOf("You've logged ${input.waterMl}ml of ${input.waterGoalMl}ml"), listOf("Drink a glass of water now", "Keep water nearby"),
                "Water intake is lower than your goal today. Staying hydrated supports recovery and energy.", InsightType.WATER), 110)
    }

    private fun fallbackMorning(input: SolInput): Script {
        val rec = input.recoveryScore
        return when {
            rec == null -> Script("", "", "", emptyList(), emptyList(), "", InsightType.MORNING_GREETING)
            rec >= 80 -> MORNING_EXCELLENT
            rec >= 50 -> MORNING_AVERAGE
            rec >= 30 -> MORNING_LOW
            else -> MORNING_VERY_LOW
        }
    }

    private fun fallbackEvening(input: SolInput): Script {
        val rec = input.recoveryScore
        return when {
            rec == null -> EVENING_STRONG
            rec >= 50 -> EVENING_STRONG
            rec >= 30 -> EVENING_BALANCED
            else -> EVENING_RECOVERY_FOCUS
        }
    }

    // ─── SCRIPT LIBRARY ───

    private val MORNING_EXCELLENT = Script("Excellent Recovery", "Your body appears ready for a normal or challenging training session.",
        "A regular training day should feel comfortable.",
        listOf("Recovery score is high", "Sleep and movement are supporting recovery"),
        listOf("Proceed with your planned workout", "Push yourself if you feel good"),
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

    private val WORKOUT_COMPLETED = Script("Workout Complete", "Consistency is one of the strongest predictors of progress.", "Nice work today.",
        listOf("You logged a workout today"), listOf("Stay hydrated", "Consider a light cool-down walk"),
        "Workout complete. Consistency is one of the strongest predictors of progress. Nice work today.", InsightType.WORKOUT)

    private val WORKOUT_STREAK = Script("Workout Streak", "You've trained consistently this week.", "Building momentum is often more valuable than a perfect workout.",
        listOf("You've worked out at least 4 days this week"), listOf("Maintain your routine", "Consider a recovery day if needed"),
        "You've trained consistently this week. Building momentum is often more valuable than a perfect workout.", InsightType.WORKOUT)

    private val PERSONAL_BEST = Script("New Personal Best", "Progress is moving in the right direction.", "Keep building on this momentum.",
        listOf("Strength is improving across your lifts"), listOf("Celebrate the win", "Keep training consistently"),
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

    private val STREAK_100 = Script("One Hundred Days of Consistency", "Progress like this is built one day at a time.", "This is a significant achievement.",
        listOf("You've trained for 100 days"), listOf("Reflect on how far you've come", "Set new goals for the next phase"),
        "One hundred days of consistency. Progress like this is built one day at a time.", InsightType.STREAK_MILESTONE)

    private val EVENING_STRONG = Script("Strong Day", "You completed your key wellness goals.", "Well done.",
        listOf("You made meaningful progress today"), listOf("Prepare for tomorrow", "Get quality sleep"),
        "Today is nearly complete. You completed your key wellness goals. Well done.", InsightType.EVENING)

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
