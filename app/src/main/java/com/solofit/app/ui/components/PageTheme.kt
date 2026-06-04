package com.solofit.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.runtime.remember
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.Cyan
import com.solofit.app.ui.theme.Emerald
import com.solofit.app.ui.theme.Moss
import com.solofit.app.ui.theme.Sage
import com.solofit.app.ui.theme.SageDark
import com.solofit.app.ui.theme.WorkoutTypography
import com.solofit.app.ui.theme.NutritionTypography
import com.solofit.app.ui.theme.AppTypography

data class PageAccent(
    val primary: Color,
    val secondary: Color = primary,
    val container: Color = primary.copy(alpha = 0.15f),
    val onContainer: Color = primary,
    val name: String = "",
    val typography: Typography = AppTypography
)

val LocalPageAccent = staticCompositionLocalOf {
    PageAccent(primary = Sage, secondary = SageDark, name = "default")
}

val DefaultAccent = PageAccent(primary = Sage, secondary = SageDark, name = "default", typography = AppTypography)
val WorkoutAccent = PageAccent(primary = Amber, secondary = Cyan, name = "workout", typography = WorkoutTypography)
val NutritionAccent = PageAccent(primary = Emerald, secondary = Emerald, name = "nutrition", typography = NutritionTypography)
val StrengthAccent = PageAccent(primary = Moss, secondary = Moss, name = "strength", typography = AppTypography)

@Composable
fun WorkoutTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides WorkoutAccent) {
        MaterialTheme(typography = WorkoutTypography) { content() }
    }
}

@Composable
fun NutritionTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides NutritionAccent) {
        MaterialTheme(typography = NutritionTypography) { content() }
    }
}

@Composable
fun StrengthTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides StrengthAccent) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun JournalTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides DefaultAccent) {
        MaterialTheme(typography = NutritionTypography) { content() }
    }
}


