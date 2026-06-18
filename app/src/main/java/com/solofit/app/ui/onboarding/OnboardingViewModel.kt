package com.solofit.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.domain.model.ActivityLevel
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender
import com.solofit.app.domain.model.NutritionTargets
import com.solofit.app.domain.model.OnboardingFocus
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.usecase.CalculateNutritionTargetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val step: Int = 0,
    val name: String = "",
    val age: String = "",
    val gender: Gender = Gender.MALE,
    val weight: String = "",
    val height: String = "",
    val goal: FitnessGoal? = null,
    val focus: OnboardingFocus? = null,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val selectedModules: Set<SoloFitModule> = SoloFitModule.DEFAULT_ENABLED.toSet(),
    val preview: NutritionTargets? = null
) {
    val isPersonalInfoValid: Boolean
        get() = name.isNotBlank() &&
            age.toIntOrNull()?.let { it in 1..120 } == true &&
            weight.toDoubleOrNull()?.let { it in 20.0..400.0 } == true &&
            height.toDoubleOrNull()?.let { it in 80.0..260.0 } == true
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val calculate: CalculateNutritionTargetsUseCase,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    fun nextStep() = _state.update { it.copy(step = (it.step + 1).coerceAtMost(9)) }
    fun previousStep() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }

    fun onActivityLevel(v: ActivityLevel) {
        _state.update { it.copy(activityLevel = v) }
    }

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun onAge(v: String) = _state.update { it.copy(age = v.filter { c -> c.isDigit() }) }
    fun onGender(v: Gender) = _state.update { it.copy(gender = v) }
    fun onWeight(v: String) = _state.update { it.copy(weight = v.filterDecimal()) }
    fun onHeight(v: String) = _state.update { it.copy(height = v.filterDecimal()) }

    fun onGoal(v: FitnessGoal) {
        _state.update {
            it.copy(goal = v).recompute()
        }
    }

    fun onFocus(v: OnboardingFocus) {
        _state.update { it.copy(focus = v) }
    }

    fun toggleModule(module: SoloFitModule) {
        _state.update {
            it.copy(
                selectedModules = if (module in it.selectedModules)
                    it.selectedModules - module
                else
                    it.selectedModules + module
            )
        }
    }

    private fun String.filterDecimal(): String {
        val filtered = filterIndexed { i, c -> c.isDigit() || (c == '.' && !substring(0, i).contains('.')) }
        return filtered
    }

    private fun OnboardingState.recompute(): OnboardingState {
        val g = goal ?: return this
        val a = age.toIntOrNull()
        val w = weight.toDoubleOrNull()
        val h = height.toDoubleOrNull()
        if (a == null || w == null || h == null) return copy(preview = null)
        return try {
            copy(
                preview = calculate(
                    CalculateNutritionTargetsUseCase.Params(
                        age = a, gender = gender, weightKg = w, heightCm = h,
                        activityLevel = activityLevel, goal = g
                    )
                )
            )
        } catch (e: IllegalArgumentException) {
            copy(preview = null)
        }
    }

    fun finish(onDone: () -> Unit) {
        val s = _state.value
        val targets = s.preview ?: computeFallbackTargets(s)
        val g = s.goal ?: FitnessGoal.STAY_HEALTHY
        viewModelScope.launch {
            repository.saveProfile(
                UserProfileEntity(
                    name = s.name.trim(),
                    age = s.age.toIntOrNull() ?: 30,
                    gender = s.gender,
                    weightKg = s.weight.toDoubleOrNull() ?: 70.0,
                    heightCm = s.height.toDoubleOrNull() ?: 170.0,
                    activityLevel = s.activityLevel,
                    goal = g,
                    targetCalories = targets.targetCalories,
                    targetProtein = targets.targetProteinG,
                    targetCarbs = targets.targetCarbsG,
                    targetFats = targets.targetFatsG,
                    bmr = targets.bmr,
                    tdee = targets.tdee
                )
            )
            repository.setOnboardingComplete(true)
            prefs.setEnabledModules(SoloFitModule.DEFAULT_ENABLED)
            prefs.setModuleOrder(SoloFitModule.DEFAULT_ENABLED)
            prefs.setModuleSelectionComplete(true)
            prefs.setOnboardingFocus(s.focus)
            onDone()
        }
    }

    private fun computeFallbackTargets(s: OnboardingState): NutritionTargets {
        val w = s.weight.toDoubleOrNull() ?: 70.0
        val h = s.height.toDoubleOrNull() ?: 170.0
        val a = s.age.toIntOrNull() ?: 30
        return try {
            calculate(CalculateNutritionTargetsUseCase.Params(a, s.gender, w, h, s.activityLevel, s.goal ?: FitnessGoal.STAY_HEALTHY))
        } catch (_: IllegalArgumentException) {
            NutritionTargets(2000, 150, 250, 65, 1700, 2000)
        }
    }
}
