package com.solofit.app.ui.dashboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.components.DustCompletionAnimation
import com.solofit.app.ui.components.VaporizeCelebration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.sol.SolCard
import com.solofit.app.sol.SolViewModel
import com.solofit.app.ui.components.rememberAnimationsActive
import com.solofit.app.core.DateUtils
import com.solofit.app.ui.components.CalorieRing
import com.solofit.app.ui.components.MacroBar
import com.solofit.app.ui.components.WaterTracker
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.ui.modules.ModuleSuggestion
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.HighGreen
import com.solofit.app.ui.theme.CarbsColor
import com.solofit.app.ui.theme.FatsColor
import com.solofit.app.ui.theme.ProteinColor
import com.solofit.app.ui.dashboard.SnackbarEvent
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    enabledModules: List<SoloFitModule> = SoloFitModule.DEFAULT_ENABLED,
    suggestions: List<ModuleSuggestion> = emptyList(),
    onEnableModule: (SoloFitModule) -> Unit = {},
    onLogMeal: () -> Unit,
    onLogWorkout: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenJournal: () -> Unit = {},
    onOpenBody: () -> Unit = {},
    onEditPhase: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
    onOpenWeight: () -> Unit = {},
    onOpenRecovery: () -> Unit = {},
    onOpenMeditation: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    solViewModel: SolViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val solState by solViewModel.state.collectAsStateWithLifecycle()
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
        onRefresh = { viewModel.refresh(); solViewModel.refresh() },
        modifier = Modifier.fillMaxSize().padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {

        // ── HERO: Sol Insight ──
        SolCard(
            state = solState,
            onToggleWhy = solViewModel::toggleWhy,
            onToggleWhat = solViewModel::toggleWhat,
            onListen = solViewModel::speak,
            onPersonalityChange = { },
            onLogMeal = onLogMeal,
            onLogWorkout = onLogWorkout,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // ── Header (below Sol) ──
        Row(
            Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                DateUtils.prettyMedium(state.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (solState.isSpeaking) {
                    Text("Speaking…", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp))
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── WEEKLY REFLECTION (Sundays) ──
        AnimatedVisibility(
            visible = solState.isSunday,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut()
        ) {
            Column {
                WeeklyReflection(
                    workoutCount = solState.weeklyWorkoutCount,
                    proteinDays = solState.weeklyProteinDays,
                    walkingTrend = solState.weeklyWalkingTrend
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        // ── SUPPORTING: Phase + Streak ──
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onEditPhase),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(state.phaseName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("Day ${state.phaseDay}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Text("Building Foundations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp))
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.streakDays > 0) {
                        Text("${state.streakDays}-day streak",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                    }
                    state.recoveryScore?.let { rec ->
                        Text("Recovery $rec%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } ?: Text("Building baseline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Today's Plan ──
        AnimatedVisibility(
            visible = planName.isNotEmpty() && !planDismissed,
            exit = fadeOut()
        ) {
            Box {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FitnessCenter, contentDescription = "Workout plan",
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.size(8.dp))
                            Text(planName, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        exercises.forEach { ex ->
                            val view = LocalView.current
                            Row(
                                modifier = Modifier.fillMaxWidth()
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
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    "${ex.exerciseName}  ${ex.sets}×${ex.reps} @ ${ex.weight}${ex.weightUnit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (ex.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
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

        // ── SECONDARY: Nutrition ──
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val consumedKcal = state.consumed.calories.roundToInt()
                val targetKcal = profile?.targetCalories ?: 2000
                Column(
                    Modifier.clearAndSetSemantics {
                        contentDescription = "$consumedKcal of $targetKcal kilocalories consumed today"
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CalorieRing(consumed = consumedKcal, target = targetKcal, animate = animate, onClick = onLogMeal)
                }
                Spacer(Modifier.height(20.dp))

                Text("Protein", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ProteinColor)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${state.consumed.proteinG.roundToInt()}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ProteinColor)
                    Spacer(Modifier.width(6.dp))
                    Text("g", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ProteinColor.copy(alpha = 0.7f))
                    Spacer(Modifier.weight(1f))
                    Text("${profile?.targetProtein ?: 0}g goal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                val proteinFrac = (state.consumed.proteinG.toFloat() / (profile?.targetProtein ?: 1).coerceAtLeast(1)).coerceAtMost(1f)
                Box(
                    Modifier.fillMaxWidth().height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        Modifier.fillMaxWidth(proteinFrac).height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(ProteinColor)
                    )
                }

                Spacer(Modifier.height(20.dp))
                MacroBar("Carbs", state.consumed.carbsG.roundToInt(), profile?.targetCarbs ?: 0, CarbsColor, animate = animate)
                Spacer(Modifier.height(12.dp))
                MacroBar("Fats", state.consumed.fatsG.roundToInt(), profile?.targetFats ?: 0, FatsColor, animate = animate)
            }
        }

        Spacer(Modifier.height(16.dp))

        WaterTracker(
            currentMl = state.waterMl,
            goalMl = state.waterGoalMl,
            onAdd = viewModel::addWater,
            onRemove = viewModel::removeWater,
            animate = animate
        )

        // ── Tracking Grid ──
        Spacer(Modifier.height(24.dp))
        Text("Tracking", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        val trackingItems = listOf(
            Triple("Profile", Icons.Filled.Person, onOpenProfile),
            Triple("Journal", Icons.AutoMirrored.Filled.MenuBook, onOpenJournal),
            Triple("Body", Icons.Filled.Straighten, onOpenBody),
            Triple("Weight", Icons.Filled.MonitorWeight, onOpenWeight),
            Triple("Reminders", Icons.Filled.Notifications, onOpenReminders),
            Triple("Meal", Icons.Filled.Restaurant, onLogMeal)
        )
        val rows = trackingItems.chunked(3)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (label, icon, onClick) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).clickable(onClick = onClick),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(icon, contentDescription = label,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(label, style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                if (row.size < 3) {
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(24.dp))

        suggestions.forEach { suggestion ->
            ModuleSuggestionCard(suggestion = suggestion, onAdd = { onEnableModule(suggestion.module) })
            Spacer(Modifier.size(12.dp))
        }
        }
    }
    }
    if (showWaterCelebration) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xCC000000)),
            contentAlignment = Alignment.Center
        ) {
            VaporizeCelebration(
                text = "WATER GOAL REACHED", fontSize = 48.sp, color = HighGreen,
                spread = 8f, density = 7f, vaporizeDurationMs = 3000, animate = animate,
                onAnimationEnd = { showWaterCelebration = false }
            )
        }
    }
    }
    }
}

@Composable
private fun ModuleSuggestionCard(suggestion: ModuleSuggestion, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(suggestion.reason, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(2.dp))
            Text(suggestion.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            Modifier.clip(RoundedCornerShape(10.dp)).background(Amber).clickable(onClick = onAdd)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("Add Module", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}
