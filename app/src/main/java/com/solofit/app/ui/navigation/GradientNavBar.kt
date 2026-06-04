package com.solofit.app.ui.navigation

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val collapsedSize = 56.dp
private val expandedWidth = 160.dp
private val itemSpacing = 8.dp
private val tightLetterSpacing = TextUnit(-0.3f, TextUnitType.Sp)

@Composable
fun GradientNavBar(
    destinations: List<BottomDestination>,
    selectedDestination: BottomDestination?,
    onDestinationSelected: (BottomDestination) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .then(
                if (Build.VERSION.SDK_INT >= 31) Modifier.clip(RoundedCornerShape(28.dp)) else Modifier
            ),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        destinations.forEach { dest ->
            GradientNavItem(
                dest = dest,
                selected = selectedDestination == dest,
                onClick = { onDestinationSelected(dest) }
            )
        }
    }
}

@Composable
private fun GradientNavItem(
    dest: BottomDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val targetWidth by animateDpAsState(
        targetValue = if (selected) expandedWidth else collapsedSize,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "navWidth"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (selected) 0f else 1f,
        animationSpec = tween(200),
        label = "navIconAlpha"
    )

    val labelScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 350f),
        label = "navLabelScale"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (selected) 0.3f else 0.1f,
        animationSpec = tween(300),
        label = "navBorderAlpha"
    )

    val borderColor = dest.gradientFrom.copy(alpha = borderAlpha)

    Box(
        modifier = Modifier
            .size(width = targetWidth, height = collapsedSize)
            .shadow(
                elevation = if (selected) 10.dp else 4.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(dest.gradientFrom, dest.gradientTo)
                        )
                    )
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                }
            )
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = dest.icon,
            contentDescription = dest.label,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer(alpha = iconAlpha),
            tint = if (selected) Color.White
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Text(
            text = dest.label,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = tightLetterSpacing,
            modifier = Modifier.graphicsLayer(
                scaleX = labelScale,
                scaleY = labelScale,
                alpha = labelScale
            )
        )
    }
}
