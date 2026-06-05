package com.solofit.app.ui.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class RecoveryUiState(
    val date: String = LocalDate.now().toString(),
    val sleepHours: Float = 0f,
    val stressLevel: Int = 3,
    val moodLevel: Int = 3,
    val energyLevel: Int = 3,
    val meditationMinutes: Int = 0,
    val readinessScore: Int = 0
)

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(RecoveryUiState())
    val state: StateFlow<RecoveryUiState> = _state.asStateFlow()

    val readinessScore: StateFlow<Int> = prefs.readinessScore(LocalDate.now().toString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            prefs.dailyWellness(today).collect { wellness ->
                _state.value = _state.value.copy(
                    date = today,
                    sleepHours = wellness.sleepHours,
                    stressLevel = wellness.stressLevel,
                    moodLevel = wellness.moodLevel,
                    energyLevel = wellness.energyLevel,
                    meditationMinutes = wellness.meditationMinutes
                )
            }
        }
    }

    fun setSleepHours(hours: Float) {
        viewModelScope.launch {
            prefs.setSleepHours(_state.value.date, hours)
        }
    }

    fun setStressLevel(level: Int) {
        viewModelScope.launch {
            prefs.setStressLevel(_state.value.date, level)
        }
    }

    fun setMoodLevel(level: Int) {
        viewModelScope.launch {
            prefs.setMoodLevel(_state.value.date, level)
        }
    }

    fun setEnergyLevel(level: Int) {
        viewModelScope.launch {
            prefs.setEnergyLevel(_state.value.date, level)
        }
    }

    fun setMeditationMinutes(minutes: Int) {
        viewModelScope.launch {
            prefs.setMeditationMinutes(_state.value.date, minutes)
        }
    }
}
