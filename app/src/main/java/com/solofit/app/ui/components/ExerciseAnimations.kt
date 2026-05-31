package com.solofit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Playful exercise micro-animations drawn with **Compose Canvas + the animation
 * APIs already in the project** — no Lottie, no GIFs, no bitmaps → zero extra
 * APK/asset footprint.
 *
 * All of them accept `animate`: pass false (user disabled "fun animations", or the
 * OS has reduce-motion on) to render a calm static state instead.
 */

private fun DrawScope.drawDumbbell(color: Color, fill: Float) {
    val w = size.width
    val h = size.height
    val cy = h / 2f
    val barThickness = h * 0.12f
    val plateW = w * 0.16f
    val plateH = h * (0.45f + 0.35f * fill) // plates grow slightly when "lifted"
    val barColor = color.copy(alpha = 0.35f + 0.65f * fill)

    drawLine(
        color = barColor,
        start = Offset(w * 0.22f, cy),
        end = Offset(w * 0.78f, cy),
        strokeWidth = barThickness,
        cap = StrokeCap.Round
    )
    drawRoundRect(
        color = barColor,
        topLeft = Offset(w * 0.12f, cy - plateH / 2),
        size = Size(plateW, plateH),
        cornerRadius = CornerRadius(plateW * 0.4f)
    )
    drawRoundRect(
        color = barColor,
        topLeft = Offset(w * 0.72f, cy - plateH / 2),
        size = Size(plateW, plateH),
        cornerRadius = CornerRadius(plateW * 0.4f)
    )
}

/**
 * A checkbox that "curls" a dumbbell up with a little rep-bounce when checked.
 * Tap toggles. Falls back to a static dumbbell when [animate] is false.
 */
@Composable
fun DumbbellCheck(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    size: Dp = 34.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    idleColor: Color = MaterialTheme.colorScheme.outline
) {
    val lift = remember { Animatable(if (checked) 1f else 0f) }
    LaunchedEffect(checked, animate) {
        when {
            !animate -> lift.snapTo(if (checked) 1f else 0f)
            checked -> {
                lift.animateTo(1.15f, tween(180, easing = FastOutSlowInEasing)) // overshoot
                lift.animateTo(1f, tween(160, easing = FastOutSlowInEasing))     // settle
            }
            else -> lift.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(onClickLabel = "Toggle goal") { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size)) {
            val t = lift.value
            val dy = -this.size.height * 0.12f * t
            translate(top = dy) {
                drawDumbbell(if (t > 0.02f) color else idleColor, t.coerceIn(0f, 1f))
            }
        }
    }
}

/**
 * A figure pressing a barbell overhead. [press] = 0f (bar at shoulders) .. 1f
 * (locked out overhead). The caller feeds scroll position in, so scrolling
 * literally performs the rep.
 */
@Composable
fun OverheadPressHeader(
    press: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val p = press.coerceIn(0f, 1f)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val headR = h * 0.10f
        val stroke = h * 0.05f
        val hipY = h * 0.92f
        val shoulderY = h * 0.45f
        val barTopY = shoulderY - (shoulderY - h * 0.12f) * p
        val c = color.copy(alpha = 0.9f)

        // legs
        drawLine(c, Offset(cx, hipY), Offset(cx - w * 0.06f, h * 0.99f), stroke, StrokeCap.Round)
        drawLine(c, Offset(cx, hipY), Offset(cx + w * 0.06f, h * 0.99f), stroke, StrokeCap.Round)
        // torso
        drawLine(c, Offset(cx, hipY), Offset(cx, shoulderY), stroke, StrokeCap.Round)
        // head
        drawCircle(c, headR, Offset(cx, shoulderY - headR * 1.2f))
        // arms -> hands at the bar
        drawLine(c, Offset(cx, shoulderY), Offset(cx - w * 0.10f, barTopY), stroke, StrokeCap.Round)
        drawLine(c, Offset(cx, shoulderY), Offset(cx + w * 0.10f, barTopY), stroke, StrokeCap.Round)
        // barbell
        drawLine(color, Offset(cx - w * 0.20f, barTopY), Offset(cx + w * 0.20f, barTopY),
            stroke * 1.3f, StrokeCap.Round)
        // plates
        val plateH = h * 0.18f
        drawRoundRect(color, Offset(cx - w * 0.24f, barTopY - plateH / 2),
            Size(w * 0.04f, plateH), CornerRadius(6f))
        drawRoundRect(color, Offset(cx + w * 0.20f, barTopY - plateH / 2),
            Size(w * 0.04f, plateH), CornerRadius(6f))
    }
}

/**
 * Lat-pulldown flourish shown when every goal is done. Gently loops; static bar
 * when [animate] is false.
 */
@Composable
fun LatPulldownCelebration(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val pull = if (animate) {
        val transition = rememberInfiniteTransition(label = "latpull")
        val v by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pull"
        )
        v
    } else 0.6f

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val stroke = h * 0.06f
        val barY = h * (0.25f + 0.35f * pull)
        // cable
        drawLine(color.copy(alpha = 0.4f), Offset(cx, 0f), Offset(cx, barY - h * 0.05f),
            stroke * 0.5f, StrokeCap.Round)
        // pulldown bar
        drawLine(color, Offset(cx - w * 0.28f, barY), Offset(cx + w * 0.28f, barY),
            stroke, StrokeCap.Round)
        // grips
        drawLine(color.copy(alpha = 0.8f), Offset(cx - w * 0.22f, barY),
            Offset(cx - w * 0.22f, barY + h * 0.12f), stroke * 0.7f, StrokeCap.Round)
        drawLine(color.copy(alpha = 0.8f), Offset(cx + w * 0.22f, barY),
            Offset(cx + w * 0.22f, barY + h * 0.12f), stroke * 0.7f, StrokeCap.Round)
    }
}

/**
 * A glass that "fills up" toward the goal with a gentle wave on the surface.
 * [fraction] 0f..1f is animated; the wave only animates when [animate] is true.
 * Pure Canvas — no assets.
 */
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
        // glass as a slightly tapered cup
        val topInset = w * 0.12f
        val bottomInset = w * 0.20f
        val glassPath = Path().apply {
            moveTo(topInset, h * 0.06f)
            lineTo(w - topInset, h * 0.06f)
            lineTo(w - bottomInset, h * 0.96f)
            lineTo(bottomInset, h * 0.96f)
            close()
        }

        // water fills bottom-up, clipped to the glass shape
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

        // glass outline on top
        drawPath(
            glassPath,
            color = glassColor,
            style = Stroke(width = h * 0.025f)
        )
    }
}
