package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.data.local.entity.FoodItemEntity
import kotlinx.coroutines.flow.Flow

/** A joined row: a log entry together with its referenced food item. */
data class LoggedFoodRow(
    @Embedded val log: DailyLogEntity,
    @Embedded(prefix = "food_") val food: FoodItemEntity
)

/**
 * Pre-aggregated daily totals computed *in SQLite*, so the dashboard never has to
 * materialize every row just to sum it. Nullable because a day with no entries
 * returns all-NULL from SUM().
 */
data class DailyTotals(
    val calories: Double?,
    val proteinG: Double?,
    val carbsG: Double?,
    val fatsG: Double?,
    val fiberG: Double?
)

@Dao
interface DailyLogDao {

    @Insert
    suspend fun insert(entry: DailyLogEntity): Long

    @Delete
    suspend fun delete(entry: DailyLogEntity)

    @Query(
        """
        SELECT 
            dl.id AS id, dl.date AS date, dl.foodId AS foodId,
            dl.gramsConsumed AS gramsConsumed, dl.mealCategory AS mealCategory,
            dl.loggedAt AS loggedAt,
            fi.id AS food_id, fi.name AS food_name, fi.category AS food_category,
            fi.caloriesPer100g AS food_caloriesPer100g, fi.proteinPer100g AS food_proteinPer100g,
            fi.carbsPer100g AS food_carbsPer100g, fi.fatsPer100g AS food_fatsPer100g,
            fi.fiberPer100g AS food_fiberPer100g, fi.isCustom AS food_isCustom
        FROM daily_log dl
        INNER JOIN food_items fi ON fi.id = dl.foodId
        WHERE dl.date = :date
        ORDER BY dl.loggedAt ASC
        """
    )
    fun observeForDate(date: String): Flow<List<LoggedFoodRow>>

    /**
     * Dashboard fast-path: SQLite multiplies grams/100 * per-100g and sums it,
     * returning a single row instead of N entities. Far fewer allocations.
     */
    @Query(
        """
        SELECT 
            SUM(dl.gramsConsumed / 100.0 * fi.caloriesPer100g) AS calories,
            SUM(dl.gramsConsumed / 100.0 * fi.proteinPer100g)  AS proteinG,
            SUM(dl.gramsConsumed / 100.0 * fi.carbsPer100g)    AS carbsG,
            SUM(dl.gramsConsumed / 100.0 * fi.fatsPer100g)     AS fatsG,
            SUM(dl.gramsConsumed / 100.0 * fi.fiberPer100g)    AS fiberG
        FROM daily_log dl
        INNER JOIN food_items fi ON fi.id = dl.foodId
        WHERE dl.date = :date
        """
    )
    fun observeTotalsForDate(date: String): Flow<DailyTotals>
}
