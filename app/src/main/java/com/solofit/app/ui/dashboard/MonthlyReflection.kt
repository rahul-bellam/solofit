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
import com.solofit.app.ui.theme.DarkSuccess

data class MonthlyReflectionData(
    val workoutsCompleted: Int = 0,
    val proteinConsistency: String = "",
    val walkingTrend: String = "",
    val recoveryTrend: String = "",
    val mostImprovedHabit: String = "",
    val suggestedFocus: String = ""
)

@Composable
fun MonthlyReflection(
    data: MonthlyReflectionData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                "Monthly Reflection",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )

            Spacer(Modifier.height(14.dp))

            Text(
                buildIdentityNarrative(data),
                fontSize = 14.sp,
                color = PrimaryText,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                buildImprovement(data),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkSuccess,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                buildNextFocus(data),
                fontSize = 13.sp,
                color = SecondaryText,
                lineHeight = 18.sp
            )
        }
    }
}

private fun buildIdentityNarrative(data: MonthlyReflectionData): String {
    val w = data.workoutsCompleted
    val trend = data.walkingTrend.lowercase()

    return when {
        w >= 20 -> "You\u2019ve become someone who consistently moves. At $w workouts this month, training is no longer a chore \u2014 it\u2019s part of your identity."
        w >= 16 -> "With $w workouts this month, you\u2019re showing up reliably. The person who follows through is the person you\u2019re becoming."
        w >= 12 -> "$w workouts this month. Consistency is building. You\u2019re proving to yourself that you can commit."
        w >= 8 -> "$w workouts this month \u2014 you\u2019re laying the foundation. Each session is a brick in the person you\u2019re building."
        w >= 4 -> "$w workouts this month. You\u2019re getting started, and that matters more than intensity. Momentum grows from presence."
        w >= 1 -> "$w workouts this month. Starting is the hardest part. You did it."
        else -> "This month was about showing up when you could. Even tracking your habits is progress."
    } + " "

    return when {
        trend.contains("increased") -> "$trend Walking has become more consistent, and that quiet movement adds up in ways you don\u2019t always notice."
        trend.contains("stable") -> "$trend Walking has remained steady, a reliable anchor in your routine."
        else -> ""
    }.trim()
}

private fun buildImprovement(data: MonthlyReflectionData): String {
    val habit = data.mostImprovedHabit
    return if (habit.isNotBlank()) "Most improved: $habit"
    else if (data.workoutsCompleted > 0) "Most improved: Consistency is building naturally."
    else "Tracking in progress \u2014 patterns will emerge with more data."
}

private fun buildNextFocus(data: MonthlyReflectionData): String {
    return data.suggestedFocus.ifBlank {
        if (data.workoutsCompleted < 12) "Next month: aim for 3 workouts per week as a baseline."
        else if (data.proteinConsistency.contains("building", ignoreCase = true)) "Next month: focus on protein consistency to match your workout rhythm."
        else "Next month: maintain the momentum. You\u2019re on a strong path."
    }
}
