package com.solofit.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.theme.SolAccent
import kotlin.random.Random

private val DISCIPLINE_MESSAGES = listOf(
    "Stay locked in. Finish the set with control.",
    "Do not rush the movement. Own every rep.",
    "Keep your rest tight. Stay focused.",
    "Hold the tempo. Control the weight.",
    "Stay in position. Do not compromise form.",
    "One set at a time. Complete this one fully.",
    "Finish strong. The last rep matters most.",
    "Reset and breathe. Then execute.",
    "Do not rush. Controlled reps build more strength.",
    "Stay present. This set is all that matters."
)

private val INTENSITY_MESSAGES = mapOf(
    "HIGH" to listOf(
        "Push through. Stay controlled.",
        "This is where strength is built. Hold form.",
        "Stay tight. Do not lose position."
    ),
    "MED" to listOf(
        "Stay focused. Control the weight through the full range.",
        "Keep the tempo steady. No rushing."
    ),
    "LOW" to listOf(
        "Focus on form. Each rep should be deliberate.",
        "Slow and controlled. Quality over speed."
    )
)

private val REST_MESSAGES = listOf(
    "Rest is part of the set. Recover and refocus.",
    "Breathe deeply. Prepare for the next set.",
    "Reset mentally. The next set starts now."
)

private val COMPLETION_MESSAGES = listOf(
    "Set complete. Reset and prepare for the next one.",
    "Good set. Stay in rhythm.",
    "Nice work. Keep the pace consistent."
)

@Composable
fun WorkoutDisciplineCard(
    intensity: String = "MED",
    isResting: Boolean = false,
    justCompleted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pool = remember(intensity, isResting, justCompleted) {
        when {
            justCompleted -> COMPLETION_MESSAGES
            isResting -> REST_MESSAGES
            else -> INTENSITY_MESSAGES[intensity]?.ifEmpty { DISCIPLINE_MESSAGES } ?: DISCIPLINE_MESSAGES
        }
    }
    val message = remember(pool, intensity, isResting, justCompleted) {
        pool[Random.nextInt(pool.size)]
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SolAccent.copy(alpha = 0.06f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(6.dp).clip(CircleShape).background(SolAccent)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}
