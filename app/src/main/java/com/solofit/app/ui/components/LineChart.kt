package com.solofit.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Minimal dependency-free line chart for a numeric series (e.g. body weight).
 * Draws a smoothed-ish polyline with point markers, auto-scaled to min/max.
 */
@Composable
fun LineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    pointColor: Color = MaterialTheme.colorScheme.secondary
) {
    if (values.size < 2) {
        Box(
            modifier
                .fillMaxWidth()
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Log at least two entries to see your trend.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val minV = values.min()
    val maxV = values.max()
    val range = (maxV - minV).takeIf { it > 0f } ?: 1f

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val padding = 16f
        val w = size.width - padding * 2
        val h = size.height - padding * 2
        val stepX = if (values.size > 1) w / (values.size - 1) else w

        fun pointAt(i: Int): Offset {
            val x = padding + stepX * i
            val norm = (values[i] - minV) / range
            val y = padding + (1f - norm) * h
            return Offset(x, y)
        }

        // Baseline grid (top/bottom)
        drawLine(
            color = lineColor.copy(alpha = 0.15f),
            start = Offset(padding, padding),
            end = Offset(padding + w, padding),
            strokeWidth = 2f
        )
        drawLine(
            color = lineColor.copy(alpha = 0.15f),
            start = Offset(padding, padding + h),
            end = Offset(padding + w, padding + h),
            strokeWidth = 2f
        )

        val path = Path().apply {
            val first = pointAt(0)
            moveTo(first.x, first.y)
            for (i in 1 until values.size) {
                val p = pointAt(i)
                lineTo(p.x, p.y)
            }
        }
        drawPath(path, color = lineColor, style = Stroke(width = 6f))

        for (i in values.indices) {
            drawCircle(color = pointColor, radius = 7f, center = pointAt(i))
        }
    }
}
