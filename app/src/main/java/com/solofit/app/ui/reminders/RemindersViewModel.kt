package com.solofit.app.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.domain.model.ReminderSettings
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    val settings = profileRepository.reminderSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReminderSettings())

    /** Persist new settings and (re)apply the WorkManager schedule. */
    private fun update(transform: (ReminderSettings) -> ReminderSettings) {
        viewModelScope.launch {
            val next = transform(settings.value)
            profileRepository.setReminderSettings(next)
            scheduler.apply(next)
        }
    }

    fun setHydrationEnabled(enabled: Boolean) = update { it.copy(hydrationEnabled = enabled) }
    fun setHydrationInterval(minutes: Int) = update { it.copy(hydrationIntervalMinutes = minutes) }
    fun setWorkoutEnabled(enabled: Boolean) = update { it.copy(workoutEnabled = enabled) }
    fun setWorkoutTime(minutes: Int) = update { it.copy(workoutTimeMinutes = minutes) }
    fun setMorningGoalsEnabled(enabled: Boolean) = update { it.copy(morningGoalsEnabled = enabled) }
    fun setMorningGoalsTime(minutes: Int) = update { it.copy(morningGoalsTimeMinutes = minutes) }
    fun setEveningGratitudeEnabled(enabled: Boolean) = update { it.copy(eveningGratitudeEnabled = enabled) }
    fun setEveningGratitudeTime(minutes: Int) = update { it.copy(eveningGratitudeTimeMinutes = minutes) }
    fun setQuietHours(startMin: Int, endMin: Int) =
        update { it.copy(quietStartMinutes = startMin, quietEndMinutes = endMin) }
}
