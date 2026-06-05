package com.solofit.app.sol

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import com.solofit.app.ui.theme.CardCream
import com.solofit.app.ui.theme.DarkSuccess
import com.solofit.app.ui.theme.DarkWarning
import com.solofit.app.ui.theme.DarkError
import com.solofit.app.ui.theme.ProteinColor
import com.solofit.app.ui.theme.WalkingAccent
import com.solofit.app.ui.theme.RecoveryAccent

@Composable
fun SolCard(
    state: SolUiState,
    onToggleWhy: () -> Unit,
    onToggleWhat: () -> Unit,
    onListen: () -> Unit,
    onPersonalityChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.visible) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {

            // ── SOL header (amber only on glow + icon) ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Amber.copy(alpha = 0.25f), Amber.copy(alpha = 0.05f)),
                                radius = 24f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(14.dp).clip(CircleShape).background(Amber))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("SOL", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    Text(state.personality.displayName, fontSize = 11.sp, color = SecondaryText)
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp, null,
                    tint = if (state.isSpeaking) Amber else SecondaryText,
                    modifier = Modifier.size(20.dp).clickable(onClick = onListen)
                )
            }

            Spacer(Modifier.height(16.dp))

            if (!state.hasSufficientData) {
                // ── Empty State ──
                Text(
                    state.headline.ifEmpty { "You're still building your wellness profile." },
                    fontSize = 16.sp, fontWeight = FontWeight.Medium, color = PrimaryText
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    state.detail.ifEmpty { "Complete a few days of tracking and I'll begin identifying trends." },
                    fontSize = 13.sp, color = SecondaryText, lineHeight = 18.sp
                )
                return@Column
            }

            // ── Greeting with name ──
            val namePart = if (state.userName.isNotBlank()) " ${state.userName}" else ""
            Text(
                "${state.greeting.trimEnd('.')}$namePart.",
                fontSize = 20.sp, fontWeight = FontWeight.Medium, color = PrimaryText
            )

            Spacer(Modifier.height(2.dp))

            // Rotating header (no amber)
            Text(
                state.briefingHeader,
                fontSize = 13.sp, color = SecondaryText, fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            // Primary headline + detail
            val headlineColor = when {
                state.type == InsightType.OVERTRAINING -> DarkError
                state.dayLabel == DayLabel.PERFORMANCE -> DarkSuccess
                else -> PrimaryText
            }
            if (state.headline.isNotBlank()) {
                Text(state.headline, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = headlineColor)
                Spacer(Modifier.height(4.dp))
            }
            Text(state.detail, fontSize = 13.sp, color = SecondaryText, lineHeight = 18.sp)

            // Supplementary insights
            if (state.supplementaryHeadlines.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                state.supplementaryHeadlines.forEach { sh ->
                    Text("• $sh", fontSize = 12.sp, color = SecondaryText, lineHeight = 16.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Signal summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.signals.forEach { signal ->
                    val chipColor = when (signal.status) {
                        SignalStatus.GOOD -> DarkSuccess
                        SignalStatus.ON_TRACK -> DarkWarning
                        SignalStatus.LOW -> DarkError
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(chipColor.copy(alpha = 0.08f))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(signal.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                        Text(signal.detail, fontSize = 10.sp, color = SecondaryText)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Day label with new types
            val labelColor = when (state.dayLabel) {
                DayLabel.PERFORMANCE -> DarkSuccess
                DayLabel.RECOVERY_FOCUS -> DarkWarning
                DayLabel.NUTRITION_FOCUS -> ProteinColor
                DayLabel.MINDFULNESS -> RecoveryAccent
                DayLabel.CONSISTENCY -> DarkWarning
                DayLabel.BALANCED -> SecondaryText
            }
            Text(
                state.dayLabel.displayName,
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelColor
            )

            Spacer(Modifier.height(14.dp))

            // ── Trend Visualization (always visible) ──
            if (state.trends.isNotEmpty()) {
                Text("Show Trend", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SecondaryText)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.trends.forEach { trend ->
                        val trendColor = when (trend.status) {
                            SignalStatus.GOOD -> DarkSuccess
                            SignalStatus.ON_TRACK -> DarkWarning
                            SignalStatus.LOW -> DarkError
                        }
                        val arrowColor = when (trend.direction) {
                            TrendDirection.UP -> DarkSuccess
                            TrendDirection.DOWN -> DarkError
                            TrendDirection.STABLE -> SecondaryText
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardCream)
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${trend.direction.arrow} ${trend.percentage}%",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = arrowColor
                            )
                            Text(
                                trend.label,
                                fontSize = 10.sp, color = trendColor
                            )
                            Text(
                                "Past 7 Days",
                                fontSize = 8.sp, color = SecondaryText
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Always-visible: Why? reasoning
            if (state.reasoning.isNotEmpty()) {
                Text("Why?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
                state.reasoning.forEach { reason ->
                    Text("• $reason", fontSize = 12.sp, color = SecondaryText, lineHeight = 17.sp)
                }
                Spacer(Modifier.height(10.dp))
            }

            // Always-visible: recommendations
            if (state.recommendations.isNotEmpty()) {
                Text("Recommended", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
                state.recommendations.forEach { rec ->
                    Text("✓ $rec", fontSize = 12.sp, color = SecondaryText, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
fun PersonalityDialog(
    current: VoicePersonality,
    onSelect: (VoicePersonality) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Narrator Style", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                VoicePersonality.entries.forEach { p ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (p == current) Amber.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { onSelect(p) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(p.displayName, fontSize = 15.sp, color = PrimaryText)
                        Spacer(Modifier.weight(1f))
                        if (p == current) Text("Active", fontSize = 12.sp, color = Amber)
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
