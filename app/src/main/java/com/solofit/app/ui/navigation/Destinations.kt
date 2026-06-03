package com.solofit.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.Clay
import com.solofit.app.ui.theme.Emerald
import com.solofit.app.ui.theme.Taupe

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val NUTRITION = "nutrition"
    const val WORKOUT = "workout"
    const val HISTORY = "history"

    const val ROUTINE_BUILDER = "routine_builder"
    const val ACTIVE_WORKOUT = "active_workout"

    const val SCAN = "scan"

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
    const val FOOD_LOOKUP = "food_lookup"
    const val WEEKLY_PLANNER = "weekly_planner"
}

enum class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val gradientFrom: Color,
    val gradientTo: Color
) {
    DASHBOARD(Routes.DASHBOARD, "Home", Icons.Filled.Home, Clay, Clay),
    NUTRITION(Routes.NUTRITION, "Nutrition", Icons.Filled.Restaurant, Clay, Clay),
    WORKOUT(Routes.WORKOUT, "Workouts", Icons.Filled.FitnessCenter, Clay, Clay),
    HISTORY(Routes.HISTORY, "History", Icons.Filled.CalendarMonth, Clay, Clay)
}
