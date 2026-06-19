package com.solofit.app.data.repository

import com.solofit.app.core.cache.LruTtlCache
import com.solofit.app.core.perf.PerfTrace
import com.solofit.app.data.local.dao.FoodDao
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.data.remote.OpenFoodFactsService
import com.solofit.app.data.remote.dto.OffNutriments
import com.solofit.app.domain.model.BarcodeLookupResult
import com.solofit.app.domain.model.ScannedFood
import com.solofit.app.domain.repository.BarcodeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Two-tier **cache-aside** barcode resolution (CDN/buffer-pool principle):
 *
 *   L1: in-memory LRU+TTL cache  (nanoseconds, survives within a session)
 *   L2: local Room cache         (microseconds, survives across launches, offline)
 *   L3: Open Food Facts network  (slowest, only on a full miss)
 *
 * Each hit at a faster tier avoids all slower tiers — the same tiered-cache idea
 * used by CPUs (L1/L2/L3) and web infrastructure (browser → CDN → origin).
 */
@Singleton
class BarcodeRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao,
    private val service: OpenFoodFactsService,
    @Named("io") private val io: CoroutineDispatcher
) : BarcodeRepository {

    // L1 memory cache: small + short TTL (products rarely change mid-session).
    private val memCache = LruTtlCache<String, ScannedFood>(
        maxEntries = 128,
        ttlMillis = 30 * 60 * 1000L
    )

    override suspend fun lookup(barcode: String): BarcodeLookupResult = withContext(io) {
        PerfTrace.measureSuspend("barcode.lookup") {
            try {
                // L1: memory
                memCache.get(barcode)?.let {
                    return@measureSuspend BarcodeLookupResult.Found(it)
                }

                // L2: local Room cache — fully offline path for repeat scans.
                foodDao.getByBarcode(barcode)?.let { cached ->
                    val food = cached.toScannedFood()
                    memCache.put(barcode, food)
                    return@measureSuspend BarcodeLookupResult.Found(food)
                }

                // L3: network (Open Food Facts).
                val response = service.getProduct(barcode)
                val product = response.product
                if (response.status != 1 || product == null) {
                    return@measureSuspend BarcodeLookupResult.NotFound(barcode)
                }
                val n = product.nutriments
                val calories = n?.resolvedKcal()
                if (n == null || calories == null) {
                    return@measureSuspend BarcodeLookupResult.NotFound(barcode)
                }
                val name = product.productName?.takeIf { it.isNotBlank() }
                    ?: product.productNameEn?.takeIf { it.isNotBlank() }
                    ?: product.genericName?.takeIf { it.isNotBlank() }
                    ?: product.brands?.takeIf { it.isNotBlank() }
                    ?: "Product $barcode"

                val food = ScannedFood(
                    barcode = barcode,
                    name = name.trim(),
                    caloriesPer100g = calories,
                    proteinPer100g = n.proteins100g ?: 0.0,
                    carbsPer100g = n.carbs100g ?: 0.0,
                    fatsPer100g = n.fat100g ?: 0.0,
                    fiberPer100g = n.fiber100g ?: 0.0
                )
                memCache.put(barcode, food)
                BarcodeLookupResult.Found(food)
            } catch (e: IOException) {
                BarcodeLookupResult.Error("No connection. Check internet and try again.")
            } catch (e: Exception) {
                BarcodeLookupResult.Error(e.message ?: "Lookup failed. Try manual entry.")
            }
        }
    }

    override suspend fun saveScannedFood(food: ScannedFood, category: String): Long =
        withContext(io) {
            foodDao.getByBarcode(food.barcode)?.let { return@withContext it.id }
            val id = foodDao.insert(
                FoodItemEntity(
                    name = food.name,
                    category = category,
                    caloriesPer100g = food.caloriesPer100g,
                    proteinPer100g = food.proteinPer100g,
                    carbsPer100g = food.carbsPer100g,
                    fatsPer100g = food.fatsPer100g,
                    fiberPer100g = food.fiberPer100g,
                    isCustom = true,
                    barcode = food.barcode
                )
            )
            // Refresh L1 so the next lookup is instant.
            if (food.barcode.isNotBlank()) memCache.put(food.barcode, food)
            id
        }

    private fun FoodItemEntity.toScannedFood() = ScannedFood(
        barcode = barcode ?: "",
        name = name,
        caloriesPer100g = caloriesPer100g,
        proteinPer100g = proteinPer100g,
        carbsPer100g = carbsPer100g,
        fatsPer100g = fatsPer100g,
        fiberPer100g = fiberPer100g
    )

    private fun OffNutriments.resolvedKcal(): Double? =
        energyKcal100g ?: energyKj100g?.let { it / 4.184 }
}
