package com.solofit.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.core.DateUtils
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.sol.BurnoutLevel
import com.solofit.app.sol.DailyPriority
import com.solofit.app.sol.SetbackPrediction
import com.solofit.app.sol.themeColor
import com.solofit.app.sol.SolViewModel
import com.solofit.app.ui.components.WaterTracker
import com.solofit.app.ui.modules.ModuleSuggestion
import com.solofit.app.domain.model.FeatureVisibility
import com.solofit.app.domain.model.VisibilityContext
import com.solofit.app.ui.dashboard.WeeklyReflection
import com.solofit.app.ui.dashboard.MonthlyReflection
import com.solofit.app.ui.dashboard.MonthlyReflectionData
import com.solofit.app.ui.theme.Hairline
import com.solofit.app.ui.theme.HabitsAccent
import com.solofit.app.ui.theme.MeditationAccent
import com.solofit.app.ui.theme.MossGreen
import com.solofit.app.ui.theme.NutritionAccent
import com.solofit.app.ui.theme.OliveClay
import com.solofit.app.ui.theme.RecoveryAccent
import com.solofit.app.ui.theme.WorkoutAccent
import com.solofit.app.ui.theme.RustIron
import com.solofit.app.ui.theme.Terracotta
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import com.solofit.app.ui.theme.TwilightBlue
import com.solofit.app.ui.theme.WalkingAccent
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    suggestions: List<ModuleSuggestion> = emptyList(),
    onEnableModule: (SoloFitModule) -> Unit = {},
    onLogMeal: () -> Unit,
    onLogWorkout: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenJournal: () -> Unit = {},
    onOpenBody: () -> Unit = {},
    onOpenWeight: () -> Unit = {},
    onOpenRecovery: () -> Unit = {},
    onOpenMeditation: () -> Unit = {},
    onOpenWalking: () -> Unit = {},
    onOpenStress: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    solViewModel: SolViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val solState by solViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val visibilityCtx = remember(state.daysTracked, state.daysActiveThisWeek, state.streakDays,
        state.recoveryScore, state.workoutToday, solState.weeklyWorkoutCount,
        state.meditationMinutes, solState.journalDays, solState.lifestyleMode) {
        VisibilityContext(
            daysTracked = state.daysTracked,
            daysActiveThisWeek = state.daysActiveThisWeek,
            streakDays = state.streakDays,
            recoveryScore = state.recoveryScore,
            workoutToday = state.workoutToday,
            weeklyWorkoutCount = solState.weeklyWorkoutCount,
            meditationMinutes = state.meditationMinutes,
            journalDays = solState.journalDays,
            lifestyleModeName = solState.lifestyleMode.name
        )
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh(); solViewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(Modifier.height(48.dp))

                    // ── HERO: Calm Greeting ──
                    val greeting = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
                        in 0..11 -> "Good morning"
                        in 12..16 -> "Good afternoon"
                        else -> "Good evening"
                    }
                    Text(
                        "$greeting.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "You are exactly where you need to be today.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            DateUtils.prettyMedium(state.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(4.dp).clip(CircleShape).background(solState.lifestyleMode.color))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            solState.lifestyleMode.displayName,
                            fontSize = 11.sp,
                            color = solState.lifestyleMode.color,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── TODAY'S STORY (Points 5 + 7 — Daily Narrative) ──
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = solState.todayTheme.themeColor().copy(alpha = 0.06f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp)) {
                            Text(
                                "Today's Theme",
                                style = MaterialTheme.typography.titleSmall,
                                color = solState.todayTheme.themeColor(),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                solState.todayTheme.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = solState.todayTheme.themeColor(),
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                solState.themeReason.ifEmpty { solState.todayTheme.description },
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )

                            // ── Causal explanation (Point 7) ──
                            if (solState.causalExplanation.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Why",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    solState.causalExplanation,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }

                            // ── Priority (feeds into the narrative) ──
                            Spacer(Modifier.height(12.dp))
                            Box(
                                Modifier.fillMaxWidth().height(1.dp)
                                    .background(Hairline.copy(alpha = 0.4f))
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Today's Priority",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                solState.dailyPriority.displayName,
                                fontSize = 15.sp,
                                color = priorityColor(solState.dailyPriority),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                solState.priorityAction,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── BURNOUT EARLY-WARNING (gentle, only when elevated) ──
                    val burnout = solState.burnout
                    if (burnout != null &&
                        (burnout.level == BurnoutLevel.ELEVATED || burnout.level == BurnoutLevel.HIGH)
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenStress),
                            colors = CardDefaults.cardColors(
                                containerColor = RecoveryAccent.copy(alpha = 0.08f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(RecoveryAccent))
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        burnout.insight?.title ?: "Energy Appears Lower Than Usual",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    burnout.insight?.observation
                                        ?: "A few recovery signals are moving the wrong way.",
                                    fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "View energy and stress",
                                    fontSize = 12.sp, color = RecoveryAccent, fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // ── FIRST WEEK GOALS ──
                    if (state.isFirstWeek) {
                        FirstWeekGoalsCard()
                        Spacer(Modifier.height(24.dp))
                    }

                    // ── THREE PILLARS CAROUSEL ──
                    val pillars = listOf(
                        PillarData("Move", "Steps", state.steps,
                            state.stepGoal.coerceAtLeast(1), WalkingAccent, onOpenWalking),
                        PillarData("Recover", "Recovery",
                            state.recoveryScore?.toInt() ?: 0, 100, TwilightBlue, onOpenRecovery),
                        PillarData("Nourish", "Nutrition", state.consumed.calories.roundToInt(),
                            state.profile?.targetCalories ?: 2000, MossGreen, onLogMeal)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        items(pillars) { pillar ->
                            PillarCard(pillar)
                        }
                    }

                    Spacer(Modifier.height(24.dp))


                    // ── MICRO WIN ──
                    val topWin = solState.microWins.firstOrNull()
                    if (topWin != null && FeatureVisibility.shouldShowMicroWins(visibilityCtx)) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MossGreen.copy(alpha = 0.06f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(MossGreen))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        topWin.description,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        lineHeight = 18.sp
                                    )
                                    Text(
                                        topWin.improvement,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // ── IDENTITY MESSAGE (sparingly) ──
                    val identityMsg = solState.identityMessage
                    if (identityMsg != null) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Terracotta.copy(alpha = 0.06f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(32.dp).clip(CircleShape)
                                        .background(Terracotta.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(Terracotta))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    identityMsg.statement,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    
                    // ── TODAY'S PLAN ──
                    val planName by viewModel.todayPlanName.collectAsStateWithLifecycle()
                    val exercises by viewModel.todayExercises.collectAsStateWithLifecycle()
                    val allDone by viewModel.todayAllDone.collectAsStateWithLifecycle()
                    val planDismissed by viewModel.planDismissed.collectAsStateWithLifecycle()

                    AnimatedVisibility(
                        visible = planName.isNotEmpty() && !planDismissed,
                        exit = fadeOut()
                    ) {
                        Column {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Today's Plan",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextSecondary,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    exercises.take(4).forEach { ex ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                Modifier.size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (ex.isCompleted) OliveClay
                                                        else Hairline
                                                    )
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                "${ex.exerciseName}  ${ex.sets}x${ex.reps} @ ${ex.weight}${ex.weightUnit}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (ex.isCompleted) TextSecondary.copy(alpha = 0.5f)
                                                else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── MOVEMENT SNACK (for low-activity days) ──
                    if (planName.isEmpty() && state.steps < 4000) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "Movement Snack",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "A short walk or stretch can help reset focus and energy.",
                                    fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("\u2192 Walk for 10 minutes", fontSize = 12.sp, color = WalkingAccent)
                                    Spacer(Modifier.width(8.dp))
                                    Text("\u2192 Stand and stretch", fontSize = 12.sp, color = WalkingAccent)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── SETBACK RECOVERY ──
                    solState.setbackMessages.forEach { msg ->
                        Spacer(Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = TwilightBlue.copy(alpha = 0.06f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    msg.message,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    msg.action,
                                    fontSize = 12.sp,
                                    color = TwilightBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // ── SETBACK PREDICTION (ML) ──
                    val pred = solState.setbackPrediction
                    if (pred != null && pred.riskLevel != "Low") {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = setbackPredColor(pred).copy(alpha = 0.06f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(8.dp).clip(CircleShape)
                                            .background(setbackPredColor(pred))
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Slip Risk: ${pred.riskLevel}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = setbackPredColor(pred)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Top factor: ${pred.topDriver}",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // ── NUTRITION BAR ──
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Fuel",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    "${state.consumed.calories.roundToInt()} / ${state.profile?.targetCalories ?: 2000}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            MacroRow("Protein", state.consumed.proteinG.roundToInt(), state.profile?.targetProtein ?: 1, MossGreen)
                            Spacer(Modifier.height(8.dp))
                            MacroRow("Carbs", state.consumed.carbsG.roundToInt(), state.profile?.targetCarbs ?: 1, Terracotta)
                            Spacer(Modifier.height(8.dp))
                            MacroRow("Fats", state.consumed.fatsG.roundToInt(), state.profile?.targetFats ?: 1, TextSecondary)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── WATER TRACKER ──
                    WaterTracker(
                        currentMl = state.waterMl,
                        goalMl = state.waterGoalMl,
                        onAdd = { viewModel.addWater(it) },
                        onRemove = { viewModel.removeWater(it) },
                        modifier = Modifier.fillMaxWidth(),
                        prominent = FeatureVisibility.shouldShowWaterProminent(visibilityCtx),
                        reason = FeatureVisibility.waterProminentReason(visibilityCtx)
                    )

                    Spacer(Modifier.height(12.dp))

                    // ── WEEKLY REFLECTION ──
                    if (solState.isSunday && FeatureVisibility.shouldShowWeeklyReflection(visibilityCtx)) {
                        WeeklyReflection(
                            workoutCount = solState.weeklyWorkoutCount,
                            proteinDays = solState.weeklyProteinDays,
                            walkingTrend = solState.weeklyWalkingTrend,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── MONTHLY REFLECTION ──
                    if (FeatureVisibility.shouldShowMonthlyReflection(visibilityCtx) && solState.hasSufficientData) {
                        MonthlyReflection(
                            data = MonthlyReflectionData(
                                workoutsCompleted = solState.weeklyWorkoutCount * 4,
                                proteinConsistency = if (solState.weeklyProteinDays >= 4) "Good" else "Building",
                                walkingTrend = solState.weeklyWalkingTrend,
                                recoveryTrend = "",
                                mostImprovedHabit = "",
                                suggestedFocus = solState.dailyPriority.displayName
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── EXPLORE (adaptive) ──
                    val quickLinks = buildList {
                        this += QuickLink("Energy", onOpenStress)
                        this += QuickLink("Walking", onOpenWalking)
                        if (FeatureVisibility.shouldShowJournal(visibilityCtx)) {
                            this += QuickLink("Journal", onOpenJournal)
                        }
                        if (FeatureVisibility.shouldShowMeditation(visibilityCtx)) {
                            this += QuickLink("Meditation", onOpenMeditation)
                        }
                        if (FeatureVisibility.shouldShowBodyRecomp(visibilityCtx)) {
                            this += QuickLink("Body", onOpenBody)
                        }
                        this += QuickLink("Weight", onOpenWeight)
                        this += QuickLink("Settings", onOpenSettings)
                    }
                    if (quickLinks.isNotEmpty()) {
                        Text(
                            "Explore",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        val rows = quickLinks.chunked(3)
                        rows.forEach { row ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { link ->
                                    QuickLinkChip(link.label, link.onClick, Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // ── MODULE SUGGESTIONS ──
                    suggestions.forEach { suggestion ->
                        Spacer(Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        suggestion.reason,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        suggestion.detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Terracotta)
                                        .clickable { onEnableModule(suggestion.module) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "Add",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }

        }
    }
}

}

private data class PillarData(
    val label: String,
    val title: String,
    val current: Int,
    val target: Int,
    val color: Color,
    val onClick: () -> Unit
)

private data class QuickLink(val label: String, val onClick: () -> Unit)

@Composable
private fun PillarCard(pillar: PillarData) {
    val frac = (pillar.current.toFloat() / pillar.target.coerceAtLeast(1)).coerceAtMost(1f)
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = pillar.onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                pillar.label,
                style = MaterialTheme.typography.labelSmall,
                color = pillar.color,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                pillar.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier.fillMaxWidth().height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Hairline)
            ) {
                Box(
                    Modifier.fillMaxWidth(frac).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(pillar.color)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${pillar.current} / ${pillar.target}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun MacroRow(label: String, current: Int, target: Int, color: Color) {
    val frac = (current.toFloat() / target.coerceAtLeast(1)).coerceAtMost(1f)
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text("$current g", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.fillMaxWidth().height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Hairline)
        ) {
            Box(
                Modifier.fillMaxWidth(frac).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun QuickLinkChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            label,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

private fun priorityColor(priority: DailyPriority): Color = when (priority) {
    DailyPriority.MOVEMENT -> WalkingAccent
    DailyPriority.RECOVERY -> RecoveryAccent
    DailyPriority.NUTRITION -> NutritionAccent
    DailyPriority.SLEEP -> RecoveryAccent
    DailyPriority.PERFORMANCE -> WorkoutAccent
    DailyPriority.CONSISTENCY -> HabitsAccent
    DailyPriority.MINDFULNESS -> MeditationAccent
}

private fun setbackPredColor(pred: SetbackPrediction): Color = when (pred.riskLevel) {
    "Elevated" -> RustIron
    "Moderate" -> Color(0xFFE09F3E)
    else -> MossGreen
}

// ── FIRST WEEK GOALS ──
@Composable
private fun FirstWeekGoalsCard() {
    var walked by remember { mutableStateOf(false) }
    var hydrated by remember { mutableStateOf(false) }
    var sleptEarly by remember { mutableStateOf(false) }
    var stretched by remember { mutableStateOf(false) }
    val done = listOf(walked, hydrated, sleptEarly, stretched).count { it }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WalkingAccent.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape)
                        .background(WalkingAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(WalkingAccent))
                }
                Spacer(Modifier.width(10.dp))
                Text("Your First Week", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                Text("$done/4", fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Start small. These four actions build momentum.",
                fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            GoalCheckbox("Walk for 10 minutes", checked = walked) { walked = it }
            Spacer(Modifier.height(8.dp))
            GoalCheckbox("Drink enough water", checked = hydrated) { hydrated = it }
            Spacer(Modifier.height(8.dp))
            GoalCheckbox("Sleep 15 minutes earlier", checked = sleptEarly) { sleptEarly = it }
            Spacer(Modifier.height(8.dp))
            GoalCheckbox("Stretch for 5 minutes", checked = stretched) { stretched = it }
            if (done == 4) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Good start. Tomorrow, try again.",
                    fontSize = 13.sp, color = WalkingAccent, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun GoalCheckbox(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onChecked(!checked) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) WalkingAccent else Hairline),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Text("\u2713", fontSize = 12.sp, color = Color(0xFF1E1D1B), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = if (checked) TextSecondary else TextPrimary)
    }
}
