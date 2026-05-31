package com.solofit.app.ui.phase

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.core.DateUtils
import com.solofit.app.domain.model.TrainingGoal
import com.solofit.app.ui.components.ChipSelector
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPhaseScreen(
    onDone: () -> Unit,
    viewModel: EditPhaseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Phase") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                "Name your current training phase and how long you're committing to it. " +
                    "The dashboard counts the days for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onName,
                label = { Text("Phase name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            // Quick presets
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Foundation Recomp", "Cut", "Lean Bulk", "Maintenance").forEach { preset ->
                    AssistChip(onClick = { viewModel.onName(preset) }, label = { Text(preset) })
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.targetDays,
                onValueChange = viewModel::onTargetDays,
                label = { Text("Target length (days)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("90" to "12 wk", "180" to "6 mo", "365" to "1 yr").forEach { (d, lbl) ->
                    AssistChip(onClick = { viewModel.onTargetDays(d) }, label = { Text("$lbl ($d)") })
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Start date", style = MaterialTheme.typography.labelLarge)
                        Text(
                            DateUtils.prettyMedium(state.startDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AssistChip(
                        onClick = {
                            val d = runCatching { LocalDate.parse(state.startDate) }
                                .getOrDefault(LocalDate.now())
                            DatePickerDialog(
                                context,
                                { _, y, m, day ->
                                    viewModel.onStartDate(
                                        LocalDate.of(y, m + 1, day).toString()
                                    )
                                },
                                d.year, d.monthValue - 1, d.dayOfMonth
                            ).show()
                        },
                        label = { Text("Change") }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Training goal", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Tunes your Transformation Score's emphasis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ChipSelector(
                options = TrainingGoal.entries,
                selected = state.goal,
                label = { it.displayName },
                onSelect = viewModel::onGoal
            )
            Text(
                state.goal.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.save(onDone) },
                enabled = state.isValid,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save phase") }
        }
    }
}
