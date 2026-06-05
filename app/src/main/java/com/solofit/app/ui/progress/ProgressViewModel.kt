package com.solofit.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.local.dao.CompletedSetRow
import com.solofit.app.data.local.dao.ExerciseVolume
import com.solofit.app.data.local.entity.WeightEntryEntity
import com.solofit.app.data.local.entity.WorkoutSessionEntity
import com.solofit.app.data.repository.ProfileRepositoryImpl
import com.solofit.app.domain.repository.BodyRepository
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.WeightRepository
import com.solofit.app.domain.repository.WorkoutRepository
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class Achievement(
    val title: String,
    val description: String,
    val unlocked: Boolean
)

data class ProgressUiState(
    val streakDays: Int = 0,
    val bestStreak: Int = 0,
    val workoutsThisMonth: Int = 0,
    val totalVolumeKg: Int = 0,
    val milestonesUnlocked: Int = 0,
    val consistencyPct: Int = 0,
    val achievements: List<Achievement> = emptyList()
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val weightRepository: WeightRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val history = workoutRepository.observeHistory().first()
            val volume = workoutRepository.observeVolumeSince("2000-01-01").first()
            val enabled = prefs.enabledModules.first()
            computeState(history, volume, enabled)
        }

        combine(
            workoutRepository.observeHistory(),
            workoutRepository.observeVolumeSince("2000-01-01"),
            prefs.enabledModules
        ) { history, volume, enabled ->
            computeState(history, volume, enabled)
        }.launchIn(viewModelScope)
    }

    private fun computeState(
        history: List<com.solofit.app.data.local.relation.SessionWithSets>,
        volume: List<ExerciseVolume>,
        enabled: List<SoloFitModule>
    ) {
        val dates = history.map { it.session.date }.sorted()
        val streak = computeCurrentStreak(dates)
        val bestStreak = computeBestStreak(dates)

        val thisMonth = LocalDate.now().withDayOfMonth(1).toString()
        val workoutsThisMonth = history.count {
            it.session.date >= thisMonth && it.session.isCompleted
        }

        val totalVolumeKg = volume.sumOf { it.totalVolume }.toInt()

        val milestonesUnlocked = listOfNotNull(
            if (streak >= 7) "Week Warrior" else null,
            if (history.size >= 10) "10 Workouts" else null,
            if (streak >= 30) "30 Days" else null,
            if (history.size >= 100) "100 Workouts" else null
        ).size

        val firstOfMonth = LocalDate.now().withDayOfMonth(1)
        val daysThisMonth = firstOfMonth.datesUntil(
            LocalDate.now().plusDays(1), Period.ofDays(1)
        ).toList().size
        val workoutDays = history.filter { it.session.isCompleted }
            .map { it.session.date }.distinct()
            .count { it >= thisMonth }
        val consistencyPct = if (daysThisMonth > 0)
            (workoutDays * 100 / daysThisMonth).coerceIn(0, 100) else 0

        val achievements = buildList {
            add(Achievement("Week Warrior", "7-day streak", streak >= 7))
            add(Achievement("Iron Will", "Complete 10 workouts", history.size >= 10))
            add(Achievement("Consistent", "Log workouts for 30 days", streak >= 30))
            add(Achievement("Century", "100 workouts total", history.size >= 100))
        }

        _state.value = ProgressUiState(
            streakDays = streak,
            bestStreak = bestStreak.coerceAtLeast(streak),
            workoutsThisMonth = workoutsThisMonth,
            totalVolumeKg = totalVolumeKg,
            milestonesUnlocked = milestonesUnlocked,
            consistencyPct = consistencyPct,
            achievements = achievements
        )
    }
}

private fun computeCurrentStreak(dates: List<String>): Int {
    if (dates.isEmpty()) return 0
    val today = LocalDate.now()
    var count = 0
    var expected = today
    for (date in dates.reversed()) {
        val d = LocalDate.parse(date)
        if (d == expected || d == expected.minusDays(1)) {
            if (d == expected) {
                count++
                expected = expected.minusDays(1)
            } else {
                // Gap day
                break
            }
        } else if (d < expected.minusDays(1)) {
            break
        }
    }
    return count
}

private fun computeBestStreak(dates: List<String>): Int {
    if (dates.isEmpty()) return 0
    val unique = dates.map { LocalDate.parse(it) }.distinct().sorted()
    var best = 1
    var current = 1
    for (i in 1 until unique.size) {
        if (ChronoUnit.DAYS.between(unique[i - 1], unique[i]) == 1L) {
            current++
            best = maxOf(best, current)
        } else {
            current = 1
        }
    }
    return best
}
