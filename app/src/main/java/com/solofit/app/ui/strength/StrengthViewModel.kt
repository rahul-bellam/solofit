package com.solofit.app.ui.strength

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.FitnessMath
import com.solofit.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Per-exercise estimated-1RM progression. */
data class LiftProgress(
    val exerciseName: String,
    /** best estimated 1RM per day, in date order */
    val points: List<Float>,
    val best1RM: Int,
    val first1RM: Int
) {
    val deltaKg: Int get() = best1RM - first1RM
}

@HiltViewModel
class StrengthViewModel @Inject constructor(
    repository: WorkoutRepository
) : ViewModel() {

    val lifts = repository.observeCompletedSetRows()
        .map { rows ->
            rows.groupBy { it.exerciseName }
                .map { (name, sets) ->
                    // Best estimated 1RM per date, then ordered by date.
                    val bestPerDay = sets
                        .groupBy { it.date }
                        .toSortedMap()
                        .map { (_, daySets) ->
                            daySets.maxOf { FitnessMath.epley1RM(it.weightKg, it.reps) }
                        }
                    LiftProgress(
                        exerciseName = name,
                        points = bestPerDay.map { it.toFloat() },
                        best1RM = (bestPerDay.maxOrNull() ?: 0.0).toInt(),
                        first1RM = (bestPerDay.firstOrNull() ?: 0.0).toInt()
                    )
                }
                // Most-trained / most-improved first.
                .sortedByDescending { it.points.size }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
