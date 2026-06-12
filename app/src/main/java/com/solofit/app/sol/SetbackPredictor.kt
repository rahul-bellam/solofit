package com.solofit.app.sol

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

data class SetbackPrediction(
    val probability: Double,
    val riskLevel: String,
    val topDriver: String
)

data class TrainingExample(
    val features: DoubleArray,
    val label: Int
)

object SetbackPredictor {

    private const val LR = 0.01
    private const val EPOCHS = 4
    private const val L2 = 0.001
    private const val FEATURE_COUNT = 9

    fun defaultWeights(): DoubleArray = doubleArrayOf(
        0.35,   // 0: days_since_last_workout (normalized 0‑1)
        0.25,   // 1: avg_recovery_7d (inverted: 1 – recovery/100)
        0.30,   // 2: sleep_trend_3d (inverted: 1 – avg/8)
        0.20,   // 3: protein_adherence_7d (inverted: 1 – ratio capped)
        0.15,   // 4: step_trend_3d (inverted: 1 – avg/8000)
        -0.25,  // 5: meditation_any_7d (protective → negative)
        -0.20,  // 6: journal_any_7d (protective → negative)
        0.08,   // 7: is_weekend
        -0.30   // 8: momentum_direction (0=declining, 1=stable, 2=improving)
    )

    fun predict(weights: DoubleArray, bias: Double, features: DoubleArray): Double {
        val z = features.indices.sumOf { i -> weights[i] * features[i] } + bias
        return sigmoid(z)
    }

    fun predict(weights: DoubleArray, bias: Double, example: TrainingExample): Double =
        predict(weights, bias, example.features)

    fun train(weights: DoubleArray, bias: Double, examples: List<TrainingExample>): Pair<DoubleArray, Double> {
        var w = weights.copyOf()
        var b = bias
        for (epoch in 0 until EPOCHS) {
            var totalLoss = 0.0
            for (ex in examples.shuffled()) {
                val pred = predict(w, b, ex)
                val error = pred - ex.label
                totalLoss += error * error
                for (i in w.indices) {
                    val grad = error * ex.features[i] + L2 * w[i]
                    w[i] -= LR * grad
                }
                b -= LR * error
            }
        }
        return w to b
    }

    fun extractFeatures(
        daysSinceLastWorkout: Int,
        avgRecovery7d: Int?,
        avgSleep3d: Double?,
        avgProteinAdherence7d: Double?,
        avgSteps3d: Int?,
        meditatedAny7d: Boolean,
        journaledAny7d: Boolean,
        isWeekend: Boolean,
        momentumDirection: Int
    ): DoubleArray = doubleArrayOf(
        min(max(daysSinceLastWorkout, 0).toDouble() / 14.0, 1.0),
        if (avgRecovery7d != null) 1.0 - avgRecovery7d.toDouble() / 100.0 else 0.5,
        if (avgSleep3d != null) 1.0 - min(avgSleep3d / 8.0, 1.0) else 0.5,
        if (avgProteinAdherence7d != null) 1.0 - min(avgProteinAdherence7d, 1.0) else 0.5,
        if (avgSteps3d != null) 1.0 - min(avgSteps3d.toDouble() / 8000.0, 1.0) else 0.5,
        if (meditatedAny7d) 1.0 else 0.0,
        if (journaledAny7d) 1.0 else 0.0,
        if (isWeekend) 1.0 else 0.0,
        momentumDirection.toDouble()
    )

    private fun sigmoid(z: Double): Double = 1.0 / (1.0 + exp(-z.coerceIn(-15.0, 15.0)))

    fun serializeWeights(weights: DoubleArray): String =
        weights.joinToString(",") { "%.10f".format(it) }

    fun deserializeWeights(raw: String): DoubleArray? =
        runCatching { raw.split(",").map { it.toDouble() }.toDoubleArray() }.getOrNull()

    fun probabilityToRisk(prob: Double): String = when {
        prob >= 0.55 -> "Elevated"
        prob >= 0.35 -> "Moderate"
        else -> "Low"
    }

    fun riskColor(risk: String): String = when (risk) {
        "Elevated" -> "#9E4733"
        "Moderate" -> "#E09F3E"
        else -> "#556B2F"
    }

    fun topDriver(weights: DoubleArray, features: DoubleArray): String {
        val featureNames = listOf(
            "days since last workout",
            "recovery trend",
            "sleep trend",
            "nutrition adherence",
            "movement trend",
            "meditation consistency",
            "journal consistency",
            "weekend pattern",
            "momentum shift"
        )
        val driverIdx = features.indices
            .filter { kotlin.math.abs(features[it]) > 0.1 }
            .maxByOrNull { kotlin.math.abs(weights[it]) * features[it] }
            ?: return "baseline trend"
        val isProtective = weights[driverIdx] < 0 && features[driverIdx] > 0.1
        val name = featureNames[driverIdx]
        return if (isProtective) "$name (protective)" else name
    }
}
