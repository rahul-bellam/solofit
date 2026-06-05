package com.solofit.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import com.solofit.app.ui.theme.CardCream
import com.solofit.app.ui.theme.DarkSuccess

@Composable
fun WeeklyReflection(
    workoutCount: Int,
    proteinDays: Int,
    walkingTrend: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Amber.copy(alpha = 0.2f), Amber.copy(alpha = 0.05f)),
                                radius = 20f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("W", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Amber)
                }
                Spacer(Modifier.width(10.dp))
                Text("This Week", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            }

            Spacer(Modifier.height(14.dp))

            if (workoutCount > 0) {
                Text("$workoutCount workouts completed", fontSize = 14.sp, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
            }
            if (proteinDays > 0) {
                Text("Protein goal reached on $proteinDays days", fontSize = 14.sp, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
            }
            if (walkingTrend.isNotBlank()) {
                Text(walkingTrend, fontSize = 14.sp, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(12.dp))

            Text(
                when {
                    workoutCount >= 4 -> "Most consistent week this month."
                    workoutCount >= 3 -> "Building momentum steadily."
                    workoutCount >= 1 -> "Every session counts."
                    else -> "Small steps lead to big changes."
                },
                fontSize = 13.sp,
                color = SecondaryText
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Next Week Focus",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Amber
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    workoutCount < 3 -> "Aim for 3-4 workouts next week."
                    proteinDays < 4 -> "Try to hit protein goals more consistently."
                    else -> "Maintain the momentum."
                },
                fontSize = 13.sp,
                color = SecondaryText
            )
        }
    }
}
