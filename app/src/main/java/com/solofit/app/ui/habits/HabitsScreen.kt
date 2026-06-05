package com.solofit.app.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.AmberSoft
import com.solofit.app.ui.theme.HighGreen
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import com.solofit.app.ui.theme.PageBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    onBack: () -> Unit,
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val total = state.habits.size
    val done = state.habits.count { it.completed }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habits") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setAddDialogOpen(true) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add habit", tint = Amber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PageBg,
                    titleContentColor = PrimaryText
                )
            )
        },
        containerColor = PageBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "Daily Habits",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            if (total > 0) {
                Text(
                    "$done of $total completed",
                    fontSize = 14.sp,
                    color = if (done == total) HighGreen else SecondaryText
                )
            }
            Spacer(Modifier.height(20.dp))

            if (state.habits.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No habits yet. Tap + to add one.",
                        color = SecondaryText,
                        fontSize = 15.sp
                    )
                }
            } else {
                state.habits.forEach { habit ->
                    HabitRow(
                        habit = habit,
                        onToggle = { viewModel.toggleHabit(habit.id) },
                        onRemove = if (habit.id.startsWith("custom_"))
                            ({ viewModel.removeHabit(habit.id) } as (() -> Unit)) else null
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }

    if (state.addDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setAddDialogOpen(false) },
            title = { Text("Add Habit", fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(
                    value = state.addInput,
                    onValueChange = { viewModel.setAddInput(it) },
                    label = { Text("Habit name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber,
                        cursorColor = Amber
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.addCustomHabit() }) {
                    Text("Add", color = Amber)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setAddDialogOpen(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HabitRow(
    habit: HabitItem,
    onToggle: () -> Unit,
    onRemove: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (habit.completed) AmberSoft.copy(alpha = 0.3f) else PageBg)
            .clickable { onToggle() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (habit.completed) HighGreen else SecondaryText.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (habit.completed) {
                Icon(Icons.Filled.Check, null, tint = PageBg, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            habit.name,
            fontSize = 16.sp,
            color = if (habit.completed) PrimaryText.copy(alpha = 0.6f) else PrimaryText,
            modifier = Modifier.weight(1f)
        )
        if (onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = SecondaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
