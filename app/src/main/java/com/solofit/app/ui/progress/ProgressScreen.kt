package com.solofit.app.ui.progress

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.components.ProgressTheme
import com.solofit.app.ui.components.WellnessStaticCard
import androidx.compose.material3.MaterialTheme
import com.solofit.app.ui.theme.SlateBlue
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProgressTheme {
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
                Text("Progress", style = MaterialTheme.typography.headlineMedium, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
                Text("Your journey at a glance", fontSize = 14.sp, color = SecondaryText)
                Spacer(Modifier.height(24.dp))

                // ── Streak Hero ──
                WellnessStaticCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.size(72.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LocalFireDepartment, null, tint = SlateBlue, modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "${state.activeDaysThisWeek}",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Light,
                            color = SlateBlue,
                            letterSpacing = (-2).sp
                        )
                        Text("active days this week", fontSize = 18.sp, color = SecondaryText, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Monthly Overview ──
                WellnessStaticCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(28.dp)) {
                        Text("Monthly Overview", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                        Spacer(Modifier.height(20.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem(Icons.Filled.FitnessCenter, "Workouts", "${state.workoutsThisMonth}", "this month")
                            StatItem(Icons.AutoMirrored.Filled.TrendingUp, "Volume", formatNumber(state.totalVolumeKg), "kg total")
                        }
                        Spacer(Modifier.height(20.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem(Icons.Filled.Star, "Milestones", "${state.milestonesUnlocked}", "achieved")
                            StatItem(Icons.Filled.LocalFireDepartment, "Consistency", "${state.consistencyPct}%", "this month")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Achievements ──
                Text("Achievements", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                Spacer(Modifier.height(12.dp))
                WellnessStaticCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(24.dp)) {
                        state.achievements.forEachIndexed { index, achievement ->
                            if (index > 0) Spacer(Modifier.height(16.dp))
                            AchievementRow(achievement.title, achievement.description, achievement.unlocked)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = SecondaryText, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(8.dp))
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
        Text(label, fontSize = 12.sp, color = SecondaryText)
        Text(unit, fontSize = 10.sp, color = Color(0xFF9CA3AF))
    }
}

@Composable
private fun AchievementRow(title: String, description: String, unlocked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(36.dp).clip(CircleShape)
                .background(if (unlocked) SlateBlue else SlateBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Star, null, tint = if (unlocked) Color.White else SlateBlue.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (unlocked) PrimaryText else Color(0xFF9CA3AF))
            Text(description, fontSize = 12.sp, color = SecondaryText)
        }
        Text(
            if (unlocked) "Unlocked" else "Locked",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (unlocked) SlateBlue else Color(0xFF9CA3AF)
        )
    }
}

private fun formatNumber(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}
