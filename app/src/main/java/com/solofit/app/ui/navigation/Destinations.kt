package com.solofit.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val ONBOARDING = "onboarding"
    const val MODULE_SELECTION = "module_selection"
    const val MODULE_MANAGEMENT = "module_management"
    const val DASHBOARD = "dashboard"
    const val NUTRITION = "nutrition"
    const val WORKOUT = "workout"
    const val WALKING = "walking"
    const val YOGA = "yoga"
    const val HABITS = "habits"

    const val ROUTINE_BUILDER = "routine_builder"
    const val ACTIVE_WORKOUT = "active_workout"

    const val SCAN = "scan"

    const val SETTINGS = "settings"
    const val EDIT_PROFILE = "edit_profile"
    const val REMINDERS = "reminders"
    const val WEIGHT = "weight"
    const val JOURNAL = "journal"
    const val BODY = "body"
    const val STRENGTH = "strength"
    const val PHOTOS = "photos"
    const val FOOD_LOOKUP = "food_lookup"
    const val WEEKLY_PLANNER = "weekly_planner"
    const val RECOVERY = "recovery"
    const val STRESS = "stress"
    const val MEDITATION = "meditation"
    const val PROGRESS = "progress"
    const val SOL = "sol"

    const val FRIENDS = "friends"
    const val ADD_FRIEND = "add_friend"
    const val FRIEND_DETAIL = "friend_detail/{friendId}"
    const val GROUPS = "groups"

    fun friendDetail(friendId: Long) = "friend_detail/$friendId"
}

data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val gradientFrom: Color = Color.Transparent,
    val gradientTo: Color = Color.Transparent
) {
    companion object {
        val HOME = BottomDestination(
            route = Routes.DASHBOARD,
            label = "Home",
            icon = Icons.Filled.Home
        )
        val SOL = BottomDestination(
            route = Routes.SOL,
            label = "Sol",
            icon = Icons.Filled.AutoAwesome
        )
    }
}
