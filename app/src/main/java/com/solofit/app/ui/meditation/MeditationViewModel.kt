package com.solofit.app.ui.meditation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MeditationUiState(
    val isRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val targetMinutes: Int = 5,
    val phase: BreathingPhase = BreathingPhase.IDLE
)

enum class BreathingPhase {
    IDLE, INHALE, HOLD, EXHALE
}

@HiltViewModel
class MeditationViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(MeditationUiState())
    val state: StateFlow<MeditationUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun setTargetMinutes(minutes: Int) {
        if (!_state.value.isRunning) {
            _state.value = _state.value.copy(targetMinutes = minutes.coerceIn(1, 120))
        }
    }

    fun toggleTimer() {
        if (_state.value.isRunning) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _state.value = _state.value.copy(isRunning = true, elapsedSeconds = 0)
        val targetSeconds = _state.value.targetMinutes * 60

        timerJob = viewModelScope.launch {
            while (_state.value.elapsedSeconds < targetSeconds) {
                delay(1000L)
                _state.value = _state.value.copy(
                    elapsedSeconds = _state.value.elapsedSeconds + 1
                )
                val cycle = _state.value.elapsedSeconds % 8
                _state.value = _state.value.copy(
                    phase = when {
                        cycle < 4 -> BreathingPhase.INHALE
                        cycle < 5 -> BreathingPhase.HOLD
                        else -> BreathingPhase.EXHALE
                    }
                )
            }
            stopTimer()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        if (_state.value.elapsedSeconds > 0) {
            saveMeditation(_state.value.elapsedSeconds / 60)
        }
        _state.value = _state.value.copy(
            isRunning = false,
            phase = BreathingPhase.IDLE
        )
    }

    private fun saveMeditation(minutes: Int) {
        if (minutes <= 0) return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existing = prefs.meditationMinutes(today).first()
            prefs.setMeditationMinutes(today, existing + minutes)
        }
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        _state.value = MeditationUiState(targetMinutes = _state.value.targetMinutes)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
