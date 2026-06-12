package com.solofit.app.ui.bodyrecomp

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.components.LineChart
import com.solofit.app.ui.theme.BodyRecompAccent
import com.solofit.app.ui.theme.HighGreen
import com.solofit.app.ui.theme.LowRed
import com.solofit.app.ui.theme.MidAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyRecompScreen(
    onBack: () -> Unit,
    viewModel: BodyRecompViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Recomposition", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ── Hero: Latest Readings ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(24.dp).fillMaxWidth()) {
                    RecompStatusHeader(state)

                    Spacer(Modifier.height(20.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        MetricBlock(
                            label = "Weight",
                            value = state.latestWeight?.let { "%.1f kg".format(it) } ?: "—",
                            trend = state.weightTrend
                        )
                        MetricBlock(
                            label = "Waist",
                            value = state.latestWaist?.let { "%.1f cm".format(it) } ?: "—",
                            trend = state.waistTrend
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    val statusColor = when (state.recompStatus) {
                        RecompStatus.EXCELLENT -> HighGreen
                        RecompStatus.GOOD -> BodyRecompAccent
                        RecompStatus.MUSCLE_LOSS_RISK, RecompStatus.WEIGHT_GAIN_RISK -> LowRed
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        state.feedbackMessage,
                        fontSize = 13.sp,
                        color = statusColor,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Changes ──
            if (state.weightChange4w != null || state.waistChange4w != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("4-Week Change", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ChangeBlock("Weight", state.weightChange4w, "kg")
                            ChangeBlock("Waist", state.waistChange4w, "cm")
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Weight Chart ──
            if (state.weightEntries.size >= 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Weight Trend", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        LineChart(
                            values = state.weightEntries.map { it.weightKg.toFloat() },
                            height = 140.dp
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Quick Log ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Quick Log", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.weightInput,
                            onValueChange = viewModel::updateWeightInput,
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = viewModel::saveWeight,
                            enabled = state.weightInput.toDoubleOrNull() != null,
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Log") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.waistInput,
                            onValueChange = viewModel::updateWaistInput,
                            label = { Text("Waist (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = viewModel::saveWaist,
                            enabled = state.waistInput.toDoubleOrNull() != null,
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Log") }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RecompStatusHeader(state: BodyRecompState) {
    val (label, color) = when (state.recompStatus) {
        RecompStatus.EXCELLENT -> "Excellent Recomposition" to HighGreen
        RecompStatus.GOOD -> "Positive Trend" to BodyRecompAccent
        RecompStatus.WATCH_CALORIES -> "Stable" to MidAmber
        RecompStatus.WATCH_RATE -> "Watch Rate" to MidAmber
        RecompStatus.MUSCLE_LOSS_RISK -> "Watch Rate" to LowRed
        RecompStatus.WEIGHT_GAIN_RISK -> "Watch Rate" to LowRed
        RecompStatus.INSUFFICIENT_DATA -> "Tracking" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MetricBlock(label: String, value: String, trend: TrendDirection) {
    val arrow = when (trend) {
        TrendDirection.UP -> "\u2191"
        TrendDirection.DOWN -> "\u2193"
        TrendDirection.STABLE -> "\u2192"
    }
    val trendColor = when (trend) {
        TrendDirection.UP -> if (label == "Waist") LowRed else HighGreen
        TrendDirection.DOWN -> if (label == "Waist") HighGreen else LowRed
        TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Text(arrow, fontSize = 14.sp, color = trendColor)
        }
    }
}

@Composable
private fun ChangeBlock(label: String, value: Double?, unit: String) {
    val prefix = if (value != null && value > 0) "+" else ""
    val color = when {
        value == null -> MaterialTheme.colorScheme.onSurfaceVariant
        label == "Weight" && value < -2.0 -> LowRed
        label == "Weight" && value > 2.0 -> LowRed
        label == "Waist" && value < -0.5 -> HighGreen
        label == "Waist" && value > 0.5 -> LowRed
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${prefix}${value?.let { "%.1f".format(it) } ?: "\u2014"} $unit",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color
        )
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
