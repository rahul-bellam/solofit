package com.solofit.app.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.HapticFeedbackConstants
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.components.JournalTheme
import com.solofit.app.ui.components.LiquidProgressBar
import com.solofit.app.ui.components.WellnessCard
import com.solofit.app.ui.components.WellnessStaticCard
import com.solofit.app.ui.components.rememberAnimationsActive
import com.solofit.app.core.DateUtils
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.JournalBg
import com.solofit.app.ui.theme.JournalAccent
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import com.solofit.app.ui.theme.CardCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onBack: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val gratitudeToday by viewModel.gratitudeToday.collectAsStateWithLifecycle()
    val recent by viewModel.recentGratitude.collectAsStateWithLifecycle()
    val animateEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val animate = rememberAnimationsActive(animateEnabled)

    var newGoal by remember { mutableStateOf("") }
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvent.collect { event ->
            val result = snackbarHostState.showSnackbar(message = event.message, actionLabel = event.actionLabel)
            if (result == SnackbarResult.ActionPerformed) event.onAction()
        }
    }

    val allDone = goals.isNotEmpty() && goals.all { it.done }

    JournalTheme {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JournalBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = JournalBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(DateUtils.prettyMedium(DateUtils.today()), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                Spacer(Modifier.height(4.dp))
                Text("Today's reflection", fontSize = 14.sp, color = SecondaryText)
                Spacer(Modifier.height(20.dp))
            }

            // ── HERO: Today's Reflection ──
            item {
                WellnessStaticCard(
                    containerColor = CardCream,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(28.dp)) {
                        Text("Evening gratitude", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                        Spacer(Modifier.height(4.dp))
                        Text("What are you grateful for today?", fontSize = 14.sp, color = SecondaryText)
                        Spacer(Modifier.height(16.dp))
                        GratitudeEditor(initial = gratitudeToday?.text ?: "", onSave = viewModel::saveGratitude)
                    }
                }
            }

            // ── MEDIUM: Today's Goals ──
            if (goals.isNotEmpty()) {
                item {
                    val done = goals.count { it.done }
                    WellnessStaticCard(
                        containerColor = CardCream,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Today's Goals", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                                Text("$done of ${goals.size}", fontSize = 14.sp, color = SecondaryText)
                            }
                            Spacer(Modifier.height(12.dp))
                            LiquidProgressBar(progress = done.toFloat() / goals.size, color = JournalAccent, animate = animate, animationKey = "journalGoals")
                            Spacer(Modifier.height(12.dp))
                            goals.forEach { goal ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        if (!goal.done) { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                                        viewModel.toggleGoal(goal)
                                    }.padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        Modifier.size(24.dp).clip(CircleShape)
                                            .background(if (goal.done) JournalAccent else Color(0xFFDCD5CE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (goal.done) Text("\u2713", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(goal.text, modifier = Modifier.weight(1f), fontSize = 15.sp, textDecoration = if (goal.done) TextDecoration.LineThrough else null, color = if (goal.done) Color(0xFF9CA3AF) else PrimaryText)
                                    if (!goal.done) {
                                        IconButton(onClick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); viewModel.deleteGoal(goal.id) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Filled.Close, "Delete", tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    WellnessStaticCard(
                        containerColor = CardCream,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text("Today's Goals", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = newGoal, onValueChange = { newGoal = it },
                                placeholder = { Text("Add a goal for today\u2026") },
                                singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                trailingIcon = { IconButton(onClick = { viewModel.addGoal(newGoal); newGoal = "" }, enabled = newGoal.isNotBlank()) { Icon(Icons.Filled.Add, "Add") } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }

            // All done celebration
            item {
                AnimatedVisibility(visible = allDone, enter = fadeIn(tween(300)) + expandVertically(tween(300)), exit = fadeOut(tween(200)) + shrinkVertically(tween(200))) {
                    WellnessCard(
                        onClick = {},
                        containerColor = JournalAccent.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column {
                                Text("All goals complete", fontWeight = FontWeight.SemiBold, color = PrimaryText)
                                Text("Great work today", fontSize = 13.sp, color = SecondaryText)
                            }
                        }
                    }
                }
            }

            // ── SMALL: Recent Reflections ──
            if (recent.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Recent reflections", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                    Spacer(Modifier.height(12.dp))
                    WellnessStaticCard(
                        containerColor = CardCream,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            recent.take(3).forEach { entry ->
                                Text(DateUtils.prettyMedium(entry.date), fontSize = 11.sp, color = Color(0xFF9CA3AF))
                                Spacer(Modifier.height(4.dp))
                                Text(entry.text, fontSize = 14.sp, color = PrimaryText)
                                if (entry != recent.take(3).last()) Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
    }
}

@Composable
private fun GratitudeEditor(initial: String, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    LaunchedEffect(initial) { if (text.isBlank() && initial.isNotBlank()) text = initial }
    val dirty = text != initial
    Column {
        OutlinedTextField(
            value = text, onValueChange = { text = it },
            placeholder = { Text("I'm grateful for\u2026") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        if (dirty) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) {
                Text("Save", fontWeight = FontWeight.Medium)
            }
        }
    }
}
