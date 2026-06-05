package com.solofit.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.AppTypography
import com.solofit.app.ui.theme.NutritionAccent
import com.solofit.app.ui.theme.RecoveryAccent
import com.solofit.app.ui.theme.MeditationAccent
import com.solofit.app.ui.theme.JournalAccent
import com.solofit.app.ui.theme.ProgressAccent

data class PageAccent(
    val primary: Color = Amber,
    val name: String = "default"
)

val LocalPageAccent = staticCompositionLocalOf { PageAccent() }

val DefaultAccent = PageAccent(primary = Amber)

@Composable
fun NutritionTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = NutritionAccent, name = "nutrition")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun JournalTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = JournalAccent, name = "journal")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun ProgressTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = ProgressAccent, name = "progress")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun RecoveryTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = RecoveryAccent, name = "recovery")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun MeditationTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = MeditationAccent, name = "meditation")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun TrainingTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = Amber, name = "training")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun WorkoutTheme(content: @Composable () -> Unit) = TrainingTheme(content)

@Composable
fun StrengthTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = Amber, name = "strength")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}
