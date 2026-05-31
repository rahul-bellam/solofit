package com.solofit.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val NUTRITION = "nutrition"
    const val WORKOUT = "workout"
    const val HISTORY = "history"

    const val ROUTINE_BUILDER = "routine_builder"        // ?routineId={id}
    const val ACTIVE_WORKOUT = "active_workout"           // /{sessionId}

    const val SCAN = "scan"                               // barcode pipeline
    const val PHOTO = "photo"                             // photo classification pipeline

    const val SETTINGS = "settings"
    const val EDIT_PROFILE = "edit_profile"
    const val REMINDERS = "reminders"
    const val WEIGHT = "weight"
    const val JOURNAL = "journal"
    const val PERF = "perf"
    const val BODY = "body"
    const val EDIT_PHASE = "edit_phase"
    const val STRENGTH = "strength"
    const val PHOTOS = "photos"
}

enum class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD(Routes.DASHBOARD, "Home", Icons.Filled.Home),
    NUTRITION(Routes.NUTRITION, "Nutrition", Icons.Filled.Restaurant),
    WORKOUT(Routes.WORKOUT, "Workouts", Icons.Filled.FitnessCenter),
    HISTORY(Routes.HISTORY, "History", Icons.Filled.CalendarMonth)
}
