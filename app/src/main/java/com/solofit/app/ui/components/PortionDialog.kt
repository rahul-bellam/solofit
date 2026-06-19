package com.solofit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.solofit.app.data.local.entity.FrequentMealEntity
import com.solofit.app.domain.model.MealCategory
import com.solofit.app.ui.theme.CardPrimary
import com.solofit.app.ui.theme.Hairline
import com.solofit.app.ui.theme.NutritionAccent
import com.solofit.app.ui.theme.NutritionProtein
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import com.solofit.app.ui.theme.ProteinColor
import kotlin.math.roundToInt

/**
 * NutritionConfidence indicates how reliable a nutrition estimate is.
 */
enum class NutritionConfidence {
    HIGH, MEDIUM, LOW;

    val label: String get() = when (this) {
        HIGH -> "High"
        MEDIUM -> "Medium"
        LOW -> "Low"
    }
}

/**
 * A premium "how much did you eat?" dialog with:
 * - Slider with quick portion markers (25/50/75/100/150/200g)
 * - Live macro display that updates in real time
 * - Nutrition confidence indicator
 * - Protein target progress indicator
 * - Frequent meal one-tap shortcuts
 * - Meal category selector
 * - Ingredient breakdown support for mixed foods (curry/dal/biryani)
 */
@Composable
fun PortionDialog(
    title: String,
    caloriesPer100g: Double,
    proteinPer100g: Double,
    carbsPer100g: Double,
    fatsPer100g: Double,
    fiberPer100g: Double = 0.0,
    confidence: NutritionConfidence = NutritionConfidence.MEDIUM,
    onDismiss: () -> Unit,
    onConfirm: (grams: Double, category: MealCategory) -> Unit,
    initialGrams: Int = 150,
    showIngredientOption: Boolean = false,
    onImproveAccuracy: (() -> Unit)? = null,
    // ── Protein Progress ──
    dailyProteinSoFar: Int = 0,
    dailyProteinTarget: Int = 0,
    // ── Frequent Meals ──
    frequentMeals: List<FrequentMealEntity> = emptyList(),
    onSelectFrequentMeal: ((FrequentMealEntity) -> Unit)? = null
) {
    var grams by remember { mutableStateOf(initialGrams.coerceIn(10, 500)) }
    var category by remember { mutableStateOf(MealCategory.LUNCH) }
    var customInput by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(false) }
    val g = if (useCustom) (customInput.toDoubleOrNull() ?: 0.0) else grams.toDouble()
    val f = g / 100.0

    val portionMarkers = listOf(25, 50, 75, 100, 150, 200)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(CardPrimary, RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // ── Header ──
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Nutrition per 100g",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))

            // ── Per-100g reference table ──
            val baseItems = listOf(
                "${caloriesPer100g.roundToInt()} kcal" to NutritionAccent,
                "P ${proteinPer100g.roundToInt()}g" to NutritionProtein,
                "C ${carbsPer100g.roundToInt()}g" to TextSecondary,
                "F ${fatsPer100g.roundToInt()}g" to TextSecondary,
                "Fib ${fiberPer100g.roundToInt()}g" to TextSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                baseItems.forEach { (text, color) ->
                    Box(
                        modifier = Modifier
                            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = text,
                            color = color,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // ── Section: How much did you eat? ──
            Text(
                text = "How much did you eat?",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            // ── Quick portion chips ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                portionMarkers.forEach { value ->
                    val selected = !useCustom && grams == value
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) NutritionAccent.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) NutritionAccent else Hairline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                useCustom = false
                                grams = value
                                customInput = ""
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${value}g",
                            color = if (selected) NutritionAccent else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // ── Slider ──
            Slider(
                value = if (useCustom) grams.toFloat() else grams.toFloat(),
                onValueChange = { useCustom = false; grams = it.roundToInt() },
                valueRange = 10f..500f,
                steps = 48,
                colors = SliderDefaults.colors(
                    thumbColor = NutritionAccent,
                    activeTrackColor = NutritionAccent,
                    inactiveTrackColor = Hairline
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("10g", color = TextSecondary, fontSize = 11.sp)
                Text("500g", color = TextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(4.dp))

            // ── Custom grams input ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "or enter custom: ",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { input ->
                        val filtered = input.filterIndexed { i, c ->
                            c.isDigit() || (c == '.' && !input.substring(0, i).contains('.'))
                        }
                        customInput = filtered
                        if (filtered.toDoubleOrNull() != null) {
                            useCustom = true
                        }
                    },
                    placeholder = { Text("g", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.width(100.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                )
            }
            Spacer(Modifier.height(20.dp))

            // ── Live macro display ──
            if (g > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NutritionAccent.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "${g.roundToInt()}g of this food",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroDisplay("Calories", "${(caloriesPer100g * f).roundToInt()}", "kcal", NutritionAccent)
                            MacroDisplay("Protein", "${(proteinPer100g * f).roundToInt()}", "g", NutritionProtein)
                            MacroDisplay("Carbs", "${(carbsPer100g * f).roundToInt()}", "g", TextSecondary)
                            MacroDisplay("Fats", "${(fatsPer100g * f).roundToInt()}", "g", TextSecondary)
                            MacroDisplay("Fiber", "${(fiberPer100g * f).roundToInt()}", "g", TextSecondary)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Protein target progress ──
            if (dailyProteinTarget > 0) {
                val proteinFromThisMeal = (proteinPer100g * f).roundToInt()
                val proteinAfter = dailyProteinSoFar + proteinFromThisMeal
                val proteinFrac = (proteinAfter.toFloat() / dailyProteinTarget).coerceAtMost(1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ProteinColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Protein target progress",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$proteinAfter / $dailyProteinTarget g",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier.fillMaxWidth().height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Hairline)
                        ) {
                            Box(
                                Modifier.fillMaxWidth(proteinFrac).height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(ProteinColor)
                            )
                        }
                        if (dailyProteinSoFar < dailyProteinTarget) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "This meal will add ${proteinFromThisMeal}g protein.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Frequent meal one-tap shortcuts ──
            if (frequentMeals.isNotEmpty() && onSelectFrequentMeal != null) {
                Text(
                    text = "Recent meals",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                frequentMeals.take(3).forEach { meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectFrequentMeal(meal) }
                            .background(NutritionAccent.copy(alpha = 0.04f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = meal.name,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${meal.caloriesPer100g.roundToInt()} kcal · P ${meal.proteinPer100g.roundToInt()}g",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "${meal.logCount}x",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Confidence indicator ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val confidenceColor = when (confidence) {
                    NutritionConfidence.HIGH -> Color(0xFF5B8C5A)
                    NutritionConfidence.MEDIUM -> Color(0xFFC49A4A)
                    NutritionConfidence.LOW -> Color(0xFFC26A5A)
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(confidenceColor)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Confidence: ${confidence.label}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(16.dp))

            // ── Curry / Mixed Food Improve Accuracy ──
            if (showIngredientOption && onImproveAccuracy != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NutritionAccent.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                        .clickable { onImproveAccuracy() }
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Improve Accuracy",
                            color = NutritionAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Meals with multiple ingredients are harder to estimate.\nAdding ingredients improves calorie and protein accuracy.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Meal category selector ──
            Text(
                text = "Meal",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            ChipSelector(
                options = MealCategory.entries,
                selected = category,
                label = { it.displayName },
                onSelect = { category = it }
            )
            Spacer(Modifier.height(20.dp))

            // ── Actions ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { onConfirm(g, category) }, enabled = g > 0) {
                    Text(
                        "Add to log",
                        color = if (g > 0) NutritionAccent else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun MacroDisplay(
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value $unit",
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}
