package com.solofit.app.ui.reminders

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.domain.model.ReminderSettings
import com.solofit.app.domain.model.asClockString
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var notificationsAllowed by remember { mutableStateOf(hasNotificationPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notificationsAllowed = granted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
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
                "Gentle, silent nudges — they appear in your notification shade with no sound or vibration.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            if (!notificationsAllowed) {
                PermissionCard(
                    onGrant = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openAppNotificationSettings(context)
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
            }

            // ---- Hydration ----
            SettingCard {
                ToggleRow(
                    title = "Hydration reminders",
                    subtitle = if (settings.hydrationEnabled)
                        "Every ${settings.hydrationIntervalMinutes} min during active hours"
                    else "Off",
                    checked = settings.hydrationEnabled,
                    onCheckedChange = viewModel::setHydrationEnabled
                )
                if (settings.hydrationEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Text("Frequency", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReminderSettings.HYDRATION_INTERVALS.forEach { (label, minutes) ->
                            FilterChip(
                                selected = settings.hydrationIntervalMinutes == minutes,
                                onClick = { viewModel.setHydrationInterval(minutes) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Workout ----
            SettingCard {
                ToggleRow(
                    title = "Daily workout reminder",
                    subtitle = if (settings.workoutEnabled)
                        "At ${settings.workoutTimeMinutes.asClockString()} (skipped if you've trained)"
                    else "Off",
                    checked = settings.workoutEnabled,
                    onCheckedChange = viewModel::setWorkoutEnabled
                )
                if (settings.workoutEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Time", Modifier.weight(1f))
                        AssistChip(
                            onClick = {
                                showTimePicker(context, settings.workoutTimeMinutes) { mins ->
                                    viewModel.setWorkoutTime(mins)
                                }
                            },
                            label = { Text(settings.workoutTimeMinutes.asClockString()) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Morning goals ----
            SettingCard {
                ToggleRow(
                    title = "Morning goals prompt",
                    subtitle = if (settings.morningGoalsEnabled)
                        "At ${settings.morningGoalsTimeMinutes.asClockString()} — plan your day"
                    else "Off",
                    checked = settings.morningGoalsEnabled,
                    onCheckedChange = viewModel::setMorningGoalsEnabled
                )
                if (settings.morningGoalsEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Time", Modifier.weight(1f))
                        AssistChip(
                            onClick = {
                                showTimePicker(context, settings.morningGoalsTimeMinutes) { mins ->
                                    viewModel.setMorningGoalsTime(mins)
                                }
                            },
                            label = { Text(settings.morningGoalsTimeMinutes.asClockString()) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Evening gratitude ----
            SettingCard {
                ToggleRow(
                    title = "Evening gratitude prompt",
                    subtitle = if (settings.eveningGratitudeEnabled)
                        "At ${settings.eveningGratitudeTimeMinutes.asClockString()} — 2-min reflection"
                    else "Off",
                    checked = settings.eveningGratitudeEnabled,
                    onCheckedChange = viewModel::setEveningGratitudeEnabled
                )
                if (settings.eveningGratitudeEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Time", Modifier.weight(1f))
                        AssistChip(
                            onClick = {
                                showTimePicker(context, settings.eveningGratitudeTimeMinutes) { mins ->
                                    viewModel.setEveningGratitudeTime(mins)
                                }
                            },
                            label = { Text(settings.eveningGratitudeTimeMinutes.asClockString()) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Quiet hours ----
            SettingCard {
                Text("Quiet hours", fontWeight = FontWeight.SemiBold)
                Text(
                    "No reminders between these times.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("From", Modifier.weight(1f))
                    AssistChip(
                        onClick = {
                            showTimePicker(context, settings.quietStartMinutes) { mins ->
                                viewModel.setQuietHours(mins, settings.quietEndMinutes)
                            }
                        },
                        label = { Text(settings.quietStartMinutes.asClockString()) }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("To", Modifier.weight(1f))
                    AssistChip(
                        onClick = {
                            showTimePicker(context, settings.quietEndMinutes) { mins ->
                                viewModel.setQuietHours(settings.quietStartMinutes, mins)
                            }
                        },
                        label = { Text(settings.quietEndMinutes.asClockString()) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionCard(onGrant: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.NotificationsOff, contentDescription = null)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Notifications are off", fontWeight = FontWeight.SemiBold)
                Text(
                    "Allow notifications so reminders can reach you (they'll still be silent).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onGrant) { Text("Allow") }
        }
    }
}

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun hasNotificationPermission(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else true

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun showTimePicker(context: Context, initialMinutes: Int, onPicked: (Int) -> Unit) {
    val hour = initialMinutes / 60
    val minute = initialMinutes % 60
    TimePickerDialog(
        context,
        { _, h, m -> onPicked(h * 60 + m) },
        hour, minute, true
    ).show()
}
