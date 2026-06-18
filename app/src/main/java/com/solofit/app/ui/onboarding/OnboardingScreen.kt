package com.solofit.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender
import com.solofit.app.domain.model.OnboardingFocus
import com.solofit.app.domain.model.ThemeMode
import com.solofit.app.ui.theme.Hairline
import com.solofit.app.ui.theme.Terracotta
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    themeMode: ThemeMode,
    onSetThemeMode: (ThemeMode) -> Unit,
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                (slideInVertically(tween(400)) { it } + fadeIn(tween(300)))
                    .togetherWith(slideOutVertically(tween(300)) { -it / 3 } + fadeOut(tween(200)))
            },
            label = "onboarding",
            modifier = Modifier.fillMaxSize()
        ) { step ->
            when (step) {
                0 -> WelcomeStep(onGetStarted = viewModel::nextStep)
                1 -> NameStep(name = state.name, onName = viewModel::onName, onContinue = viewModel::nextStep)
                2 -> AgeStep(age = state.age, onAge = viewModel::onAge, onContinue = viewModel::nextStep)
                3 -> GenderStep(gender = state.gender, onGender = viewModel::onGender, onContinue = viewModel::nextStep)
                4 -> HeightStep(heightCm = state.height.toDoubleOrNull() ?: 170.0, onHeight = { viewModel.onHeight(it.roundToInt().toString()) }, onContinue = viewModel::nextStep)
                5 -> WeightStep(weightKg = state.weight.toDoubleOrNull() ?: 70.0, onWeight = { viewModel.onWeight(it.roundToInt().toString()) }, onContinue = viewModel::nextStep)
                6 -> GoalStep(selected = state.goal, onSelect = viewModel::onGoal, onContinue = if (state.goal != null) viewModel::nextStep else null)
                7 -> DescribeStep(selected = state.focus, onSelect = viewModel::onFocus, onContinue = if (state.focus != null) viewModel::nextStep else null)
                8 -> ReadyStep(onEnter = { viewModel.finish(onComplete) })
            }
        }

        // Back button (except on first and last steps)
        if (state.step in 1..7) {
            TextButton(
                onClick = viewModel::previousStep,
                modifier = Modifier.padding(start = 8.dp, top = 48.dp).align(Alignment.TopStart)
            ) {
                Text("Back", color = TextSecondary, fontSize = 14.sp)
            }
        }

        // Step indicator dots
        if (state.step in 1..7) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(7) { i ->
                    Box(
                        Modifier
                            .size(if (i == state.step - 1) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= state.step - 1) Terracotta
                                else Hairline
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onGetStarted: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.6f))

        Text(
            "Welcome.",
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Take a breath. There's nothing to set up right now.\nJust a few gentle questions.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        TextButton(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Terracotta)
        ) {
            Text("Begin", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun NameStep(name: String, onName: (String) -> Unit, onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.5f))
        Text("What should I call you?", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, letterSpacing = 1.sp)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onName,
            placeholder = { Text("Your name", color = TextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onContinue,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (name.isNotBlank()) Terracotta else Hairline)
        ) {
            Text("Continue", color = if (name.isNotBlank()) Color.White else TextSecondary, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun AgeStep(age: String, onAge: (String) -> Unit, onContinue: () -> Unit) {
    val valid = age.toIntOrNull()?.let { it in 1..120 } == true
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.5f))
        Text("How old are you?", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, letterSpacing = 1.sp)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = age,
            onValueChange = onAge,
            placeholder = { Text("Your age", color = TextSecondary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onContinue,
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (valid) Terracotta else Hairline)
        ) {
            Text("Continue", color = if (valid) Color.White else TextSecondary, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun GenderStep(gender: Gender, onGender: (Gender) -> Unit, onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.5f))
        Text("Which describes you?", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, letterSpacing = 1.sp)
        Spacer(Modifier.height(32.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Gender.entries.forEach { g ->
                val selected = gender == g
                Box(
                    Modifier.weight(1f).height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Terracotta.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onGender(g) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        g.displayName,
                        color = if (selected) Terracotta else TextSecondary,
                        fontSize = 20.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Terracotta)
        ) {
            Text("Continue", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun HeightStep(heightCm: Double, onHeight: (Double) -> Unit, onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.3f))
        Text("Your height", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Text("Slide to set your height in centimetres.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(40.dp))

        Text(
            "${heightCm.roundToInt()} cm",
            style = MaterialTheme.typography.displaySmall,
            color = Terracotta
        )
        Spacer(Modifier.height(32.dp))

        Box(
            Modifier.fillMaxWidth().height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.size(48.dp, 300.dp)
                    .graphicsLayer {
                        rotationZ = -90f
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                    }
            ) {
                Slider(
                    value = heightCm.toFloat(),
                    onValueChange = { onHeight(it.toDouble()) },
                    valueRange = 100f..250f,
                    steps = 149,
                    modifier = Modifier.fillMaxSize(),
                    colors = SliderDefaults.colors(
                        thumbColor = Terracotta,
                        activeTrackColor = Terracotta,
                        inactiveTrackColor = Hairline
                    )
                )
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("100", color = TextSecondary, fontSize = 12.sp)
            Text("250", color = TextSecondary, fontSize = 12.sp)
        }

        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Terracotta)
        ) {
            Text("Continue", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun WeightStep(weightKg: Double, onWeight: (Double) -> Unit, onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.3f))
        Text("Your weight", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Text("Slide to set your weight in kilograms.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(40.dp))

        Text(
            "${weightKg.roundToInt()} kg",
            style = MaterialTheme.typography.displaySmall,
            color = Terracotta
        )
        Spacer(Modifier.height(32.dp))

        Box(
            Modifier.fillMaxWidth().height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.size(48.dp, 300.dp)
                    .graphicsLayer {
                        rotationZ = -90f
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                    }
            ) {
                Slider(
                    value = weightKg.toFloat(),
                    onValueChange = { onWeight(it.toDouble()) },
                    valueRange = 20f..250f,
                    steps = 229,
                    modifier = Modifier.fillMaxSize(),
                    colors = SliderDefaults.colors(
                        thumbColor = Terracotta,
                        activeTrackColor = Terracotta,
                        inactiveTrackColor = Hairline
                    )
                )
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("20", color = TextSecondary, fontSize = 12.sp)
            Text("250", color = TextSecondary, fontSize = 12.sp)
        }

        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Terracotta)
        ) {
            Text("Continue", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun GoalStep(
    selected: FitnessGoal?,
    onSelect: (FitnessGoal) -> Unit,
    onContinue: (() -> Unit)?
) {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.3f))
        Text("What brings you here?", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Text("Pick one — you can adjust anytime.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(28.dp))

        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FitnessGoal.entries.forEach { goal ->
                val isSelected = selected == goal
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Terracotta.copy(alpha = 0.08f) else Color.Transparent)
                        .clickable { onSelect(goal) }
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    Text(
                        goal.displayName,
                        color = if (isSelected) Terracotta else TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        onContinue?.let { cb ->
            TextButton(
                onClick = cb,
                modifier = Modifier.fillMaxWidth().height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Terracotta)
            ) {
                Text("Continue", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DescribeStep(
    selected: OnboardingFocus?,
    onSelect: (OnboardingFocus) -> Unit,
    onContinue: (() -> Unit)?
) {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.3f))
        Text("Which best describes you?", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Text("This helps tailor your experience.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(28.dp))

        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OnboardingFocus.entries.forEach { focus ->
                val isSelected = selected == focus
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Terracotta.copy(alpha = 0.08f) else Color.Transparent)
                        .clickable { onSelect(focus) }
                        .padding(vertical = 14.dp, horizontal = 16.dp)
                ) {
                    Column {
                        Text(
                            focus.displayName,
                            color = if (isSelected) Terracotta else TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        )
                        if (isSelected) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                focus.description,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        onContinue?.let { cb ->
            TextButton(
                onClick = cb,
                modifier = Modifier.fillMaxWidth().height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Terracotta)
            ) {
                Text("Continue", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ReadyStep(onEnter: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.6f))
        Text(
            "You're all set.",
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Everything stays on your device. Take your time getting familiar — there's no rush.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onEnter,
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Terracotta)
        ) {
            Text("Enter SoloFit", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}
