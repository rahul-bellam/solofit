package com.solofit.app.ui.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.core.FitnessMath
import com.solofit.app.data.local.entity.BodyMeasurementEntity
import com.solofit.app.data.local.entity.DailyMetricEntity
import com.solofit.app.domain.repository.BodyRepository
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BodyState(
    val measurements: List<BodyMeasurementEntity> = emptyList(),
    val latest: BodyMeasurementEntity? = null,
    val previous: BodyMeasurementEntity? = null,
    val todayMetric: DailyMetricEntity? = null
) {
    val vTaper: Double?
        get() = FitnessMath.vTaperRatio(latest?.shouldersCm, latest?.waistCm)
    val vTaperLabel: String get() = FitnessMath.vTaperLabel(vTaper)

    /** Waist change vs the previous logged measurement (cm). Negative = shrinking. */
    val waistDeltaCm: Double?
        get() {
            val now = latest?.waistCm ?: return null
            val prev = previous?.waistCm ?: return null
            return now - prev
        }
}

@HiltViewModel
class BodyViewModel @Inject constructor(
    private val bodyRepository: BodyRepository,
    private val profileRepository: ProfileRepository,
    private val weightRepository: WeightRepository
) : ViewModel() {

    private val today = DateUtils.today()

    val state = combine(
        bodyRepository.observeMeasurements(),
        bodyRepository.observeMetric(today)
    ) { measurements, metric ->
        val sorted = measurements.sortedBy { it.date }
        BodyState(
            measurements = sorted,
            latest = sorted.lastOrNull(),
            previous = if (sorted.size >= 2) sorted[sorted.size - 2] else null,
            todayMetric = metric
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BodyState())

    fun saveMeasurement(
        waist: Double?, chest: Double?, shoulders: Double?,
        arms: Double?, thighs: Double?, neck: Double?
    ) {
        viewModelScope.launch {
            val existing = bodyRepository.getMeasurementForDate(today)
            bodyRepository.saveMeasurement(
                BodyMeasurementEntity(
                    id = existing?.id ?: 0,
                    date = today,
                    waistCm = waist, chestCm = chest, shouldersCm = shoulders,
                    armsCm = arms, thighsCm = thighs, neckCm = neck
                )
            )
        }
    }

    fun saveMetric(sleep: Double?, steps: Int?, mood: Int?, energy: Int?) {
        viewModelScope.launch {
            val existing = bodyRepository.getMetric(today)
            bodyRepository.saveMetric(
                DailyMetricEntity(
                    id = existing?.id ?: 0,
                    date = today,
                    sleepHours = sleep, steps = steps,
                    moodScore = mood, energyScore = energy
                )
            )
        }
    }

    /** Build a series for the trend chart from a chosen measurement field. */
    fun series(selector: (BodyMeasurementEntity) -> Double?): List<Float> =
        state.value.measurements.mapNotNull { selector(it)?.toFloat() }
}
