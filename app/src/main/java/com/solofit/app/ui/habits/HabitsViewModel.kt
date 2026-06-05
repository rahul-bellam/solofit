package com.solofit.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitItem(
    val id: String,
    val name: String,
    val completed: Boolean
)

data class HabitsUiState(
    val date: String = LocalDate.now().toString(),
    val habits: List<HabitItem> = emptyList(),
    val addDialogOpen: Boolean = false,
    val addInput: String = ""
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    companion object {
        val DEFAULT_HABITS = listOf(
            "drink_water" to "Drink 8 glasses of water",
            "exercise" to "Exercise",
            "meditate" to "Meditate",
            "read" to "Read",
            "walk" to "Walk 10,000 steps",
            "journal" to "Journal",
            "sleep" to "Sleep 8 hours",
            "protein" to "Hit protein goal"
        )
    }

    private val _state = MutableStateFlow(HabitsUiState())
    val state: StateFlow<HabitsUiState> = _state.asStateFlow()

    init {
        val today = LocalDate.now().toString()
        combine(
            prefs.customHabits,
            prefs.habitCompleted(today, "_dummy")
        ) { custom, _ -> custom }
            .map { custom ->
                val all = DEFAULT_HABITS + custom
                all.map { (id, name) ->
                    HabitItem(id, name, false)
                }
            }
            .launchIn(viewModelScope)

        refresh(today)
    }

    private fun refresh(date: String) {
        viewModelScope.launch {
            val custom = prefs.customHabits.first()
            val items = mutableListOf<HabitItem>()
            for ((id, name) in DEFAULT_HABITS + custom) {
                val done = prefs.habitCompleted(date, id).first()
                items.add(HabitItem(id, name, done))
            }
            _state.value = HabitsUiState(date = date, habits = items)
        }
    }

    fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            val current = _state.value
            val item = current.habits.find { it.id == habitId } ?: return@launch
            prefs.setHabitCompleted(current.date, habitId, !item.completed)
            refresh(current.date)
        }
    }

    fun setAddDialogOpen(open: Boolean) {
        _state.value = _state.value.copy(addDialogOpen = open, addInput = "")
    }

    fun setAddInput(input: String) {
        _state.value = _state.value.copy(addInput = input)
    }

    fun addCustomHabit() {
        viewModelScope.launch {
            val name = _state.value.addInput.trim()
            if (name.isEmpty()) return@launch
            val id = "custom_${System.currentTimeMillis()}"
            prefs.addCustomHabit(id, name)
            setAddDialogOpen(false)
            refresh(_state.value.date)
        }
    }

    fun removeHabit(habitId: String) {
        viewModelScope.launch {
            if (habitId.startsWith("custom_")) {
                prefs.removeCustomHabit(habitId)
                refresh(_state.value.date)
            }
        }
    }
}
