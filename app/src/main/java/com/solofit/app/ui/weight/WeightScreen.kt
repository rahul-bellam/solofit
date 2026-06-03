package com.solofit.app.ui.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.solofit.app.core.DateUtils
import com.solofit.app.ui.components.EmptyState
import com.solofit.app.ui.components.LineChart
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    onBack: () -> Unit,
    viewModel: WeightViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight Monitor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---- Quick log ----
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Log today's weight", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { v ->
                                input = v.filterIndexed { i, c ->
                                    c.isDigit() || (c == '.' && !v.substring(0, i).contains('.'))
                                }
                            },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.height(0.dp))
                        Button(
                            onClick = {
                                input.toDoubleOrNull()?.let {
                                    viewModel.logWeight(it)
                                    input = ""
                                }
                            },
                            enabled = input.toDoubleOrNull() != null,
                            modifier = Modifier.padding(start = 12.dp)
                        ) { Text("Save") }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.entries.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.MonitorWeight,
                    title = "No weigh-ins yet",
                    message = "Log your weight regularly to see your trend and whether you're on track for your goal.",
                    modifier = Modifier.padding(top = 24.dp)
                )
                Spacer(Modifier.height(24.dp))
            } else {
                // ---- Summary ----
                SummaryCard(state)

                Spacer(Modifier.height(16.dp))
                Text("Trend", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        LineChart(values = state.entries.map { it.weightKg.toFloat() })
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("History", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                state.entries.reversed().forEach { e ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(DateUtils.prettyMedium(e.date))
                        Text("${e.weightKg} kg", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(state: WeightState) {
    val change = state.changeKg
    val onTrack = state.onTrack
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (onTrack) {
                true -> MaterialTheme.colorScheme.primaryContainer
                false -> MaterialTheme.colorScheme.errorContainer
                null -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when {
                change == null -> Icons.AutoMirrored.Filled.TrendingFlat
                change < 0 -> Icons.AutoMirrored.Filled.TrendingDown
                change > 0 -> Icons.AutoMirrored.Filled.TrendingUp
                else -> Icons.AutoMirrored.Filled.TrendingFlat
            }
            Icon(icon, contentDescription = "Weight trend")
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Latest: ${state.latestWeight ?: "—"} kg", fontWeight = FontWeight.Bold)
                Text(
                    when {
                        change == null -> "Goal: ${state.goal.displayName}"
                        else -> "${if (change >= 0) "+" else "-"}${"%.1f".format(abs(change))} kg since start · ${state.goal.displayName}"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (onTrack != null) {
                Text(
                    if (onTrack) "On track" else "Off track",
                    fontWeight = FontWeight.SemiBold,
                    color = if (onTrack) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
