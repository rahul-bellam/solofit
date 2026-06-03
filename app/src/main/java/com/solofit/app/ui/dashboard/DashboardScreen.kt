package com.solofit.app.ui.dashboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.data.local.entity.PlannedExerciseEntity
import com.solofit.app.ui.components.DustCompletionAnimation
import com.solofit.app.ui.components.VaporizeCelebration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.components.rememberAnimationsActive
import com.solofit.app.core.DateUtils
import com.solofit.app.ui.components.CalorieRing
import com.solofit.app.ui.components.MacroBar
import com.solofit.app.ui.components.WaterTracker
import com.solofit.app.ui.theme.CarbsColor
import com.solofit.app.ui.theme.FatsColor
import com.solofit.app.ui.theme.ProteinColor
import com.solofit.app.ui.theme.Emerald
import com.solofit.app.ui.dashboard.SnackbarEvent
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogMeal: () -> Unit,
    onLogWorkout: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenJournal: () -> Unit = {},
    onOpenBody: () -> Unit = {},
    onEditPhase: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
    onOpenWeight: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val animateEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val animate = rememberAnimationsActive(animateEnabled)
    val profile = state.profile
    val planName by viewModel.todayPlanName.collectAsStateWithLifecycle()
    val exercises by viewModel.todayExercises.collectAsStateWithLifecycle()
    val allDone by viewModel.todayAllDone.collectAsStateWithLifecycle()
    val planDismissed by viewModel.planDismissed.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showWaterCelebration by remember { mutableStateOf(false) }
    var prevWaterGoalHit by remember { mutableStateOf(false) }

    val waterGoalHit = state.waterGoalMl > 0 && state.waterMl >= state.waterGoalMl
    LaunchedEffect(waterGoalHit) {
        if (waterGoalHit && !prevWaterGoalHit) {
            showWaterCelebration = true
        }
        prevWaterGoalHit = waterGoalHit
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { event ->
            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                event.onAction()
            }
        }
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
    Box {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize().padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Hi${profile?.name?.let { ", $it" } ?: ""} 👋",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    DateUtils.prettyMedium(state.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                profile?.let { p ->
                    if (p.heightCm > 0) {
                        val bmi = p.weightKg / ((p.heightCm / 100.0) * (p.heightCm / 100.0))
                        Text(
                            "BMI ${"%.1f".format(bmi)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- Hero: Days Consistent (the metric that actually matters) ----
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEditPhase),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    state.phaseName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "Day ${state.phaseDay}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        " / ${state.phaseTargetDays}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    "Consistency beats intensity. Keep showing up.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Transformation Score ",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "${state.transformationScore}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "  · ${state.trainingGoal.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        if (state.streakDays > 0) "${state.streakDays}-day streak"
                        else "No streak yet"
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = "Streak",
                        modifier = Modifier.height(18.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(7) { i ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < state.daysActiveThisWeek) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                    if (i < 6) Spacer(Modifier.size(3.dp))
                }
            }
            state.recoveryScore?.let { rec ->
                AssistChip(
                    onClick = onOpenBody,
                    label = { Text("Recovery $rec% · ${state.recoveryLabel}") }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---- Today's Summary ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val calsPct = profile?.let {
                    if (it.targetCalories > 0) (state.consumed.calories / it.targetCalories * 100).roundToInt()
                    else 0
                } ?: 0
                val waterPct = if (state.waterGoalMl > 0) (state.waterMl * 100 / state.waterGoalMl).coerceAtMost(100) else 0

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Calories", tint = MaterialTheme.colorScheme.primary)
                    Text("${calsPct}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("calories", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.WaterDrop, contentDescription = "Water", tint = MaterialTheme.colorScheme.primary)
                    Text("${waterPct}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("water", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (state.remindersActive) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Reminders", tint = MaterialTheme.colorScheme.primary)
                        Text("ON", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("reminders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (state.workoutToday) Icons.Filled.CheckCircle else Icons.Filled.FitnessCenter,
                        contentDescription = "Workout status",
                        tint = if (state.workoutToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (state.workoutToday) "Done" else "—",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text("workout", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---- Today's Plan ----
        AnimatedVisibility(
            visible = planName.isNotEmpty() && !planDismissed,
            exit = fadeOut()
        ) {
            Box {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FitnessCenter, contentDescription = "Workout plan", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(Modifier.size(8.dp))
                            Text(planName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        exercises.forEach { ex ->
                            val view = LocalView.current
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        viewModel.toggleExercise(ex)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                            Icon(
                                if (ex.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = "Exercise status",
                                    tint = if (ex.isCompleted) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    "${ex.exerciseName}  ${ex.sets}×${ex.reps} @ ${ex.weight}${ex.weightUnit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (ex.isCompleted) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
                if (allDone) {
                    DustCompletionAnimation(
                        animate = animate,
                        onAnimationEnd = viewModel::dismissCompletedPlan
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val consumedKcal = state.consumed.calories.roundToInt()
                val targetKcal = profile?.targetCalories ?: 2000
                Column(
                    Modifier.clearAndSetSemantics {
                        contentDescription =
                            "$consumedKcal of $targetKcal kilocalories consumed today"
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CalorieRing(consumed = consumedKcal, target = targetKcal, animate = animate, onClick = onLogMeal)
                }
                Spacer(Modifier.height(28.dp))
                MacroBar(
                    "Protein",
                    state.consumed.proteinG.roundToInt(),
                    profile?.targetProtein ?: 0,
                    ProteinColor,
                    animate = animate
                )
                Spacer(Modifier.height(16.dp))
                MacroBar(
                    "Carbs",
                    state.consumed.carbsG.roundToInt(),
                    profile?.targetCarbs ?: 0,
                    CarbsColor,
                    animate = animate
                )
                Spacer(Modifier.height(16.dp))
                MacroBar(
                    "Fats",
                    state.consumed.fatsG.roundToInt(),
                    profile?.targetFats ?: 0,
                    FatsColor,
                    animate = animate
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        WaterTracker(
            currentMl = state.waterMl,
            goalMl = state.waterGoalMl,
            onAdd = viewModel::addWater,
            onRemove = viewModel::removeWater,
            animate = animate
        )
        Spacer(Modifier.height(8.dp))
        WaterGoalRow(
            goalMl = state.waterGoalMl,
            onGoalChange = viewModel::setWaterGoalMl
        )

        Spacer(Modifier.height(24.dp))
        Text("Tracking", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenProfile),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text("Profile", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenJournal),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Journal", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text("Journal", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenBody),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Straighten, contentDescription = "Body", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text("Body", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenWeight),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.MonitorWeight, contentDescription = "Weight", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text("Weight", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenReminders),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Reminders", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text("Reminders", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        }
    }
    }
    if (showWaterCelebration) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000)),
            contentAlignment = Alignment.Center
        ) {
            VaporizeCelebration(
                text = "WATER GOAL REACHED",
                fontSize = 48.sp,
                color = Emerald,
                spread = 8f,
                density = 7f,
                vaporizeDurationMs = 3000,
                animate = animate,
                onAnimationEnd = { showWaterCelebration = false }
            )
        }
    }
    }
}

@Composable
private fun WaterGoalRow(
    goalMl: Int,
    onGoalChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Daily goal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${goalMl} ml",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1500, 2000, 2500, 3000, 3500, 4000).forEach { preset ->
                    val selected = goalMl == preset
                    androidx.compose.material3.FilterChip(
                        selected = selected,
                        onClick = { onGoalChange(preset) },
                        label = { Text("${preset / 1000}.${if (preset % 1000 == 0) "0" else "5"}L", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}
