package com.solofit.app.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.entity.GoalItemEntity
import com.solofit.app.data.local.entity.GratitudeEntryEntity
import com.solofit.app.domain.repository.JournalRepository
import com.solofit.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalState(
    val goals: List<GoalItemEntity> = emptyList(),
    val gratitudeToday: GratitudeEntryEntity? = null,
    val recentGratitude: List<GratitudeEntryEntity> = emptyList()
) {
    val goalsDone: Int get() = goals.count { it.done }
    val goalsTotal: Int get() = goals.size
}

data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val onAction: () -> Unit = {}
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
    profileRepository: ProfileRepository
) : ViewModel() {

    val animationsEnabled = profileRepository.animationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val today: String get() = DateUtils.today()

    private val _snackbar = Channel<SnackbarEvent>(Channel.CONFLATED)
    val snackbarEvent = _snackbar.receiveAsFlow()

    val goals = repository.observeGoals(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gratitudeToday = repository.observeGratitude(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentGratitude = repository.observeRecentGratitude(14)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { repository.addGoal(today, text) }
    }

    fun toggleGoal(goal: GoalItemEntity) {
        viewModelScope.launch {
            repository.toggleGoal(goal)
            if (goal.done) {
                // was done, being unchecked — offer undo
                _snackbar.send(SnackbarEvent("Goal unchecked", "Undo") {
                    viewModelScope.launch { repository.toggleGoal(goal.copy(done = false)) }
                })
            }
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch { repository.deleteGoal(id) }
    }

    fun saveGratitude(text: String) {
        viewModelScope.launch { repository.saveGratitude(today, text) }
    }
}
