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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.solofit.app.domain.model.CommonIngredients
import com.solofit.app.domain.model.Ingredient
import com.solofit.app.domain.model.IngredientMeal
import com.solofit.app.ui.theme.CardPrimary
import com.solofit.app.ui.theme.Hairline
import com.solofit.app.ui.theme.NutritionAccent
import com.solofit.app.ui.theme.NutritionProtein
import com.solofit.app.ui.theme.TextPrimary
import com.solofit.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun IngredientSelectionSheet(
    dishName: String,
    onDismiss: () -> Unit,
    onConfirm: (IngredientMeal) -> Unit
) {
    val ingredientWeights = remember { mutableStateMapOf<String, Double>() }
    val selectedIngredients = remember { mutableStateMapOf<String, Boolean>() }

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
            Text(
                text = "Break down $dishName",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Add ingredients that make up this dish for accurate macros",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(20.dp))

            val activeIngredients = CommonIngredients.list.filter { it.name in selectedIngredients && selectedIngredients[it.name] == true }

            if (activeIngredients.isNotEmpty()) {
                val meal = buildIngredientMeal(dishName, activeIngredients.map { ing ->
                    val weight = ingredientWeights[ing.name] ?: ing.grams
                    ing.copy(grams = weight)
                })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NutritionAccent.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Per 100g of this dish",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MacroDisplay("Cal", "${meal.caloriesPer100g.roundToInt()}", "kcal", NutritionAccent)
                            MacroDisplay("Protein", "${meal.proteinPer100g.roundToInt()}", "g", NutritionProtein)
                            MacroDisplay("Carbs", "${meal.carbsPer100g.roundToInt()}", "g", TextSecondary)
                            MacroDisplay("Fats", "${meal.fatsPer100g.roundToInt()}", "g", TextSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Total: ${meal.totalGrams.roundToInt()}g dish · ${meal.totalCalories.roundToInt()} kcal",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                text = "Common ingredients",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            CommonIngredients.list.forEach { ingredient ->
                val isSelected = selectedIngredients[ingredient.name] == true
                val weight = ingredientWeights[ingredient.name] ?: ingredient.grams

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) NutritionAccent.copy(alpha = 0.04f)
                            else Color.Transparent
                        )
                        .clickable {
                            selectedIngredients[ingredient.name] = !isSelected
                            if (!isSelected) {
                                ingredientWeights[ingredient.name] = ingredient.grams
                            }
                        }
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) NutritionAccent else Hairline,
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NutritionAccent else Hairline,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ingredient.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "${ingredient.caloriesPer100g.roundToInt()} kcal/100g · P ${ingredient.proteinPer100g.roundToInt()}g",
                                color = TextSecondary, fontSize = 11.sp
                            )
                        }
                    }

                    if (isSelected) {
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${weight.roundToInt()}g",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.width(50.dp)
                            )
                            Slider(
                                value = weight.toFloat().coerceIn(5f, 500f),
                                onValueChange = { ingredientWeights[ingredient.name] = it.toDouble().coerceAtLeast(1.0) },
                                valueRange = 5f..500f,
                                steps = 48,
                                colors = SliderDefaults.colors(
                                    thumbColor = NutritionAccent,
                                    activeTrackColor = NutritionAccent,
                                    inactiveTrackColor = Hairline
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        val ingMacros = ingredient.copy(grams = weight)
                        Text(
                            text = "${ingMacros.calories.roundToInt()} kcal · P ${ingMacros.protein.roundToInt()}g",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 60.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        if (activeIngredients.isNotEmpty()) {
                            val meal = buildIngredientMeal(
                                dishName,
                                activeIngredients.map { ing ->
                                    val w = ingredientWeights[ing.name] ?: ing.grams
                                    ing.copy(grams = w)
                                }
                            )
                            onConfirm(meal)
                        }
                    },
                    enabled = activeIngredients.isNotEmpty()
                ) {
                    Text(
                        "Use These Ingredients",
                        color = if (activeIngredients.isNotEmpty()) NutritionAccent else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun buildIngredientMeal(name: String, ingredients: List<Ingredient>): IngredientMeal {
    val totalGrams = ingredients.sumOf { it.grams }
    val totalCalories = ingredients.sumOf { it.calories }
    val totalProtein = ingredients.sumOf { it.protein }
    val totalCarbs = ingredients.sumOf { it.carbs }
    val totalFats = ingredients.sumOf { it.fats }
    return IngredientMeal(
        name = name,
        ingredients = ingredients,
        totalGrams = totalGrams,
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFats = totalFats
    )
}
