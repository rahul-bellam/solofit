package com.solofit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Daily water intake card with an animated filling glass + quick +/- a glass. */
@Composable
fun WaterTracker(
    currentMl: Int,
    goalMl: Int,
    onAdd: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    glassMl: Int = 250,
    animate: Boolean = true
) {
    val fraction = if (goalMl > 0) (currentMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
    val glasses = currentMl / glassMl
    val goalGlasses = goalMl / glassMl
    val goalHit = goalMl > 0 && currentMl >= goalMl

    val view = LocalView.current
    // One-shot celebratory ripple when the goal is first reached.
    val ripple = remember { Animatable(0f) }
    LaunchedEffect(goalHit) {
        if (goalHit) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            if (animate) {
                ripple.snapTo(0f)
                ripple.animateTo(1f, androidx.compose.animation.core.tween(600))
            }
        } else {
            ripple.snapTo(0f)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated glass with a goal-hit ripple overlay.
            Box(contentAlignment = Alignment.Center) {
                WaterGlass(
                    fraction = fraction,
                    animate = animate,
                    modifier = Modifier.size(width = 44.dp, height = 64.dp)
                )
                if (ripple.value > 0f && ripple.value < 1f) {
                    val rippleColor = MaterialTheme.colorScheme.secondary
                    Canvas(Modifier.size(64.dp)) {
                        val r = size.minDimension / 2f * ripple.value
                        drawCircle(
                            color = rippleColor.copy(alpha = (1f - ripple.value) * 0.6f),
                            radius = r,
                            center = Offset(size.width / 2f, size.height / 2f),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        if (goalHit) "  Water · goal hit! 🎉" else "  Water",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(2.dp))
                val waterPct = if (goalMl > 0) (currentMl * 100 / goalMl).coerceAtMost(100) else 0
                Text(
                    "$currentMl / $goalMl ml ($waterPct%) · $glasses of $goalGlasses glasses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onRemove(glassMl)
                    }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Remove a glass")
                    }
                    Text("$glassMl ml", fontWeight = FontWeight.SemiBold)
                    FilledTonalIconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onAdd(glassMl)
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add a glass")
                    }
                }
            }
        }
    }
}
