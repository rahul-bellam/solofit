package com.solofit.app.ui

import com.solofit.app.BuildConfig
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import com.solofit.app.ui.dashboard.DashboardScreen
import com.solofit.app.ui.foodlookup.FoodLookupScreen
import com.solofit.app.ui.meditation.MeditationScreen
import com.solofit.app.ui.modules.ModuleManagementScreen
import com.solofit.app.ui.modules.ModuleSelectionScreen
import com.solofit.app.ui.modules.ModuleViewModel
import com.solofit.app.ui.navigation.BottomDestination
import com.solofit.app.ui.navigation.GradientNavBar
import com.solofit.app.ui.navigation.Routes
import com.solofit.app.ui.nutrition.NutritionScreen
import com.solofit.app.ui.recovery.RecoveryScreen
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
import com.solofit.app.ui.workout.plan.WorkoutPlannerScreen
import com.solofit.app.ui.workout.WorkoutScreen
import com.solofit.app.ui.progress.ProgressScreen
import com.solofit.app.ui.bodyrecomp.BodyRecompScreen
import com.solofit.app.ui.walking.WalkingScreen
import com.solofit.app.ui.habits.HabitsScreen
import com.solofit.app.ui.yoga.YogaScreen
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.domain.model.ThemeMode
import com.solofit.app.ui.theme.CardPrimary
import com.solofit.app.ui.theme.SurfaceBg

private val SNAV_ROUTES = setOf(
    Routes.DASHBOARD, Routes.WORKOUT, Routes.NUTRITION, Routes.RECOVERY,
    Routes.MEDITATION, Routes.JOURNAL, Routes.BODY, Routes.PROGRESS, Routes.WALKING, Routes.HABITS, Routes.YOGA
)

private fun hasScreen(module: SoloFitModule): Boolean = module.route in SNAV_ROUTES

@Composable
fun SoloFitApp(rootViewModel: RootViewModel = hiltViewModel()) {
    val startState by rootViewModel.startState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val moduleViewModel: ModuleViewModel = hiltViewModel()

    when (startState) {
        StartState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            val start = when (startState) {
                StartState.Onboarding -> Routes.ONBOARDING
                else -> Routes.DASHBOARD
            }

            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val enabledModules by moduleViewModel.enabledModules.collectAsState()
            val suggestions by moduleViewModel.suggestions.collectAsState()
            val navModules = remember(enabledModules) {
                enabledModules.filter { hasScreen(it) }
            }
            val navDestinations = remember(navModules) {
                navModules.map { module ->
                    BottomDestination(
                        route = module.route,
                        label = module.displayName,
                        icon = com.solofit.app.ui.modules.moduleIcon(module)
                    )
                }
            }
            val allDestinations = remember(navDestinations) {
                listOf(BottomDestination.HOME) + navDestinations
            }
            val showBottomBar = currentRoute in allDestinations.map { it.route }
            val selectedDestination = allDestinations.firstOrNull { it.route == currentRoute }

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        GradientNavBar(
                            destinations = allDestinations,
                            selectedDestination = selectedDestination,
                            onDestinationSelected = { dest ->
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            barColor = SurfaceBg
                        )
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = start,
                    modifier = Modifier.padding(padding),
                    enterTransition = { fadeIn(tween(250, delayMillis = 50)) + slideInHorizontally(tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it / 3 } },
                    exitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 6 } },
                    popEnterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(250)) { -it / 6 } },
                    popExitTransition = { fadeOut(tween(150, delayMillis = 50)) + slideOutHorizontally(tween(250)) { it / 3 } }
                ) {
                    composable(Routes.ONBOARDING) {
                        val themeMode by rootViewModel.themeMode.collectAsStateWithLifecycle()
                        OnboardingScreen(
                            themeMode = themeMode,
                            onSetThemeMode = rootViewModel::setThemeMode,
                            onComplete = {
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Routes.MODULE_SELECTION) {
                        val moduleVm: ModuleViewModel = hiltViewModel()
                        val selected = remember {
                            mutableStateOf(SoloFitModule.DEFAULT_ENABLED.toSet())
                        }
                        ModuleSelectionScreen(
                            selected = selected.value,
                            onToggle = { module ->
                                selected.value = if (module in selected.value)
                                    selected.value - module
                                else
                                    selected.value + module
                            },
                            onContinue = {
                                val modules = selected.value.toList().ifEmpty {
                                    SoloFitModule.DEFAULT_ENABLED
                                }
                                moduleVm.selectModules(modules)
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.MODULE_SELECTION) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Routes.DASHBOARD) {
                        DashboardScreen(
                            enabledModules = enabledModules,
                            suggestions = suggestions,
                            onEnableModule = { moduleViewModel.enableModule(it) },
                            onLogMeal = {
                                navController.navigate(Routes.NUTRITION) {
                                    popUpTo(Routes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onLogWorkout = {
                                navController.navigate(Routes.WORKOUT) {
                                    popUpTo(Routes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                            onOpenJournal = { navController.navigate(Routes.JOURNAL) },
                            onOpenBody = { navController.navigate(Routes.BODY) },
                            onOpenWeight = { navController.navigate(Routes.WEIGHT) },
                            onOpenRecovery = {
                                navController.navigate(Routes.RECOVERY) {
                                    popUpTo(Routes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenMeditation = { navController.navigate(Routes.MEDITATION) },
                            onOpenWalking = { navController.navigate(Routes.WALKING) },
                            onOpenStress = { navController.navigate(Routes.STRESS) }
                        )
                    }
                    composable(Routes.NUTRITION) {
                        NutritionScreen(
                            onScanBarcode = { navController.navigate(Routes.SCAN) },
                            onFoodLookup = { navController.navigate(Routes.FOOD_LOOKUP) },
                            onOpenReminders = { navController.navigate(Routes.REMINDERS) }
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
                            onManageModules = { navController.navigate(Routes.MODULE_MANAGEMENT) }
                        )
                    }
                    composable(Routes.MODULE_MANAGEMENT) {
                        ModuleManagementScreen(onBack = { navController.popBackStack() })
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
                    composable(Routes.RECOVERY) {
                        RecoveryScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.STRESS) {
                        com.solofit.app.ui.stress.StressScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.MEDITATION) {
                        MeditationScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.WELLNESS) {
                        RecoveryScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.WALKING) {
                        WalkingScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.HABITS) {
                        HabitsScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.YOGA) {
                        YogaScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.PROGRESS) {
                        ProgressScreen()
                    }
                    composable(Routes.BODY_RECOMP) {
                        BodyRecompScreen(onBack = { navController.popBackStack() })
                    }
                    if (BuildConfig.DEBUG) {
                        composable(Routes.PERF) {
                            PerfScreen(onBack = { navController.popBackStack() })
                        }
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
                            },
                            onOpenWeeklyPlanner = {
                                navController.navigate(Routes.WEEKLY_PLANNER)
                            }
                        )
                    }
                    composable(Routes.WEEKLY_PLANNER) {
                        WorkoutPlannerScreen(
                            onBack = { navController.popBackStack() }
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
