package com.solofit.app.ui.phase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.domain.model.TrainingGoal
import com.solofit.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditPhaseState(
    val loaded: Boolean = false,
    val name: String = "Foundation Recomp",
    val targetDays: String = "365",
    val startDate: String = DateUtils.today(),
    val goal: TrainingGoal = TrainingGoal.BODYBUILDING
) {
    val isValid: Boolean
        get() = name.isNotBlank() && (targetDays.toIntOrNull()?.let { it in 1..3650 } == true)
}

@HiltViewModel
class EditPhaseViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditPhaseState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = repository.phaseName.first()
            val target = repository.phaseTargetDays.first()
            val start = repository.phaseStartDate.first() ?: DateUtils.today()
            val goal = repository.trainingGoal.first()
            _state.update {
                it.copy(loaded = true, name = name, targetDays = target.toString(), startDate = start, goal = goal)
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun onTargetDays(v: String) = _state.update { it.copy(targetDays = v.filter { c -> c.isDigit() }) }
    fun onStartDate(iso: String) = _state.update { it.copy(startDate = iso) }
    fun onGoal(g: TrainingGoal) = _state.update { it.copy(goal = g) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (!s.isValid) return
        viewModelScope.launch {
            repository.setPhase(s.name.trim(), s.startDate, s.targetDays.toInt())
            repository.setTrainingGoal(s.goal)
            onDone()
        }
    }
}
