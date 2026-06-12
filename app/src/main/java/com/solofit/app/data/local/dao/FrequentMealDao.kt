package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solofit.app.data.local.entity.FrequentMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FrequentMealDao {

    @Query("""
        SELECT * FROM frequent_meals
        ORDER BY logCount DESC, lastLoggedAt DESC
        LIMIT :limit
    """)
    fun observeTopFrequent(limit: Int = 10): Flow<List<FrequentMealEntity>>

    @Query("""
        SELECT * FROM frequent_meals
        WHERE normalizedName LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'
        ORDER BY logCount DESC, lastLoggedAt DESC
        LIMIT :limit
    """)
    fun searchFrequent(query: String, limit: Int = 5): Flow<List<FrequentMealEntity>>

    @Query("SELECT * FROM frequent_meals WHERE normalizedName = :normalizedName LIMIT 1")
    suspend fun getByNormalizedName(normalizedName: String): FrequentMealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meal: FrequentMealEntity)

    @Query("UPDATE frequent_meals SET logCount = logCount + 1, lastLoggedAt = :now WHERE normalizedName = :normalizedName")
    suspend fun incrementLogCount(normalizedName: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM frequent_meals WHERE normalizedName = :normalizedName")
    suspend fun delete(normalizedName: String)

    @Query("DELETE FROM frequent_meals WHERE logCount <= 1 AND lastLoggedAt < :threshold")
    suspend fun pruneOld(threshold: Long)
}
