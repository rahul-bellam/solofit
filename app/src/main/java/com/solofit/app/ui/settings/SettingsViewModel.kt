package com.solofit.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.domain.model.ThemeMode
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.sol.WellnessThresholds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val profile: UserProfileEntity? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    val profile = repository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeMode = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val animationsEnabled = repository.animationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val waterGoalMl = repository.waterGoalMl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WellnessThresholds.WATER_DEFAULT_GOAL_ML)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setAnimationsEnabled(enabled) }
    }

    fun setWaterGoalMl(ml: Int) {
        viewModelScope.launch { repository.setWaterGoalMl(ml) }
    }
}
