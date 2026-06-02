package com.solofit.app.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import kotlin.math.roundToInt
import com.solofit.app.domain.model.ThemeMode
import com.solofit.app.ui.components.CalorieRing
import com.solofit.app.ui.components.MacroBar
import com.solofit.app.ui.components.WaterGlass
import com.solofit.app.ui.components.rememberAnimationsActive
import com.solofit.app.ui.theme.CarbsColor
import com.solofit.app.ui.theme.FatsColor
import com.solofit.app.ui.theme.ProteinColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onReminders: () -> Unit = {},
    onWeight: () -> Unit = {},
    onBody: () -> Unit = {},
    onJournal: () -> Unit = {},
    onPerf: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val animationsEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val waterGoalMl by viewModel.waterGoalMl.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            // ---- Profile summary ----
            SectionTitle("Profile")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditProfile)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            profile?.name?.ifBlank { "Your profile" } ?: "Set up profile",
                            fontWeight = FontWeight.SemiBold
                        )
                        profile?.let {
                            Text(
                                "${it.weightKg.toInt()} kg · target ${it.targetCalories} kcal · ${it.goal.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text("Edit", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- Tracking & reminders ----
            SectionTitle("Tracking")
            NavRow(
                icon = Icons.Filled.Notifications,
                title = "Reminders",
                subtitle = "Silent hydration & workout nudges",
                onClick = onReminders
            )
            Spacer(Modifier.height(8.dp))
            NavRow(
                icon = Icons.Filled.MonitorWeight,
                title = "Weight monitor",
                subtitle = "Log weigh-ins and track your trend",
                onClick = onWeight
            )
            Spacer(Modifier.height(8.dp))
            NavRow(
                icon = Icons.Filled.Straighten,
                title = "Body & recovery",
                subtitle = "Measurements, V-Taper score, check-ins",
                onClick = onBody
            )
            Spacer(Modifier.height(8.dp))
            NavRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Journal",
                subtitle = "Morning goals & evening gratitude",
                onClick = onJournal
            )
            if (com.solofit.app.BuildConfig.DEBUG) {
                Spacer(Modifier.height(8.dp))
                NavRow(
                    icon = Icons.Filled.Speed,
                    title = "Performance (debug)",
                    subtitle = "Latency p50/p95 for hot paths",
                    onClick = onPerf
                )
            }

            Spacer(Modifier.height(20.dp))

            // ---- Water goal ----
            SectionTitle("Hydration")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily water goal", fontWeight = FontWeight.SemiBold)
                        Text(
                            "$waterGoalMl ml",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Slider(
                        value = waterGoalMl.toFloat(),
                        onValueChange = { viewModel.setWaterGoalMl(it.roundToInt()) },
                        valueRange = 500f..6000f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(1000, 2000, 3000, 4000).forEach { preset ->
                            androidx.compose.material3.TextButton(
                                onClick = { viewModel.setWaterGoalMl(preset) },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("${preset / 1000}L", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- Theme ----
            SectionTitle("Appearance")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == themeMode,
                                onClick = { viewModel.setThemeMode(mode) }
                            )
                            Text(mode.displayName, Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setAnimationsEnabled(!animationsEnabled) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Playful animations", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Liquid fills, dumbbell check-offs & scroll-to-press. " +
                                    "Turn off for a calm, still UI.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = animationsEnabled,
                            onCheckedChange = { viewModel.setAnimationsEnabled(it) }
                        )
                    }
                    AnimationPreview(animate = rememberAnimationsActive(animationsEnabled))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- Privacy ----
            SectionTitle("Privacy")
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                        Text(
                            "  Your data stays on device",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "All profile, nutrition and workout data is stored locally in an on-device " +
                            "database. The internet is used only for barcode lookups (Open Food " +
                            "Facts, HTTPS-only). Meal photos are classified on-device and never uploaded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("About")
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SoloFit", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Version 1.1.0 · Local Edition",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Live, self-contained preview of the liquid components — lets users see the effect
 * before toggling. Uses fixed demo values wrapped in remember (no DB/flow reads), and
 * the infinite animations only run while this Settings screen is on screen.
 */
@Composable
private fun AnimationPreview(animate: Boolean) {
    // Static demo data — remembered so toggling doesn't reallocate.
    val proteinColor = ProteinColor
    val carbsColor = CarbsColor
    val fatsColor = FatsColor

    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Text(
            "Preview",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CalorieRing(
                consumed = 1500,
                target = 2200,
                size = 84.dp,
                strokeWidth = 9.dp,
                animate = animate,
                modifier = Modifier.size(84.dp)
            )
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f)) {
                MacroBar("Protein", 90, 160, proteinColor, animate = animate)
                Spacer(Modifier.height(8.dp))
                MacroBar("Carbs", 180, 250, carbsColor, animate = animate)
                Spacer(Modifier.height(8.dp))
                MacroBar("Fats", 40, 70, fatsColor, animate = animate)
            }
            Spacer(Modifier.size(16.dp))
            WaterGlass(
                fraction = 0.65f,
                animate = animate,
                modifier = Modifier.size(width = 34.dp, height = 56.dp)
            )
        }
    }
}

@Composable
private fun NavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}
