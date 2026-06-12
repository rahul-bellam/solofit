package com.solofit.app.sol

data class UserBaseline(
    val avgSleep7d: Double? = null,
    val avgSleep30d: Double? = null,
    val avgSleep90d: Double? = null,
    val avgSteps7d: Int? = null,
    val avgSteps30d: Int? = null,
    val avgSteps90d: Int? = null,
    val avgProteinAdherence7d: Double? = null,
    val avgProteinAdherence30d: Double? = null,
    val avgProteinAdherence90d: Double? = null,
    val avgWorkoutsPerWeek7d: Double? = null,
    val avgWorkoutsPerWeek30d: Double? = null,
    val avgWorkoutsPerWeek90d: Double? = null,
    val avgRecovery7d: Int? = null,
    val avgRecovery30d: Int? = null,
    val avgRecovery90d: Int? = null,
    val avgMeditationMinutesPerDay7d: Double? = null,
    val avgMeditationMinutesPerDay30d: Double? = null,
    val avgMeditationMinutesPerDay90d: Double? = null,
    val avgBodyWeight7d: Double? = null,
    val avgBodyWeight30d: Double? = null,
    val avgBodyWeight90d: Double? = null
)
