package com.solofit.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.domain.model.ActivityLevel
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender
import com.solofit.app.ui.components.ChipSelector

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Welcome to SoloFit", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Your data never leaves this device. Let's set up your targets.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onName,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.age,
                onValueChange = viewModel::onAge,
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.weight,
                onValueChange = viewModel::onWeight,
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.height,
            onValueChange = viewModel::onHeight,
            label = { Text("Height (cm)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        SectionLabel("Gender")
        ChipSelector(
            options = Gender.entries,
            selected = state.gender,
            label = { it.displayName },
            onSelect = viewModel::onGender
        )

        Spacer(Modifier.height(16.dp))
        SectionLabel("Activity Level")
        ChipSelector(
            options = ActivityLevel.entries,
            selected = state.activityLevel,
            label = { it.displayName },
            onSelect = viewModel::onActivity
        )
        Text(
            state.activityLevel.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(16.dp))
        SectionLabel("Goal")
        ChipSelector(
            options = FitnessGoal.entries,
            selected = state.goal,
            label = { it.displayName },
            onSelect = viewModel::onGoal
        )

        Spacer(Modifier.height(20.dp))
        state.preview?.let { t ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Your Daily Targets", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    PreviewRow("BMR", "${t.bmr} kcal")
                    PreviewRow("TDEE", "${t.tdee} kcal")
                    PreviewRow("Calorie Target", "${t.targetCalories} kcal", bold = true)
                    PreviewRow("Protein", "${t.targetProteinG} g")
                    PreviewRow("Carbs", "${t.targetCarbsG} g")
                    PreviewRow("Fats", "${t.targetFatsG} g")
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.save(onComplete) },
            enabled = state.isValid && state.preview != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Tracking")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun PreviewRow(label: String, value: String, bold: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
