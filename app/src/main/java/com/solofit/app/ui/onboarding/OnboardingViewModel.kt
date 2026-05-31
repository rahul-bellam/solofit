package com.solofit.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.domain.model.ActivityLevel
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender
import com.solofit.app.domain.model.NutritionTargets
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.usecase.CalculateNutritionTargetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val name: String = "",
    val age: String = "",
    val gender: Gender = Gender.MALE,
    val weight: String = "",
    val height: String = "",
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goal: FitnessGoal = FitnessGoal.MAINTAIN,
    val preview: NutritionTargets? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
            age.toIntOrNull()?.let { it in 1..120 } == true &&
            weight.toDoubleOrNull()?.let { it in 20.0..400.0 } == true &&
            height.toDoubleOrNull()?.let { it in 80.0..260.0 } == true
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val calculate: CalculateNutritionTargetsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    fun onName(v: String) = _state.update { it.copy(name = v).recompute() }
    fun onAge(v: String) = _state.update { it.copy(age = v.filter { c -> c.isDigit() }).recompute() }
    fun onGender(v: Gender) = _state.update { it.copy(gender = v).recompute() }
    fun onWeight(v: String) = _state.update { it.copy(weight = v.filterDecimal()).recompute() }
    fun onHeight(v: String) = _state.update { it.copy(height = v.filterDecimal()).recompute() }
    fun onActivity(v: ActivityLevel) = _state.update { it.copy(activityLevel = v).recompute() }
    fun onGoal(v: FitnessGoal) = _state.update { it.copy(goal = v).recompute() }

    private fun String.filterDecimal(): String {
        val filtered = filterIndexed { i, c -> c.isDigit() || (c == '.' && !substring(0, i).contains('.')) }
        return filtered
    }

    private fun OnboardingState.recompute(): OnboardingState {
        val a = age.toIntOrNull()
        val w = weight.toDoubleOrNull()
        val h = height.toDoubleOrNull()
        if (a == null || w == null || h == null) return copy(preview = null)
        return try {
            copy(
                preview = calculate(
                    CalculateNutritionTargetsUseCase.Params(
                        age = a, gender = gender, weightKg = w, heightCm = h,
                        activityLevel = activityLevel, goal = goal
                    )
                )
            )
        } catch (e: IllegalArgumentException) {
            copy(preview = null)
        }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val targets = s.preview ?: return
        viewModelScope.launch {
            repository.saveProfile(
                UserProfileEntity(
                    name = s.name.trim(),
                    age = s.age.toInt(),
                    gender = s.gender,
                    weightKg = s.weight.toDouble(),
                    heightCm = s.height.toDouble(),
                    activityLevel = s.activityLevel,
                    goal = s.goal,
                    targetCalories = targets.targetCalories,
                    targetProtein = targets.targetProteinG,
                    targetCarbs = targets.targetCarbsG,
                    targetFats = targets.targetFatsG,
                    bmr = targets.bmr,
                    tdee = targets.tdee
                )
            )
            repository.setOnboardingComplete(true)
            onDone()
        }
    }
}
