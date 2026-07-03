package com.solofit.app.ui.nutrition

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.data.local.entity.FrequentMealEntity
import com.solofit.app.domain.model.MealCategory
import com.solofit.app.ui.components.IngredientSelectionSheet
import com.solofit.app.ui.components.MacroBar
import com.solofit.app.ui.components.NutritionTheme
import com.solofit.app.ui.components.WellnessStaticCard
import com.solofit.app.ui.components.PortionDialog
import com.solofit.app.ui.components.NutritionConfidence
import com.solofit.app.sol.NutritionConfidenceEngine
import com.solofit.app.sol.ProteinDayStatus
import com.solofit.app.ui.scan.AiFoodScanViewModel
import com.solofit.app.ui.scan.AiScanResult
import com.solofit.app.ui.theme.MossGreen
import com.solofit.app.ui.nutrition.NutritionHeader
import com.solofit.app.ui.nutrition.NutritionEmptyState
import com.solofit.app.ui.nutrition.nutritionColors
import androidx.compose.foundation.isSystemInDarkTheme
import com.solofit.app.ui.theme.ProteinColor
import com.solofit.app.ui.theme.CarbsColor
import com.solofit.app.ui.theme.FatsColor
import com.solofit.app.ui.theme.Hairline
import com.solofit.app.ui.theme.NutritionAccent
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.roundToInt

private fun inferCategory(): MealCategory {
    val hour = LocalTime.now().hour
    return when {
        hour < 10 -> MealCategory.BREAKFAST
        hour < 14 -> MealCategory.LUNCH
        hour < 17 -> MealCategory.SNACKS
        else -> MealCategory.DINNER
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    onScanBarcode: () -> Unit = {},
    onFoodLookup: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
    viewModel: NutritionViewModel = hiltViewModel(),
    aiScanViewModel: AiFoodScanViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val loaded by viewModel.sectionsLoaded.collectAsStateWithLifecycle()
    val isAiScanning by aiScanViewModel.isScanning.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val adaptedTargets by viewModel.adaptedTargets.collectAsStateWithLifecycle()

    var selectedFood by remember { mutableStateOf<FoodItemEntity?>(null) }
    var showCreateFood by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSearch by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val captureUri = remember { mutableStateOf<Uri?>(null) }
    var tempPhotoFile by remember { mutableStateOf<java.io.File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = captureUri.value
        if (success && uri != null) {
            scope.launch(Dispatchers.IO) {
                val bmp = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        android.graphics.BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
                if (bmp != null) {
                    // analyzeFood takes ownership of the bitmap and recycles it when
                    // done — recycling here would race its async encode/upload.
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        aiScanViewModel.analyzeFood(bmp)
                    }
                }
                tempPhotoFile?.let { if (it.exists()) it.delete() }
            }
        }
    }

    val launchCamera: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            try {
                val file = java.io.File(context.cacheDir, "camera").apply { mkdirs() }
                val photo = java.io.File(file, "food_scan_${System.currentTimeMillis()}.jpg")
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photo)
                tempPhotoFile = photo
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    captureUri.value = uri
                    cameraLauncher.launch(uri)
                }
            } catch (_: Exception) { /* Camera unavailable */ }
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    var aiResult by remember { mutableStateOf<AiScanResult.Success?>(null) }
    var showIngredientSheet by remember { mutableStateOf(false) }
    var ingredientMealOverride by remember { mutableStateOf<com.solofit.app.domain.model.IngredientMeal?>(null) }
    var pendingAiResult by remember { mutableStateOf<AiScanResult.Success?>(null) }

    val displayResult = ingredientMealOverride?.let { meal ->
        aiResult?.copy(
            caloriesPer100g = meal.caloriesPer100g,
            proteinPer100g = meal.proteinPer100g,
            carbsPer100g = meal.carbsPer100g,
            fatsPer100g = meal.fatsPer100g
        )
    } ?: aiResult

    LaunchedEffect(Unit) {
        aiScanViewModel.scanResult.collect { result ->
            when (result) {
                is AiScanResult.Success -> aiResult = result
                is AiScanResult.Error -> snackbarHostState.showSnackbar(result.message, duration = SnackbarDuration.Short)
            }
        }
    }

    val dailyCalories = sections.sumOf { s -> s.totals.calories.roundToInt() }
    val dailyProtein = sections.sumOf { s -> s.totals.proteinG.roundToInt() }
    val dailyCarbs = sections.sumOf { s -> s.totals.carbsG.roundToInt() }
    val dailyFats = sections.sumOf { s -> s.totals.fatsG.roundToInt() }
    val adapted = adaptedTargets
    val useAdapted = adapted?.isAdapted == true
    val calGoal = adapted?.takeIf { it.isAdapted }?.targetCalories ?: (profile?.targetCalories ?: 2000)
    val proteinGoal = adapted?.takeIf { it.isAdapted }?.targetProteinG ?: (profile?.targetProtein ?: 85)
    val carbsGoal = adapted?.takeIf { it.isAdapted }?.targetCarbsG ?: (profile?.targetCarbs ?: 220)
    val fatGoal = adapted?.takeIf { it.isAdapted }?.targetFatsG ?: (profile?.targetFats ?: 65)
    val remaining = (calGoal - dailyCalories).coerceAtLeast(0)
    val reasonText = adapted?.reason

    if (!loaded && query.isBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
    NutritionTheme {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                NutritionHeader(
                    colors = nutritionColors(),
                    greeting = "Track what you eat",
                    tagline = "Nutrition"
                )
            }

            // ── HERO: Calories Remaining ──
            item {
                WellnessStaticCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Column(Modifier.padding(28.dp)) {
                        Text("Calories Remaining", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$remaining",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Light,
                            color = NutritionAccent,
                            letterSpacing = (-2).sp
                        )
                        Text(
                            "of $calGoal target",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (useAdapted) {
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .background(NutritionAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "auto-adjusted",
                                        fontSize = 10.sp,
                                        color = NutritionAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (reasonText != null) {
                                    Spacer(Modifier.width(4.dp))
                                    Text(reasonText, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }

            // ── MEDIUM: Layer Cake Macros ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Today's Fuel", style = MaterialTheme.typography.titleSmall, color = TextSecondary, letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(16.dp))

                        val totalCals = dailyCalories.coerceAtLeast(1)
                        val proteinCals = (dailyProtein * 4).coerceAtLeast(0)
                        val carbsCals = (dailyCarbs * 4).coerceAtLeast(0)
                        val fatCals = (dailyFats * 9).coerceAtLeast(0)
                        val pFrac = (proteinCals.toFloat() / totalCals).coerceIn(0f, 1f)
                        val cFrac = (carbsCals.toFloat() / totalCals).coerceIn(0f, 1f)
                        val fFrac = (fatCals.toFloat() / totalCals).coerceIn(0f, 1f)

                        // Layer cake bar
                        Box(
                            Modifier.fillMaxWidth().height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Hairline)
                        ) {
                            Row(Modifier.fillMaxSize()) {
                                if (pFrac > 0.01f)
                                    Box(Modifier.weight(pFrac).fillMaxSize().background(ProteinColor))
                                if (cFrac > 0.01f)
                                    Box(Modifier.weight(cFrac).fillMaxSize().background(CarbsColor))
                                if (fFrac > 0.01f)
                                    Box(Modifier.weight(fFrac).fillMaxSize().background(FatsColor))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            MacroLegend("Protein", "$dailyProtein g", ProteinColor)
                            MacroLegend("Carbs", "$dailyCarbs g", CarbsColor)
                            MacroLegend("Fats", "$dailyFats g", FatsColor)
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Hairline, thickness = 1.dp)
                        Spacer(Modifier.height(12.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Calories", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("$dailyCalories / $calGoal", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }

                        val pattern by viewModel.proteinPattern.collectAsStateWithLifecycle()
                        val p = pattern
                        if (p != null) {
                            Spacer(Modifier.height(12.dp))
                            Box(
                                Modifier.fillMaxWidth()
                                    .background(
                                        if (p.todayStatus != ProteinDayStatus.ON_TRACK)
                                            Color(0xFFC49A4A).copy(alpha = 0.06f)
                                        else MossGreen.copy(alpha = 0.04f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = p.message,
                                            color = if (p.todayStatus != ProteinDayStatus.ON_TRACK) Color(0xFFC49A4A) else MossGreen,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (p.todayStatus != ProteinDayStatus.ON_TRACK) {
                                        Text(
                                            text = p.action,
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Action buttons
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionChip(Icons.Filled.CameraAlt, "Scan", {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    launchCamera()
                                } else {
                                    cameraPermLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }, Modifier.weight(1f), isScanning = isAiScanning)
                            ActionChip(Icons.Filled.QrCodeScanner, "Barcode", onScanBarcode, Modifier.weight(1f))
                            ActionChip(Icons.Filled.Search, "Search", { showSearch = true }, Modifier.weight(1f))
                            ActionChip(Icons.Filled.Add, "Manual", { showCreateFood = true }, Modifier.weight(1f))
                        }
                    }
                }
            }

            // ── FREQUENT MEALS ──
            val recentMeals = viewModel.frequentMeals.value
            if (recentMeals.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Spacer(Modifier.height(8.dp))
                        Text("Recent Meals", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        recentMeals.take(4).forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        val food = FoodItemEntity(
                                            name = meal.name, category = "Frequent",
                                            caloriesPer100g = meal.caloriesPer100g,
                                            proteinPer100g = meal.proteinPer100g,
                                            carbsPer100g = meal.carbsPer100g,
                                            fatsPer100g = meal.fatsPer100g,
                                            fiberPer100g = meal.fiberPer100g,
                                            isCustom = true
                                        )
                                        viewModel.logFood(food, 100.0, inferCategory())
                                    }
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(meal.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${meal.caloriesPer100g.roundToInt()} kcal · P ${meal.proteinPer100g.roundToInt()}g",
                                        color = TextSecondary, fontSize = 11.sp
                                    )
                                }
                                Text(
                                    "${meal.logCount}x",
                                    color = TextSecondary, fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tap a meal to add 100g — adjust portion in the dialog",
                            color = TextSecondary, fontSize = 11.sp
                        )
                    }
                }
            }

            // ── SMALL: Today's Log ──
            item {
                Spacer(Modifier.height(4.dp))
                Text("Today's Log", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(12.dp))
            }

            if (query.isNotBlank()) {
                items(results, key = { "search_${it.id}" }) { food ->
                    NutritionFoodLogItem(
                        emoji = "\uD83C\uDF4E",
                        name = food.name,
                        quantity = "per 100g",
                        time = "${food.caloriesPer100g.roundToInt()} kcal",
                        onClick = { selectedFood = food }
                    )
                }
                if (results.isEmpty()) {
                    item { Text("No foods match \"$query\".", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp)) }
                }
            } else {
                val allEntries = sections.flatMap { section ->
                    section.entries.map { entry ->
                        FoodLogEntry(
                            name = entry.food.name,
                            emoji = "\uD83C\uDF7D\uFE0F",
                            quantity = "${entry.log.gramsConsumed.roundToInt()}g",
                            time = section.category.displayName,
                            calories = (entry.food.caloriesPer100g * entry.log.gramsConsumed / 100.0).roundToInt(),
                            raw = entry
                        )
                    }
                }

                if (allEntries.isEmpty()) {
                    item {
                        NutritionEmptyState(
                            colors = nutritionColors(),
                            onClick = { showCreateFood = true }
                        )
                    }
                } else {
                    items(allEntries, key = { it.raw?.hashCode()?.toString() ?: "${it.name}_${it.time}" }) { entry ->
                        NutritionFoodLogItem(
                            emoji = entry.emoji,
                            name = entry.name,
                            quantity = entry.quantity,
                            time = entry.time,
                            onClick = {
                                val row = entry.raw
                                if (row is com.solofit.app.data.local.dao.LoggedFoodRow) {
                                    viewModel.removeEntry(row)
                                    scope.launch { snackbarHostState.showSnackbar("Deleted ${entry.name}") }
                                }
                            }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    displayResult?.let { result ->
        val confidence = when {
            ingredientMealOverride != null -> NutritionConfidence.HIGH
            else -> when (NutritionConfidenceEngine.fromSource(false, result.name)) {
                NutritionConfidenceEngine.Level.HIGH -> NutritionConfidence.HIGH
                NutritionConfidenceEngine.Level.MEDIUM -> NutritionConfidence.MEDIUM
                NutritionConfidenceEngine.Level.LOW -> NutritionConfidence.LOW
            }
        }
        PortionDialog(
            title = result.name,
            caloriesPer100g = result.caloriesPer100g,
            proteinPer100g = result.proteinPer100g,
            carbsPer100g = result.carbsPer100g,
            fatsPer100g = result.fatsPer100g,
            fiberPer100g = result.fiberPer100g,
            confidence = confidence,
            showIngredientOption = ingredientMealOverride == null,
            onImproveAccuracy = {
                pendingAiResult = aiResult
                aiResult = null
                showIngredientSheet = true
            },
            dailyProteinSoFar = dailyProtein,
            dailyProteinTarget = proteinGoal,
            frequentMeals = viewModel.frequentMeals.value,
            onSelectFrequentMeal = { meal ->
                val food = FoodItemEntity(
                    name = meal.name, category = "Frequent",
                    caloriesPer100g = meal.caloriesPer100g,
                    proteinPer100g = meal.proteinPer100g,
                    carbsPer100g = meal.carbsPer100g,
                    fatsPer100g = meal.fatsPer100g,
                    fiberPer100g = meal.fiberPer100g,
                    isCustom = true
                )
                viewModel.logFood(food, 100.0, inferCategory())
                aiResult = null; ingredientMealOverride = null
            },
            onDismiss = { aiResult = null; ingredientMealOverride = null },
            onConfirm = { grams, category ->
                viewModel.logFood(
                    FoodItemEntity(name = result.name, category = "AI Scan",
                        caloriesPer100g = result.caloriesPer100g,
                        proteinPer100g = result.proteinPer100g,
                        carbsPer100g = result.carbsPer100g,
                        fatsPer100g = result.fatsPer100g,
                        fiberPer100g = result.fiberPer100g,
                        isCustom = true),
                    grams, category
                )
                aiResult = null; ingredientMealOverride = null
            }
        )
    }

    if (showIngredientSheet) {
        val dishName = pendingAiResult?.name ?: "your meal"
        val savedResult = pendingAiResult
        IngredientSelectionSheet(
            dishName = dishName,
            onDismiss = {
                showIngredientSheet = false
                savedResult?.let { aiResult = it }
                pendingAiResult = null
            },
            onConfirm = { meal ->
                ingredientMealOverride = meal
                showIngredientSheet = false
                savedResult?.let { aiResult = it }
                pendingAiResult = null
            }
        )
    }

    selectedFood?.let { food ->
        LogFoodDialog(
            food = food,
            dailyProtein = dailyProtein,
            proteinGoal = proteinGoal,
            frequentMeals = viewModel.frequentMeals.value,
            onDismiss = { selectedFood = null },
            onConfirm = { grams, category ->
                viewModel.logFood(food, grams, category)
                selectedFood = null
            }
        )
    }

    if (showCreateFood) {
        CreateFoodDialog(
            onDismiss = { showCreateFood = false },
            onConfirm = { name, kcal, protein, carbs, fats, fiber ->
                viewModel.addCustomFood(name, kcal, protein, carbs, fats, fiber)
                showCreateFood = false
                viewModel.onQueryChange("")
            }
        )
    }

    if (showSearch) {
        SearchFoodDialog(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            results = results,
            onSelectFood = { selectedFood = it },
            onDismiss = { showSearch = false },
            onBarcode = onScanBarcode,
            onCamera = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    cameraPermLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }
    }
    }
}

@Composable
private fun ActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isScanning: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val s = if (isPressed) 0.94f else 1f
    Box(modifier = modifier.scale(s)) {
    androidx.compose.material3.Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (isScanning) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = NutritionAccent)
            } else {
                Icon(icon, null, tint = NutritionAccent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
        }
    }
    }
}

@Composable
private fun NutritionFoodLogItem(emoji: String, name: String, quantity: String, time: String, onClick: () -> Unit) {
    androidx.compose.material3.Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                Text("$quantity  \u00B7  $time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private data class FoodLogEntry(
    val name: String,
    val emoji: String,
    val quantity: String,
    val time: String,
    val calories: Int,
    val raw: Any? = null
)

// ─── Dialogs ───

@Composable
private fun SearchFoodDialog(query: String, onQueryChange: (String) -> Unit, results: List<FoodItemEntity>, onSelectFood: (FoodItemEntity) -> Unit, onDismiss: () -> Unit, onBarcode: () -> Unit, onCamera: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Food", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search food...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                if (query.isBlank()) {
                    Text("QUICK ADD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    listOf(Triple("Banana", "\uD83C\uDF4C", 105), Triple("Oatmeal with berries", "\uD83C\uDF53", 310), Triple("Almond butter", "\uD83E\uDD5C", 190), Triple("Chia seed pudding", "\uD83C\uDF31", 175)).forEach { (name, emoji, cal) ->
                        Row(Modifier.fillMaxWidth().clickable { onQueryChange(name) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(emoji, fontSize = 24.sp)
                            Text(name, modifier = Modifier.weight(1f))
                            Text("$cal cal", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    results.forEach { food ->
                        Row(Modifier.fillMaxWidth().clickable { onSelectFood(food) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(food.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Text("${food.caloriesPer100g.roundToInt()} kcal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        dismissButton = {}
    )
}

@Composable
private fun LogFoodDialog(
    food: FoodItemEntity,
    dailyProtein: Int = 0,
    proteinGoal: Int = 0,
    frequentMeals: List<com.solofit.app.data.local.entity.FrequentMealEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (grams: Double, category: MealCategory) -> Unit
) {
    PortionDialog(
        title = food.name,
        caloriesPer100g = food.caloriesPer100g,
        proteinPer100g = food.proteinPer100g,
        carbsPer100g = food.carbsPer100g,
        fatsPer100g = food.fatsPer100g,
        fiberPer100g = food.fiberPer100g,
        confidence = NutritionConfidence.MEDIUM,
        dailyProteinSoFar = dailyProtein,
        dailyProteinTarget = proteinGoal,
        frequentMeals = frequentMeals,
        onSelectFrequentMeal = { meal ->
            onConfirm(100.0, inferCategory())
            onDismiss()
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        initialGrams = if (food.servingGrams != null && food.servingGrams > 0) food.servingGrams.toInt() else 100
    )
}

@Composable
private fun CreateFoodDialog(onDismiss: () -> Unit, onConfirm: (name: String, kcal: Double, protein: Double, carbs: Double, fats: Double, fiber: Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }

    val kcalNum = kcal.toDoubleOrNull()
    val proteinNum = protein.toDoubleOrNull()
    val carbsNum = carbs.toDoubleOrNull()
    val fatsNum = fats.toDoubleOrNull()
    val fiberNum = fiber.toDoubleOrNull()
    val valid = name.isNotBlank() && name.length <= 100 &&
        kcalNum != null && kcalNum in 0.0..900.0 &&
        proteinNum != null && proteinNum in 0.0..100.0 &&
        carbsNum != null && carbsNum in 0.0..100.0 &&
        fatsNum != null && fatsNum in 0.0..100.0 &&
        fiberNum != null && fiberNum in 0.0..100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Food") },
        text = {
            Column {
                Text("Macros per 100g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = name, onValueChange = { if (it.length <= 100) name = it }, label = { Text("Food name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                DecimalField("Calories", kcal, { kcal = it })
                DecimalField("Protein (g)", protein, { protein = it })
                DecimalField("Carbs (g)", carbs, { carbs = it })
                DecimalField("Fats (g)", fats, { fats = it })
                DecimalField("Fiber (g)", fiber, { fiber = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name.trim(), kcalNum!!, proteinNum!!, carbsNum!!, fatsNum!!, fiberNum!!) }, enabled = valid) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DecimalField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { input -> onChange(input.filterIndexed { i, c -> c.isDigit() || (c == '.' && !input.substring(0, i).contains('.')) }) }, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
}

@Composable
private fun MacroLegend(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(Modifier.width(4.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
