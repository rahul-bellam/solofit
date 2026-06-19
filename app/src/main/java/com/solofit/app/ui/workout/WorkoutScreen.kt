package com.solofit.app.ui.workout

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.components.EmptyState
import com.solofit.app.ui.components.WellnessDarkCard
import com.solofit.app.ui.components.WellnessStaticCard
import com.solofit.app.ui.components.WorkoutTheme
import com.solofit.app.ui.theme.WorkoutAccent
import com.solofit.app.ui.theme.DarkBg
import com.solofit.app.ui.theme.DarkText
import com.solofit.app.ui.theme.DarkTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onCreateRoutine: () -> Unit,
    onEditRoutine: (Long) -> Unit,
    onStartSession: (Long) -> Unit,
    onOpenWeeklyPlanner: () -> Unit = {},
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val loaded by viewModel.routinesLoaded.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf<Long?>(null) }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
    WorkoutTheme {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Training", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Build routines and track progressive overload", fontSize = 14.sp, color = DarkTextSecondary)
                Spacer(Modifier.height(20.dp))
            }

            // ── HERO: Today's Workout ──
            item {
                WellnessDarkCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.size(72.dp).clip(CircleShape).background(WorkoutAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.FitnessCenter, null, tint = WorkoutAccent, modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (routines.isNotEmpty()) "${routines.size} Routine${if (routines.size != 1) "s" else ""}"
                            else "No Routines Yet",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (routines.isNotEmpty()) "Ready to train? Pick a routine below."
                            else "Create your first routine to get started.",
                            fontSize = 14.sp,
                            color = DarkTextSecondary
                        )
                        Spacer(Modifier.height(20.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledTonalButton(
                                onClick = onCreateRoutine,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = WorkoutAccent, contentColor = Color.White)
                            ) {
                                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("New Routine", fontWeight = FontWeight.Medium)
                            }
                            FilledTonalButton(
                                onClick = onOpenWeeklyPlanner,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Weekly Plan", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            if (routines.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.FitnessCenter,
                        title = "No routines yet",
                        message = "Create a routine like \"Strength Day\" to start tracking your sets and progress.",
                        actionLabel = "Create your first routine",
                        onAction = onCreateRoutine,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            // ── MEDIUM: Routines List ──
            if (routines.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Your Routines", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${routines.size} routine${if (routines.size != 1) "s" else ""} saved",
                        fontSize = 13.sp,
                        color = WorkoutAccent
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            items(routines, key = { it.routine.id }) { item ->
                WellnessDarkCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.routine.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = DarkText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${item.exercises.size} exercise${if (item.exercises.size != 1) "s" else ""}",
                                    fontSize = 14.sp,
                                    color = DarkTextSecondary
                                )
                            }
                            Row {
                                IconButton(onClick = { onEditRoutine(item.routine.id) }) {
                                    Icon(Icons.Filled.Edit, "Edit", tint = DarkTextSecondary)
                                }
                                IconButton(onClick = { showDeleteConfirm = item.routine.id }) {
                                    Icon(Icons.Filled.Delete, "Delete", tint = DarkTextSecondary)
                                }
                            }
                        }
                        if (item.exercises.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            item.exercises.sortedBy { it.orderIndex }.forEach { ex ->
                                val muscle = ex.muscleGroup
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(32.dp).clip(CircleShape).background(WorkoutAccent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.FitnessCenter, null, tint = WorkoutAccent, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(ex.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                                        Text(
                                            muscle,
                                            fontSize = 12.sp,
                                            color = DarkTextSecondary
                                        )
                                    }
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(WorkoutAccent.copy(alpha = 0.3f)))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            FilledTonalButton(
                                onClick = { viewModel.startSession(item, onStartSession) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = WorkoutAccent, contentColor = Color.White)
                            ) {
                                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Start Workout", fontWeight = FontWeight.Medium)
                            }
                        } else {
                            Spacer(Modifier.height(12.dp))
                            Text("No exercises added yet. Tap Edit to add exercises.", fontSize = 13.sp, color = DarkTextSecondary)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
    }

    showDeleteConfirm?.let { routineId ->
        val routine = routines.find { it.routine.id == routineId }
        if (routine != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title = { Text("Delete Routine") },
                text = { Text("Delete \"${routine.routine.name}\"? This cannot be undone.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { viewModel.deleteRoutine(routine.routine); showDeleteConfirm = null }) {
                        Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    }
    }
}
