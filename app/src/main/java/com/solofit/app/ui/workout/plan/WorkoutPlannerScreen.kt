package com.solofit.app.ui.workout.plan

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.components.WorkoutTheme

private val DAY_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlannerScreen(
    onBack: () -> Unit,
    viewModel: WorkoutPlannerViewModel = hiltViewModel()
) {
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val exercises by viewModel.planExercises.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    WorkoutTheme {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Workout Plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Text("Select a day to plan:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DAY_NAMES.forEachIndexed { i, name ->
                        FilterChip(
                            selected = selectedDay == i + 1,
                            onClick = { viewModel.selectDay(i + 1) },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            if (selectedDay != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                    PlanNameEditor(
                        dayName = DAY_NAMES[selectedDay!! - 1],
                        onSave = viewModel::savePlan
                    )
                }

                if (exercises.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("Exercises", style = MaterialTheme.typography.titleMedium)
                    }
                    items(exercises, key = { it.id }) { ex ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(ex.exerciseName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${ex.sets}×${ex.reps} @ ${ex.weight} ${ex.weightUnit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteExercise(ex) }) {
                                    Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, null)
                        Text("  Add exercise")
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
    } // WorkoutTheme

    if (showAddDialog && selectedDay != null) {
        AddExerciseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, sets, reps, weight, unit ->
                viewModel.addExercise(name, sets, reps, weight, unit)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PlanNameEditor(dayName: String, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("$dayName plan name (e.g. Push Day)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onSave(name) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Plan")
        }
    }
}

@Composable
private fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, sets: Int, reps: Int, weight: Double, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }
    var weight by remember { mutableStateOf("0") }
    var useKg by remember { mutableStateOf(true) }

    val setsVal = sets.toIntOrNull() ?: 0
    val repsVal = reps.toIntOrNull() ?: 0
    val weightVal = weight.toDoubleOrNull() ?: 0.0
    val valid = name.isNotBlank() && setsVal > 0 && repsVal > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it.filter { c -> c.isDigit() } },
                        label = { Text("Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it.filter { c -> c.isDigit() } },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { w ->
                            weight = w.filterIndexed { i, c ->
                                c.isDigit() || (c == '.' && !w.substring(0, i).contains('.'))
                            }
                        },
                        label = { Text("Weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = useKg,
                        onClick = { useKg = !useKg },
                        label = { Text(if (useKg) "kg" else "lbs") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, setsVal, repsVal, weightVal, if (useKg) "kg" else "lbs") },
                enabled = valid
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
