package com.solofit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = HoneyGold,
    onPrimary = Color(0xFF1E1C1A),
    primaryContainer = CardSecondary,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = SurfaceBg,
    secondaryContainer = CardSecondary,
    onSecondaryContainer = TextPrimary,
    tertiary = HoneyGold,
    background = SurfaceBg,
    onBackground = TextPrimary,
    surface = CardPrimary,
    onSurface = TextPrimary,
    surfaceVariant = CardSecondary,
    onSurfaceVariant = TextSecondary,
    error = DarkError,
    onError = TextPrimary,
    outline = Hairline,
    outlineVariant = Hairline,
    inverseSurface = TextPrimary,
    inverseOnSurface = SurfaceBg,
    inversePrimary = HoneyGold,
    surfaceTint = HoneyGold
)

private val LightColorScheme = lightColorScheme(
    primary = Clay,
    onPrimary = Color.White,
    primaryContainer = LightCardSecondary,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightCardSecondary,
    onSecondaryContainer = LightTextPrimary,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightCardPrimary,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCardSecondary,
    onSurfaceVariant = LightTextSecondary,
    error = LowRed,
    onError = Color.White,
    outline = LightHairline,
    outlineVariant = LightHairline,
    inversePrimary = Clay,
    surfaceTint = Clay
)

@Composable
fun SoloFitTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
