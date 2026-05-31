package com.solofit.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.solofit.app.domain.model.MealCategory
import kotlin.math.roundToInt

/**
 * Shared "how much did you eat?" dialog used by the barcode and photo flows.
 * Computes live macros from per-100g values and offers quick bowl/serving presets.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortionDialog(
    title: String,
    caloriesPer100g: Double,
    proteinPer100g: Double,
    carbsPer100g: Double,
    fatsPer100g: Double,
    onDismiss: () -> Unit,
    onConfirm: (grams: Double, category: MealCategory) -> Unit,
    initialGrams: Int = 300,
    estimateNote: String? = null,
    bowlPresets: List<Pair<String, Int>> = listOf(
        "½ bowl" to 150, "1 bowl" to 300, "1 plate" to 350, "100 g" to 100
    )
) {
    var grams by remember { mutableStateOf(initialGrams.coerceAtLeast(1).toString()) }
    var category by remember { mutableStateOf(MealCategory.LUNCH) }
    val g = grams.toDoubleOrNull() ?: 0.0
    val f = g / 100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (estimateNote != null) {
                    Text(
                        "📷 $estimateNote",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text("Quick portions", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bowlPresets.forEach { (label, value) ->
                        AssistChip(
                            onClick = { grams = value.toString() },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = grams,
                    onValueChange = { input ->
                        grams = input.filterIndexed { i, c ->
                            c.isDigit() || (c == '.' && !input.substring(0, i).contains('.'))
                        }
                    },
                    label = { Text("Grams") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "= ${(caloriesPer100g * f).roundToInt()} kcal · " +
                        "P ${(proteinPer100g * f).roundToInt()}g · " +
                        "C ${(carbsPer100g * f).roundToInt()}g · " +
                        "F ${(fatsPer100g * f).roundToInt()}g",
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
            TextButton(onClick = { onConfirm(g, category) }, enabled = g > 0) { Text("Add to log") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
