package com.solofit.app.ui.scan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.solofit.app.ui.components.PortionDialog
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onClose: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Once logged, bounce back to the previous screen.
    LaunchedEffect(state) {
        if (state is ScanUiState.Logged) onClose()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Barcode") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val s = state) {
                ScanUiState.Idle, is ScanUiState.Error -> {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.height(96.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Point your camera at a product barcode.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (s is ScanUiState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = viewModel::startScan, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.QrCodeScanner, null)
                        Text("  Start Scanner")
                    }
                    Spacer(Modifier.height(24.dp))
                    ManualBarcodeEntry(onLookup = viewModel::lookupManual)
                }

                ScanUiState.Scanning, ScanUiState.LookingUp -> {
                    Spacer(Modifier.height(60.dp))
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (s == ScanUiState.Scanning) "Opening scanner…"
                        else "Looking up in Open Food Facts…"
                    )
                }

                is ScanUiState.Found -> {
                    var showPortion by remember { mutableStateOf(true) }
                    FoundCard(s)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showPortion = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Add to today's log")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = viewModel::reset, modifier = Modifier.fillMaxWidth()) {
                        Text("Scan another")
                    }
                    if (showPortion) {
                        PortionDialog(
                            title = s.food.name,
                            caloriesPer100g = s.food.caloriesPer100g,
                            proteinPer100g = s.food.proteinPer100g,
                            carbsPer100g = s.food.carbsPer100g,
                            fatsPer100g = s.food.fatsPer100g,
                            onDismiss = { showPortion = false },
                            onConfirm = { grams, cat ->
                                viewModel.logFood(s.food, grams, cat)
                            }
                        )
                    }
                }

                is ScanUiState.ManualEntry -> {
                    ManualEntryForm(
                        barcode = s.barcode,
                        onSubmit = { name, kcal, p, c, f ->
                            viewModel.submitManual(s.barcode, name, kcal, p, c, f)
                        },
                        onCancel = viewModel::reset
                    )
                }

                ScanUiState.Logged -> {
                    Text("Logged ✓")
                }
            }
        }
    }
}

@Composable
private fun FoundCard(s: ScanUiState.Found) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Found in Open Food Facts", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(s.food.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "${s.food.caloriesPer100g.roundToInt()} kcal · " +
                    "P ${s.food.proteinPer100g.roundToInt()}g · " +
                    "C ${s.food.carbsPer100g.roundToInt()}g · " +
                    "F ${s.food.fatsPer100g.roundToInt()}g  (per 100g)"
            )
        }
    }
}

@Composable
private fun ManualBarcodeEntry(onLookup: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Or enter a barcode manually", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filter { ch -> ch.isDigit() } },
                label = { Text("Barcode digits") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onLookup(code) },
                enabled = code.length >= 8,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Look up") }
        }
    }
}

@Composable
private fun ManualEntryForm(
    barcode: String,
    onSubmit: (name: String, kcal: Double, p: Double, c: Double, f: Double) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }

    val valid = name.isNotBlank() && kcal.toDoubleOrNull() != null

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Food Not Found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Barcode $barcode isn't in Open Food Facts. Add it once and it's saved locally for next time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            DecimalField("Product name", name, { name = it }, isText = true)
            DecimalField("Calories / 100g", kcal, { kcal = it })
            DecimalField("Protein / 100g", protein, { protein = it })
            DecimalField("Carbs / 100g", carbs, { carbs = it })
            DecimalField("Fats / 100g", fats, { fats = it })
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    onSubmit(
                        name,
                        kcal.toDoubleOrNull() ?: 0.0,
                        protein.toDoubleOrNull() ?: 0.0,
                        carbs.toDoubleOrNull() ?: 0.0,
                        fats.toDoubleOrNull() ?: 0.0
                    )
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save & continue") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}

@Composable
private fun DecimalField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    isText: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (isText) onChange(input)
            else onChange(input.filterIndexed { i, c ->
                c.isDigit() || (c == '.' && !input.substring(0, i).contains('.'))
            })
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = if (isText) KeyboardOptions.Default
        else KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
