package com.solofit.app.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.dao.LoggedFoodRow
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.domain.model.MacroTotals
import com.solofit.app.domain.model.MealCategory
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MealSection(
    val category: MealCategory,
    val entries: List<LoggedFoodRow>,
    val totals: MacroTotals
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val dailyLogRepository: DailyLogRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val searchResults = _query
        .debounce { q -> if (q.isBlank()) 0L else 250L }
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.isBlank()) foodRepository.observeAll() else foodRepository.search(q)
        }
        .conflate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sections = dailyLogRepository.observeForDate(DateUtils.today())
        .map { rows ->
            MealCategory.entries.map { cat ->
                val entries = rows.filter { it.log.mealCategory == cat.name }
                MealSection(cat, entries, sumMacros(entries))
            }
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sectionsLoaded = sections
        .drop(1)
        .map { true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onQueryChange(q: String) { _query.value = q }

    fun logFood(food: FoodItemEntity, grams: Double, category: MealCategory) {
        viewModelScope.launch {
            val actualId = if (food.id == 0L) {
                foodRepository.addCustomFood(food)
            } else food.id
            dailyLogRepository.logFood(
                DailyLogEntity(
                    date = DateUtils.today(),
                    foodId = actualId,
                    gramsConsumed = grams,
                    mealCategory = category.name
                )
            )
        }
    }

    fun removeEntry(row: LoggedFoodRow) {
        viewModelScope.launch { dailyLogRepository.removeEntry(row.log) }
    }

    fun addCustomFood(name: String, kcal: Double, protein: Double, carbs: Double, fats: Double, fiber: Double = 0.0) {
        viewModelScope.launch {
            foodRepository.addCustomFood(
                FoodItemEntity(
                    name = name,
                    category = "Custom",
                    caloriesPer100g = kcal,
                    proteinPer100g = protein,
                    carbsPer100g = carbs,
                    fatsPer100g = fats,
                    fiberPer100g = fiber,
                    isCustom = true
                )
            )
        }
    }

    private fun sumMacros(rows: List<LoggedFoodRow>): MacroTotals =
        rows.fold(MacroTotals()) { acc, row ->
            val f = row.log.gramsConsumed / 100.0
            acc + MacroTotals(
                row.food.caloriesPer100g * f,
                row.food.proteinPer100g * f,
                row.food.carbsPer100g * f,
                row.food.fatsPer100g * f,
                row.food.fiberPer100g * f
            )
        }
}
