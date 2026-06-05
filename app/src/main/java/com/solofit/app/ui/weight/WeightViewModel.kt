package com.solofit.app.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.entity.WeightEntryEntity
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightState(
    val entries: List<WeightEntryEntity> = emptyList(),
    val goal: FitnessGoal = FitnessGoal.IMPROVE_FITNESS,
    val startWeight: Double? = null,
    val latestWeight: Double? = null
) {
    val changeKg: Double?
        get() = if (startWeight != null && latestWeight != null) latestWeight - startWeight else null

    /** Is the trend moving the right way for the user's goal? */
    val onTrack: Boolean?
        get() {
            val c = changeKg ?: return null
            return when (goal) {
                FitnessGoal.LOSE_FAT -> c <= 0.0
                FitnessGoal.BUILD_MUSCLE -> c >= 0.0
                FitnessGoal.BODY_RECOMPOSITION, FitnessGoal.IMPROVE_FITNESS, FitnessGoal.STAY_HEALTHY -> kotlin.math.abs(c) <= 1.0
            }
        }
}

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val state = combine(
        weightRepository.observeAll(),
        profileRepository.observeProfile()
    ) { entries, profile ->
        WeightState(
            entries = entries,
            goal = profile?.goal ?: FitnessGoal.IMPROVE_FITNESS,
            startWeight = entries.firstOrNull()?.weightKg,
            latestWeight = entries.lastOrNull()?.weightKg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightState())

    fun logWeight(weightKg: Double) {
        if (weightKg <= 0) return
        viewModelScope.launch {
            weightRepository.logWeight(DateUtils.today(), weightKg)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { weightRepository.delete(id) }
    }
}
