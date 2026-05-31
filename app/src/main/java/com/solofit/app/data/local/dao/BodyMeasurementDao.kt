package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solofit.app.data.local.entity.BodyMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BodyMeasurementEntity): Long

    @Query("SELECT * FROM body_measurements ORDER BY date ASC")
    fun observeAll(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC LIMIT 1")
    fun observeLatest(): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: String): BodyMeasurementEntity?

    @Query("DELETE FROM body_measurements WHERE id = :id")
    suspend fun delete(id: Long)
}
