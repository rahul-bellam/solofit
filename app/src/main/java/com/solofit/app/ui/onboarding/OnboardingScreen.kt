package com.solofit.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.ui.modules.moduleIcon
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.PageBg
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import com.solofit.app.ui.theme.CardCream
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(PageBg)) {
        if (state.step > 0 && state.step < 4) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, null,
                tint = PrimaryText,
                modifier = Modifier
                    .padding(16.dp)
                    .size(28.dp)
                    .clickable { viewModel.previousStep() }
                    .align(Alignment.TopStart)
            )
        }

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(350)) { it * direction } + fadeIn(tween(250)))
                    .togetherWith(
                        slideOutHorizontally(tween(350)) { -it * direction / 2 } + fadeOut(tween(250))
                    )
            },
            label = "onboarding",
            modifier = Modifier.fillMaxSize()
        ) { step ->
            when (step) {
                0 -> HeroStep(onGetStarted = viewModel::nextStep)
                1 -> PersonalInfoStep(
                    name = state.name, age = state.age, gender = state.gender,
                    weight = state.weight, height = state.height,
                    onName = viewModel::onName, onAge = viewModel::onAge,
                    onGender = viewModel::onGender, onWeight = viewModel::onWeight,
                    onHeight = viewModel::onHeight,
                    isValid = state.isPersonalInfoValid,
                    onContinue = viewModel::nextStep
                )
                2 -> GoalStep(
                    selected = state.goal,
                    onSelect = viewModel::onGoal,
                    onContinue = if (state.goal != null) viewModel::nextStep else null
                )
                3 -> ModuleSelectionStep(
                    selected = state.selectedModules,
                    onToggle = viewModel::toggleModule,
                    onContinue = viewModel::nextStep
                )
                4 -> ReadyStep(onEnter = {
                    viewModel.finish(onComplete)
                })
            }
        }
    }
}

// ───────────────────────────── STEP 1: HERO ─────────────────────────────

@Composable
private fun HeroStep(onGetStarted: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.6f))

        Text(
            "Your Wellness.",
            fontSize = 36.sp, fontWeight = FontWeight.Bold,
            color = PrimaryText, lineHeight = 42.sp
        )
        Text(
            "Your Data.",
            fontSize = 36.sp, fontWeight = FontWeight.Bold,
            color = PrimaryText, lineHeight = 42.sp
        )
        Text(
            "Your Rules.",
            fontSize = 36.sp, fontWeight = FontWeight.Bold,
            color = Amber, lineHeight = 42.sp
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "Build a private wellness system that helps you train, eat, recover, and improve.\n\nEverything stays on your device.",
            fontSize = 15.sp, color = SecondaryText,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        // App preview mockup
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                Icons.Filled.FitnessCenter to "Workout",
                Icons.AutoMirrored.Filled.ArrowBack to "Recovery"
            ).forEach { (icon, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                            .background(Amber.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = Amber, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PrimaryText)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Amber)
        ) {
            Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ───────────────────────── STEP 2: PERSONAL INFO ────────────────────────

@Composable
private fun PersonalInfoStep(
    name: String, age: String, gender: Gender,
    weight: String, height: String,
    onName: (String) -> Unit, onAge: (String) -> Unit,
    onGender: (Gender) -> Unit, onWeight: (String) -> Unit,
    onHeight: (String) -> Unit,
    isValid: Boolean, onContinue: () -> Unit
) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(72.dp))

        Text("Let's personalise SoloFit", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
        Spacer(Modifier.height(8.dp))
        Text(
            "A few details help us create more accurate recommendations.",
            fontSize = 14.sp, color = SecondaryText
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onName,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = age,
                onValueChange = onAge,
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = onWeight,
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = height,
            onValueChange = onHeight,
            label = { Text("Height (cm)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(20.dp))

        Text("Gender", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Gender.entries.forEach { g ->
                val selected = gender == g
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Amber else CardCream)
                        .border(
                            BorderStroke(if (selected) 0.dp else 1.dp, Color(0xFFE5E7EB)),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onGender(g) }
                        .padding(horizontal = 28.dp, vertical = 14.dp)
                ) {
                    Text(
                        g.displayName,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) Color.White else PrimaryText
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Amber)
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ──────────────────────────── STEP 3: GOAL ─────────────────────────────

@Composable
private fun GoalStep(
    selected: FitnessGoal?,
    onSelect: (FitnessGoal) -> Unit,
    onContinue: (() -> Unit)?
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(72.dp))

        Text("What's your current goal?", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
        Spacer(Modifier.height(8.dp))
        Text(
            "We'll tailor your nutrition and recommendations.",
            fontSize = 14.sp, color = SecondaryText
        )
        Spacer(Modifier.height(28.dp))

        val goals = listOf(
            GoalDisplay("Build Muscle", "Increase strength and size", Icons.Filled.FitnessCenter),
            GoalDisplay("Lose Fat", "Drop body fat while keeping muscle", Icons.Filled.FitnessCenter),
            GoalDisplay("Body Recomposition", "Build muscle while losing fat", Icons.Filled.FitnessCenter),
            GoalDisplay("Improve Fitness", "Boost endurance and health", Icons.Filled.FitnessCenter),
            GoalDisplay("Stay Healthy", "Maintain current physique", Icons.Filled.FitnessCenter),
        )

        goals.forEach { g ->
            val goalEnum = FitnessGoal.entries.firstOrNull { it.displayName == g.label }
            val isSelected = selected?.displayName == g.label
            GoalCard(
                label = g.label,
                description = g.description,
                icon = g.icon,
                isSelected = isSelected,
                onClick = { goalEnum?.let(onSelect) }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.weight(1f))

        onContinue?.let { cb ->
            Button(
                onClick = cb,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber)
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private data class GoalDisplay(val label: String, val description: String, val icon: ImageVector)

@Composable
private fun GoalCard(
    label: String, description: String, icon: ImageVector,
    isSelected: Boolean, onClick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Amber.copy(alpha = 0.08f) else CardCream)
            .border(
                BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) Amber else Color(0xFFE5E7EB)
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(CircleShape)
                    .background(if (isSelected) Amber.copy(alpha = 0.12f) else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = if (isSelected) Amber else SecondaryText, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                Text(description, fontSize = 12.sp, color = SecondaryText)
            }
            if (isSelected) {
                Icon(Icons.Filled.CheckCircle, null, tint = Amber, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ─────────────────────── STEP 4: MODULE SELECTION ───────────────────────

@Composable
private fun ModuleSelectionStep(
    selected: Set<SoloFitModule>,
    onToggle: (SoloFitModule) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(PageBg)
    ) {
        Spacer(Modifier.height(72.dp))
        Text(
            "Build Your Wellness System",
            fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = PrimaryText, modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Choose what matters most right now.",
            fontSize = 14.sp, color = SecondaryText,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(28.dp))

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SoloFitModule.entries) { module ->
                ModuleCard(
                    module = module,
                    isSelected = module in selected,
                    onToggle = { onToggle(module) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Box(Modifier.padding(20.dp)) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber)
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ModuleCard(
    module: SoloFitModule,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardCream)
            .border(
                border = if (isSelected) BorderStroke(1.5.dp, Amber) else BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onToggle)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(if (isSelected) Amber.copy(alpha = 0.12f) else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    moduleIcon(module),
                    null,
                    tint = if (isSelected) Amber else SecondaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(module.displayName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                Text(module.description, fontSize = 12.sp, color = SecondaryText)
            }
            if (isSelected) {
                Icon(Icons.Filled.CheckCircle, null, tint = Amber, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ──────────────────────────── STEP 5: READY ─────────────────────────────

@Composable
private fun ReadyStep(onEnter: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.7f))

        Text("You're Ready", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
        Spacer(Modifier.height(32.dp))

        val benefits = listOf(
            "Private" to "Your data never leaves your device",
            "Offline First" to "Works without internet access",
            "Personalised" to "Built around your goals",
            "Privacy Focused" to "No accounts, no tracking"
        )
        benefits.forEach { (title, desc) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CheckCircle, null, tint = Amber, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                    Text(desc, fontSize = 13.sp, color = SecondaryText)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onEnter,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Amber)
        ) {
            Text("Enter SoloFit", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
    }
}
