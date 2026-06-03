package com.solofit.app.ui.foodlookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.data.remote.UsdaFood
import com.solofit.app.data.remote.UsdaNutrient
import com.solofit.app.data.remote.UsdaFoodService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LookupState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<UsdaFood> = emptyList(),
    val selectedFood: UsdaFood? = null,
    val error: String? = null
)

@HiltViewModel
class FoodLookupViewModel @Inject constructor(
    private val usdaService: UsdaFoodService
) : ViewModel() {

    private val _state = MutableStateFlow(LookupState())
    val state = _state.asStateFlow()

    fun onQueryChange(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun search() {
        val q = _state.value.query.trim()
        if (q.length < 2) return
        _state.value = _state.value.copy(isSearching = true, error = null, selectedFood = null)
        viewModelScope.launch {
            try {
                val response = usdaService.searchFoods(query = q, pageSize = 10)
                _state.value = _state.value.copy(
                    isSearching = false,
                    results = response.foods,
                    error = if (response.foods.isEmpty()) "No foods found for \"$q\"." else null
                )
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("DEMO_KEY") == true ||
                        e.message?.contains("unauthorized") == true ||
                        e.message?.contains("403") == true ||
                        e.message?.contains("401") == true ->
                        "Set a valid USDA API key in app/build.gradle.kts (BuildConfig.USDA_API_KEY)."
                    e.message?.contains("Unable to resolve host") == true ||
                        e.message?.contains("timeout") == true ->
                        "No internet connection."
                    else -> "Lookup failed: ${e.message}"
                }
                _state.value = _state.value.copy(isSearching = false, error = msg)
            }
        }
    }

    fun selectFood(food: UsdaFood) {
        _state.value = _state.value.copy(selectedFood = food)
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selectedFood = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

fun extractNutrients(nutrients: List<UsdaNutrient>): Map<String, String> {
    val map = mutableMapOf<String, String>()
    fun add(label: String, id: Int) {
        val v = nutrients.find { it.nutrientId == id }?.value
        if (v != null) map[label] = v.formatNutrient()
    }
    add("Calories", 1008)
    add("Protein", 1003)
    add("Carbs", 1005)
    add("Fat", 1004)
    add("Fiber", 1079)
    add("Sugars", 2000)
    add("Saturated Fat", 1258)
    add("Trans Fat", 1257)
    add("Cholesterol", 1253)
    add("Sodium", 1093)
    add("Potassium", 1092)
    add("Calcium", 1087)
    add("Iron", 1089)
    add("Magnesium", 1090)
    add("Zinc", 1109)
    add("Vitamin C", 1162)
    add("Vitamin A", 1106)
    add("Vitamin D", 1114)
    add("Vitamin E", 1130)
    add("Vitamin B6", 1165)
    add("Vitamin B12", 1175)
    return map
}

internal fun Double.formatNutrient(): String = when {
    kotlin.math.abs(this - kotlin.math.round(this)) < 0.001 -> kotlin.math.round(this).toLong().toString()
    else -> "%.1f".format(this)
}
