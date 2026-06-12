package com.solofit.app.ui.recovery

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.material3.MaterialTheme
import com.solofit.app.ui.components.WellnessStaticCard
import com.solofit.app.ui.components.RecoveryTheme
import com.solofit.app.ui.theme.TwilightBlue
import com.solofit.app.ui.theme.HighGreen
import com.solofit.app.ui.theme.MidAmber
import com.solofit.app.ui.theme.LowRed
import com.solofit.app.ui.theme.Hairline
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import kotlin.math.roundToInt

@Composable
fun RecoveryScreen(
    onBack: () -> Unit = {},
    viewModel: RecoveryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val readiness by viewModel.readinessScore.collectAsStateWithLifecycle()

    RecoveryTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.height(8.dp))

                // ── HERO: Readiness Score ──
                val barColor = when {
                    readiness >= 80 -> HighGreen
                    readiness >= 60 -> MidAmber
                    else -> LowRed
                }
                WellnessStaticCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Readiness", fontSize = 15.sp, color = SecondaryText, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "$readiness",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Light,
                            color = TwilightBlue,
                            letterSpacing = (-2).sp
                        )
                        Spacer(Modifier.height(4.dp))
                        val label = when {
                            readiness >= 80 -> "Optimal — ready to perform"
                            readiness >= 60 -> "Moderate — listen to your body"
                            readiness >= 40 -> "Low — prioritize recovery"
                            else -> "Rest needed — take it easy"
                        }
                        Text(label, fontSize = 15.sp, color = SecondaryText)
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { readiness / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = barColor,
                            trackColor = Hairline
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Recovery Factors ──
                WellnessStaticCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(28.dp)) {
                        Text("Recovery Factors", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                        Spacer(Modifier.height(20.dp))

                        FactorSlider(
                            icon = Icons.Filled.Bedtime,
                            iconTint = TwilightBlue,
                            label = "Sleep",
                            value = "%.1f h".format(state.sleepHours),
                            sliderValue = state.sleepHours,
                            onSliderChange = viewModel::setSleepHours,
                            range = 0f..12f,
                            steps = 47,
                            leftLabel = "0h",
                            rightLabel = "12h"
                        )

                        Spacer(Modifier.height(20.dp))

                        FactorSelector(
                            icon = Icons.Filled.Mood,
                            iconTint = SecondaryText,
                            label = "Mood",
                            subtitle = listOf("Terrible", "Bad", "Okay", "Good", "Great").getOrElse(state.moodLevel - 1) { "—" },
                            levels = 5,
                            selected = state.moodLevel,
                            onSelect = viewModel::setMoodLevel,
                            accentColor = TwilightBlue
                        )

                        Spacer(Modifier.height(20.dp))

                        FactorSelector(
                            icon = Icons.Filled.Bolt,
                            iconTint = SecondaryText,
                            label = "Energy",
                            subtitle = "Level ${state.energyLevel} / 5",
                            levels = 5,
                            selected = state.energyLevel,
                            onSelect = viewModel::setEnergyLevel,
                            accentColor = SecondaryText
                        )

                        Spacer(Modifier.height(20.dp))

                        FactorSelector(
                            icon = Icons.Filled.Favorite,
                            iconTint = TwilightBlue,
                            label = "Stress",
                            subtitle = "Level ${state.stressLevel} / 5",
                            levels = 5,
                            selected = state.stressLevel,
                            onSelect = viewModel::setStressLevel,
                            accentColor = TwilightBlue
                        )

                        Spacer(Modifier.height(20.dp))

                        FactorSlider(
                            icon = Icons.Filled.SelfImprovement,
                            iconTint = SecondaryText,
                            label = "Meditation",
                            value = "${state.meditationMinutes}m today",
                            sliderValue = state.meditationMinutes.toFloat(),
                            onSliderChange = { viewModel.setMeditationMinutes(it.roundToInt()) },
                            range = 0f..120f,
                            steps = 23,
                            leftLabel = "0m",
                            rightLabel = "120m"
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Recommendations ──
                RecommendationsSection(score = readiness, sleep = state.sleepHours, stress = state.stressLevel, energy = state.energyLevel)

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FactorSlider(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    sliderValue: Float,
    onSliderChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    leftLabel: String,
    rightLabel: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            Text(value, fontSize = 13.sp, color = SecondaryText)
        }
    }
    Spacer(Modifier.height(8.dp))
    Slider(
        value = sliderValue,
        onValueChange = onSliderChange,
        valueRange = range,
        steps = steps,
        colors = SliderDefaults.colors(thumbColor = TwilightBlue, activeTrackColor = TwilightBlue)
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(leftLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF))
        Text(rightLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF))
    }
}

@Composable
private fun FactorSelector(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    subtitle: String,
    levels: Int,
    selected: Int,
    onSelect: (Int) -> Unit,
    accentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            Text(subtitle, fontSize = 13.sp, color = SecondaryText)
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        (1..levels).forEach { n ->
            val isSelected = n == selected
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .clickable { onSelect(n) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$n",
                    fontSize = if (isSelected) 15.sp else 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) PrimaryText else Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
private fun RecommendationsSection(score: Int, sleep: Float, stress: Int, energy: Int) {
    val recommendations = buildList {
        if (sleep < 7f) add("Aim for 7–9 hours of sleep tonight for optimal recovery.")
        if (stress > 3) add("Try a 5-minute breathing exercise to lower stress levels.")
        if (energy < 3) add("Your energy is low — consider a lighter workout or rest day.")
        if (score < 60) add("Focus on recovery today: prioritize sleep, nutrition, and stretching.")
        if (score >= 80 && sleep >= 7f) add("You're in great shape — maintain your routine and keep moving!")
        if (isEmpty()) add("Everything looks balanced. Keep up your consistent habits.")
    }

    WellnessStaticCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lightbulb, null, tint = SecondaryText, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text("Recommendations", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            }
            Spacer(Modifier.height(16.dp))
            recommendations.forEach { rec ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFC8D2CC)))
                    Spacer(Modifier.size(12.dp))
                    Text(rec, fontSize = 14.sp, color = SecondaryText, lineHeight = 22.sp)
                }
            }
        }
    }
}
