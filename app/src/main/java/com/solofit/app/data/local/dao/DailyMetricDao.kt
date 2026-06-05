package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solofit.app.data.local.entity.DailyMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMetricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DailyMetricEntity): Long

    @Query("SELECT * FROM daily_metrics WHERE date = :date LIMIT 1")
    fun observeForDate(date: String): Flow<DailyMetricEntity?>

    @Query("SELECT * FROM daily_metrics WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: String): DailyMetricEntity?

    @Query("SELECT * FROM daily_metrics ORDER BY date ASC")
    fun observeAll(): Flow<List<DailyMetricEntity>>

    @Query("SELECT * FROM daily_metrics WHERE date >= :startDate ORDER BY date ASC")
    fun observeSince(startDate: String): Flow<List<DailyMetricEntity>>
}
