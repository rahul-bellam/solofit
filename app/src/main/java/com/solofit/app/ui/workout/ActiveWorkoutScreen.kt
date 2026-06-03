package com.solofit.app.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import com.solofit.app.ui.components.rememberAnimationsActive
import com.solofit.app.ui.components.DumbbellCheck
import com.solofit.app.data.local.entity.ExerciseSetEntity

private val restOptions = listOf(30, 60, 90, 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onFinish: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val animateEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val animate = rememberAnimationsActive(animateEnabled)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val groups = viewModel.groupedExercises(session)
    val completedCount = session?.sets?.count { it.isCompleted && !it.isWarmUp } ?: 0
    val totalCount = session?.sets?.count { !it.isWarmUp } ?: 0

    val prMessage = uiState.prCelebrationMessage
    LaunchedEffect(prMessage) {
        if (prMessage != null) {
            kotlinx.coroutines.delay(3000L)
            viewModel.dismissPrCelebration()
        }
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
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
                        onUpdateNotes = viewModel::updateNotes,
                        onUpdateWarmUp = viewModel::updateWarmUp,
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

            if (uiState.restTimerRunning) {
                RestTimerOverlay(
                    secondsRemaining = uiState.restSecondsRemaining,
                    duration = uiState.restDuration,
                    onDurationChange = viewModel::setRestDuration,
                    onDismiss = viewModel::dismissRestTimer
                )
            }

            AnimatedVisibility(
                visible = prMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        prMessage ?: "",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RestTimerOverlay(
    secondsRemaining: Int,
    duration: Int,
    onDurationChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Timer,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                "Rest Timer",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatTime(secondsRemaining),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = if (secondsRemaining <= 10) MaterialTheme.colorScheme.error else Color.White
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                restOptions.forEach { secs ->
                    SuggestionChip(
                        onClick = { onDurationChange(secs) },
                        label = { Text("${secs}s") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (duration == secs)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDismiss) {
                Text("Skip", color = Color.White)
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun ExerciseCard(
    group: ExerciseGroup,
    animate: Boolean,
    onUpdate: (ExerciseSetEntity, Double?, Int?, Boolean?) -> Unit,
    onUpdateRir: (ExerciseSetEntity, Int?) -> Unit,
    onUpdateNotes: (ExerciseSetEntity, String) -> Unit,
    onUpdateWarmUp: (ExerciseSetEntity, Boolean) -> Unit,
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
            val best1RM = group.sets
                .filter { it.isCompleted && !it.isWarmUp && it.weightKg > 0 && it.reps > 0 }
                .maxOfOrNull { com.solofit.app.core.FitnessMath.epley1RM(it.weightKg, it.reps) }
            if (best1RM != null && best1RM > 0) {
                Text(
                    "Est. 1RM: ${best1RM.roundToInt()} kg",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            val completed = group.sets.filter { it.isCompleted && !it.isWarmUp && it.reps > 0 }
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
                SetRow(
                    set = set,
                    animate = animate,
                    onUpdate = onUpdate,
                    onUpdateRir = onUpdateRir,
                    onUpdateNotes = onUpdateNotes,
                    onUpdateWarmUp = onUpdateWarmUp,
                    onDeleteSet = onDeleteSet
                )
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
    onUpdateNotes: (ExerciseSetEntity, String) -> Unit,
    onUpdateWarmUp: (ExerciseSetEntity, Boolean) -> Unit,
    onDeleteSet: (ExerciseSetEntity) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var showNotes by remember { mutableStateOf(set.notes.isNotBlank()) }
    var notesText by remember(set.notes) { mutableStateOf(set.notes) }

    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (set.isWarmUp) {
                Text(
                    "W",
                    Modifier.width(36.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    set.setNumber.toString(),
                    Modifier.width(36.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
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

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(36.dp))
            AssistChip(
                onClick = { onUpdateWarmUp(set, !set.isWarmUp) },
                label = {
                    Text(
                        if (set.isWarmUp) "Warm-up" else "Working",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (set.isWarmUp)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            )
            TextButton(onClick = { showNotes = !showNotes }) {
                Text(
                    if (set.notes.isNotBlank()) "Notes *" else "Notes",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        AnimatedVisibility(visible = showNotes) {
            Column {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("e.g. felt easy, slow negative...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp)
                        .height(56.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                LaunchedEffect(notesText) {
                    onUpdateNotes(set, notesText)
                }
            }
        }
    }
}

private fun trimNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
