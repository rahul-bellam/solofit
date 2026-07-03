package com.solofit.app.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.core.FitnessMath
import com.solofit.app.core.StreakCalculator
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.domain.model.MacroTotals
import com.solofit.app.domain.model.TrainingGoal
import com.solofit.app.data.local.entity.PlannedExerciseEntity
import com.solofit.app.data.local.entity.WeeklyPlanEntity
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.domain.repository.BodyRepository
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WeeklyPlanRepository
import com.solofit.app.domain.repository.WorkoutRepository
import com.solofit.app.sol.WellnessThresholds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val onAction: () -> Unit = {}
)

data class DashboardState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val profile: UserProfileEntity? = null,
    val consumed: MacroTotals = MacroTotals(),
    val date: String = DateUtils.today(),
    val waterMl: Int = 0,
    val waterGoalMl: Int = WellnessThresholds.WATER_DEFAULT_GOAL_ML,
    val streakDays: Int = 0,
    val daysActiveThisWeek: Int = 0,
    val daysTracked: Int = 0,
    val workoutToday: Boolean = false,
    val remindersActive: Boolean = false,
    val steps: Int = 0,
    val stepGoal: Int = WellnessThresholds.DEFAULT_STEP_GOAL,
    val meditationMinutes: Int = 0,
    // Transformation dashboard
    val phaseName: String = "Foundation Recomp",
    val phaseDay: Int = 1,
    val phaseTargetDays: Int = 365,
    val recoveryScore: Int? = null,
    val transformationScore: Int = 0,
    val trainingGoal: TrainingGoal = TrainingGoal.BODYBUILDING
) {
    val recoveryLabel: String get() = FitnessMath.readinessLabel(recoveryScore)
    val isFirstWeek: Boolean get() = daysTracked < 7
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val workoutRepository: WorkoutRepository,
    private val bodyRepository: BodyRepository,
    private val weeklyPlanRepository: WeeklyPlanRepository,
    private val prefs: UserPreferences,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _snackbar = Channel<SnackbarEvent>(Channel.CONFLATED)
    val snackbarEvent = _snackbar.receiveAsFlow()
    private val dayFlow = MutableStateFlow(java.time.LocalDate.now().dayOfWeek.value)

    // The current day key, made reactive so all date-scoped flows re-subscribe when
    // the day rolls over while the screen is open (avoids showing yesterday's data
    // past midnight). Updated by the ticker below.
    private val dateFlow = MutableStateFlow(DateUtils.today())

    init {
        viewModelScope.launch {
            while (true) {
                val now = java.time.LocalTime.now()
                val nextMidnight = java.time.Duration.between(now, java.time.LocalTime.MAX).toMillis() + 1
                delay(nextMidnight)
                val newNow = java.time.LocalDate.now()
                dayFlow.value = newNow.dayOfWeek.value
                dateFlow.value = DateUtils.today()
            }
        }
    }

    /** Snapshot of the reactive day key; use for one-shot writes (water logging, etc.). */
    private val today: String get() = dateFlow.value

    @OptIn(ExperimentalCoroutinesApi::class)
    private val core = dateFlow.flatMapLatest { date ->
        combine(
            profileRepository.observeProfile(),
            dailyLogRepository.observeTotalsForDate(date),
            profileRepository.waterMl(date),
            profileRepository.waterGoalMl,
            workoutRepository.observeHistory()
        ) { profile, totals, water, goal, history ->
            val dates = history.map { it.session.date }
            val now = LocalDate.now()
            val firstDate = (dates.minOrNull()?.let { runCatching { LocalDate.parse(it) }.getOrNull() }) ?: now
            val daysTracked = ChronoUnit.DAYS.between(firstDate, now).toInt().coerceAtLeast(0) + 1
            CoreData(
                profile = profile,
                totals = totals,
                water = water,
                waterGoal = goal,
                streak = StreakCalculator.currentStreak(dates, now),
                activeThisWeek = StreakCalculator.daysActiveInWindow(dates, now, 7),
                workoutToday = dates.contains(date),
                daysTracked = daysTracked
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val transform = dateFlow.flatMapLatest { date ->
        combine(
            profileRepository.phaseName,
            profileRepository.phaseStartDate,
            profileRepository.phaseTargetDays,
            bodyRepository.observeMetric(date)
        ) { name, start, target, metric ->
            TransformData(name, start, target, metric?.sleepHours, metric?.steps, metric?.energyScore)
        }
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
            val bests = byDate.values.maxOfOrNull { day -> day.maxOf { set -> FitnessMath.epley1RM(set.weightKg, set.reps) } } ?: 0.0
            if (firsts <= 0) null else ((bests - firsts) / firsts)
        }
        val avgGain = if (gains.isEmpty()) 0.0 else gains.average()
        val strengthProgress = (avgGain / 0.20).coerceIn(0.0, 1.0)  // 20% gain -> 1.0
        ScoreInputs(goal, waistProgress, strengthProgress)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val prefsFlow = dateFlow.flatMapLatest { date ->
        combine(prefs.stepGoal, prefs.meditationMinutes(date)) { g, m -> StepGoalAndMed(g, m) }
    }

    private val isRefreshingAndPrefs = combine(_isRefreshing, prefsFlow) { refreshing, p -> refreshing to p }

    val state = combine(isRefreshingAndPrefs, core, transform, scoreInputs, profileRepository.reminderSettings) { (refreshing, prefsData), c, t, si, reminders ->
        val phaseDay = t.startDate?.let {
            runCatching {
                ChronoUnit.DAYS.between(LocalDate.parse(it), LocalDate.now()).toInt() + 1
            }.getOrDefault(1)
        } ?: 1
        val recovery = FitnessMath.recoveryScore(
            sleepHours = t.sleepHours, steps = t.steps,
            workoutDone = c.workoutToday, waterMl = c.water,
            waterGoalMl = c.waterGoal, energyScore = t.energy
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
            isLoading = false,
            isRefreshing = refreshing,
            profile = c.profile,
            consumed = c.totals,
            date = today,
            waterMl = c.water,
            waterGoalMl = c.waterGoal,
            streakDays = c.streak,
            daysActiveThisWeek = c.activeThisWeek,
            daysTracked = c.daysTracked,
            workoutToday = c.workoutToday,
            remindersActive = reminders.hydrationEnabled || reminders.workoutEnabled ||
                reminders.morningGoalsEnabled || reminders.eveningGratitudeEnabled,
            steps = t.steps ?: 0,
            stepGoal = prefsData.stepGoal,
            meditationMinutes = prefsData.meditationMinutes,
            phaseName = t.name,
            phaseDay = phaseDay.coerceAtLeast(1),
            phaseTargetDays = t.target,
            recoveryScore = recovery,
            transformationScore = transformationScore,
            trainingGoal = si.goal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(300)
            _isRefreshing.value = false
        }
    }

    val animationsEnabled = profileRepository.animationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // ---- Weekly plan for today ----

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todayPlan: Flow<WeeklyPlanEntity?> =
        dayFlow.flatMapLatest { weeklyPlanRepository.observePlanForDay(it) }

    val todayPlanName = todayPlan
        .map { it?.name ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayExercises = todayPlan
        .flatMapLatest { plan ->
            if (plan != null) weeklyPlanRepository.observeExercisesForPlan(plan.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAllDone = todayExercises
        .map { list -> list.isNotEmpty() && list.all { it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleExercise(exercise: PlannedExerciseEntity) {
        viewModelScope.launch {
            weeklyPlanRepository.setExerciseCompleted(exercise.id, !exercise.isCompleted)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val planDismissed: StateFlow<Boolean> = dateFlow
        .flatMapLatest { date -> savedStateHandle.getStateFlow("plan_dismissed_$date", false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun dismissCompletedPlan() {
        savedStateHandle["plan_dismissed_$today"] = true
    }

    fun addWater(ml: Int) = viewModelScope.launch {
        try {
            profileRepository.addWaterMl(today, ml)
        } catch (e: Exception) {
            _snackbar.send(SnackbarEvent("Could not log water"))
        }
    }

    fun removeWater(ml: Int) = viewModelScope.launch {
        try {
            profileRepository.addWaterMl(today, -ml)
            _snackbar.send(
                SnackbarEvent(
                    message = "${ml}ml water removed",
                    actionLabel = "Undo",
                    onAction = { addWater(ml) }
                )
            )
        } catch (e: Exception) {
            _snackbar.send(SnackbarEvent("Could not remove water"))
        }
    }

    fun setWaterGoalMl(ml: Int) = viewModelScope.launch {
        try {
            profileRepository.setWaterGoalMl(ml)
        } catch (e: Exception) {
            _snackbar.send(SnackbarEvent("Could not update water goal"))
        }
    }

    private data class CoreData(
        val profile: UserProfileEntity?,
        val totals: MacroTotals,
        val water: Int,
        val waterGoal: Int,
        val streak: Int,
        val activeThisWeek: Int,
        val workoutToday: Boolean,
        val daysTracked: Int
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

    private data class StepGoalAndMed(val stepGoal: Int, val meditationMinutes: Int)
}
