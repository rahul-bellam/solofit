package com.solofit.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.core.FitnessMath
import com.solofit.app.core.StreakCalculator
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.domain.model.MacroTotals
import com.solofit.app.domain.model.TrainingGoal
import com.solofit.app.domain.repository.BodyRepository
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class DashboardState(
    val profile: UserProfileEntity? = null,
    val consumed: MacroTotals = MacroTotals(),
    val date: String = DateUtils.today(),
    val waterMl: Int = 0,
    val waterGoalMl: Int = 3000,
    val streakDays: Int = 0,
    val daysActiveThisWeek: Int = 0,
    // Transformation dashboard
    val phaseName: String = "Foundation Recomp",
    val phaseDay: Int = 1,
    val phaseTargetDays: Int = 365,
    val recoveryScore: Int? = null,
    val transformationScore: Int = 0,
    val trainingGoal: TrainingGoal = TrainingGoal.BODYBUILDING
) {
    val recoveryLabel: String get() = FitnessMath.readinessLabel(recoveryScore)
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    dailyLogRepository: DailyLogRepository,
    workoutRepository: WorkoutRepository,
    bodyRepository: BodyRepository
) : ViewModel() {

    private val today = DateUtils.today()

    private val core = combine(
        profileRepository.observeProfile(),
        dailyLogRepository.observeTotalsForDate(today),
        profileRepository.waterMl(today),
        workoutRepository.observeHistory()
    ) { profile, totals, water, history ->
        val dates = history.map { it.session.date }
        val now = LocalDate.now()
        CoreData(
            profile = profile,
            totals = totals,
            water = water,
            streak = StreakCalculator.currentStreak(dates, now),
            activeThisWeek = StreakCalculator.daysActiveInWindow(dates, now, 7),
            workoutToday = dates.contains(today)
        )
    }

    private val transform = combine(
        profileRepository.phaseName,
        profileRepository.phaseStartDate,
        profileRepository.phaseTargetDays,
        bodyRepository.observeMetric(today)
    ) { name, start, target, metric ->
        TransformData(name, start, target, metric?.sleepHours, metric?.steps, metric?.energyScore)
    }

    // Inputs for the Transformation Score: goal weights + waist progress + strength progress.
    private val scoreInputs = combine(
        profileRepository.trainingGoal,
        bodyRepository.observeMeasurements(),
        workoutRepository.observeCompletedSetRows()
    ) { goal, measurements, setRows ->
        // Waist progress: 0.5 = no change; >0.5 = shrinking (good). Map a 5cm drop to 1.0.
        val waists = measurements.mapNotNull { it.waistCm }
        val waistProgress = if (waists.size >= 2) {
            val drop = waists.first() - waists.last()        // positive = shrunk
            (0.5 + drop / 10.0).coerceIn(0.0, 1.0)           // 5cm drop -> 1.0
        } else 0.5
        // Strength progress: best-1RM gain across all lifts vs a 20% target.
        val byLift = setRows.groupBy { it.exerciseName }
        val gains = byLift.values.mapNotNull { sets ->
            val byDate = sets.groupBy { it.date }.toSortedMap()
            val firsts = byDate.values.firstOrNull()?.maxOfOrNull {
                FitnessMath.epley1RM(it.weightKg, it.reps)
            } ?: return@mapNotNull null
            val bests = byDate.values.maxOf { day -> day.maxOf { FitnessMath.epley1RM(it.weightKg, it.reps) } }
            if (firsts <= 0) null else ((bests - firsts) / firsts)
        }
        val avgGain = if (gains.isEmpty()) 0.0 else gains.average()
        val strengthProgress = (avgGain / 0.20).coerceIn(0.0, 1.0)  // 20% gain -> 1.0
        ScoreInputs(goal, waistProgress, strengthProgress)
    }

    val state = combine(core, transform, scoreInputs) { c, t, si ->
        val phaseDay = t.startDate?.let {
            runCatching {
                ChronoUnit.DAYS.between(LocalDate.parse(it), LocalDate.now()).toInt() + 1
            }.getOrDefault(1)
        } ?: 1
        val recovery = FitnessMath.recoveryScore(
            sleepHours = t.sleepHours, steps = t.steps,
            workoutDone = c.workoutToday, waterMl = c.water,
            waterGoalMl = 3000, energyScore = t.energy
        )
        val consistency = (c.activeThisWeek / 7.0).coerceIn(0.0, 1.0)
        val transformationScore = FitnessMath.transformationScore(
            strengthProgress = si.strengthProgress,
            waistProgress = si.waistProgress,
            consistency = consistency,
            recovery = (recovery ?: 0) / 100.0,
            wStrength = si.goal.wStrength,
            wWaist = si.goal.wWaist,
            wConsistency = si.goal.wConsistency,
            wRecovery = si.goal.wRecovery
        )
        DashboardState(
            profile = c.profile,
            consumed = c.totals,
            date = today,
            waterMl = c.water,
            streakDays = c.streak,
            daysActiveThisWeek = c.activeThisWeek,
            phaseName = t.name,
            phaseDay = phaseDay.coerceAtLeast(1),
            phaseTargetDays = t.target,
            recoveryScore = recovery,
            transformationScore = transformationScore,
            trainingGoal = si.goal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    val animationsEnabled = profileRepository.animationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun addWater(ml: Int) = viewModelScope.launch { profileRepository.addWaterMl(today, ml) }
    fun removeWater(ml: Int) = viewModelScope.launch { profileRepository.addWaterMl(today, -ml) }

    private data class CoreData(
        val profile: UserProfileEntity?,
        val totals: MacroTotals,
        val water: Int,
        val streak: Int,
        val activeThisWeek: Int,
        val workoutToday: Boolean
    )

    private data class TransformData(
        val name: String,
        val startDate: String?,
        val target: Int,
        val sleepHours: Double?,
        val steps: Int?,
        val energy: Int?
    )

    private data class ScoreInputs(
        val goal: TrainingGoal,
        val waistProgress: Double,
        val strengthProgress: Double
    )
}
