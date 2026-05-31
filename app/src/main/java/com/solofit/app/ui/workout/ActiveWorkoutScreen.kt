package com.solofit.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import com.solofit.app.ui.components.rememberAnimationsActive
import com.solofit.app.ui.components.DumbbellCheck
import com.solofit.app.data.local.entity.ExerciseSetEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onFinish: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val animateEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val animate = rememberAnimationsActive(animateEnabled)
    val groups = viewModel.groupedExercises(session)
    val completedCount = session?.sets?.count { it.isCompleted } ?: 0
    val totalCount = session?.sets?.size ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(session?.session?.routineName ?: "Workout")
                        Text(
                            "$completedCount / $totalCount sets done",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            items(groups, key = { it.exerciseName }) { group ->
                ExerciseCard(
                    group = group,
                    animate = animate,
                    onUpdate = viewModel::updateSet,
                    onUpdateRir = viewModel::updateRir,
                    onAddSet = { viewModel.addSet(group) },
                    onDeleteSet = viewModel::deleteSet
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.finish(onFinish) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Finish & Save Workout") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    group: ExerciseGroup,
    animate: Boolean,
    onUpdate: (ExerciseSetEntity, Double?, Int?, Boolean?) -> Unit,
    onUpdateRir: (ExerciseSetEntity, Int?) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (ExerciseSetEntity) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(group.exerciseName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                group.muscleGroup,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Estimated 1RM from the best completed set (Epley). Updates live.
            val best1RM = group.sets
                .filter { it.isCompleted && it.weightKg > 0 && it.reps > 0 }
                .maxOfOrNull { com.solofit.app.core.FitnessMath.epley1RM(it.weightKg, it.reps) }
            if (best1RM != null && best1RM > 0) {
                Text(
                    "Est. 1RM: ${best1RM.roundToInt()} kg",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            // Auto-progression coaching from completed sets (double-progression, top=12).
            val completed = group.sets.filter { it.isCompleted && it.reps > 0 }
            if (completed.isNotEmpty()) {
                val prog = com.solofit.app.core.FitnessMath.progression(
                    repsPerSet = completed.map { it.reps },
                    rirPerSet = completed.map { it.rir },
                    topOfRange = 12
                )
                if (prog != com.solofit.app.core.FitnessMath.Progression.NONE) {
                    Text(
                        prog.message,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Set", Modifier.width(36.dp), style = MaterialTheme.typography.labelSmall)
                Text("Weight (kg)", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
                Text("Reps", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
                Text("RIR", Modifier.weight(0.7f), style = MaterialTheme.typography.labelSmall)
                Text("Done", Modifier.width(48.dp), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(36.dp))
            }
            Spacer(Modifier.height(4.dp))
            group.sets.forEach { set ->
                SetRow(set, animate, onUpdate, onUpdateRir, onDeleteSet)
                Spacer(Modifier.height(6.dp))
            }
            TextButton(onClick = onAddSet) {
                Icon(Icons.Filled.Add, null)
                Text(" Add set")
            }
        }
    }
}

@Composable
private fun SetRow(
    set: ExerciseSetEntity,
    animate: Boolean,
    onUpdate: (ExerciseSetEntity, Double?, Int?, Boolean?) -> Unit,
    onUpdateRir: (ExerciseSetEntity, Int?) -> Unit,
    onDeleteSet: (ExerciseSetEntity) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            set.setNumber.toString(),
            Modifier.width(36.dp),
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = if (set.weightKg == 0.0) "" else trimNumber(set.weightKg),
            onValueChange = { input ->
                val clean = input.filterIndexed { i, c ->
                    c.isDigit() || (c == '.' && !input.substring(0, i).contains('.'))
                }
                onUpdate(set, clean.toDoubleOrNull() ?: 0.0, null, null)
            },
            placeholder = { Text("0") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = if (set.reps == 0) "" else set.reps.toString(),
            onValueChange = { input ->
                onUpdate(set, null, input.filter { it.isDigit() }.toIntOrNull() ?: 0, null)
            },
            placeholder = { Text("0") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = set.rir?.toString() ?: "",
            onValueChange = { input ->
                onUpdateRir(set, input.filter { it.isDigit() }.toIntOrNull())
            },
            placeholder = { Text("-") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.7f)
        )
        Box(Modifier.width(48.dp), contentAlignment = Alignment.Center) {
            DumbbellCheck(
                checked = set.isCompleted,
                animate = animate,
                onToggle = {
                    val nowDone = !set.isCompleted
                    if (nowDone) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdate(set, null, null, nowDone)
                }
            )
        }
        IconButton(onClick = { onDeleteSet(set) }, modifier = Modifier.width(36.dp)) {
            Icon(Icons.Filled.Close, "Delete set")
        }
    }
}

private fun trimNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
