package com.solofit.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.ReduceMotionPolicy
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StartState {
    data object Loading : StartState
    data object Onboarding : StartState
    data object Ready : StartState
}

@HiltViewModel
class RootViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    val startState = prefs.onboardingComplete
        .map { done -> if (done) StartState.Ready else StartState.Onboarding }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StartState.Loading)

    val themeMode: StateFlow<ThemeMode> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.LIGHT)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun applyReducedMotionOnce(osAnimatorScale: Float) {
        viewModelScope.launch {
            val alreadyApplied = prefs.reducedMotionApplied.first()
            if (alreadyApplied) return@launch
            if (ReduceMotionPolicy.shouldDisableAnimations(osAnimatorScale, alreadyApplied = false)) {
                prefs.setAnimationsEnabled(false)
            }
            prefs.setReducedMotionApplied(true)
        }
    }
}
