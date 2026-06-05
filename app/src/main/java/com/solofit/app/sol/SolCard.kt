package com.solofit.app.sol

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.getValue
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
import androidx.compose.material.icons.filled.Settings
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import com.solofit.app.ui.theme.PageBg
import com.solofit.app.ui.theme.CardCream

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

    val recColor = when {
        state.type == InsightType.MORNING_GREETING && state.detail.contains("strong", ignoreCase = true) -> Amber
        state.type == InsightType.MORNING_GREETING && state.detail.contains("lower", ignoreCase = true) -> Color(0xFFC19148)
        state.type == InsightType.OVERTRAINING -> Color(0xFFA26B57)
        else -> Amber
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            // SOL header
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
                    Box(
                        Modifier.size(14.dp).clip(CircleShape).background(Amber)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("SOL", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    Text(
                        state.personality.displayName,
                        fontSize = 11.sp,
                        color = SecondaryText
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Filled.Settings, null,
                    tint = SecondaryText,
                    modifier = Modifier.size(20.dp).clickable(onClick = onPersonalityChange)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Greeting
            if (state.greeting.isNotBlank()) {
                Text(
                    state.greeting,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText
                )
                Spacer(Modifier.height(4.dp))
            }

            // Headline
            if (state.headline.isNotBlank()) {
                Text(
                    state.headline,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = recColor
                )
                Spacer(Modifier.height(4.dp))
            }

            // Detail
            Text(
                state.detail,
                fontSize = 13.sp,
                color = SecondaryText,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(14.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.reasoning.isNotEmpty()) {
                    ActionChip("Why?", onClick = onToggleWhy, expanded = state.expandedWhy)
                }
                if (state.recommendations.isNotEmpty()) {
                    ActionChip("What Should I Do?", onClick = onToggleWhat, expanded = state.expandedWhat)
                }
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(if (state.isSpeaking) Amber else Color(0xFFF3F4F6))
                        .clickable(onClick = onListen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp, null,
                        tint = if (state.isSpeaking) Color.White else SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanded Why?
            AnimatedVisibility(
                visible = state.expandedWhy,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.padding(top = 12.dp)) {
                    Text(
                        "Why?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                    Spacer(Modifier.height(4.dp))
                    state.reasoning.forEach { reason ->
                        Text(
                            "• $reason",
                            fontSize = 12.sp,
                            color = SecondaryText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Expanded What Should I Do?
            AnimatedVisibility(
                visible = state.expandedWhat,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.padding(top = 12.dp)) {
                    Text(
                        "Recommended",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                    Spacer(Modifier.height(4.dp))
                    state.recommendations.forEach { rec ->
                        Text(
                            "✓ $rec",
                            fontSize = 12.sp,
                            color = SecondaryText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit, expanded: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (expanded) Amber.copy(alpha = 0.1f) else Color(0xFFF3F4F6))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (expanded) Amber else SecondaryText
        )
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
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (p == current) Amber.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { onSelect(p) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(p.displayName, fontSize = 15.sp, color = PrimaryText)
                        Spacer(Modifier.weight(1f))
                        if (p == current) {
                            Text("Active", fontSize = 12.sp, color = Amber)
                        }
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
