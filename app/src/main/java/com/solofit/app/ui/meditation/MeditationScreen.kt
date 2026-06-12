package com.solofit.app.ui.meditation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.theme.LavenderGrey
import com.solofit.app.ui.components.MeditationTheme
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import androidx.compose.material3.MaterialTheme

@Composable
fun MeditationScreen(
    onBack: () -> Unit = {},
    viewModel: MeditationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MeditationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BreathingCircle(
                            phase = state.phase,
                            elapsedSeconds = state.elapsedSeconds,
                            modifier = Modifier.size(280.dp)
                        )

                        Spacer(Modifier.height(24.dp))

                        Text(
                            when (state.phase) {
                                BreathingPhase.IDLE -> if (state.elapsedSeconds > 0) "Complete" else "Ready"
                                BreathingPhase.INHALE -> "Breathe In"
                                BreathingPhase.HOLD -> "Hold"
                                BreathingPhase.EXHALE -> "Breathe Out"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            if (state.isRunning) "${state.elapsedSeconds / 60}:${"%02d".format(state.elapsedSeconds % 60)}"
                            else "${state.targetMinutes}:00",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Light,
                            color = PrimaryText,
                            letterSpacing = (-1).sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (!state.isRunning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Choose duration", fontSize = 14.sp, color = SecondaryText)
                            Spacer(Modifier.height(12.dp))
                            DurationSelector(
                                minutes = state.targetMinutes,
                                onMinutesChange = viewModel::setTargetMinutes
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (state.isRunning) {
                        FilledTonalButton(
                            onClick = viewModel::toggleTimer,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = LavenderGrey, contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Filled.Stop, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Stop", fontWeight = FontWeight.Medium)
                        }
                    } else {
                        FilledTonalButton(
                            onClick = viewModel::toggleTimer,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = LavenderGrey, contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Start", fontWeight = FontWeight.Medium)
                        }
                    }
                    if (!state.isRunning && state.elapsedSeconds > 0) {
                        FilledTonalButton(
                            onClick = viewModel::reset,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Reset", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BreathingCircle(phase: BreathingPhase, elapsedSeconds: Int, modifier: Modifier = Modifier) {
    val targetScale = when (phase) {
        BreathingPhase.INHALE -> 1.25f
        BreathingPhase.HOLD -> 1.25f
        BreathingPhase.EXHALE -> 0.75f
        BreathingPhase.IDLE -> 1f
    }
    val duration = when (phase) {
        BreathingPhase.INHALE -> 4000
        BreathingPhase.HOLD -> 1000
        BreathingPhase.EXHALE -> 3000
        BreathingPhase.IDLE -> 300
    }

    val scale by animateFloatAsState(targetScale, tween(duration), label = "breath")
    val bgColor by animateColorAsState(
        when (phase) {
            BreathingPhase.INHALE -> LavenderGrey
            BreathingPhase.HOLD -> LavenderGrey
            BreathingPhase.EXHALE -> LavenderGrey.copy(alpha = 0.5f)
            BreathingPhase.IDLE -> LavenderGrey.copy(alpha = 0.2f)
        },
        tween(duration), label = "breathColor"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.fillMaxSize().scale(scale).clip(CircleShape).background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when (phase) {
                    BreathingPhase.IDLE -> "\u221E"
                    BreathingPhase.INHALE -> "\u2191"
                    BreathingPhase.HOLD -> "\u25CF"
                    BreathingPhase.EXHALE -> "\u2193"
                },
                fontSize = 44.sp, color = TextPrimary, fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun DurationSelector(minutes: Int, onMinutesChange: (Int) -> Unit) {
    val presets = listOf(5, 10, 15, 20)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        presets.forEach { preset ->
            val selected = minutes == preset
            FilledTonalButton(
                onClick = { onMinutesChange(preset) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (selected) LavenderGrey else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) TextPrimary else PrimaryText
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("${preset}m", fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}
