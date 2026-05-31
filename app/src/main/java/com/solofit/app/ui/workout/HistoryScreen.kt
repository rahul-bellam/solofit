package com.solofit.app.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.components.EmptyState
import com.solofit.app.core.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val workoutDates = viewModel.workoutDates(history)
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("Workout History", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            CalendarCard(
                month = visibleMonth,
                workoutDates = workoutDates,
                onPrev = { visibleMonth = visibleMonth.minusMonths(1) },
                onNext = { visibleMonth = visibleMonth.plusMonths(1) }
            )
            Spacer(Modifier.height(8.dp))
            Text("Completed Workouts", style = MaterialTheme.typography.titleLarge)
        }

        if (history.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.CalendarMonth,
                    title = "No completed workouts yet",
                    message = "Finish a workout session and it'll appear here, with your active days highlighted on the calendar.",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }

        items(history, key = { it.session.id }) { s ->
            val volume = s.sets
                .filter { it.isCompleted }
                .sumOf { it.weightKg * it.reps }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(s.session.routineName, fontWeight = FontWeight.Bold)
                        Text(
                            DateUtils.prettyMedium(s.session.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    val exercises = s.sets.map { it.exerciseName }.distinct().size
                    val completedSets = s.sets.count { it.isCompleted }
                    Text(
                        "$exercises exercises · $completedSets sets · ${volume.toInt()} kg total volume",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun CalendarCard(
    month: YearMonth,
    workoutDates: Set<String>,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val iso = DateTimeFormatter.ISO_LOCAL_DATE
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev) { Icon(Icons.Filled.ChevronLeft, "Previous") }
                Text(
                    "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, "Next") }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(d, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))

            val firstDay = month.atDay(1)
            // dayOfWeek: Monday=1 .. Sunday=7
            val leadingBlanks = firstDay.dayOfWeek.value - 1
            val daysInMonth = month.lengthOfMonth()
            val today = LocalDate.now()

            val cells = mutableListOf<LocalDate?>()
            repeat(leadingBlanks) { cells.add(null) }
            for (day in 1..daysInMonth) cells.add(month.atDay(day))
            while (cells.size % 7 != 0) cells.add(null)

            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (date != null) {
                                val key = date.format(iso)
                                val hasWorkout = workoutDates.contains(key)
                                val isToday = date == today
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                hasWorkout -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (hasWorkout) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
