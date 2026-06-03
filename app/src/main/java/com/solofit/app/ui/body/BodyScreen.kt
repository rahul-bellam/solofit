package com.solofit.app.ui.body

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.solofit.app.ui.components.LineChart
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyScreen(
    onBack: () -> Unit,
    onStrength: () -> Unit = {},
    onPhotos: () -> Unit = {},
    viewModel: BodyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body & Recovery") },
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
            // ---- V-Taper hero ----
            VTaperCard(state)

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onStrength, modifier = Modifier.weight(1f)) {
                    Text("Strength trends")
                }
                OutlinedButton(onClick = onPhotos, modifier = Modifier.weight(1f)) {
                    Text("Progress photos")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Waist trend (the metric that matters more than scale) ----
            Text("Waist trend", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    val waistSeries = viewModel.series { it.waistCm }
                    if (waistSeries.size < 2) {
                        Text(
                            "Log your waist on 2+ days to see the trend.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LineChart(values = waistSeries)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Measurement entry (biweekly) ----
            Text("Log measurements (cm)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            MeasurementForm(
                latest = state.latest,
                onSave = viewModel::saveMeasurement
            )

            Spacer(Modifier.height(16.dp))

            // ---- Today's recovery inputs ----
            Text("Today's check-in", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            MetricForm(
                metric = state.todayMetric,
                onSave = viewModel::saveMetric
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VTaperCard(state: BodyState) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("V-Taper Score", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    state.vTaper?.let { String.format("%.2f", it) } ?: "—",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    state.vTaperLabel,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Text(
                "Shoulders ÷ Waist  ·  golden target ≈ 1.62",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.waistDeltaCm?.let { d ->
                Spacer(Modifier.height(8.dp))
                val shrinking = d <= 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (shrinking) Icons.AutoMirrored.Filled.TrendingDown
                        else Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = if (shrinking) "Waist decreasing" else "Waist increasing"
                    )
                    Text(
                        "  Waist ${if (shrinking) "down" else "up"} " +
                            "${String.format("%.1f", abs(d))} cm since last entry",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun MeasurementForm(
    latest: com.solofit.app.data.local.entity.BodyMeasurementEntity?,
    onSave: (Double?, Double?, Double?, Double?, Double?, Double?) -> Unit
) {
    var waist by remember(latest) { mutableStateOf(latest?.waistCm?.numText() ?: "") }
    var chest by remember(latest) { mutableStateOf(latest?.chestCm?.numText() ?: "") }
    var shoulders by remember(latest) { mutableStateOf(latest?.shouldersCm?.numText() ?: "") }
    var arms by remember(latest) { mutableStateOf(latest?.armsCm?.numText() ?: "") }
    var thighs by remember(latest) { mutableStateOf(latest?.thighsCm?.numText() ?: "") }
    var neck by remember(latest) { mutableStateOf(latest?.neckCm?.numText() ?: "") }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumField("Waist", waist, { waist = it }, Modifier.weight(1f))
                NumField("Shoulders", shoulders, { shoulders = it }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumField("Chest", chest, { chest = it }, Modifier.weight(1f))
                NumField("Arms", arms, { arms = it }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumField("Thighs", thighs, { thighs = it }, Modifier.weight(1f))
                NumField("Neck", neck, { neck = it }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    onSave(
                        waist.toDoubleOrNull(), chest.toDoubleOrNull(), shoulders.toDoubleOrNull(),
                        arms.toDoubleOrNull(), thighs.toDoubleOrNull(), neck.toDoubleOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save today's measurements") }
        }
    }
}

@Composable
private fun MetricForm(
    metric: com.solofit.app.data.local.entity.DailyMetricEntity?,
    onSave: (Double?, Int?, Int?, Int?) -> Unit
) {
    var sleep by remember(metric) { mutableStateOf(metric?.sleepHours?.numText() ?: "") }
    var steps by remember(metric) { mutableStateOf(metric?.steps?.toString() ?: "") }
    var mood by remember(metric) { mutableStateOf(metric?.moodScore?.toString() ?: "") }
    var energy by remember(metric) { mutableStateOf(metric?.energyScore?.toString() ?: "") }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumField("Sleep (h)", sleep, { sleep = it }, Modifier.weight(1f))
                NumField("Steps", steps, { steps = it }, Modifier.weight(1f), integer = true)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumField("Mood 1-10", mood, { mood = it }, Modifier.weight(1f), integer = true)
                NumField("Energy 1-10", energy, { energy = it }, Modifier.weight(1f), integer = true)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    onSave(
                        sleep.toDoubleOrNull(),
                        steps.toIntOrNull(),
                        mood.toIntOrNull()?.coerceIn(1, 10),
                        energy.toIntOrNull()?.coerceIn(1, 10)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save check-in") }
        }
    }
}

@Composable
private fun NumField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    integer: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val clean = if (integer) input.filter { it.isDigit() }
            else input.filterIndexed { i, c -> c.isDigit() || (c == '.' && !input.substring(0, i).contains('.')) }
            onChange(clean)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (integer) KeyboardType.Number else KeyboardType.Decimal
        ),
        modifier = modifier
    )
}

private fun Double.numText(): String =
    if (this % 1.0 == 0.0) this.roundToInt().toString() else this.toString()

