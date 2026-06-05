package com.solofit.app.ui.walking

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.ui.components.LineChart
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import com.solofit.app.ui.theme.WalkingAccent
import com.solofit.app.ui.theme.WalkingBg
import com.solofit.app.ui.theme.WalkingCard as WalkingCardColor
import com.solofit.app.ui.theme.HighGreen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkingScreen(
    onBack: () -> Unit,
    viewModel: WalkingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val progress = if (state.stepGoal > 0)
        (state.todaySteps.toFloat() / state.stepGoal).coerceAtMost(1f) else 0f

    var editingGoal by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Walking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WalkingBg,
                    titleContentColor = PrimaryText
                )
            )
        },
        containerColor = WalkingBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Daily Goal: ${formatNumber(state.stepGoal)} steps",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryText
                )
                Text(
                    if (editingGoal) "Done" else "Edit",
                    color = WalkingAccent,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        if (editingGoal) {
                            goalInput.toIntOrNull()?.let { viewModel.setStepGoal(it) }
                        }
                        editingGoal = !editingGoal
                    }
                )
            }

            if (editingGoal) {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Step goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WalkingAccent,
                        cursorColor = WalkingAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(WalkingCardColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatNumber(state.todaySteps),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.todaySteps >= state.stepGoal) HighGreen else WalkingAccent
                            )
                            Text(
                                text = "steps",
                                fontSize = 14.sp,
                                color = SecondaryText
                            )
                        }
                    }
                    Text(
                        text = "${(progress * 100).toInt()}% of daily goal",
                        fontSize = 14.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.todaySteps.toString(),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { viewModel.updateSteps(it) }
                    },
                    label = { Text("Steps today") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WalkingAccent,
                        cursorColor = WalkingAccent
                    )
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(WalkingAccent)
                        .clickable { viewModel.syncFromHealthConnect() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Sync,
                        contentDescription = "Sync Health Connect",
                        tint = WalkingCardColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (state.healthConnectAvailable) {
                Text(
                    text = "Health Connect available",
                    fontSize = 12.sp,
                    color = HighGreen
                )
            }

            Text(
                "This Week",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (state.weeklySteps.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WalkingCardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            state.weeklyLabels.takeLast(7).forEach { label ->
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = SecondaryText
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        LineChart(
                            values = state.weeklySteps.takeLast(7).map { it.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            lineColor = WalkingAccent,
                            pointColor = WalkingAccent
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(WalkingCardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tips", fontWeight = FontWeight.SemiBold, color = PrimaryText)
                    Spacer(Modifier.height(8.dp))
                    WalkingTip("10,000 steps per day is associated with improved cardiovascular health.")
                    WalkingTip("A 30-minute brisk walk typically covers 3,000\u20134,000 steps.")
                    WalkingTip("Breaking walks into 10-minute sessions throughout the day is equally effective.")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WalkingTip(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "\u2022  ",
            color = WalkingAccent,
            fontSize = 14.sp
        )
        Text(
            text = text,
            color = SecondaryText,
            fontSize = 14.sp
        )
    }
}

private fun formatNumber(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}
