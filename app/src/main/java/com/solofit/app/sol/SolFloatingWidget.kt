package com.solofit.app.sol

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.domain.model.VoiceMode
import com.solofit.app.ui.theme.SolAccent

@Composable
fun SolFloatingWidget(
    state: SolUiState,
    onListen: () -> Unit,
    onStopSpeaking: () -> Unit,
    onVoiceModeChange: (VoiceMode) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.visible) return

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded && state.voiceMode == VoiceMode.AUTO_WHEN_OPENED) {
            onListen()
        }
    }

    Box(modifier = modifier) {
        if (expanded) {
            Box(
                Modifier.fillMaxWidth().height(600.dp)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { expanded = false }
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    // ── Header: Sol - Wellness Briefing ──
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(12.dp).clip(CircleShape).background(SolAccent)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Wellness Briefing", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.weight(1f))

                        if (state.isSpeaking) {
                            IconButton(onClick = onStopSpeaking) {
                                Box(
                                    Modifier.size(18.dp).clip(CircleShape).background(SolAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(Modifier.size(6.dp, 6.dp).background(Color.White))
                                }
                            }
                        } else {
                            IconButton(onClick = onListen) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = SolAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { expanded = false }) {
                            Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }

                    // ── Wellness State badge ──
                    state.userTwin?.let { twin ->
                        Spacer(Modifier.height(8.dp))
                        WellnessBadge(twin)
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Body: What did I notice? → Why does it matter? → What should you do next? ──
                    if (!state.hasSufficientData) {
                        Text(
                            state.headline.ifEmpty { "You're still building your wellness profile." },
                            fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.detail.ifEmpty { "Log your first workout or meal and I'll begin identifying trends." },
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp
                        )
                    } else {
                        // What did I notice?
                        BriefingSection("What did I notice?")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            state.headline,
                            fontSize = 14.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        // Why does it matter?
                        BriefingSection("Why does it matter?")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            state.detail,
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        // What should you do next?
                        BriefingSection("What should you do next?")
                        Spacer(Modifier.height(4.dp))
                        if (state.recommendations.isNotEmpty()) {
                            state.recommendations.forEach { rec ->
                                Text("\u2022 $rec", fontSize = 12.sp, color = SolAccent, lineHeight = 17.sp)
                            }
                        } else {
                            Text(
                                state.priorityAction.ifEmpty { "Stay consistent with your current routine." },
                                fontSize = 12.sp, color = SolAccent, lineHeight = 17.sp
                            )
                        }

                        // ── Risks (only if present) ──
                        state.userTwin?.let { twin ->
                            if (twin.risks.isNotEmpty()) {
                                Spacer(Modifier.height(14.dp))
                                BriefingSection("Things I'm watching")
                                Spacer(Modifier.height(4.dp))
                                twin.risks.forEach { risk ->
                                    Text("\u26A0 ${risk.signal}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, lineHeight = 16.sp)
                                }
                            }
                        }

                        // ── Live transcript when speaking ──
                        if (state.isSpeaking && state.transcript.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    state.transcript,
                                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Floating trigger (collapsed) ──
        if (!expanded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SolAccent)
                    .clickable { expanded = true },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                    Spacer(Modifier.height(2.dp))
                    Text("Sol", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BriefingSection(text: String) {
    Text(
        text,
        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun WellnessBadge(twin: UserTwin) {
    val (label, color) = when (twin.currentState) {
        WellnessState.THRIVING -> "Thriving" to Color(0xFF4CAF50)
        WellnessState.MAINTAINING -> "Maintaining" to Color(0xFF8BC34A)
        WellnessState.STRUGGLING -> "Struggling" to Color(0xFFFFC107)
        WellnessState.AT_RISK -> "At Risk" to Color(0xFFF44336)
        WellnessState.INSUFFICIENT_DATA -> "Building Profile" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        Spacer(Modifier.width(12.dp))
        Text("${twin.daysTracked} days tracked", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (twin.confidence != "Low") {
            Spacer(Modifier.width(8.dp))
            Text("Confidence: ${twin.confidence}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
