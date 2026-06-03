package com.solofit.app.ui.nutrition

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.domain.model.MealCategory
import com.solofit.app.ui.components.ChipSelector
import com.solofit.app.ui.scan.AiFoodScanViewModel
import com.solofit.app.ui.scan.AiScanResult
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(
    onScanBarcode: () -> Unit = {},
    onFoodLookup: () -> Unit = {},
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

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) aiScanViewModel.analyzeAndLog(bmp)
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

    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("Nutrition Log", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Search 100+ foods...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Filled.Close, "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onScanBarcode,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAiScanning
            ) {
                Icon(Icons.Filled.QrCodeScanner, null)
                Text("  Scan barcode")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAiScanning
            ) {
                if (isAiScanning) {
                    CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp)
                    Text("  Analyzing food…")
                } else {
                    Icon(Icons.Filled.PhotoCamera, null)
                    Text("  AI Food Scan (photo)")
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onFoodLookup, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Search, null)
                Text("  Food nutrition lookup")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showCreateFood = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, null)
                Text("  Add custom food")
            }
        }

        if (query.isNotBlank()) {
            items(results, key = { "search_${it.id}" }) { food ->
                FoodSearchRow(food) { selectedFood = food }
            }
            if (results.isEmpty()) {
                item {
                    Text(
                        "No foods match \"$query\".",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Today's Diary", style = MaterialTheme.typography.titleLarge)
            }
            items(sections, key = { it.category.name }) { section ->
                MealSectionCard(section, onRemove = viewModel::removeEntry)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
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
}

@Composable
private fun FoodSearchRow(food: FoodItemEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(food.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${food.caloriesPer100g.roundToInt()} kcal · P${food.proteinPer100g.roundToInt()} " +
                            "C${food.carbsPer100g.roundToInt()} F${food.fatsPer100g.roundToInt()} " +
                            "Fib${food.fiberPer100g.roundToInt()} (per 100g)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            }
            Text(food.category, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun MealSectionCard(
    section: MealSection,
    onRemove: (com.solofit.app.data.local.dao.LoggedFoodRow) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(section.category.displayName, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${section.totals.calories.roundToInt()} kcal",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "P${section.totals.proteinG.roundToInt()} " +
                            "C${section.totals.carbsG.roundToInt()} " +
                            "F${section.totals.fatsG.roundToInt()} " +
                            "Fib${section.totals.fiberG.roundToInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (section.entries.isEmpty()) {
                Text(
                    "Nothing logged yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                section.entries.forEach { row ->
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    val f = row.log.gramsConsumed / 100.0
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(row.food.name)
                            Text(
                                "${row.log.gramsConsumed.roundToInt()}g · " +
                                    "${(row.food.caloriesPer100g * f).roundToInt()} kcal · " +
                                    "P${(row.food.proteinPer100g * f).roundToInt()} " +
                                    "C${(row.food.carbsPer100g * f).roundToInt()} " +
                                    "F${(row.food.fatsPer100g * f).roundToInt()} " +
                                    "Fib${(row.food.fiberPer100g * f).roundToInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onRemove(row) }) {
                            Icon(Icons.Filled.Close, "Remove")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogFoodDialog(
    food: FoodItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (grams: Double, category: MealCategory) -> Unit
) {
    val hasUnits = food.servingGrams != null && food.servingGrams > 0
    // entry mode: true = count of units (e.g. "5 eggs"), false = grams
    var byUnit by remember { mutableStateOf(hasUnits) }
    var amount by remember { mutableStateOf(if (hasUnits) "1" else "100") }
    var category by remember { mutableStateOf(MealCategory.BREAKFAST) }

    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val gramsValue = if (byUnit && hasUnits) amountValue * food.servingGrams!! else amountValue
    val f = gramsValue / 100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.name) },
        text = {
            Column {
                if (hasUnits) {
                    // toggle grams vs unit count
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
                            "(${food.servingGrams!!.roundToInt()} g each)",
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
