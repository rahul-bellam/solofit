package com.solofit.app.ui.nutrition

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.solofit.app.domain.model.MealCategory
import com.solofit.app.ui.components.AnimatedThemeToggle
import com.solofit.app.ui.components.ChipSelector
import com.solofit.app.ui.components.NutritionTheme
import com.solofit.app.ui.scan.AiFoodScanViewModel
import com.solofit.app.ui.scan.AiScanResult
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

    var selectedFood by remember { mutableStateOf<FoodItemEntity?>(null) }
    var showCreateFood by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isDark by rememberSaveable { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val captureUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = captureUri.value
        if (success && uri != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val bmp = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        android.graphics.BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
                if (bmp != null) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        aiScanViewModel.analyzeAndLog(bmp)
                    }
                }
            }
        }
    }

    val launchCamera: () -> Unit = {
        try {
            val file = java.io.File(context.cacheDir, "camera").apply { mkdirs() }
            val photo = java.io.File(file, "food_scan_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photo
            )
            captureUri.value = uri
            cameraLauncher.launch(uri)
        } catch (_: Exception) { }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
    }

    LaunchedEffect(Unit) {
        aiScanViewModel.scanResult.collect { result ->
            when (result) {
                is AiScanResult.Success -> {
                    val kcal = (result.caloriesPer100g * result.estimatedGrams / 100.0).roundToInt()
                    snackbarHostState.showSnackbar(
                        "AI: ${result.name} · ${result.estimatedGrams.roundToInt()}g · ${kcal}kcal",
                        duration = SnackbarDuration.Short
                    )
                }
                is AiScanResult.Error -> {
                    snackbarHostState.showSnackbar(result.message, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    if (!loaded && query.isBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val colors = nutritionColors(isDark)

    NutritionTheme {
        Box(Modifier.fillMaxSize().background(colors.bg)) {

            // Main scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header area with greeting
                item {
                    Column {
                        // Top bar: logo + icons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.headerBg)
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.green),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌿", fontSize = 16.sp)
                                }
                                Text(
                                    "nourish",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AnimatedThemeToggle(
                                    isDark = isDark,
                                    onToggle = { isDark = !isDark }
                                )

                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.iconBg)
                                        .clickable { onOpenReminders() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔔", fontSize = 16.sp)
                                }
                            }
                        }

                        NutritionHeader(
                            colors = colors,
                            greeting = "GOOD MORNING",
                            tagline = "What are you eating today?"
                        )
                    }
                }

                // Daily progress card (offset negative)
                item {
                    val dailyCalories = sections.sumOf { s -> s.totals.calories.roundToInt() }
                    val dailyProtein = sections.sumOf { s -> s.totals.proteinG.roundToInt() }
                    val dailyCarbs = sections.sumOf { s -> s.totals.carbsG.roundToInt() }
                    val dailyFat = sections.sumOf { s -> s.totals.fatsG.roundToInt() }
                    val calGoal = 1980
                    val proteinGoal = 85
                    val carbsGoal = 220
                    val fatGoal = 65
                    val progress = if (calGoal > 0) (dailyCalories * 100 / calGoal).coerceAtMost(100) else 0
                    DailyProgressCard(
                        colors = colors,
                        progressPercent = progress,
                        calories = dailyCalories,
                        caloriesGoal = calGoal,
                        protein = dailyProtein,
                        proteinGoal = proteinGoal,
                        carbs = dailyCarbs,
                        carbsGoal = carbsGoal,
                        fat = dailyFat,
                        fatGoal = fatGoal
                    )
                }

                // Quick add
                item {
                    QuickAddButton(
                        colors = colors,
                        onClick = { showSearch = true }
                    )
                }

                // Action cards grid
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NutritionActionCard(
                                colors = colors,
                                icon = "📦",
                                title = "Barcode",
                                subtitle = "Scan package\nnutrition label",
                                onClick = onScanBarcode,
                                modifier = Modifier.weight(1f)
                            )
                            NutritionActionCard(
                                colors = colors,
                                icon = "🔍",
                                title = "Food Lookup",
                                subtitle = "Search our\ndatabase",
                                onClick = onFoodLookup,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NutritionActionCard(
                                colors = colors,
                                icon = "⌨️",
                                title = "Manual Entry",
                                subtitle = "Add custom\nfoods",
                                onClick = { showCreateFood = true },
                                modifier = Modifier.weight(1f)
                            )
                            NutritionActionCard(
                                colors = colors,
                                icon = "📷",
                                title = "AI Food Scan",
                                subtitle = "Snap a photo &\nget instant info",
                                badge = "NEW",
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        launchCamera()
                                    } else {
                                        cameraPermLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Today's log header
                item {
                    Spacer(Modifier.height(8.dp))
                    NutritionSectionHeader(
                        colors = colors,
                        title = "Today's Log",
                        actionLabel = "all",
                        onAction = { showSearch = true }
                    )
                }

                // Food log items or search results
                if (query.isNotBlank()) {
                    items(results, key = { "search_${it.id}" }) { food ->
                        FoodLogItem(
                            colors = colors,
                            emoji = "🍎",
                            name = food.name,
                            quantity = "per 100g",
                            time = "${food.caloriesPer100g.roundToInt()} kcal",
                            calories = food.caloriesPer100g.roundToInt(),
                            onClick = { selectedFood = food }
                        )
                    }
                    if (results.isEmpty()) {
                        item {
                            Text(
                                "No foods match \"$query\".",
                                color = colors.textMuted,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                } else {
                    val allEntries = sections.flatMap { section ->
                        section.entries.map { entry ->
                            FoodLogEntry(
                                name = entry.food.name,
                                emoji = "🍽️",
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
                                colors = colors,
                                onClick = { showSearch = true }
                            )
                        }
                    } else {
                        items(allEntries, key = { "${it.name}_${it.time}" }) { entry ->
                            FoodLogItem(
                                colors = colors,
                                emoji = entry.emoji,
                                name = entry.name,
                                quantity = entry.quantity,
                                time = entry.time,
                                calories = entry.calories,
                                onClick = { }
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }

            // FAB scanner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp)
            ) {
                ScannerFloatingButton(
                    colors = colors,
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            launchCamera()
                        } else {
                            cameraPermLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
            }

            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        }

        selectedFood?.let { food ->
            LogFoodDialog(
                food = food,
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

private data class FoodLogEntry(
    val name: String,
    val emoji: String,
    val quantity: String,
    val time: String,
    val calories: Int,
    val raw: Any? = null
)

@Composable
private fun SearchFoodDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<FoodItemEntity>,
    onSelectFood: (FoodItemEntity) -> Unit,
    onDismiss: () -> Unit,
    onBarcode: () -> Unit,
    onCamera: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a food") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("What did you eat?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                if (query.isBlank()) {
                    Text("SUGGESTED", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        Triple("Banana", "🍌", 105),
                        Triple("Oatmeal with berries", "🍓", 310),
                        Triple("Almond butter", "🥜", 190),
                        Triple("Chia seed pudding", "🌱", 175)
                    ).forEach { (name, emoji, cal) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onQueryChange(name) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(emoji, fontSize = 24.sp)
                            Text(name, modifier = Modifier.weight(1f))
                            Text("$cal cal", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    results.forEach { food ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onSelectFood(food) }.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(food.name, modifier = Modifier.weight(1f))
                            Text("${food.caloriesPer100g.roundToInt()} kcal",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun LogFoodDialog(
    food: FoodItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (grams: Double, category: MealCategory) -> Unit
) {
    val hasUnits = food.servingGrams != null && food.servingGrams > 0
    var byUnit by remember { mutableStateOf(hasUnits) }
    var amount by remember { mutableStateOf(if (hasUnits) "1" else "100") }
    var category by remember { mutableStateOf(MealCategory.BREAKFAST) }

    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val gramsValue = if (byUnit && hasUnits) amountValue * (food.servingGrams ?: 0.0) else amountValue
    val f = gramsValue / 100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.name) },
        text = {
            Column {
                if (hasUnits) {
                    ChipSelector(
                        options = listOf(true, false),
                        selected = byUnit,
                        label = { isUnit ->
                            if (isUnit) (food.servingLabel ?: "unit") + "s"
                            else "grams"
                        },
                        onSelect = { sel ->
                            byUnit = sel
                            amount = if (sel) "1" else "100"
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        amount = input.filterIndexed { i, c ->
                            c.isDigit() || (c == '.' && !input.substring(0, i).contains('.'))
                        }
                    },
                    label = {
                        Text(
                            if (byUnit && hasUnits)
                                "Number of ${(food.servingLabel ?: "unit")}s"
                            else "Grams"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (byUnit && hasUnits) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "= ${gramsValue.roundToInt()} g " +
                            "(${(food.servingGrams ?: 0.0).roundToInt()} g each)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "= ${(food.caloriesPer100g * f).roundToInt()} kcal · " +
                        "P ${(food.proteinPer100g * f).roundToInt()}g · " +
                        "C ${(food.carbsPer100g * f).roundToInt()}g · " +
                        "F ${(food.fatsPer100g * f).roundToInt()}g · " +
                        "Fib ${(food.fiberPer100g * f).roundToInt()}g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Text("Meal", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                ChipSelector(
                    options = MealCategory.entries,
                    selected = category,
                    label = { it.displayName },
                    onSelect = { category = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(gramsValue, category) },
                enabled = gramsValue > 0
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CreateFoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, kcal: Double, protein: Double, carbs: Double, fats: Double, fiber: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }

    val valid = name.isNotBlank() &&
        kcal.toDoubleOrNull() != null &&
        protein.toDoubleOrNull() != null &&
        carbs.toDoubleOrNull() != null &&
        fats.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Food") },
        text = {
            Column {
                Text(
                    "Macros per 100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                DecimalField("Calories", kcal, { kcal = it })
                DecimalField("Protein (g)", protein, { protein = it })
                DecimalField("Carbs (g)", carbs, { carbs = it })
                DecimalField("Fats (g)", fats, { fats = it })
                DecimalField("Fiber (g)", fiber, { fiber = it })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name,
                        kcal.toDouble(),
                        protein.toDouble(),
                        carbs.toDouble(),
                        fats.toDouble(),
                        fiber.toDoubleOrNull() ?: 0.0
                    )
                },
                enabled = valid
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DecimalField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onChange(input.filterIndexed { i, c ->
                c.isDigit() || (c == '.' && !input.substring(0, i).contains('.'))
            })
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}
