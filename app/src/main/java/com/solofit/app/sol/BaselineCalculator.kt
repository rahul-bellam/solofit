package com.solofit.app.sol

import com.solofit.app.data.local.entity.UserProfileEntity

object BaselineCalculator {

    fun compute(
        weeklySteps: List<Int>,
        weeklyRecovery: List<Int>,
        weeklyProteinPct: List<Double>,
        weeklySleep: List<Double>,
        historyCount: Int,
        daysTracked: Int,
        meditationMinutes: Int,
        profile: UserProfileEntity?,
        steps30d: List<Int> = emptyList(),
        steps90d: List<Int> = emptyList(),
        recovery30d: List<Int> = emptyList(),
        recovery90d: List<Int> = emptyList(),
        protein30d: List<Double> = emptyList(),
        protein90d: List<Double> = emptyList(),
        sleep30d: List<Double> = emptyList(),
        sleep90d: List<Double> = emptyList(),
        meditation30dTotal: Int = 0,
        meditation90dTotal: Int = 0,
        daysTracked30d: Int = 0,
        daysTracked90d: Int = 0,
        workouts30d: Int = 0,
        workouts90d: Int = 0,
        bodyWeight30d: List<Double> = emptyList(),
        bodyWeight90d: List<Double> = emptyList()
    ): UserBaseline {
        val avgSteps7d = if (weeklySteps.isNotEmpty()) weeklySteps.average().toInt() else null
        val avgRecovery7d = if (weeklyRecovery.isNotEmpty()) weeklyRecovery.average().toInt() else null
        val avgProtein7d = if (weeklyProteinPct.isNotEmpty()) weeklyProteinPct.average() else null
        val avgSleep7d = if (weeklySleep.isNotEmpty()) weeklySleep.average() else null

        val avgSteps30d = if (steps30d.isNotEmpty()) steps30d.average().toInt() else avgSteps7d
        val avgSteps90d = if (steps90d.isNotEmpty()) steps90d.average().toInt() else avgSteps30d
        val avgRecovery30d = if (recovery30d.isNotEmpty()) recovery30d.average().toInt() else avgRecovery7d
        val avgRecovery90d = if (recovery90d.isNotEmpty()) recovery90d.average().toInt() else avgRecovery30d
        val avgProtein30d = if (protein30d.isNotEmpty()) protein30d.average() else avgProtein7d
        val avgProtein90d = if (protein90d.isNotEmpty()) protein90d.average() else avgProtein30d
        val avgSleep30d = if (sleep30d.isNotEmpty()) sleep30d.average() else avgSleep7d
        val avgSleep90d = if (sleep90d.isNotEmpty()) sleep90d.average() else avgSleep30d
        val avgWeight30d = if (bodyWeight30d.isNotEmpty()) bodyWeight30d.average() else profile?.weightKg
        val avgWeight90d = if (bodyWeight90d.isNotEmpty()) bodyWeight90d.average() else avgWeight30d

        val avgWorkouts7d = if (daysTracked > 0) historyCount.toDouble() / (daysTracked / 7.0).coerceAtLeast(1.0) else null
        val avgWorkouts30d = if (daysTracked30d > 0) workouts30d.toDouble() / (daysTracked30d / 7.0).coerceAtLeast(1.0) else avgWorkouts7d
        val avgWorkouts90d = if (daysTracked90d > 0) workouts90d.toDouble() / (daysTracked90d / 7.0).coerceAtLeast(1.0) else avgWorkouts30d

        val avgMeditation7d = if (daysTracked > 0) meditationMinutes.toDouble() / daysTracked.coerceAtLeast(1) else null
        val avgMeditation30d = if (daysTracked30d > 0) meditation30dTotal.toDouble() / daysTracked30d.coerceAtLeast(1) else avgMeditation7d
        val avgMeditation90d = if (daysTracked90d > 0) meditation90dTotal.toDouble() / daysTracked90d.coerceAtLeast(1) else avgMeditation30d

        return UserBaseline(
            avgSleep7d = avgSleep7d,
            avgSleep30d = avgSleep30d,
            avgSleep90d = avgSleep90d,
            avgSteps7d = avgSteps7d,
            avgSteps30d = avgSteps30d,
            avgSteps90d = avgSteps90d,
            avgProteinAdherence7d = avgProtein7d,
            avgProteinAdherence30d = avgProtein30d,
            avgProteinAdherence90d = avgProtein90d,
            avgWorkoutsPerWeek7d = avgWorkouts7d,
            avgWorkoutsPerWeek30d = avgWorkouts30d,
            avgWorkoutsPerWeek90d = avgWorkouts90d,
            avgRecovery7d = avgRecovery7d,
            avgRecovery30d = avgRecovery30d,
            avgRecovery90d = avgRecovery90d,
            avgMeditationMinutesPerDay7d = avgMeditation7d,
            avgMeditationMinutesPerDay30d = avgMeditation30d,
            avgMeditationMinutesPerDay90d = avgMeditation90d,
            avgBodyWeight7d = profile?.weightKg,
            avgBodyWeight30d = avgWeight30d,
            avgBodyWeight90d = avgWeight90d
        )
    }
}
