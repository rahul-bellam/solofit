package com.solofit.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.domain.model.ActivityLevel
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender
import com.solofit.app.domain.model.NutritionTargets
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

    fun nextStep() = _state.update { it.copy(step = (it.step + 1).coerceAtMost(4)) }
    fun previousStep() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }

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
                        activityLevel = ActivityLevel.MODERATE, goal = g
                    )
                )
            )
        } catch (e: IllegalArgumentException) {
            copy(preview = null)
        }
    }

    fun finish(onDone: () -> Unit) {
        val s = _state.value
        val targets = s.preview ?: return
        val g = s.goal ?: return
        viewModelScope.launch {
            repository.saveProfile(
                UserProfileEntity(
                    name = s.name.trim(),
                    age = s.age.toInt(),
                    gender = s.gender,
                    weightKg = s.weight.toDouble(),
                    heightCm = s.height.toDouble(),
                    activityLevel = ActivityLevel.MODERATE,
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
            val modules = s.selectedModules.toList().ifEmpty { SoloFitModule.DEFAULT_ENABLED }
            prefs.setEnabledModules(modules)
            prefs.setModuleOrder(SoloFitModule.entries.filter { it in modules })
            prefs.setModuleSelectionComplete(true)
            onDone()
        }
    }
}
