package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solofit.app.data.local.entity.GoalItemEntity
import com.solofit.app.data.local.entity.GratitudeEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    // ---- Goals (morning checklist) ----
    @Insert
    suspend fun insertGoal(goal: GoalItemEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalItemEntity)

    @Query("DELETE FROM goal_items WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    @Query("SELECT * FROM goal_items WHERE date = :date ORDER BY orderIndex ASC, id ASC")
    fun observeGoals(date: String): Flow<List<GoalItemEntity>>

    @Query("SELECT COUNT(*) FROM goal_items WHERE date = :date AND done = 1")
    suspend fun completedCount(date: String): Int

    @Query("SELECT COUNT(*) FROM goal_items WHERE date = :date")
    suspend fun totalCount(date: String): Int

    // ---- Gratitude (evening note) ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGratitude(entry: GratitudeEntryEntity): Long

    @Query("SELECT * FROM gratitude_entries WHERE date = :date LIMIT 1")
    fun observeGratitude(date: String): Flow<GratitudeEntryEntity?>

    @Query("SELECT * FROM gratitude_entries ORDER BY date DESC LIMIT :limit")
    fun observeRecentGratitude(limit: Int): Flow<List<GratitudeEntryEntity>>
}
