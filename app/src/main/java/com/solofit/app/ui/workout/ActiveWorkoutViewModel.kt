package com.solofit.app.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.core.FitnessMath
import com.solofit.app.core.StreakCalculator
import com.solofit.app.data.local.entity.ExerciseSetEntity
import com.solofit.app.data.local.entity.PersonalRecordEntity
import com.solofit.app.data.local.relation.SessionWithSets
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import javax.inject.Inject

/** Exercises grouped from the flat set list, preserving order. */
data class ExerciseGroup(
    val exerciseName: String,
    val muscleGroup: String,
    val orderIndex: Int,
    val sets: List<ExerciseSetEntity>
)

data class ActiveWorkoutUiState(
    val restTimerRunning: Boolean = false,
    val restSecondsRemaining: Int = 0,
    val restDuration: Int = 90,
    val isPaused: Boolean = false,
    val prCelebrationMessage: String? = null
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

    val streak = repository.observeHistory()
        .map { history ->
            val dates = history.map { it.session.date }
            StreakCalculator.currentStreak(dates, java.time.LocalDate.now())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private var timerJob: Job? = null

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState = _uiState.asStateFlow()

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
            val newWeight = weight ?: set.weightKg
            val newReps = reps ?: set.reps
            val newCompleted = completed ?: set.isCompleted
            repository.updateSet(
                set.copy(
                    weightKg = newWeight,
                    reps = newReps,
                    isCompleted = newCompleted
                )
            )
            if (newCompleted && !set.isWarmUp && newWeight > 0 && newReps > 0) {
                checkAndSavePr(set.exerciseName, newWeight, newReps)
                startRestTimer()
            }
        }
    }

    fun updateRir(set: ExerciseSetEntity, rir: Int?) {
        viewModelScope.launch { repository.updateSet(set.copy(rir = rir)) }
    }

    fun updateNotes(set: ExerciseSetEntity, notes: String) {
        viewModelScope.launch { repository.updateSet(set.copy(notes = notes)) }
    }

    fun updateWarmUp(set: ExerciseSetEntity, isWarmUp: Boolean) {
        viewModelScope.launch { repository.updateSet(set.copy(isWarmUp = isWarmUp)) }
    }

    fun addSet(group: ExerciseGroup) {
        viewModelScope.launch {
            repository.addSet(sessionId, group.exerciseName, group.muscleGroup, group.orderIndex)
        }
    }

    fun deleteSet(set: ExerciseSetEntity) {
        viewModelScope.launch { repository.deleteSet(set) }
    }

    fun setRestDuration(seconds: Int) {
        _uiState.update { it.copy(restDuration = seconds) }
    }

    fun togglePause() {
        val wasPaused = _uiState.value.isPaused
        _uiState.update { it.copy(isPaused = !wasPaused) }
        if (_uiState.value.restTimerRunning) {
            if (!wasPaused) {
                timerJob?.cancel()
            } else {
                resumeRestTimer()
            }
        }
    }

    private fun resumeRestTimer() {
        timerJob?.cancel()
        val remaining = _uiState.value.restSecondsRemaining
        timerJob = viewModelScope.launch {
            for (i in remaining - 1 downTo 0) {
                if (_uiState.value.isPaused) break
                kotlinx.coroutines.delay(1000L)
                _uiState.update { it.copy(restSecondsRemaining = i) }
            }
            if (!_uiState.value.isPaused) {
                _uiState.update { it.copy(restTimerRunning = false) }
            }
        }
    }

    private fun startRestTimer() {
        timerJob?.cancel()
        val duration = _uiState.value.restDuration
        _uiState.update { it.copy(restTimerRunning = true, restSecondsRemaining = duration) }
        if (_uiState.value.isPaused) return
        timerJob = viewModelScope.launch {
            for (i in duration - 1 downTo 0) {
                kotlinx.coroutines.delay(1000L)
                _uiState.update { it.copy(restSecondsRemaining = i) }
            }
            _uiState.update { it.copy(restTimerRunning = false) }
        }
    }

    fun dismissRestTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(restTimerRunning = false, restSecondsRemaining = 0) }
    }

    fun dismissPrCelebration() {
        _uiState.update { it.copy(prCelebrationMessage = null) }
    }

    private suspend fun checkAndSavePr(exerciseName: String, weight: Double, reps: Int) {
        val estimated1RM = FitnessMath.epley1RM(weight, reps)
        val existing = repository.getPersonalRecord(exerciseName)
        if (existing == null || estimated1RM > existing.estimated1RM) {
            val pr = PersonalRecordEntity(
                exerciseName = exerciseName,
                bestWeightKg = weight,
                bestReps = reps,
                estimated1RM = estimated1RM,
                date = DateUtils.today(),
                sessionId = sessionId
            )
            repository.savePersonalRecord(pr)
            _uiState.update {
                it.copy(prCelebrationMessage = "New PR! $exerciseName: ${estimated1RM.roundToInt()} kg 1RM")
            }
        }
    }

    fun finish(onFinished: () -> Unit) {
        val current = session.value?.session ?: return
        viewModelScope.launch {
            repository.completeSession(current)
            onFinished()
        }
    }
}
