package com.solofit.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.entity.RoutineEntity
import com.solofit.app.data.local.relation.RoutineWithExercises
import com.solofit.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
<<<<<<< HEAD
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
=======
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
>>>>>>> a45e67a (Workout system enhancements, UI fixes, and settings cleanup)
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

<<<<<<< HEAD
    private val _routines = MutableStateFlow<List<RoutineWithExercises>>(emptyList())
    val routines: StateFlow<List<RoutineWithExercises>> = _routines.asStateFlow()

    private val _routinesLoaded = MutableStateFlow(false)
    val routinesLoaded: StateFlow<Boolean> = _routinesLoaded.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeRoutines().collect { list ->
                _routines.value = list
=======
    private val _routinesLoaded = MutableStateFlow(false)
    val routinesLoaded = _routinesLoaded.asStateFlow()

    val routines = repository.observeRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.observeRoutines().collect {
>>>>>>> a45e67a (Workout system enhancements, UI fixes, and settings cleanup)
                _routinesLoaded.value = true
            }
        }
    }

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
