package com.solofit.app.ui.walking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.data.local.entity.DailyMetricEntity
import com.solofit.app.domain.repository.BodyRepository
import com.solofit.app.sol.HealthConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class WalkingUiState(
    val todaySteps: Int = 0,
    val stepGoal: Int = 8000,
    val weeklySteps: List<Int> = emptyList(),
    val weeklyLabels: List<String> = emptyList(),
    val healthConnectAvailable: Boolean = false
)

@HiltViewModel
class WalkingViewModel @Inject constructor(
    private val bodyRepository: BodyRepository,
    private val prefs: UserPreferences,
    private val healthConnectRepository: HealthConnectRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WalkingUiState())
    val state: StateFlow<WalkingUiState> = _state.asStateFlow()

    init {
        val today = LocalDate.now().toString()
        val startOfWeek = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toString()

        combine(
            bodyRepository.observeMetric(today),
            bodyRepository.observeMetricsSince(startOfWeek),
            prefs.stepGoal
        ) { metric, metrics, goal ->
            val steps = metric?.steps ?: 0
            val weeklySteps = metrics.map { it.steps ?: 0 }
            val labels = metrics.map { formatDayLabel(it.date) }

            _state.value = WalkingUiState(
                todaySteps = steps,
                stepGoal = goal,
                weeklySteps = weeklySteps,
                weeklyLabels = labels,
                healthConnectAvailable = healthConnectRepository.isAvailable()
            )
        }.launchIn(viewModelScope)

        syncFromHealthConnect()
    }

    fun updateSteps(steps: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existing = bodyRepository.getMetric(today)
            bodyRepository.saveMetric(
                (existing ?: DailyMetricEntity(date = today)).copy(steps = steps.coerceAtLeast(0))
            )
        }
    }

    fun setStepGoal(goal: Int) {
        viewModelScope.launch { prefs.setStepGoal(goal) }
    }

    fun syncFromHealthConnect() {
        viewModelScope.launch {
            if (!healthConnectRepository.isAvailable()) return@launch
            val hasPerms = healthConnectRepository.hasAllPermissions()
            if (!hasPerms) return@launch
            val data = healthConnectRepository.readTodayData()
            val hcSteps = data.steps ?: return@launch
            val today = LocalDate.now().toString()
            val existing = bodyRepository.getMetric(today)
            val current = existing?.steps ?: 0
            if (hcSteps > current) {
                bodyRepository.saveMetric(
                    (existing ?: DailyMetricEntity(date = today)).copy(steps = hcSteps)
                )
            }
        }
    }

    private fun formatDayLabel(dateStr: String): String {
        return runCatching {
            val day = LocalDate.parse(dateStr).dayOfWeek
            day.name.take(3)
        }.getOrElse { dateStr.takeLast(5) }
    }
}
