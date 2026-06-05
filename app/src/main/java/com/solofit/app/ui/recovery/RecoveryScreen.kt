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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.solofit.app.ui.components.WellnessStaticCard
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.RecoveryBg
import com.solofit.app.ui.theme.RecoveryCard
import com.solofit.app.ui.theme.RecoveryAccent
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import com.solofit.app.ui.theme.HighGreen
import com.solofit.app.ui.theme.MidAmber
import com.solofit.app.ui.theme.LowRed
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryScreen(
    onBack: () -> Unit = {},
    viewModel: RecoveryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val readiness by viewModel.readinessScore.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recovery", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RecoveryBg)
            )
        },
        containerColor = RecoveryBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ── HERO: Readiness Score ──
            val barColor = when {
                readiness >= 80 -> HighGreen
                readiness >= 60 -> MidAmber
                else -> LowRed
            }
            WellnessStaticCard(
                containerColor = RecoveryCard,
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
                        color = Amber,
                        letterSpacing = (-2).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    val label = when {
                        readiness >= 80 -> "Optimal \u2014 ready to perform"
                        readiness >= 60 -> "Moderate \u2014 listen to your body"
                        readiness >= 40 -> "Low \u2014 prioritize recovery"
                        else -> "Rest needed \u2014 take it easy"
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

            // ── MEDIUM: Recovery Factors (grouped) ──
            WellnessStaticCard(
                containerColor = RecoveryCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(28.dp)) {
                    Text("Recovery Factors", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                    Spacer(Modifier.height(20.dp))

                    // Sleep
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(RecoveryAccent.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Bedtime, null, tint = RecoveryAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Sleep", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                            Text("%.1f h".format(state.sleepHours), fontSize = 13.sp, color = SecondaryText)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = state.sleepHours,
                        onValueChange = viewModel::setSleepHours,
                        valueRange = 0f..12f,
                        steps = 47,
                        colors = SliderDefaults.colors(thumbColor = RecoveryAccent, activeTrackColor = RecoveryAccent)
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("0h", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        Text("12h", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }

                    Spacer(Modifier.height(20.dp))

                    // Mood
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF6B6B6B).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Mood, null, tint = SecondaryText, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Mood", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                            val moodLabels = listOf("Terrible", "Bad", "Okay", "Good", "Great")
                            Text(moodLabels.getOrElse(state.moodLevel - 1) { "\u2014" }, fontSize = 13.sp, color = SecondaryText)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        (1..5).forEach { n ->
                            val selected = n == state.moodLevel
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Color(0xFFD5DCD6) else Color.Transparent)
                                    .clickable { viewModel.setMoodLevel(n) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$n",
                                    fontSize = if (selected) 15.sp else 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) PrimaryText else Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Energy
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF6B6B6B).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Bolt, null, tint = SecondaryText, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.size(12.dp))
                        Text("Energy", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                        Spacer(Modifier.width(8.dp))
                        Text("Level ${state.energyLevel} / 5", fontSize = 13.sp, color = SecondaryText)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        (1..5).forEach { n ->
                            val selected = n == state.energyLevel
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Color(0xFFD5DCD6) else Color.Transparent)
                                    .clickable { viewModel.setEnergyLevel(n) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$n",
                                    fontSize = if (selected) 15.sp else 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) PrimaryText else Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Stress
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(RecoveryAccent.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Favorite, null, tint = RecoveryAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.size(12.dp))
                        Text("Stress", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                        Spacer(Modifier.width(8.dp))
                        Text("Level ${state.stressLevel} / 5", fontSize = 13.sp, color = SecondaryText)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        (1..5).forEach { n ->
                            val selected = n == state.stressLevel
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) RecoveryAccent.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { viewModel.setStressLevel(n) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$n",
                                    fontSize = if (selected) 15.sp else 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) PrimaryText else Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Meditation
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF6B6B6B).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.SelfImprovement, null, tint = SecondaryText, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Meditation", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                            Text("${state.meditationMinutes}m today", fontSize = 13.sp, color = SecondaryText)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = state.meditationMinutes.toFloat(),
                        onValueChange = { viewModel.setMeditationMinutes(it.roundToInt()) },
                        valueRange = 0f..120f,
                        steps = 23,
                        colors = SliderDefaults.colors(thumbColor = RecoveryAccent, activeTrackColor = RecoveryAccent)
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("0m", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        Text("120m", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── SECONDARY: Recommendations ──
            RecommendationsSection(score = readiness, sleep = state.sleepHours, stress = state.stressLevel, energy = state.energyLevel)
        }
    }
}

private val Hairline = Color(0xFFE5E7EB)

@Composable
private fun RecommendationsSection(score: Int, sleep: Float, stress: Int, energy: Int) {
    val recommendations = buildList {
        if (sleep < 7f) add("Aim for 7\u20139 hours of sleep tonight for optimal recovery.")
        if (stress > 3) add("Try a 5-minute breathing exercise to lower stress levels.")
        if (energy < 3) add("Your energy is low \u2014 consider a lighter workout or rest day.")
        if (score < 60) add("Focus on recovery today: prioritize sleep, nutrition, and stretching.")
        if (score >= 80 && sleep >= 7f) add("You're in great shape \u2014 maintain your routine and keep pushing!")
        if (isEmpty()) add("Everything looks balanced. Keep up your consistent habits.")
    }

    WellnessStaticCard(
        containerColor = RecoveryCard,
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
