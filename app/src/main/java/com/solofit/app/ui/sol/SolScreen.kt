package com.solofit.app.ui.sol

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.sol.LifeContext
import com.solofit.app.sol.SignalStatus
import com.solofit.app.sol.SolViewModel
import com.solofit.app.sol.TrendDirection
import com.solofit.app.sol.WellnessState
import com.solofit.app.sol.themeColor
import com.solofit.app.ui.theme.Hairline
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary

@Composable
fun SolScreen(
    solViewModel: SolViewModel,
    modifier: Modifier = Modifier
) {
    val solState by solViewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        // ── Header ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(10.dp).clip(CircleShape)
                    .background(solState.todayTheme.themeColor())
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Wellness Briefing",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Greeting ──
        Text(
            solState.greeting.ifEmpty { "Good day." },
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )

        Spacer(Modifier.height(16.dp))

        // ── Today's Theme ──
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = solState.todayTheme.themeColor().copy(alpha = 0.06f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    solState.todayTheme.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = solState.todayTheme.themeColor(),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    solState.themeReason.ifEmpty { solState.todayTheme.description },
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
                if (solState.causalExplanation.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier.fillMaxWidth().height(1.dp)
                            .background(Hairline.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Why",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        solState.causalExplanation,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Key Insight ──
        Text(
            "What I Noticed",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            solState.headline.ifEmpty { "Still gathering data to identify meaningful patterns." },
            fontSize = 15.sp,
            color = TextPrimary,
            lineHeight = 22.sp
        )

        if (solState.detail.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Why It Matters",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                solState.detail,
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp
            )
        }

        if (solState.recommendations.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                "What to Do Next",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(6.dp))
            solState.recommendations.forEach { rec ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("\u2022  ", fontSize = 14.sp, color = TextSecondary)
                    Text(
                        rec,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Signals ──
        if (solState.signals.isNotEmpty()) {
            Text(
                "Today's Signals",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(10.dp))
            solState.signals.forEach { signal ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(signal.label, fontSize = 13.sp, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(6.dp).clip(CircleShape)
                                .background(signalColor(signal.status))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(signal.status.name, fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        // ── Life Context (if active) ──
        if (solState.lifeContext.active && solState.lifeContext.situation.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Life Context",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        LifeContext.PRESETS[solState.lifeContext.situation]
                            ?: solState.lifeContext.situation,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sol is adapting recommendations to your current situation.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // ── Wellness Badge ──
        solState.userTwin?.let { twin ->
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(8.dp).clip(CircleShape)
                        .background(wellnessColor(twin.currentState))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    twin.currentState.name.replace("_", " "),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "${twin.daysTracked} days tracked",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                if (twin.confidence != "Low") {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "\u00b7 ${twin.confidence} confidence",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

private fun signalColor(status: SignalStatus): androidx.compose.ui.graphics.Color = when (status) {
    SignalStatus.GOOD -> androidx.compose.ui.graphics.Color(0xFF70805E)
    SignalStatus.ON_TRACK -> androidx.compose.ui.graphics.Color(0xFFA67B4A)
    SignalStatus.LOW -> androidx.compose.ui.graphics.Color(0xFFA06052)
}

private fun wellnessColor(state: WellnessState): androidx.compose.ui.graphics.Color = when (state) {
    WellnessState.THRIVING -> androidx.compose.ui.graphics.Color(0xFF70805E)
    WellnessState.MAINTAINING -> androidx.compose.ui.graphics.Color(0xFFA67B4A)
    WellnessState.STRUGGLING -> androidx.compose.ui.graphics.Color(0xFFA67B4A)
    WellnessState.AT_RISK -> androidx.compose.ui.graphics.Color(0xFFA06052)
    WellnessState.INSUFFICIENT_DATA -> androidx.compose.ui.graphics.Color(0xFF756F69)
}
