package com.solofit.app.ui.settings

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

data class EditProfileState(
    val loaded: Boolean = false,
    val existingId: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val name: String = "",
    val age: String = "",
    val gender: Gender = Gender.MALE,
    val weight: String = "",
    val height: String = "",
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goal: FitnessGoal = FitnessGoal.MAINTAIN,
    val preview: NutritionTargets? = null,
    val saved: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
            age.toIntOrNull()?.let { it in 1..120 } == true &&
            weight.toDoubleOrNull()?.let { it in 20.0..400.0 } == true &&
            height.toDoubleOrNull()?.let { it in 80.0..260.0 } == true
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val calculate: CalculateNutritionTargetsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getProfile()?.let { p ->
                _state.update {
                    it.copy(
                        loaded = true,
                        existingId = p.id,
                        createdAt = p.createdAt,
                        name = p.name,
                        age = p.age.toString(),
                        gender = p.gender,
                        weight = p.weightKg.toString(),
                        height = p.heightCm.toString(),
                        activityLevel = p.activityLevel,
                        goal = p.goal
                    ).recompute()
                }
            } ?: _state.update { it.copy(loaded = true) }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v).recompute() }
    fun onAge(v: String) = _state.update { it.copy(age = v.filter { c -> c.isDigit() }).recompute() }
    fun onGender(v: Gender) = _state.update { it.copy(gender = v).recompute() }
    fun onWeight(v: String) = _state.update { it.copy(weight = v.decimal()).recompute() }
    fun onHeight(v: String) = _state.update { it.copy(height = v.decimal()).recompute() }
    fun onActivity(v: ActivityLevel) = _state.update { it.copy(activityLevel = v).recompute() }
    fun onGoal(v: FitnessGoal) = _state.update { it.copy(goal = v).recompute() }

    private fun String.decimal(): String =
        filterIndexed { i, c -> c.isDigit() || (c == '.' && !substring(0, i).contains('.')) }

    private fun EditProfileState.recompute(): EditProfileState {
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
        val t = s.preview ?: return
        viewModelScope.launch {
            repository.saveProfile(
                UserProfileEntity(
                    id = s.existingId,
                    name = s.name.trim(),
                    age = s.age.toInt(),
                    gender = s.gender,
                    weightKg = s.weight.toDouble(),
                    heightCm = s.height.toDouble(),
                    activityLevel = s.activityLevel,
                    goal = s.goal,
                    targetCalories = t.targetCalories,
                    targetProtein = t.targetProteinG,
                    targetCarbs = t.targetCarbsG,
                    targetFats = t.targetFatsG,
                    bmr = t.bmr,
                    tdee = t.tdee,
                    createdAt = s.createdAt
                )
            )
            _state.update { it.copy(saved = true) }
            onDone()
        }
    }
}
