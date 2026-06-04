package com.solofit.app.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    val originalX: Float,
    val originalY: Float,
    var alpha: Float,
    val color: Int,
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var speed: Float = 0f,
    var angle: Float = 0f,
    var shouldFadeQuickly: Boolean = false
)

enum class VaporizeDirection { LEFT_TO_RIGHT, RIGHT_TO_LEFT }

@Composable
fun VaporizeTextCycle(
    texts: List<String>,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 50.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    spread: Float = 5f,
    density: Float = 5f,
    vaporizeDurationMs: Long = 2000,
    fadeInDurationMs: Long = 1000,
    waitDurationMs: Long = 1500,
    direction: VaporizeDirection = VaporizeDirection.LEFT_TO_RIGHT,
    animate: Boolean = true,
    onCycleComplete: () -> Unit = {}
) {
    if (texts.isEmpty()) return

    if (!animate) {
        LaunchedEffect(Unit) { onCycleComplete() }
        return
    }

    val textColorArgb = remember(color) { color.toArgb() }
    val densityVal = remember(density) { (density / 10f).coerceIn(0.3f, 1f) }
    val densityDensity = LocalDensity.current
    val fontSizePx = with(densityDensity) { fontSize.toPx() }

    var state by remember { mutableStateOf("static") }
    val particles = remember { mutableStateListOf<Particle>() }
    val textIndex = remember { mutableIntStateOf(0) }
    val vaporizeProgress = remember { mutableFloatStateOf(0f) }
    val fadeOpacity = remember { mutableFloatStateOf(0f) }

    fun sampleText(text: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            setColor(textColorArgb)
            textSize = fontSizePx
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val measuredTextWidth = paint.measureText(text)
        val w = (measuredTextWidth * 1.3f).toInt().coerceAtLeast(10)
        val h = (fontSizePx * 2f).toInt().coerceAtLeast(10)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val cv = android.graphics.Canvas(bmp)
        val textY = h / 2f + fontSizePx * 0.35f
        cv.drawText(text, measuredTextWidth / 2f, textY, paint)

        val stride = (2f / densityVal).toInt().coerceIn(1, 4)
        val newParticles = mutableListOf<Particle>()
        for (y in 0 until h step stride) {
            for (x in 0 until w step stride) {
                val pixel = bmp.getPixel(x, y)
                val a = android.graphics.Color.alpha(pixel)
                if (a > 10) {
                    val alphaVal = a / 255f
                    newParticles.add(Particle(
                        x = x.toFloat() - measuredTextWidth / 2f,
                        y = y.toFloat() - h / 2f,
                        originalX = x.toFloat() - measuredTextWidth / 2f,
                        originalY = y.toFloat() - h / 2f,
                        alpha = alphaVal,
                        color = textColorArgb,
                        shouldFadeQuickly = Random.nextFloat() > densityVal
                    ))
                }
            }
        }
        bmp.recycle()
        particles.clear()
        particles.addAll(newParticles)
    }

    LaunchedEffect(texts) {
        withContext(kotlinx.coroutines.Dispatchers.Default) { sampleText(texts[0]) }
        state = "static"
        textIndex.intValue = 0
        vaporizeProgress.floatValue = 0f
        fadeOpacity.floatValue = 1f
    }

    LaunchedEffect(state) {
        if (state == "static") {
            delay(800)
            state = "vaporizing"
        }
    }

    LaunchedEffect(state) {
        val frameTime = 16L
        while (isActive) {
            when (state) {
                "vaporizing" -> {
                    vaporizeProgress.floatValue += frameTime / vaporizeDurationMs.toFloat()
                    if (vaporizeProgress.floatValue > 1f) vaporizeProgress.floatValue = 1f

                    val progress = vaporizeProgress.floatValue
                    val halfW = (particles.maxOfOrNull { abs(it.originalX) } ?: 0f)
                    val left = -halfW
                    val right = halfW
                    val totalWidth = (right - left).coerceAtLeast(1f)
                    val vaporizeX = when (direction) {
                        VaporizeDirection.LEFT_TO_RIGHT -> left + totalWidth * progress
                        VaporizeDirection.RIGHT_TO_LEFT -> right - totalWidth * progress
                    }

                    var allDone = true
                    for (p in particles) {
                        val shouldVaporize = when (direction) {
                            VaporizeDirection.LEFT_TO_RIGHT -> p.originalX <= vaporizeX
                            VaporizeDirection.RIGHT_TO_LEFT -> p.originalX >= vaporizeX
                        }
                        if (shouldVaporize) {
                            if (p.speed == 0f) {
                                p.angle = Random.nextFloat() * 2f * kotlin.math.PI.toFloat()
                                p.speed = (Random.nextFloat() * 1f + 0.5f) * spread * 15f
                                p.velocityX = cos(p.angle) * p.speed
                                p.velocityY = sin(p.angle) * p.speed
                            }
                            if (p.shouldFadeQuickly) {
                                p.alpha = maxOf(0f, p.alpha - 0.05f)
                            } else {
                                val dx = p.originalX - p.x
                                val dy = p.originalY - p.y
                                val dist = sqrt(dx * dx + dy * dy)
                                val damping = maxOf(0.95f, 1f - dist / (400f * spread))
                                p.velocityX += (Random.nextFloat() - 0.5f) * spread * 5f + dx * 0.002f
                                p.velocityY += (Random.nextFloat() - 0.5f) * spread * 5f + dy * 0.002f
                                p.velocityX *= damping
                                p.velocityY *= damping
                                val maxVel = spread * 30f
                                val vel = sqrt(p.velocityX * p.velocityX + p.velocityY * p.velocityY)
                                if (vel > maxVel) {
                                    val scale = maxVel / vel
                                    p.velocityX *= scale
                                    p.velocityY *= scale
                                }
                                p.x += p.velocityX * (frameTime / 16f)
                                p.y += p.velocityY * (frameTime / 16f)
                                p.alpha = maxOf(0f, p.alpha - 0.005f * (2000f / vaporizeDurationMs))
                            }
                            if (p.alpha > 0.01f) allDone = false
                        } else {
                            allDone = false
                        }
                    }

                    if (progress >= 1f && allDone && particles.isNotEmpty()) {
                        vaporizeProgress.floatValue = 0f
                        val nextIdx = (textIndex.intValue + 1) % texts.size
                        textIndex.intValue = nextIdx
                        withContext(kotlinx.coroutines.Dispatchers.Default) { sampleText(texts[nextIdx]) }
                        fadeOpacity.floatValue = 0f
                        state = "fadingIn"
                    }
                    delay(frameTime)
                }
                "fadingIn" -> {
                    fadeOpacity.floatValue += frameTime / fadeInDurationMs.toFloat()
                    if (fadeOpacity.floatValue > 1f) {
                        fadeOpacity.floatValue = 1f
                        state = "waiting"
                    }
                    delay(frameTime)
                }
                "waiting" -> {
                    delay(waitDurationMs)
                    if (textIndex.intValue == 0) {
                        onCycleComplete()
                    }
                    state = "vaporizing"
                    vaporizeProgress.floatValue = 0f
                    for (p in particles) {
                        p.speed = 0f
                        p.velocityX = 0f
                        p.velocityY = 0f
                        p.x = p.originalX
                        p.y = p.originalY
                        p.alpha = p.alpha.coerceAtLeast(0.3f)
                    }
                    delay(frameTime)
                }
                else -> delay(frameTime)
            }
        }
    }

    Canvas(modifier = modifier) {
        val progress = vaporizeProgress.floatValue
        val opacity = fadeOpacity.floatValue
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseColor = Color(textColorArgb)
        for (p in particles) {
            if (p.alpha > 0.005f) {
                val alpha = when (state) {
                    "fadingIn" -> p.alpha * opacity.coerceIn(0f, 1f)
                    else -> p.alpha
                }.coerceIn(0f, 1f)
                val strokeW = if (state == "fadingIn") 2.5f else 1.5f
                drawPoints(
                    points = listOf(Offset(cx + p.x, cy + p.y)),
                    pointMode = PointMode.Points,
                    color = baseColor.copy(alpha = alpha),
                    strokeWidth = strokeW
                )
            }
        }
    }
}

@Composable
fun VaporizeCelebration(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 60.sp,
    spread: Float = 6f,
    density: Float = 6f,
    vaporizeDurationMs: Long = 2500,
    animate: Boolean = true,
    onAnimationEnd: () -> Unit = {}
) {
    var show by remember { mutableStateOf(true) }
    if (show) {
        VaporizeTextCycle(
            animate = animate,
            texts = listOf(text),
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            spread = spread,
            density = density,
            vaporizeDurationMs = vaporizeDurationMs,
            fadeInDurationMs = 800,
            waitDurationMs = 200,
            direction = VaporizeDirection.LEFT_TO_RIGHT,
            onCycleComplete = {
                show = false
                onAnimationEnd()
            }
        )
    }
}
