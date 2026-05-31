package com.solofit.app.ui.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solofit.app.core.perf.PerfTrace

/**
 * Lightweight developer screen that surfaces PerfTrace p50/p95/max for the hot
 * paths we instrument (e.g. barcode.lookup, food.warmUp, db.maintenance).
 * Only meaningful in debug builds (PerfTrace is a no-op in release).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfScreen(onBack: () -> Unit) {
    val labels = listOf(
        "barcode.lookup",
        "food.warmUp",
        "db.maintenance",
        "db.search"
    )
    var refreshTick by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance") },
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
            Text(
                "Rolling latency (ms). Debug builds only — recorded for instrumented hot paths.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            // refreshTick is read so recomposition re-pulls fresh summaries.
            @Suppress("UNUSED_EXPRESSION") refreshTick
            labels.forEach { label ->
                val summary = PerfTrace.summary(label)
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(label, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        if (summary == null) {
                            Text(
                                "no samples yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val (p50, p95, max) = summary
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("p50: ${p50}ms")
                                Text("p95: ${p95}ms")
                                Text("max: ${max}ms")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = { refreshTick++ }, modifier = Modifier.fillMaxWidth()) {
                Text("Refresh")
            }
        }
    }
}
