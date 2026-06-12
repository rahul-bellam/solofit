package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solofit.app.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    /** Upsert: one row per date. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WeightEntryEntity): Long

    @Query("SELECT * FROM weight_entries ORDER BY date ASC")
    fun observeAll(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 1")
    suspend fun latest(): WeightEntryEntity?

    @Query("SELECT * FROM weight_entries WHERE date >= :startDate ORDER BY date ASC")
    suspend fun getEntriesSince(startDate: String): List<WeightEntryEntity>

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun delete(id: Long)
}
