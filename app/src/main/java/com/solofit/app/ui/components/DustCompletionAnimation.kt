package com.solofit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class DustParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val delay: Long
)

@Composable
fun DustCompletionAnimation(
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    val progress = remember { Animatable(0f) }
    val particles = remember {
        List(40) {
            DustParticle(
                angle = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 300f + 100f,
                size = Random.nextFloat() * 6f + 2f,
                color = Color(
                    red = Random.nextFloat() * 0.5f + 0.5f,
                    green = Random.nextFloat() * 0.3f + 0.3f,
                    blue = Random.nextFloat() * 0.2f + 0.2f,
                    alpha = 1f
                ),
                delay = Random.nextLong() * 200
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(800))
        onAnimationEnd()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val p = progress.value

        particles.forEach { particle ->
            val t = ((p * 1000 - particle.delay) / 800f).coerceIn(0f, 1f)
            if (t > 0f) {
                val distance = particle.speed * t
                val alpha = (1f - t).coerceIn(0f, 1f)
                val rad = Math.toRadians(particle.angle.toDouble())
                val x = cx + distance * cos(rad).toFloat()
                val y = cy + distance * sin(rad).toFloat()
                val s = particle.size * (1f - t * 0.5f)

                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = s,
                    center = Offset(x, y)
                )
            }
        }
    }
}
