package com.solofit.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.entity.RoutineEntity
import com.solofit.app.data.local.relation.RoutineWithExercises
import com.solofit.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    val routines = repository.observeRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routinesLoaded = routines
        .drop(1)
        .map { true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch { repository.deleteRoutine(routine) }
    }

    /** Starts a session for the routine and returns the new session id via callback. */
    fun startSession(routine: RoutineWithExercises, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = repository.startSession(routine.routine.id)
            onStarted(sessionId)
        }
    }
}
