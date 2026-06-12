package com.solofit.app.ui.bodyrecomp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.entity.BodyMeasurementEntity
import com.solofit.app.data.local.entity.WeightEntryEntity
import com.solofit.app.domain.repository.BodyRepository
import com.solofit.app.domain.repository.WeightRepository
import com.solofit.app.core.FitnessMath
import com.solofit.app.sol.WellnessThresholds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class BodyRecompState(
    val isLoading: Boolean = true,
    val weightEntries: List<WeightEntryEntity> = emptyList(),
    val measurements: List<BodyMeasurementEntity> = emptyList(),
    val latestWeight: Double? = null,
    val latestWaist: Double? = null,
    val weightTrend: TrendDirection = TrendDirection.STABLE,
    val waistTrend: TrendDirection = TrendDirection.STABLE,
    val weightChange4w: Double? = null,
    val waistChange4w: Double? = null,
    val recompStatus: RecompStatus = RecompStatus.INSUFFICIENT_DATA,
    val confidence: RecompConfidence = RecompConfidence.LOW,
    val feedbackMessage: String = "",
    val weightInput: String = "",
    val waistInput: String = ""
)

enum class TrendDirection { UP, DOWN, STABLE }
enum class RecompConfidence { LOW, MEDIUM, HIGH }
enum class RecompStatus {
    INSUFFICIENT_DATA,
    EXCELLENT,
    GOOD,
    WATCH_CALORIES,
    WATCH_RATE,
    MUSCLE_LOSS_RISK,
    WEIGHT_GAIN_RISK
}

@HiltViewModel
class BodyRecompViewModel @Inject constructor(
    private val bodyRepository: BodyRepository,
    private val weightRepository: WeightRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BodyRecompState())
    val state: StateFlow<BodyRecompState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                weightRepository.observeAll(),
                bodyRepository.observeMeasurements()
            ) { weights, measurements ->
                val sortedWeights = weights.sortedBy { it.date }
                val sortedMeasures = measurements.sortedBy { it.date }
                computeState(sortedWeights, sortedMeasures)
            }.collect { _state.value = it }
        }
    }

    private fun computeState(
        weights: List<WeightEntryEntity>,
        measurements: List<BodyMeasurementEntity>
    ): BodyRecompState {
        val latestWeight = weights.lastOrNull()
        val latestWaist = measurements.lastOrNull()?.waistCm

        val weightTrend = calcTrend(
            values = weights.map { it.weightKg },
            higherIsUp = true
        )
        val waistTrend = calcTrend(
            values = measurements.mapNotNull { it.waistCm },
            higherIsUp = false
        )

        val weightChange4w = changeOverWindow(weights.map { it.date to it.weightKg }, 28)
        val waistChange4w = changeOverWindow(measurements.mapNotNull { m ->
            m.waistCm?.let { m.date to it }
        }, 28)

        val status = classifyRecomp(weightChange4w, waistChange4w, weightTrend, waistTrend)
        val confidence = calcConfidence(weights, measurements)
        val feedback = feedbackFor(status, weightChange4w, waistChange4w)

        return BodyRecompState(
            isLoading = false,
            weightEntries = weights,
            measurements = measurements,
            latestWeight = latestWeight?.weightKg,
            latestWaist = latestWaist,
            weightTrend = weightTrend,
            waistTrend = waistTrend,
            weightChange4w = weightChange4w,
            waistChange4w = waistChange4w,
            recompStatus = status,
            confidence = confidence,
            feedbackMessage = feedback
        )
    }

    private fun calcConfidence(
        weights: List<WeightEntryEntity>,
        measurements: List<BodyMeasurementEntity>
    ): RecompConfidence {
        val weightCount28d = weights.count {
            LocalDate.parse(it.date).isAfter(LocalDate.now().minusDays(28))
        }
        val measureCount28d = measurements.count {
            LocalDate.parse(it.date).isAfter(LocalDate.now().minusDays(28))
        }
        val total = weightCount28d + measureCount28d
        return when {
            total >= 10 -> RecompConfidence.HIGH
            total >= 4 -> RecompConfidence.MEDIUM
            else -> RecompConfidence.LOW
        }
    }

    private fun calcTrend(values: List<Double>, higherIsUp: Boolean): TrendDirection {
        if (values.size < 3) return TrendDirection.STABLE
        val recent = values.takeLast(7)
        val half = recent.size / 2
        if (half < 1) return TrendDirection.STABLE
        val firstHalf = recent.take(half).average()
        val secondHalf = recent.drop(half).average()
        val diff = secondHalf - firstHalf
        return when {
            diff > WellnessThresholds.RECOMP_TREND_DIRECTION_DIFF -> if (higherIsUp) TrendDirection.UP else TrendDirection.DOWN
            diff < -WellnessThresholds.RECOMP_TREND_DIRECTION_DIFF -> if (higherIsUp) TrendDirection.DOWN else TrendDirection.UP
            else -> TrendDirection.STABLE
        }
    }

    private fun changeOverWindow(pairs: List<Pair<String, Double>>, days: Int): Double? {
        if (pairs.size < 2) return null
        val cutoff = LocalDate.now().minusDays(days.toLong())
        val inWindow = pairs.filter { LocalDate.parse(it.first).isAfter(cutoff) }
        if (inWindow.size < 2) return null
        return inWindow.last().second - inWindow.first().second
    }

    private fun classifyRecomp(
        weightChange: Double?,
        waistChange: Double?,
        weightTrend: TrendDirection,
        waistTrend: TrendDirection
    ): RecompStatus {
        if (weightChange == null && waistChange == null) return RecompStatus.INSUFFICIENT_DATA
        if (waistChange != null && waistChange < -WellnessThresholds.RECOMP_WAIST_DECREASE_CM) {
            return if (weightChange != null && kotlin.math.abs(weightChange) < WellnessThresholds.RECOMP_WEIGHT_STABLE_KG) RecompStatus.EXCELLENT
            else if (weightChange != null && weightChange < -WellnessThresholds.RECOMP_MUSCLE_LOSS_RATE_KG) RecompStatus.MUSCLE_LOSS_RISK
            else RecompStatus.GOOD
        }
        if (weightChange != null && weightChange > WellnessThresholds.RECOMP_WEIGHT_GAIN_RATE_KG) return RecompStatus.WEIGHT_GAIN_RISK
        if (weightChange != null && weightChange < -WellnessThresholds.RECOMP_MUSCLE_LOSS_RATE_KG) return RecompStatus.MUSCLE_LOSS_RISK
        if (weightChange != null && kotlin.math.abs(weightChange) < WellnessThresholds.RECOMP_GOOD_TREND_STABLE_KG && waistTrend == TrendDirection.STABLE) return RecompStatus.GOOD
        return RecompStatus.INSUFFICIENT_DATA
    }

    private fun feedbackFor(status: RecompStatus, weightChange: Double?, waistChange: Double?): String {
        val w = if (weightChange != null) "%.1f kg".format(weightChange) else "?"
        val h = if (waistChange != null) "%.1f cm".format(waistChange) else "?"
        return when (status) {
            RecompStatus.EXCELLENT -> "Weight is stable while waist is decreasing. This indicates effective recomposition."
            RecompStatus.GOOD -> "Progress is moving in the right direction. Stay consistent with training and nutrition."
            RecompStatus.WATCH_CALORIES -> "Weight is stable. Focus on protein adherence and training consistency."
            RecompStatus.WATCH_RATE -> "Weight is decreasing faster than expected. Monitor closely to ensure sustainable progress."
            RecompStatus.MUSCLE_LOSS_RISK -> "Weight is decreasing more than expected. Consider increasing calories slightly to preserve muscle."
            RecompStatus.WEIGHT_GAIN_RISK -> "Weight is increasing more than expected. Monitor measurements closely and adjust if needed."
            RecompStatus.INSUFFICIENT_DATA -> "Log weight and waist measurements regularly to track body recomposition trends."
        }
    }

    fun updateWeightInput(value: String) { _state.value = _state.value.copy(weightInput = value) }
    fun updateWaistInput(value: String) { _state.value = _state.value.copy(waistInput = value) }

    fun saveWeight() {
        val w = _state.value.weightInput.toDoubleOrNull() ?: return
        viewModelScope.launch {
            weightRepository.logWeight(LocalDate.now().toString(), w)
            _state.value = _state.value.copy(weightInput = "")
        }
    }

    fun saveWaist() {
        val h = _state.value.waistInput.toDoubleOrNull() ?: return
        viewModelScope.launch {
            bodyRepository.saveMeasurement(
                BodyMeasurementEntity(date = LocalDate.now().toString(), waistCm = h)
            )
            _state.value = _state.value.copy(waistInput = "")
        }
    }
}
