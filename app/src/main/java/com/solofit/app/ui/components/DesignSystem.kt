package com.solofit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solofit.app.ui.theme.SolAccent
import com.solofit.app.ui.theme.CardPrimary
import com.solofit.app.ui.theme.DarkSurface
import com.solofit.app.ui.theme.DarkText
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

val AppCardShape = RoundedCornerShape(24.dp)

// ─── Flat card (no elevation, no shadows) ───
@Composable
fun WellnessCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CardPrimary,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        shape = AppCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun WellnessStaticCard(
    modifier: Modifier = Modifier,
    containerColor: Color = CardPrimary,
    content: @Composable () -> Unit
) {
    Card(
        shape = AppCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun WellnessDarkCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = AppCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = modifier
    ) {
        content()
    }
}
