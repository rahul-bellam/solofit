package com.solofit.app.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.seed.ExerciseSeedData
import com.solofit.app.data.local.seed.ExerciseTemplate
import com.solofit.app.data.local.seed.WorkoutTemplate
import com.solofit.app.data.local.seed.WorkoutTemplates
import com.solofit.app.domain.model.ExercisePlan
import com.solofit.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineBuilderState(
    val routineId: Long? = null,
    val name: String = "",
    val notes: String = "",
    val selected: List<ExercisePlan> = emptyList(),
    val catalog: List<ExerciseTemplate> = ExerciseSeedData.exercises,
    val muscleGroups: List<String> = ExerciseSeedData.muscleGroups,
    val activeGroup: String? = null,
    val selectedEquipment: String? = null,
    val selectedDifficulty: String? = null,
    val showTemplates: Boolean = false,
    val templates: List<WorkoutTemplate> = WorkoutTemplates.templates
) {
    val canSave: Boolean get() = name.isNotBlank() && selected.isNotEmpty()

    val filteredCatalog: List<ExerciseTemplate>
        get() = catalog.filter { template ->
            (activeGroup == null || template.muscleGroup == activeGroup) &&
                (selectedEquipment == null || template.equipment == selectedEquipment) &&
                (selectedDifficulty == null || template.difficulty == selectedDifficulty)
        }
}

@HiltViewModel
class RoutineBuilderViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RoutineBuilderState())
    val state = _state.asStateFlow()

    init {
        val id = savedStateHandle.get<Long>("routineId") ?: -1L
        if (id > 0) loadExisting(id)
    }

    private fun loadExisting(id: Long) {
        viewModelScope.launch {
            repository.getRoutine(id)?.let { r ->
                _state.update {
                    it.copy(
                        routineId = r.routine.id,
                        name = r.routine.name,
                        notes = r.routine.notes,
                        selected = r.exercises.sortedBy { e -> e.orderIndex }
                            .map { e -> ExercisePlan(e.name, e.muscleGroup) }
                    )
                }
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun onNotes(v: String) = _state.update { it.copy(notes = v) }
    fun onGroupFilter(group: String?) = _state.update { it.copy(activeGroup = group) }
    fun onEquipmentFilter(equipment: String?) = _state.update { it.copy(selectedEquipment = equipment) }
    fun onDifficultyFilter(difficulty: String?) = _state.update { it.copy(selectedDifficulty = difficulty) }
    fun onToggleTemplates() = _state.update { it.copy(showTemplates = !it.showTemplates) }
    fun onDismissTemplates() = _state.update { it.copy(showTemplates = false) }

    fun loadTemplate(template: WorkoutTemplate) {
        val plans = template.exercises.map { te ->
            ExercisePlan(
                name = te.name,
                muscleGroup = te.muscleGroup,
                targetSets = te.sets
            )
        }
        _state.update {
            it.copy(
                name = template.name,
                selected = plans,
                showTemplates = false
            )
        }
    }

    fun toggleExercise(template: ExerciseTemplate) = _state.update { s ->
        val exists = s.selected.any { it.name == template.name }
        val updated = if (exists) s.selected.filterNot { it.name == template.name }
        else s.selected + ExercisePlan(
            name = template.name,
            muscleGroup = template.muscleGroup,
            equipment = template.equipment,
            difficulty = template.difficulty
        )
        s.copy(selected = updated)
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            repository.saveRoutine(s.name.trim(), s.notes.trim(), s.selected, s.routineId)
            onDone()
        }
    }
}
