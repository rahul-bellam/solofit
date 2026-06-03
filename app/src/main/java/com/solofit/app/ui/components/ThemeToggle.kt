package com.solofit.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private val toggleDuration = 700
private val green = Color(0xFF5F8E5A)

@Composable
fun AnimatedThemeToggle(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = if (isDark) 1f else 0f,
        animationSpec = tween(toggleDuration),
        label = "themeToggle"
    )

    Button(
        onClick = onToggle,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        ThemeToggleIcon(animProgress, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ThemeToggleIcon(
    animProgress: Float,
    modifier: Modifier = Modifier
) {
    val isDark = animProgress > 0.5f

    val sunAlpha = (1f - animProgress).coerceIn(0f, 1f)
    val sunScale = 1f - animProgress * 0.3f
    val moonAlpha = animProgress
    val moonScale = animProgress

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Sun rays (8 rays)
        Canvas(Modifier.size(20.dp).graphicsLayer(alpha = sunAlpha, scaleX = sunScale, scaleY = sunScale)) {
            val c = Offset(size.width / 2, size.height / 2)
            val rayLen = size.width * 0.18f
            val circleR = size.width * 0.22f
            val rayStart = circleR + 2f
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45).toDouble())
                val sx = c.x + rayStart * Math.cos(angle).toFloat()
                val sy = c.y + rayStart * Math.sin(angle).toFloat()
                val ex = c.x + (rayStart + rayLen) * Math.cos(angle).toFloat()
                val ey = c.y + (rayStart + rayLen) * Math.sin(angle).toFloat()
                drawLine(
                    color = Color(0xFF3f3e36),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 1.8f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(Color(0xFF3f3e36), circleR, c)
        }

        // Moon
        Canvas(Modifier.size(20.dp).graphicsLayer(alpha = moonAlpha, scaleX = moonScale, scaleY = moonScale)) {
            val c = Offset(size.width / 2, size.height / 2)
            val r = size.width * 0.35f
            drawCircle(Color(0xFFe8e4d9), r, c)
            drawCircle(
                color = Color(0xFF1a1a1a),
                radius = r * 0.85f,
                center = Offset(c.x + r * 0.3f, c.y - r * 0.2f)
            )
        }
    }
}
