package com.solofit.app.data.repository

import com.solofit.app.core.cache.LruTtlCache
import com.solofit.app.data.local.dao.FoodDao
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.data.remote.UsdaFoodService
import com.solofit.app.core.perf.PerfTrace
import com.solofit.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val dao: FoodDao,
    private val usdaService: UsdaFoodService
) : FoodRepository {

    private val usdaResultCache = LruTtlCache<String, List<FoodItemEntity>>(
        maxEntries = 32,
        ttlMillis = 10 * 60 * 1000L
    )

    override fun observeAll(): Flow<List<FoodItemEntity>> = dao.observeAll()
    override fun search(query: String): Flow<List<FoodItemEntity>> = dao.search(query.trim())
    override suspend fun getById(id: Long): FoodItemEntity? = dao.getById(id)
    override suspend fun addCustomFood(item: FoodItemEntity): Long = dao.insert(item)

    override suspend fun searchUsda(query: String): List<FoodItemEntity> {
        if (query.isBlank()) return emptyList()
        val trimmed = query.trim().lowercase()
        usdaResultCache.get(trimmed)?.let { return it }
        // Use null (not emptyList) as the failure sentinel so a transient network
        // error is NOT cached — otherwise an identical retry within the TTL would
        // return an empty list without ever hitting the API again. A genuine
        // "no matches" success IS cached to avoid re-querying.
        val result: List<FoodItemEntity>? = try {
            val response = usdaService.searchFoods(query = query.trim())
            response.foods.mapNotNull { usda ->
                val kcal = usda.foodNutrients.find { it.nutrientId == 1008 }?.value
                val protein = usda.foodNutrients.find { it.nutrientId == 1003 }?.value
                val carbs = usda.foodNutrients.find { it.nutrientId == 1005 }?.value
                val fat = usda.foodNutrients.find { it.nutrientId == 1004 }?.value
                val fiber = usda.foodNutrients.find { it.nutrientId == 1079 }?.value ?: 0.0
                if (kcal == null || protein == null || carbs == null || fat == null) return@mapNotNull null
                FoodItemEntity(
                    name = usda.description,
                    category = "USDA",
                    caloriesPer100g = kcal,
                    proteinPer100g = protein,
                    carbsPer100g = carbs,
                    fatsPer100g = fat,
                    fiberPer100g = fiber,
                    isCustom = true
                )
            }
        } catch (_: Exception) {
            null
        }
        if (result == null) return emptyList()
        usdaResultCache.put(trimmed, result)
        return result
    }

    override suspend fun warmUp() {
        PerfTrace.measureSuspend("food.warmUp") { dao.count() }
    }
}
