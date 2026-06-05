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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.solofit.app.domain.model.MealCategory
import com.solofit.app.ui.components.ChipSelector
import com.solofit.app.ui.components.MacroBar
import com.solofit.app.ui.components.NutritionTheme
import com.solofit.app.ui.components.WellnessStaticCard
import com.solofit.app.ui.scan.AiFoodScanViewModel
import com.solofit.app.ui.scan.AiScanResult
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.NutritionBg
import com.solofit.app.ui.theme.NutritionCard
import com.solofit.app.ui.theme.NutritionAccent
import com.solofit.app.ui.theme.PrimaryText
import com.solofit.app.ui.theme.SecondaryText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        aiScanViewModel.analyzeAndLog(bmp)
                    }
                    bmp.recycle()
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
            } catch (_: Exception) { }
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    LaunchedEffect(Unit) {
        aiScanViewModel.scanResult.collect { result ->
            when (result) {
                is AiScanResult.Success -> {
                    val kcal = (result.caloriesPer100g * result.estimatedGrams / 100.0).roundToInt()
                    val p = (result.proteinPer100g * result.estimatedGrams / 100.0).roundToInt()
                    val c = (result.carbsPer100g * result.estimatedGrams / 100.0).roundToInt()
                    val f = (result.fatsPer100g * result.estimatedGrams / 100.0).roundToInt()
                    snackbarHostState.showSnackbar(
                        "\u2713 ${result.name} \u2014 ${kcal} kcal \u00B7 P ${p}g \u00B7 C ${c}g \u00B7 F ${f}g",
                        duration = SnackbarDuration.Short
                    )
                }
                is AiScanResult.Error -> snackbarHostState.showSnackbar(result.message, duration = SnackbarDuration.Short)
            }
        }
    }

    val dailyCalories = sections.sumOf { s -> s.totals.calories.roundToInt() }
    val dailyProtein = sections.sumOf { s -> s.totals.proteinG.roundToInt() }
    val dailyCarbs = sections.sumOf { s -> s.totals.carbsG.roundToInt() }
    val dailyFats = sections.sumOf { s -> s.totals.fatsG.roundToInt() }
    val calGoal = profile?.targetCalories ?: 2000
    val proteinGoal = profile?.targetProtein ?: 85
    val carbsGoal = profile?.targetCarbs ?: 220
    val fatGoal = profile?.targetFats ?: 65
    val remaining = (calGoal - dailyCalories).coerceAtLeast(0)

    if (!loaded && query.isBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
    NutritionTheme {
    Box(Modifier.fillMaxSize().background(NutritionBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(8.dp))
                    Text("Nutrition", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    Text("Track what you eat", fontSize = 14.sp, color = SecondaryText)
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── HERO: Calories Remaining ──
            item {
                WellnessStaticCard(
                    containerColor = NutritionCard,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Column(Modifier.padding(28.dp)) {
                        Text("Calories Remaining", fontSize = 15.sp, color = SecondaryText, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$remaining",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Light,
                            color = Amber,
                            letterSpacing = (-2).sp
                        )
                        Text(
                            "of $calGoal target",
                            fontSize = 16.sp,
                            color = SecondaryText
                        )
                    }
                }
            }

            // ── MEDIUM: Macros + AI Scan ──
            item {
                WellnessStaticCard(
                    containerColor = NutritionCard,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Column(Modifier.padding(28.dp)) {
                        MacroBar("Protein", dailyProtein, proteinGoal, NutritionAccent, animate = true)
                        Spacer(Modifier.height(10.dp))
                        MacroBar("Carbs", dailyCarbs, carbsGoal, NutritionAccent, animate = true)
                        Spacer(Modifier.height(10.dp))
                        MacroBar("Fats", dailyFats, fatGoal, NutritionAccent, animate = true)

                        Spacer(Modifier.height(20.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

            // ── SMALL: Today's Log ──
            item {
                Spacer(Modifier.height(4.dp))
                Text("Today's Log", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText,
                    modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(12.dp))
            }

            if (query.isNotBlank()) {
                items(results, key = { "search_${it.id}" }) { food ->
                    FoodLogItem(
                        emoji = "\uD83C\uDF4E",
                        name = food.name,
                        quantity = "per 100g",
                        time = "${food.caloriesPer100g.roundToInt()} kcal",
                        onClick = { selectedFood = food }
                    )
                }
                if (results.isEmpty()) {
                    item { Text("No foods match \"$query\".", color = SecondaryText, modifier = Modifier.padding(horizontal = 20.dp)) }
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
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No meals logged today", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                        }
                    }
                } else {
                    items(allEntries, key = { it.raw?.hashCode()?.toString() ?: "${it.name}_${it.time}" }) { entry ->
                        FoodLogItem(
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

    selectedFood?.let { food ->
        LogFoodDialog(food = food, onDismiss = { selectedFood = null }, onConfirm = { grams, category ->
            viewModel.logFood(food, grams, category)
            selectedFood = null
        })
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
        colors = CardDefaults.cardColors(containerColor = NutritionCard)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (isScanning) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Amber)
            } else {
                Icon(icon, null, tint = Amber, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = PrimaryText, fontWeight = FontWeight.Medium)
        }
    }
    }
}

@Composable
private fun FoodLogItem(emoji: String, name: String, quantity: String, time: String, onClick: () -> Unit) {
    androidx.compose.material3.Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = NutritionCard),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PrimaryText)
                Text("$quantity  \u00B7  $time", fontSize = 12.sp, color = SecondaryText)
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
        title = { Text("Log a food") },
        text = {
            Column {
                OutlinedTextField(value = query, onValueChange = onQueryChange, placeholder = { Text("What did you eat?") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                if (query.isBlank()) {
                    Text("SUGGESTED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Row(Modifier.fillMaxWidth().clickable { onSelectFood(food) }.padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(food.name, modifier = Modifier.weight(1f))
                            Text("${food.caloriesPer100g.roundToInt()} kcal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun LogFoodDialog(food: FoodItemEntity, onDismiss: () -> Unit, onConfirm: (grams: Double, category: MealCategory) -> Unit) {
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
                    ChipSelector(options = listOf(true, false), selected = byUnit, label = { if (it) (food.servingLabel ?: "unit") + "s" else "grams" }, onSelect = { sel -> byUnit = sel; amount = if (sel) "1" else "100" })
                    Spacer(Modifier.height(10.dp))
                }
                OutlinedTextField(value = amount, onValueChange = { input -> amount = input.filterIndexed { i, c -> c.isDigit() || (c == '.' && !input.substring(0, i).contains('.')) } }, label = { Text(if (byUnit && hasUnits) "Number of ${(food.servingLabel ?: "unit")}s" else "Grams") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                if (byUnit && hasUnits) {
                    Spacer(Modifier.height(4.dp))
                    Text("= ${gramsValue.roundToInt()} g (${(food.servingGrams ?: 0.0).roundToInt()} g each)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
                Text("= ${(food.caloriesPer100g * f).roundToInt()} kcal \u00B7 P ${(food.proteinPer100g * f).roundToInt()}g \u00B7 C ${(food.carbsPer100g * f).roundToInt()}g \u00B7 F ${(food.fatsPer100g * f).roundToInt()}g \u00B7 Fib ${(food.fiberPer100g * f).roundToInt()}g", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text("Meal", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                ChipSelector(options = MealCategory.entries, selected = category, label = { it.displayName }, onSelect = { category = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(gramsValue, category) }, enabled = gramsValue > 0) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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

    val valid = name.isNotBlank() && kcal.toDoubleOrNull() != null && protein.toDoubleOrNull() != null && carbs.toDoubleOrNull() != null && fats.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Food") },
        text = {
            Column {
                Text("Macros per 100g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                DecimalField("Calories", kcal, { kcal = it })
                DecimalField("Protein (g)", protein, { protein = it })
                DecimalField("Carbs (g)", carbs, { carbs = it })
                DecimalField("Fats (g)", fats, { fats = it })
                DecimalField("Fiber (g)", fiber, { fiber = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, kcal.toDouble(), protein.toDouble(), carbs.toDouble(), fats.toDouble(), fiber.toDoubleOrNull() ?: 0.0) }, enabled = valid) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DecimalField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { input -> onChange(input.filterIndexed { i, c -> c.isDigit() || (c == '.' && !input.substring(0, i).contains('.')) }) }, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
}
