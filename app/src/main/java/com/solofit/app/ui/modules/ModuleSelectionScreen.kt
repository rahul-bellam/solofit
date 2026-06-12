package com.solofit.app.ui.modules

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.ui.theme.SolAccent
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import androidx.compose.material3.MaterialTheme

@Composable
fun ModuleSelectionScreen(
    selected: Set<SoloFitModule>,
    onToggle: (SoloFitModule) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            "Build Your Wellness System",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Choose what matters most right now. You can add or remove modules anytime.",
            fontSize = 14.sp,
            color = SecondaryText,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(28.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SoloFitModule.entries) { module ->
                ModuleCard(
                    module = module,
                    isSelected = module in selected,
                    onToggle = { onToggle(module) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Box(Modifier.padding(20.dp)) {
            Button(
                onClick = onContinue,
                enabled = selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SolAccent)
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ModuleCard(
    module: SoloFitModule,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) SolAccent else Color(0xFFE5E7EB),
        animationSpec = tween(200), label = "border"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) SolAccent else SecondaryText,
        animationSpec = tween(200), label = "icon"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                border = if (isSelected) BorderStroke(1.5.dp, SolAccent) else BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onToggle)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(if (isSelected) SolAccent.copy(alpha = 0.12f) else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    moduleIcon(module),
                    null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(module.displayName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                Text(module.description, fontSize = 12.sp, color = SecondaryText)
            }
            if (isSelected) {
                Icon(Icons.Filled.CheckCircle, null, tint = SolAccent, modifier = Modifier.size(22.dp))
            }
        }
    }
}

fun moduleIcon(module: SoloFitModule): ImageVector = when (module) {
    SoloFitModule.WORKOUTS -> Icons.Filled.FitnessCenter
    SoloFitModule.NUTRITION -> Icons.Filled.Restaurant
    SoloFitModule.RECOVERY -> Icons.Filled.Favorite
    SoloFitModule.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    SoloFitModule.MEDITATION -> Icons.Filled.SelfImprovement
    SoloFitModule.JOURNAL -> Icons.AutoMirrored.Filled.MenuBook
    SoloFitModule.YOGA -> Icons.Filled.SelfImprovement
    SoloFitModule.BODY_RECOMPOSITION -> Icons.AutoMirrored.Filled.TrendingUp
    SoloFitModule.HABITS -> Icons.AutoMirrored.Filled.Assignment
    SoloFitModule.PROGRESS -> Icons.Filled.BarChart
}
