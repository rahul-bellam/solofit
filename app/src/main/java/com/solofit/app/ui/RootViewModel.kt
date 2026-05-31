package com.solofit.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.ReduceMotionPolicy
import com.solofit.app.domain.model.ThemeMode
import com.solofit.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
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
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val startState = profileRepository.onboardingComplete
        .map { complete -> if (complete) StartState.Ready else StartState.Onboarding }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StartState.Loading)

    val themeMode = profileRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    /**
     * One-time: if the OS has reduce-motion enabled (animator scale == 0) and we
     * haven't already auto-applied it, turn the playful animations off by default.
     * The user can still re-enable them in Settings; we never override them again.
     */
    fun applyReducedMotionOnce(osAnimatorScale: Float) {
        viewModelScope.launch {
            val alreadyApplied = profileRepository.reducedMotionApplied.first()
            if (alreadyApplied) return@launch
            if (ReduceMotionPolicy.shouldDisableAnimations(osAnimatorScale, alreadyApplied = false)) {
                profileRepository.setAnimationsEnabled(false)
            }
            profileRepository.setReducedMotionApplied(true)
        }
    }
}
