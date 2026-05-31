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

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
    profileRepository: ProfileRepository
) : ViewModel() {

    val animationsEnabled = profileRepository.animationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val today = DateUtils.today()

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
        viewModelScope.launch { repository.toggleGoal(goal) }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch { repository.deleteGoal(id) }
    }

    fun saveGratitude(text: String) {
        viewModelScope.launch { repository.saveGratitude(today, text) }
    }
}
