package com.solofit.app.sol

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.solofit.app.ui.theme.SolAccent
import com.solofit.app.ui.theme.DarkSuccess
import com.solofit.app.ui.theme.DarkWarning
import com.solofit.app.ui.theme.DarkError
import com.solofit.app.ui.theme.ProteinColor
import com.solofit.app.ui.theme.RecoveryAccent

@Composable
fun SolCard(
    state: SolUiState,
    onToggleWhy: () -> Unit,
    onToggleWhat: () -> Unit,
    onListen: () -> Unit,
    onPersonalityChange: () -> Unit,
    onLogMeal: () -> Unit = {},
    onLogWorkout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!state.visible) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {

            // ── SOL header ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(SolAccent.copy(alpha = 0.2f), SolAccent.copy(alpha = 0.04f)),
                                radius = 20f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(SolAccent))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("SOL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(state.personality.displayName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp, null,
                    tint = if (state.isSpeaking) SolAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp).clickable(onClick = onListen)
                )
            }

            Spacer(Modifier.height(14.dp))

            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "sol_content"
            ) { targetState ->
                Column {
                    if (!targetState.hasSufficientData) {
                        EmptySolContent(targetState, onLogMeal, onLogWorkout)
                        return@Column
                    }

                    MainSolContent(targetState)
                }
            }
        }
    }
}

@Composable
private fun EmptySolContent(state: SolUiState, onLogMeal: () -> Unit, onLogWorkout: () -> Unit) {
    Text(
        state.headline.ifEmpty { "You're still building your wellness profile." },
        fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
    )
    Spacer(Modifier.height(10.dp))
    Text(
        state.detail.ifEmpty { "Log your first workout or meal and I'll begin identifying trends." },
        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 19.sp
    )
    Spacer(Modifier.height(20.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onLogWorkout,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Log Workout", fontSize = 13.sp)
        }
        Button(
            onClick = onLogMeal,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolAccent)
        ) {
            Text("Log Meal", fontSize = 13.sp, color = Color.White)
        }
    }
}

@Composable
private fun MainSolContent(state: SolUiState) {
    val namePart = if (state.userName.isNotBlank()) " ${state.userName}" else ""
    Text(
        "${state.greeting.trimEnd('.')}$namePart.",
        fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 26.sp
    )

    Spacer(Modifier.height(2.dp))

    Text(
        state.briefingHeader,
        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.3.sp
    )

    Spacer(Modifier.height(10.dp))

    val headlineColor = when {
        state.type == InsightType.OVERTRAINING -> DarkError
        state.dayLabel == DayLabel.PERFORMANCE -> DarkSuccess
        else -> MaterialTheme.colorScheme.onSurface
    }
    if (state.headline.isNotBlank()) {
        Text(state.headline, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = headlineColor, lineHeight = 22.sp)
        Spacer(Modifier.height(6.dp))
    }
    Text(state.detail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 19.sp)

    if (state.supplementaryHeadlines.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        state.supplementaryHeadlines.forEach { sh ->
            Text("• $sh", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
        }
    }

    Spacer(Modifier.height(16.dp))

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
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(signal.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(2.dp))
                Text(signal.detail, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    val labelColor = when (state.dayLabel) {
        DayLabel.PERFORMANCE -> DarkSuccess
        DayLabel.RECOVERY_FOCUS -> DarkWarning
        DayLabel.NUTRITION_FOCUS -> ProteinColor
        DayLabel.MINDFULNESS -> RecoveryAccent
        DayLabel.CONSISTENCY -> DarkWarning
        DayLabel.BALANCED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        state.dayLabel.displayName,
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelColor
    )

    if (state.trends.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
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
                    TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(trendColor.copy(alpha = 0.06f))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${trend.direction.arrow} ${trend.percentage}%",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = arrowColor
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(trend.label, fontSize = 10.sp, color = trendColor)
                    Text("Past 7 Days", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (state.reasoning.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text("Why?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 0.2.sp)
        Spacer(Modifier.height(6.dp))
        state.reasoning.forEach { reason ->
            Text("• $reason", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            Spacer(Modifier.height(3.dp))
        }
    }

    if (state.recommendations.isNotEmpty()) {
        Spacer(Modifier.height(14.dp))
        Text("Recommended", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 0.2.sp)
        Spacer(Modifier.height(6.dp))
        state.recommendations.forEach { rec ->
            Text("✓ $rec", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            Spacer(Modifier.height(3.dp))
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
                            .background(if (p == current) SolAccent.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { onSelect(p) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(p.displayName, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.weight(1f))
                        if (p == current) Text("Active", fontSize = 12.sp, color = SolAccent)
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
