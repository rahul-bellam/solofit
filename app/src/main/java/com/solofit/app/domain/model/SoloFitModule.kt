package com.solofit.app.domain.model

import com.solofit.app.ui.navigation.Routes

enum class SoloFitModule(
    val id: String,
    val displayName: String,
    val description: String,
    val route: String
) {
    WORKOUTS("workouts", "Workouts", "Track training sessions and build strength", Routes.WORKOUT),
    NUTRITION("nutrition", "Nutrition", "Log meals and track macros", Routes.NUTRITION),
    RECOVERY("recovery", "Recovery", "Monitor sleep, stress, and readiness", Routes.RECOVERY),
    WALKING("walking", "Walking", "Track walks and daily movement", Routes.WALKING),
    MEDITATION("meditation", "Meditation", "Build mindfulness with guided sessions", Routes.MEDITATION),
    JOURNAL("journal", "Journal", "Reflect with gratitude and daily goals", Routes.JOURNAL),
    YOGA("yoga", "Yoga", "Flexibility and mobility practice", Routes.YOGA),
    BODY_RECOMPOSITION("body_recomposition", "Body Recomposition", "Track measurements and progress photos", Routes.BODY),
    HABITS("habits", "Habits", "Build and maintain daily habits", Routes.HABITS),
    PROGRESS("progress", "Progress", "View your journey and achievements", Routes.PROGRESS);

    companion object {
        val DEFAULT_ENABLED = listOf(WORKOUTS, NUTRITION, PROGRESS, WALKING, HABITS)
        fun fromId(id: String): SoloFitModule? = entries.find { it.id == id }
    }
}
