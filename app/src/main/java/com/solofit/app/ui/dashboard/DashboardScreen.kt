package com.solofit.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
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
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    onLogMeal: () -> Unit,
    onLogWorkout: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenJournal: () -> Unit = {},
    onOpenBody: () -> Unit = {},
    onEditPhase: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val animateEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val animate = rememberAnimationsActive(animateEnabled)
    val profile = state.profile

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
                        contentDescription = null,
                        modifier = Modifier.height(18.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors()
            )
            AssistChip(
                onClick = {},
                label = { Text("${state.daysActiveThisWeek}/7 active days") }
            )
            state.recoveryScore?.let { rec ->
                AssistChip(
                    onClick = onOpenBody,
                    label = { Text("Recovery $rec% · ${state.recoveryLabel}") }
                )
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
                    CalorieRing(consumed = consumedKcal, target = targetKcal, animate = animate)
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

        Spacer(Modifier.height(24.dp))
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onLogMeal,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Restaurant, contentDescription = null)
                Text("  Log Meal")
            }
            OutlinedButton(
                onClick = onLogWorkout,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.FitnessCenter, contentDescription = null)
                Text("  Log Workout")
            }
        }
        Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onOpenJournal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                Text("  Open Journal — goals & gratitude")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Text("  View Workout History")
            }
        Spacer(Modifier.height(24.dp))
    }
}
