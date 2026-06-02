package com.solofit.app.ui.workout.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.entity.PlannedExerciseEntity
import com.solofit.app.data.local.entity.WeeklyPlanEntity
import com.solofit.app.domain.repository.WeeklyPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutPlannerViewModel @Inject constructor(
    private val repository: WeeklyPlanRepository
) : ViewModel() {

    val allPlans = repository.observeAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _planExercises = MutableStateFlow<List<PlannedExerciseEntity>>(emptyList())
    val planExercises: StateFlow<List<PlannedExerciseEntity>> = _planExercises.asStateFlow()

    private val _selectedDay = MutableStateFlow<Int?>(null)
    val selectedDay: StateFlow<Int?> = _selectedDay.asStateFlow()

    private var currentPlan: WeeklyPlanEntity? = null

    fun selectDay(dayOfWeek: Int) {
        _selectedDay.value = dayOfWeek
        viewModelScope.launch {
            currentPlan = repository.getPlanForDay(dayOfWeek)
            refresh()
        }
    }

    fun savePlan(name: String) {
        val day = _selectedDay.value ?: return
        viewModelScope.launch {
            val existing = repository.getPlanForDay(day)
            val plan = if (existing != null) existing.copy(name = name) else WeeklyPlanEntity(dayOfWeek = day, name = name)
            val id = repository.savePlan(plan)
            currentPlan = plan.copy(id = if (existing != null) existing.id else id)
            refresh()
        }
    }

    fun addExercise(name: String, sets: Int, reps: Int, weight: Double, unit: String) {
        val plan = currentPlan ?: return
        viewModelScope.launch {
            repository.addExercise(
                PlannedExerciseEntity(
                    planId = plan.id,
                    exerciseName = name,
                    sets = sets,
                    reps = reps,
                    weight = weight,
                    weightUnit = unit,
                    sortOrder = _planExercises.value.size
                )
            )
            refresh()
        }
    }

    fun deleteExercise(exercise: PlannedExerciseEntity) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
            refresh()
        }
    }

    private suspend fun refresh() {
        currentPlan?.let { plan ->
            repository.observeExercisesForPlan(plan.id).collect { list ->
                _planExercises.value = list
                return@collect
            }
        }
    }
}
