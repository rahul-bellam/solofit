package com.solofit.app.ui.stress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.sol.BurnoutAssessment
import com.solofit.app.sol.BurnoutContributor
import com.solofit.app.sol.BurnoutLevel
import com.solofit.app.sol.SignalDirection
import com.solofit.app.sol.SolViewModel
import com.solofit.app.ui.components.RecoveryTheme
import com.solofit.app.ui.recovery.RecoveryViewModel
import com.solofit.app.ui.theme.CardPrimary
import com.solofit.app.ui.theme.ErrorClay
import com.solofit.app.ui.theme.Hairline
import com.solofit.app.ui.theme.RecoveryAccent
import com.solofit.app.ui.theme.SuccessGreen
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import com.solofit.app.ui.theme.WarningAmber

private val checkInOptions = listOf(
    "Very calm" to 1,
    "Calm" to 2,
    "Okay" to 3,
    "Stressed" to 4,
    "Overwhelmed" to 5
)

private fun levelColor(level: BurnoutLevel): Color = when (level) {
    BurnoutLevel.LOW -> SuccessGreen
    BurnoutLevel.MODERATE -> RecoveryAccent
    BurnoutLevel.ELEVATED -> WarningAmber
    BurnoutLevel.HIGH -> ErrorClay
}

private fun energyLabel(energyLevel: Int): String = when {
    energyLevel >= 4 -> "High"
    energyLevel == 3 -> "Moderate"
    else -> "Low"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StressScreen(
    onBack: () -> Unit,
    recoveryViewModel: RecoveryViewModel = hiltViewModel(),
    solViewModel: SolViewModel = hiltViewModel()
) {
    val recovery by recoveryViewModel.state.collectAsStateWithLifecycle()
    val sol by solViewModel.state.collectAsStateWithLifecycle()
    val burnout = sol.burnout

    RecoveryTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Energy & Stress", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "A two-second check-in helps SoloFit notice when your energy is dipping — before it becomes burnout.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }

                // ── Morning check-in ──
                item {
                    SectionCard {
                        Text("How are you feeling today?", style = MaterialTheme.typography.titleSmall, color = TextSecondary, letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(12.dp))
                        checkInOptions.forEach { (label, value) ->
                            val selected = recovery.stressLevel == value
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { recoveryViewModel.setStressLevel(value) }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(18.dp).clip(CircleShape)
                                        .background(if (selected) RecoveryAccent else Hairline)
                                )
                                Spacer(Modifier.size(12.dp))
                                Text(
                                    label,
                                    color = if (selected) TextPrimary else TextSecondary,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // ── Energy today ──
                item {
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Energy Today", style = MaterialTheme.typography.titleSmall, color = TextSecondary, letterSpacing = 0.5.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    energyLabel(recovery.energyLevel),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            recovery.readinessScore.takeIf { it > 0 }?.let {
                                Text("Readiness $it", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                // ── Burnout risk ──
                if (burnout != null) {
                    item { BurnoutRiskCard(burnout) }

                    if (burnout.contributors.isNotEmpty()) {
                        item { ContributorsCard(burnout.contributors) }
                    }

                    item { RecommendedFocusCard(burnout) }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun BurnoutRiskCard(burnout: BurnoutAssessment) {
    val color = levelColor(burnout.level)
    SectionCard {
        Text("Burnout Risk", style = MaterialTheme.typography.titleSmall, color = TextSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.size(8.dp))
            Text(
                burnout.level.label,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Hairline)
        ) {
            Box(
                Modifier.fillMaxWidth(burnout.score / 100f).height(6.dp)
                    .clip(RoundedCornerShape(3.dp)).background(color)
            )
        }
        burnout.insight?.let { insight ->
            Spacer(Modifier.height(16.dp))
            Text(insight.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(insight.observation, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text(insight.action, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun ContributorsCard(contributors: List<BurnoutContributor>) {
    SectionCard {
        Text("Contributing Factors", style = MaterialTheme.typography.titleSmall, color = TextSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(10.dp))
        contributors.forEach { c ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val arrow = when (c.direction) {
                    SignalDirection.DOWN -> "↓"
                    SignalDirection.UP -> "↑"
                    SignalDirection.FLAT -> "→"
                }
                Text(arrow, color = levelColorForDirection(c.direction), fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(12.dp))
                Text(c.label, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(c.note, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

private fun levelColorForDirection(direction: SignalDirection): Color = when (direction) {
    SignalDirection.DOWN -> ErrorClay
    SignalDirection.UP -> WarningAmber
    SignalDirection.FLAT -> RecoveryAccent
}

@Composable
private fun RecommendedFocusCard(burnout: BurnoutAssessment) {
    SectionCard {
        Text("Recommended Focus", style = MaterialTheme.typography.titleSmall, color = TextSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        Text(burnout.recommendedFocus, style = MaterialTheme.typography.headlineSmall, color = RecoveryAccent, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        burnout.suggestedActions.forEach { action ->
            Row(Modifier.padding(vertical = 4.dp)) {
                Text("•  ", color = RecoveryAccent)
                Text(action, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardPrimary)
            .padding(20.dp)
    ) {
        Column(content = content)
    }
}
