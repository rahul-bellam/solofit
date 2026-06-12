package com.solofit.app.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.dao.FrequentMealDao
import com.solofit.app.data.local.dao.LoggedFoodRow
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.data.local.entity.FrequentMealEntity
import com.solofit.app.domain.model.MacroTotals
import com.solofit.app.domain.model.MealCategory
import com.solofit.app.domain.model.MealNameNormalizer
import com.solofit.app.domain.repository.DailyLogRepository
import com.solofit.app.domain.repository.FoodRepository
import com.solofit.app.domain.repository.ProfileRepository
import com.solofit.app.domain.repository.WeightRepository
import com.solofit.app.domain.usecase.AdaptedTargets
import com.solofit.app.domain.usecase.AdaptiveTargetEngine
import com.solofit.app.sol.ProteinPattern
import com.solofit.app.sol.ProteinPatternEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
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
    private val dailyLogRepository: DailyLogRepository,
    private val profileRepository: ProfileRepository,
    private val weightRepository: WeightRepository,
    private val adaptiveTargetEngine: AdaptiveTargetEngine,
    private val frequentMealDao: FrequentMealDao
) : ViewModel() {

    val profile = profileRepository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adaptedTargets = combine(
        profileRepository.observeProfile(),
        weightRepository.observeAll().map { it.lastOrNull() }
    ) { p, w -> Pair(p, w) }
        .flatMapLatest { (profile, weight) ->
            if (profile == null) flowOf<AdaptedTargets?>(null)
            else flow {
                val recent = dailyLogRepository.getDailyTotalsSince(DateUtils.daysAgo(7))
                emit(adaptiveTargetEngine(profile, weight, recent))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val frequentMeals = frequentMealDao.observeTopFrequent(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // ── Protein pattern state ──
    private val _proteinPattern = MutableStateFlow<ProteinPattern?>(null)
    val proteinPattern = _proteinPattern.asStateFlow()

    init {
        viewModelScope.launch {
            evaluateProteinPattern()
            // Prune old frequent meals on launch and daily thereafter
            pruneOldFrequentMeals()
        }
    }

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
            // Save to frequent meal memory
            saveFrequentMeal(food, category)
            // Re-evaluate protein pattern
            evaluateProteinPattern()
        }
    }

    private suspend fun saveFrequentMeal(food: FoodItemEntity, category: MealCategory) {
        val norm = MealNameNormalizer.normalize(food.name)
        val existing = frequentMealDao.getByNormalizedName(norm)
        if (existing != null) {
            frequentMealDao.incrementLogCount(norm)
        } else {
            frequentMealDao.upsert(
                FrequentMealEntity(
                    name = food.name,
                    normalizedName = norm,
                    caloriesPer100g = food.caloriesPer100g,
                    proteinPer100g = food.proteinPer100g,
                    carbsPer100g = food.carbsPer100g,
                    fatsPer100g = food.fatsPer100g,
                    fiberPer100g = food.fiberPer100g,
                    logCount = 1,
                    lastLoggedAt = System.currentTimeMillis(),
                    confidence = if (food.barcode != null) "HIGH" else "MEDIUM"
                )
            )
        }
    }

    private suspend fun evaluateProteinPattern() {
        val profile = profileRepository.observeProfile().first()
        val targetProtein = profile?.targetProtein?.toDouble() ?: return
        val recentTotals = dailyLogRepository.getDailyTotalsSince(DateUtils.daysAgo(14))

        val recentDays = recentTotals.map { totals ->
            totals.proteinG to targetProtein
        }

        val pattern = ProteinPatternEngine.evaluate(recentDays)
        _proteinPattern.value = pattern
    }

    private suspend fun pruneOldFrequentMeals() {
        while (true) {
            val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            frequentMealDao.pruneOld(thirtyDaysAgo)
            delay(24L * 60 * 60 * 1000) // repeat once per day
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
