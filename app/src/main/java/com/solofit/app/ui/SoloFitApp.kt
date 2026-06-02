package com.solofit.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.solofit.app.ui.dashboard.DashboardScreen
import com.solofit.app.ui.foodlookup.FoodLookupScreen
import com.solofit.app.ui.navigation.BottomDestination
import com.solofit.app.ui.navigation.Routes
import com.solofit.app.ui.nutrition.NutritionScreen
import com.solofit.app.ui.scan.ScanScreen
import com.solofit.app.ui.settings.EditProfileScreen
import com.solofit.app.ui.settings.SettingsScreen
import com.solofit.app.ui.reminders.RemindersScreen
import com.solofit.app.ui.devtools.PerfScreen
import com.solofit.app.ui.journal.JournalScreen
import com.solofit.app.ui.body.BodyScreen
import com.solofit.app.ui.phase.EditPhaseScreen
import com.solofit.app.ui.photos.ProgressPhotosScreen
import com.solofit.app.ui.strength.StrengthScreen
import com.solofit.app.ui.weight.WeightScreen
import com.solofit.app.ui.onboarding.OnboardingScreen
import com.solofit.app.ui.workout.ActiveWorkoutScreen
import com.solofit.app.ui.workout.HistoryScreen
import com.solofit.app.ui.workout.RoutineBuilderScreen
import com.solofit.app.ui.workout.WorkoutScreen

@Composable
fun SoloFitApp(rootViewModel: RootViewModel = hiltViewModel()) {
    val startState by rootViewModel.startState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    when (startState) {
        StartState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            val start = if (startState == StartState.Onboarding)
                Routes.ONBOARDING else Routes.DASHBOARD

            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val showBottomBar = currentRoute in BottomDestination.entries.map { it.route }

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar {
                            val currentDestination = backStackEntry?.destination
                            BottomDestination.entries.forEach { dest ->
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any {
                                        it.route == dest.route
                                    } == true,
                                    onClick = {
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                                    label = { Text(dest.label) }
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = start,
                    modifier = Modifier.padding(padding)
                ) {
                    composable(Routes.ONBOARDING) {
                        OnboardingScreen(
                            onComplete = {
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Routes.DASHBOARD) {
                        DashboardScreen(
                            onLogMeal = { navController.navigate(Routes.NUTRITION) },
                            onLogWorkout = { navController.navigate(Routes.WORKOUT) },
                            onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                            onOpenJournal = { navController.navigate(Routes.JOURNAL) },
                            onOpenBody = { navController.navigate(Routes.BODY) },
                            onEditPhase = { navController.navigate(Routes.EDIT_PHASE) },
                            onOpenHistory = { navController.navigate(Routes.HISTORY) },
                            onFoodLookup = { navController.navigate(Routes.FOOD_LOOKUP) }
                        )
                    }
                    composable(Routes.NUTRITION) {
                        NutritionScreen(
                            onScanBarcode = { navController.navigate(Routes.SCAN) },
                            onFoodLookup = { navController.navigate(Routes.FOOD_LOOKUP) }
                        )
                    }
                    composable(Routes.FOOD_LOOKUP) {
                        FoodLookupScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Routes.SCAN) {
                        ScanScreen(onClose = { navController.popBackStack() })
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                            onReminders = { navController.navigate(Routes.REMINDERS) },
                            onWeight = { navController.navigate(Routes.WEIGHT) },
                            onBody = { navController.navigate(Routes.BODY) },
                            onJournal = { navController.navigate(Routes.JOURNAL) },
                            onPerf = { navController.navigate(Routes.PERF) }
                        )
                    }
                    composable(Routes.EDIT_PROFILE) {
                        EditProfileScreen(onDone = { navController.popBackStack() })
                    }
                    composable(Routes.REMINDERS) {
                        RemindersScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.WEIGHT) {
                        WeightScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.BODY) {
                        BodyScreen(
                            onBack = { navController.popBackStack() },
                            onStrength = { navController.navigate(Routes.STRENGTH) },
                            onPhotos = { navController.navigate(Routes.PHOTOS) }
                        )
                    }
                    composable(Routes.EDIT_PHASE) {
                        EditPhaseScreen(onDone = { navController.popBackStack() })
                    }
                    composable(Routes.STRENGTH) {
                        StrengthScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.PHOTOS) {
                        ProgressPhotosScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.JOURNAL) {
                        JournalScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.PERF) {
                        PerfScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.WORKOUT) {
                        WorkoutScreen(
                            onCreateRoutine = {
                                navController.navigate(Routes.ROUTINE_BUILDER)
                            },
                            onEditRoutine = { id ->
                                navController.navigate("${Routes.ROUTINE_BUILDER}?routineId=$id")
                            },
                            onStartSession = { sessionId ->
                                navController.navigate("${Routes.ACTIVE_WORKOUT}/$sessionId")
                            }
                        )
                    }
                    composable(Routes.HISTORY) { HistoryScreen() }

                    composable(
                        route = "${Routes.ROUTINE_BUILDER}?routineId={routineId}",
                        arguments = listOf(navArgument("routineId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        })
                    ) {
                        RoutineBuilderScreen(onDone = { navController.popBackStack() })
                    }

                    composable(
                        route = "${Routes.ACTIVE_WORKOUT}/{sessionId}",
                        arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
                    ) {
                        ActiveWorkoutScreen(onFinish = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
