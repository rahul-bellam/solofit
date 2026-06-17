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

            Spacer(Modifier.height(12.dp))

            val narrative = buildWeekNarrative(workoutCount, proteinDays, walkingTrend)
            Text(
                narrative,
                fontSize = 14.sp,
                color = PrimaryText,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                buildNextWeekFocus(workoutCount, proteinDays, walkingTrend),
                fontSize = 13.sp,
                color = SecondaryText,
                lineHeight = 18.sp
            )
        }
    }
}

private fun buildWeekNarrative(workouts: Int, proteinDays: Int, walkingTrend: String): String {
    return when {
        workouts >= 5 -> "This was your most active week in recent memory. Movement is no longer a task \u2014 it's becoming part of who you are."
        workouts == 4 -> "Consistent and present. Four sessions this week shows discipline. The routine is taking root."
        workouts == 3 -> "Three workouts this week \u2014 a solid rhythm. Your body is learning to expect movement."
        workouts == 2 -> "Two sessions this week. Every workout reinforces the pattern. Next week, aim for three."
        workouts == 1 -> "One workout this week is one more than doing nothing. Momentum starts with showing up."
        workouts == 0 && proteinDays >= 3 -> "Movement took a back seat, but your nutrition stayed consistent. Use that foundation to restart next week."
        workouts == 0 && walkingTrend.contains("increased", ignoreCase = true) -> "No structured workouts, but your walking increased. Active recovery still counts."
        workouts == 0 -> "It was a quiet week. Rest is part of growth too. Reset and come back fresh."
        else -> "Small actions compound. Every week is a step toward consistency."
    }
}

private fun buildNextWeekFocus(workouts: Int, proteinDays: Int, walkingTrend: String): String {
    return when {
        workouts < 3 -> "Next week: aim for 3 workouts. Even 20-minute sessions build confidence."
        proteinDays < 4 -> "Next week: focus on protein consistency. Small prep saves the day."
        walkingTrend.contains("decreased", ignoreCase = true) -> "Next week: add a short walk after meals. It adds up."
        else -> "Next week: maintain your rhythm. Reliability beats intensity over time."
    }
}
