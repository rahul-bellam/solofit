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

            if (data.workoutsCompleted > 0) {
                Text("• ${data.workoutsCompleted} workouts completed", fontSize = 14.sp, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
            }
            if (data.proteinConsistency.isNotBlank()) {
                Text("• Protein: ${data.proteinConsistency}", fontSize = 14.sp, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
            }
            if (data.walkingTrend.isNotBlank()) {
                Text("• Walking: ${data.walkingTrend}", fontSize = 14.sp, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
            }
            if (data.recoveryTrend.isNotBlank()) {
                Text("• Recovery: ${data.recoveryTrend}", fontSize = 14.sp, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Most Improved: ${data.mostImprovedHabit.ifEmpty { "Tracking in progress" }}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkSuccess,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Focus Next Month: ${data.suggestedFocus.ifEmpty { "Continue building consistent habits." }}",
                fontSize = 13.sp,
                color = SecondaryText,
                lineHeight = 18.sp
            )
        }
    }
}
