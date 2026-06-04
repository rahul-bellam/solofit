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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.data.local.entity.ExerciseSetEntity
import com.solofit.app.ui.components.WorkoutTheme
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.components.rememberAnimationsActive
import com.solofit.app.ui.components.VaporizeCelebration
import com.solofit.app.ui.theme.Emerald

@Composable
fun ActiveWorkoutScreen(
    onFinish: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val animateEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val animate = rememberAnimationsActive(animateEnabled)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
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

    val isPaused = uiState.isPaused
    var showCelebration by remember { mutableStateOf(false) }
    var activeExerciseName by remember { mutableStateOf<String?>(null) }

    val currentExercises = groups.map { group ->
        val sets = group.sets.filter { !it.isWarmUp }
        val completedSets = sets.count { it.isCompleted }
        val totalSetsForExercise = sets.size
        val progress = if (totalSetsForExercise > 0)
            (completedSets.toFloat() / totalSetsForExercise) * 100f else 0f

        val state = when {
            completedSets == totalSetsForExercise -> RingState.COMPLETE
            activeExerciseName == group.exerciseName && completedSets < totalSetsForExercise -> RingState.ACTIVE
            completedSets > 0 && completedSets < totalSetsForExercise -> RingState.ACTIVE
            activeExerciseName != null && activeExerciseName != group.exerciseName -> RingState.UPCOMING
            else -> RingState.UPCOMING
        }

        val firstIncomplete = sets.firstOrNull { !it.isCompleted }
        val reps = if (firstIncomplete != null) "${firstIncomplete.reps} REPS" else "${sets.lastOrNull()?.reps ?: 0} REPS"

        ExerciseCardData(
            exerciseName = group.exerciseName,
            muscleGroup = group.muscleGroup,
            orderIndex = group.orderIndex,
            sets = "$totalSetsForExercise SETS",
            reps = reps,
            restTime = "${uiState.restDuration}s",
            kcal = 8 * totalSetsForExercise,
            progress = progress,
            state = state,
            firstIncomplete = firstIncomplete,
            allCompleted = completedSets == totalSetsForExercise
        )
    }

    val currentActiveIdx = currentExercises.indexOfFirst { it.exerciseName == activeExerciseName }
    val nextExerciseName = currentExercises.getOrNull(currentActiveIdx + 1)?.exerciseName
        ?: currentExercises.firstOrNull { it.state == RingState.UPCOMING }?.exerciseName

    WorkoutTheme {
        Scaffold(
            topBar = {},
            bottomBar = {}
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F0F))
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        SessionHeader(
                            subtitle = "Day • ${session?.session?.routineName ?: "WORKOUT"}",
                            title = "Today's Burn",
                            streak = streak,
                            startedAt = session?.session?.startedAt ?: 0L
                        )
                        Spacer(Modifier.height(16.dp))
                        SessionProgressBar(
                            completed = completedCount,
                            total = totalCount
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    items(currentExercises, key = { "${it.exerciseName}_${it.orderIndex}" }) { data ->
                        WorkoutExerciseCard(
                            exerciseName = data.exerciseName,
                            muscleGroup = data.muscleGroup,
                            sets = data.sets,
                            reps = data.reps,
                            restTime = data.restTime,
                            kcal = data.kcal,
                            progress = data.progress,
                            state = data.state,
                            restSecondsRemaining = uiState.restSecondsRemaining,
                            onStartSet = {
                                activeExerciseName = data.exerciseName
                            },
                            onCompleteSet = {
                                if (data.firstIncomplete != null) {
                                    viewModel.updateSet(
                                        data.firstIncomplete,
                                        weight = null,
                                        reps = null,
                                        completed = true
                                    )
                                }
                            },
                            onSkip = {
                                if (data.firstIncomplete != null) {
                                    viewModel.updateSet(
                                        data.firstIncomplete,
                                        weight = null,
                                        reps = null,
                                        completed = null
                                    )
                                }
                            },
                            onRest = {
                                viewModel.setRestDuration(60)
                            }
                        )
                    }

                    item {
                        UpNextCard(nextExerciseName = nextExerciseName)
                        Spacer(Modifier.height(16.dp))
                    }

                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    BottomActionBar(
                        isPaused = isPaused,
                        onPause = viewModel::togglePause,
                        onFinish = { showCelebration = true }
                    )
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
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
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

                if (showCelebration) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCC000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        VaporizeCelebration(
                            text = "WORKOUT COMPLETE",
                            fontSize = 48.sp,
                            color = Amber,
                            spread = 8f,
                            density = 7f,
                            vaporizeDurationMs = 3000,
                            animate = animate,
                            onAnimationEnd = {
                                showCelebration = false
                                viewModel.finish(onFinish)
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class ExerciseCardData(
    val exerciseName: String,
    val muscleGroup: String,
    val orderIndex: Int,
    val sets: String,
    val reps: String,
    val restTime: String,
    val kcal: Int,
    val progress: Float,
    val state: RingState,
    val firstIncomplete: ExerciseSetEntity?,
    val allCompleted: Boolean
)

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
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Rest Timer",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatTime(secondsRemaining),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = if (secondsRemaining <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.inverseOnSurface
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 60, 90, 120).forEach { secs ->
                    TextButton(
                        onClick = { onDurationChange(secs) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (duration == secs) Amber.copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${secs}s",
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDismiss) {
                Text("Skip", color = MaterialTheme.colorScheme.inverseOnSurface)
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
