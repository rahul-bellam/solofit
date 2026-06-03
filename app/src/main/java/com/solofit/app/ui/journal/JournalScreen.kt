package com.solofit.app.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.components.rememberAnimationsActive
import com.solofit.app.core.DateUtils
import com.solofit.app.ui.components.DumbbellCheck
import com.solofit.app.ui.components.LatPulldownCelebration
import com.solofit.app.ui.components.LiquidProgressBar
import com.solofit.app.ui.components.OverheadPressHeader

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
    val listState = rememberLazyListState()
    val view = LocalView.current

    // Scroll-driven "overhead press": as you scroll down, the figure presses the
    // bar up. Maps the first ~600px of scroll to press 0f..1f.
    val pressProgress by remember {
        derivedStateOf {
            if (!animate) 0.6f
            else {
                val offsetPx = if (listState.firstVisibleItemIndex > 0) 600f
                else listState.firstVisibleItemScrollOffset.toFloat()
                (offsetPx / 600f).coerceIn(0f, 1f)
            }
        }
    }

    val allDone = goals.isNotEmpty() && goals.all { it.done }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                // Scroll = overhead shoulder press. Subtle, playful, optional.
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OverheadPressHeader(
                        press = pressProgress,
                        modifier = Modifier.size(56.dp)
                    )
                    Column {
                        Text(DateUtils.prettyMedium(DateUtils.today()),
                            style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (animate) "Scroll to press it overhead 💪"
                            else "Your daily check-in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ---- Morning: today's goals ----
            item {
                Text("🌅 Today's goals", style = MaterialTheme.typography.titleLarge)
                if (goals.isNotEmpty()) {
                    val done = goals.count { it.done }
                    Spacer(Modifier.height(6.dp))
                    LiquidProgressBar(
                        progress = done.toFloat() / goals.size,
                        color = MaterialTheme.colorScheme.primary,
                        animate = animate,
                        animationKey = "journalGoals"
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "$done of ${goals.size} done — every rep counts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Celebration when all goals are done — a lat-pulldown flourish.
            item {
                AnimatedVisibility(visible = allDone) {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LatPulldownCelebration(
                                modifier = Modifier.size(48.dp),
                                animate = animate
                            )
                            Column {
                                Text("All goals smashed! 🎉", fontWeight = FontWeight.Bold)
                                Text(
                                    "That's a clean set. See you tomorrow.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            items(goals, key = { it.id }) { goal ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!goal.done) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.toggleGoal(goal)
                            }
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DumbbellCheck(
                                checked = goal.done,
                                onToggle = {},
                                animate = animate
                            )
                            Text(
                                goal.text,
                                modifier = Modifier.weight(1f),
                                textDecoration = if (goal.done) TextDecoration.LineThrough else null,
                                color = if (goal.done) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.deleteGoal(goal.id)
                            }) {
                                Icon(Icons.Filled.Close, "Delete goal")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = newGoal,
                    onValueChange = { newGoal = it },
                    placeholder = { Text("Add a goal for today…") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.addGoal(newGoal)
                                newGoal = ""
                            },
                            enabled = newGoal.isNotBlank()
                        ) { Icon(Icons.Filled.Add, "Add") }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---- Evening: gratitude ----
            item {
                Spacer(Modifier.height(12.dp))
                Text("🌙 Evening gratitude", style = MaterialTheme.typography.titleLarge)
                Text(
                    "A 2-minute check-in. What are you grateful for today?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                GratitudeEditor(
                    initial = gratitudeToday?.text ?: "",
                    onSave = viewModel::saveGratitude
                )
            }

            if (recent.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Recent reflections", style = MaterialTheme.typography.titleMedium)
                }
                items(recent, key = { "g_${it.id}" }) { entry ->
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                DateUtils.prettyMedium(entry.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(entry.text)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun GratitudeEditor(initial: String, onSave: (String) -> Unit) {
    var text by remember(initial) { mutableStateOf(initial) }
    val dirty = text != initial
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("I'm grateful for…") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        if (dirty) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) {
                    Text("Save")
                }
            }
        }
    }
}
