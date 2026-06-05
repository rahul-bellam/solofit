package com.solofit.app.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import com.solofit.app.ui.theme.CardCream

@Composable
fun WeeklyReflection(
    workoutCount: Int,
    proteinDays: Int,
    walkingTrend: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("This Week", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)

            Spacer(Modifier.height(14.dp))

            val summaryLines = buildList {
                if (workoutCount > 0) add("$workoutCount workouts completed")
                if (proteinDays > 0) add("Protein target reached on $proteinDays days")
                if (walkingTrend.isNotBlank()) add(walkingTrend.replaceFirstChar { it.lowercase() })
                if (isEmpty()) add("No data tracked this week yet")
            }

            summaryLines.forEach { line ->
                Text("• $line", fontSize = 14.sp, color = PrimaryText, lineHeight = 20.sp)
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(12.dp))

            Text(
                when {
                    workoutCount >= 4 -> "Most consistent week this month. The routine is becoming a habit."
                    workoutCount >= 3 -> "Building momentum steadily. Each session reinforces the pattern."
                    workoutCount >= 1 -> "Every session counts. Consistency grows one day at a time."
                    else -> "Small steps lead to big changes. Start with one session next week."
                },
                fontSize = 13.sp, color = SecondaryText, lineHeight = 18.sp
            )

            Spacer(Modifier.height(10.dp))

            Text("Next Week Focus", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    workoutCount < 3 -> "Aim for 3-4 workouts. Even short sessions count."
                    proteinDays < 4 -> "Try to hit your protein target more consistently. Meal prep may help."
                    walkingTrend.contains("decreased", ignoreCase = true) -> "Try adding a short walk to your daily routine."
                    else -> "Maintain the momentum. You're on a strong path."
                },
                fontSize = 13.sp, color = SecondaryText, lineHeight = 18.sp
            )
        }
    }
}
