package com.solofit.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.solofit.app.ui.theme.AppTypography
import com.solofit.app.ui.theme.JournalAccent
import com.solofit.app.ui.theme.LavenderGrey
import com.solofit.app.ui.theme.MossGreen
import com.solofit.app.ui.theme.RustIron
import com.solofit.app.ui.theme.SlateBlue
import com.solofit.app.ui.theme.SolAccent
import com.solofit.app.ui.theme.TwilightBlue
import com.solofit.app.ui.theme.WalkingAccent

data class PageAccent(
    val primary: Color = SolAccent,
    val name: String = "default"
)

val LocalPageAccent = staticCompositionLocalOf { PageAccent() }
val DefaultAccent = PageAccent(primary = SolAccent)

@Composable
fun NutritionTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = MossGreen, name = "nutrition")) {
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
fun BodyRecompTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = SlateBlue, name = "bodyrecomp")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun ProgressTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = SlateBlue, name = "progress")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun RecoveryTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = TwilightBlue, name = "recovery")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun MeditationTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = LavenderGrey, name = "meditation")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun BodyTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = SlateBlue, name = "body")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun WalkingPageTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = WalkingAccent, name = "walking")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun WorkoutTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = RustIron, name = "workout")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun StrengthTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = RustIron, name = "strength")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}

@Composable
fun TrainingTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = RustIron, name = "training")) {
        MaterialTheme(typography = AppTypography) { content() }
    }
}
