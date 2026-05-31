package com.solofit.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

/** Circular progress ring used on the dashboard for calories. */
@Composable
fun CalorieRing(
    consumed: Int,
    target: Int,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 18.dp,
    waveFill: Boolean = true,
    animate: Boolean = true
) {
    val progress = if (target > 0) (consumed.toFloat() / target).coerceIn(0f, 1f) else 0f
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(if (animate) 700 else 0),
        label = "calorieRing"
    )
    val remaining = (target - consumed).coerceAtLeast(0)
    val over = (consumed - target).coerceAtLeast(0)

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val ringColor = if (over > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val fillColor = ringColor.copy(alpha = 0.18f)

    // Wave phase for the liquid fill (only animates when enabled).
    val wavePhase = if (waveFill && animate) {
        val transition = rememberInfiniteTransition(label = "calWave")
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
            label = "calWavePhase"
        )
        p
    } else 0f

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

            // Liquid fill inside the ring: a circle clipped to the current level.
            if (waveFill) {
                val inset = strokeWidth.toPx() * 1.2f
                val innerLeft = inset
                val innerTop = inset
                val innerSize = this.size.width - inset * 2
                val circle = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            Offset(innerLeft, innerTop),
                            Size(innerSize, innerSize)
                        )
                    )
                }
                val level = innerTop + innerSize * (1f - animated)
                clipPath(circle) {
                    val amplitude = innerSize * 0.04f * (if (animate) 1f else 0f)
                    val wave = Path().apply {
                        moveTo(innerLeft, innerTop + innerSize)
                        lineTo(innerLeft, level)
                        val steps = 14
                        for (i in 0..steps) {
                            val x = innerLeft + innerSize * i / steps
                            val y = level + amplitude *
                                sin(wavePhase + i.toFloat() / steps * 2f * PI.toFloat())
                            lineTo(x, y)
                        }
                        lineTo(innerLeft + innerSize, innerTop + innerSize)
                        close()
                    }
                    drawPath(wave, color = fillColor)
                }
            }

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = consumed.toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "of $target kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            if (over > 0) {
                Text(
                    "$over over",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    "$remaining left",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** A labeled linear macro bar (Protein/Carbs/Fats). */
@Composable
fun MacroBar(
    label: String,
    consumed: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val progress = if (target > 0) (consumed.toFloat() / target).coerceIn(0f, 1f) else 0f
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                "${consumed}g / ${target}g",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        LiquidProgressBar(
            progress = progress,
            color = color,
            animate = animate,
            animationKey = label
        )
    }
}

/**
 * Reusable horizontal "liquid fill" progress bar with a wavy leading edge — the same
 * visual language as the water glass and calorie ring. Shared by the macro bars and
 * the Journal goals bar for full consistency.
 *
 * @param progress 0f..1f
 * @param animate when false, renders a calm static fill (no wave loop)
 * @param animationKey disambiguates animation labels when several bars share a screen
 */
@Composable
fun LiquidProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    barHeight: Dp = 12.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    animationKey: String = "liquidBar"
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(if (animate) 600 else 0),
        label = "liquidFill_$animationKey"
    )

    val wavePhase = if (animate) {
        val transition = rememberInfiniteTransition(label = "liquidWave_$animationKey")
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
            label = "liquidWavePhase_$animationKey"
        )
        p
    } else 0f

    val radiusPx = with(LocalDensity.current) { (barHeight / 2).toPx() }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(barHeight)
    ) {
        val w = size.width
        val h = size.height
        val track = Path().apply {
            addRoundRect(
                RoundRect(left = 0f, top = 0f, right = w, bottom = h, radiusX = radiusPx, radiusY = radiusPx)
            )
        }
        drawPath(track, color = trackColor)

        val fillRight = w * animated
        if (fillRight > 0.5f) {
            clipPath(track) {
                val amplitude = h * 0.30f * (if (animate) 1f else 0f)
                val wave = Path().apply {
                    moveTo(0f, 0f)
                    val steps = 10
                    for (i in 0..steps) {
                        val y = h * i / steps
                        val x = fillRight + amplitude *
                            sin(wavePhase + i.toFloat() / steps * 2f * PI.toFloat())
                        lineTo(x, y)
                    }
                    lineTo(0f, h)
                    close()
                }
                drawPath(wave, color = color)
            }
        }
    }
}
