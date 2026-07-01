package com.solofit.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary

/** Compact inline water widget. Prominent full card when [prominent] is true. */
@Composable
fun WaterTracker(
    currentMl: Int,
    goalMl: Int,
    onAdd: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    glassMl: Int = 250,
    prominent: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.secondary,
    reason: String = ""
) {
    val fraction = if (goalMl > 0) (currentMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
    val goalHit = goalMl > 0 && currentMl >= goalMl

    if (prominent) {
        ProminentWaterCard(currentMl, goalMl, fraction, goalHit, onAdd, onRemove, glassMl, accentColor, reason, modifier)
    } else {
        CompactWaterWidget(currentMl, goalMl, fraction, goalHit, onAdd, onRemove, glassMl, accentColor, modifier)
    }
}

@Composable
private fun CompactWaterWidget(
    currentMl: Int,
    goalMl: Int,
    fraction: Float,
    goalHit: Boolean,
    onAdd: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    glassMl: Int,
    accentColor: Color,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val waterPct = if (goalMl > 0) (currentMl * 100 / goalMl).coerceAtMost(100) else 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .clickable { expanded = !expanded }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Filled.WaterDrop,
            contentDescription = null,
            tint = if (goalHit) accentColor else accentColor.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Water",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "$currentMl / $goalMl ml",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(3.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f))
                .clickable { onAdd(glassMl) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add water", tint = accentColor, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ProminentWaterCard(
    currentMl: Int,
    goalMl: Int,
    fraction: Float,
    goalHit: Boolean,
    onAdd: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    glassMl: Int,
    accentColor: Color,
    reason: String,
    modifier: Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.06f))
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WaterDrop, null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hydration", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                Text("$currentMl / $goalMl ml", fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.12f))
            ) {
                Box(
                    Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor)
                )
            }
            if (reason.isNotEmpty()) {
                Text(reason, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                Spacer(Modifier.height(10.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.1f))
                        .clickable { onRemove(glassMl) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("-$glassMl ml", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = accentColor)
                }
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(accentColor)
                        .clickable { onAdd(glassMl) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+$glassMl ml", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}
