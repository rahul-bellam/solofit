package com.solofit.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = Sage,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A4A34),
    onPrimaryContainer = Color(0xFFB7F5D4),
    secondary = SageDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1A3D2C),
    onSecondaryContainer = Color(0xFF9FE5C3),
    tertiary = Sage.copy(alpha = 0.7f),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF0E3325),
    onTertiaryContainer = Color(0xFF7DD6AA),
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    error = Color(0xFFCF6679),
    onError = Color.Black,
    outline = OnDarkSurfaceVariant,
    inversePrimary = Sage,
    surfaceTint = Sage
)

private val LightColors = lightColorScheme(
    primary = Sage,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4EDE0),
    onPrimaryContainer = Color(0xFF0A3A20),
    secondary = SageDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F9F0),
    onSecondaryContainer = Color(0xFF0A2E1A),
    tertiary = Sage.copy(alpha = 0.8f),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFC2E0D0),
    onTertiaryContainer = Color(0xFF062412),
    background = LightBackground,
    onBackground = OnLightBackground,
    surface = LightSurface,
    onSurface = OnLightSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightSurfaceVariant,
    error = Color(0xFFB00020),
    onError = Color.White,
    outline = OnLightSurfaceVariant,
    inversePrimary = SageDark,
    surfaceTint = Sage
)

@Composable
fun SoloFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
