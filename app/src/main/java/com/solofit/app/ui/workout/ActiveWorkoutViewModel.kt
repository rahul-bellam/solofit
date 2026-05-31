package com.solofit.app.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.entity.ExerciseSetEntity
import com.solofit.app.data.local.relation.SessionWithSets
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exercises grouped from the flat set list, preserving order. */
data class ExerciseGroup(
    val exerciseName: String,
    val muscleGroup: String,
    val orderIndex: Int,
    val sets: List<ExerciseSetEntity>
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: -1L

    val session = repository.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val animationsEnabled = profileRepository.animationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun groupedExercises(s: SessionWithSets?): List<ExerciseGroup> {
        if (s == null) return emptyList()
        return s.sets
            .groupBy { it.exerciseName }
            .map { (name, sets) ->
                val first = sets.first()
                ExerciseGroup(
                    exerciseName = name,
                    muscleGroup = first.muscleGroup,
                    orderIndex = first.orderIndex,
                    sets = sets.sortedBy { it.setNumber }
                )
            }
            .sortedBy { it.orderIndex }
    }

    fun updateSet(set: ExerciseSetEntity, weight: Double?, reps: Int?, completed: Boolean?) {
        viewModelScope.launch {
            repository.updateSet(
                set.copy(
                    weightKg = weight ?: set.weightKg,
                    reps = reps ?: set.reps,
                    isCompleted = completed ?: set.isCompleted
                )
            )
        }
    }

    fun updateRir(set: ExerciseSetEntity, rir: Int?) {
        viewModelScope.launch { repository.updateSet(set.copy(rir = rir)) }
    }

    fun addSet(group: ExerciseGroup) {
        viewModelScope.launch {
            repository.addSet(sessionId, group.exerciseName, group.muscleGroup, group.orderIndex)
        }
    }

    fun deleteSet(set: ExerciseSetEntity) {
        viewModelScope.launch { repository.deleteSet(set) }
    }

    fun finish(onFinished: () -> Unit) {
        val current = session.value?.session ?: return
        viewModelScope.launch {
            repository.completeSession(current)
            onFinished()
        }
    }
}
