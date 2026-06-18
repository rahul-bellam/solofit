package com.solofit.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaterGlass(
    fraction: Float,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    waterColor: Color = MaterialTheme.colorScheme.secondary,
    glassColor: Color = MaterialTheme.colorScheme.outline
) {
    val targetFraction = fraction.coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "waterFill"
    )

    val wavePhase = if (animate) {
        val transition = rememberInfiniteTransition(label = "wave")
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )
        p
    } else 0f

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val topInset = w * 0.12f
        val bottomInset = w * 0.20f
        val glassPath = Path().apply {
            moveTo(topInset, h * 0.06f)
            lineTo(w - topInset, h * 0.06f)
            lineTo(w - bottomInset, h * 0.96f)
            lineTo(bottomInset, h * 0.96f)
            close()
        }

        val level = h * (0.96f - 0.90f * animatedFraction)
        clipPath(glassPath, clipOp = ClipOp.Intersect) {
            val amplitude = h * 0.03f * (if (animate) 1f else 0f)
            val wave = Path().apply {
                moveTo(0f, h)
                lineTo(0f, level)
                val steps = 12
                for (i in 0..steps) {
                    val x = w * i / steps
                    val y = level + amplitude * sin(wavePhase + i.toFloat() / steps * 2f * PI.toFloat())
                    lineTo(x, y)
                }
                lineTo(w, h)
                close()
            }
            drawPath(wave, color = waterColor.copy(alpha = 0.85f))
        }

        drawPath(
            glassPath,
            color = glassColor,
            style = Stroke(width = h * 0.025f)
        )
    }
}
