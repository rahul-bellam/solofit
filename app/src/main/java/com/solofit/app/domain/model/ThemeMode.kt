package com.solofit.app.domain.model

/** User-selectable app theme. SYSTEM follows the OS light/dark setting. */
enum class ThemeMode(val displayName: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark")
}
