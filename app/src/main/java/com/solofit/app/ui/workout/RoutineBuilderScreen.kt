package com.solofit.app.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.solofit.app.ui.components.WorkoutTheme

private val equipmentOptions = listOf("All", "Barbell", "Dumbbell", "Machine", "Cable", "Bodyweight")
private val difficultyOptions = listOf("All", "Beginner", "Intermediate", "Advanced")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoutineBuilderScreen(
    onDone: () -> Unit,
    viewModel: RoutineBuilderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.showTemplates) {
        TemplatesDialog(
            templates = state.templates,
            onSelect = viewModel::loadTemplate,
            onDismiss = viewModel::onDismissTemplates
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbar.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    WorkoutTheme {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.routineId == null) "New Routine" else "Edit Routine") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save(onDone) },
                        enabled = state.canSave
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onName,
                    label = { Text("Routine name (e.g. Strength Day)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotes,
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                TextButton(onClick = viewModel::onToggleTemplates) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Templates")
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Selected: ${state.selected.size} exercises",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                Text("Muscle Group", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.activeGroup == null,
                        onClick = { viewModel.onGroupFilter(null) },
                        label = { Text("All") }
                    )
                    state.muscleGroups.forEach { group ->
                        FilterChip(
                            selected = state.activeGroup == group,
                            onClick = { viewModel.onGroupFilter(group) },
                            label = { Text(group) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                Text("Equipment", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    equipmentOptions.forEach { eq ->
                        FilterChip(
                            selected = state.selectedEquipment == null && eq == "All" || state.selectedEquipment == eq,
                            onClick = {
                                viewModel.onEquipmentFilter(if (eq == "All") null else eq)
                            },
                            label = { Text(eq) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                Text("Difficulty", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficultyOptions.forEach { diff ->
                        FilterChip(
                            selected = state.selectedDifficulty == null && diff == "All" || state.selectedDifficulty == diff,
                            onClick = {
                                viewModel.onDifficultyFilter(if (diff == "All") null else diff)
                            },
                            label = { Text(diff) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            items(state.filteredCatalog, key = { it.name }) { template ->
                val selected = state.selected.any { it.name == template.name }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleExercise(template) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(template.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                template.muscleGroup,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(template.equipment, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.FitnessCenter,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(),
                                    border = null
                                )
                                AssistChip(
                                    onClick = {},
                                    label = { Text(template.difficulty, style = MaterialTheme.typography.labelSmall) },
                                    colors = AssistChipDefaults.assistChipColors(),
                                    border = null
                                )
                            }
                            if (template.cue.isNotBlank()) {
                                Text(
                                    template.cue,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        if (selected) Icon(Icons.Filled.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
    } // WorkoutTheme
}

@Composable
private fun TemplatesDialog(
    templates: List<com.solofit.app.data.local.seed.WorkoutTemplate>,
    onSelect: (com.solofit.app.data.local.seed.WorkoutTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workout Templates") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(templates) { template ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(template) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(template.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${template.daysPerWeek} days/week - ${template.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${template.exercises.size} exercises",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
