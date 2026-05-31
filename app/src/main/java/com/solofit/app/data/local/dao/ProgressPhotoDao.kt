package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.solofit.app.data.local.entity.ProgressPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPhotoDao {
    @Insert
    suspend fun insert(photo: ProgressPhotoEntity): Long

    @Query("SELECT * FROM progress_photos ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<ProgressPhotoEntity>>

    @Query("SELECT * FROM progress_photos WHERE pose = :pose ORDER BY date ASC, createdAt ASC")
    fun observeByPose(pose: String): Flow<List<ProgressPhotoEntity>>

    @Query("SELECT * FROM progress_photos WHERE id = :id")
    suspend fun getById(id: Long): ProgressPhotoEntity?

    @Query("DELETE FROM progress_photos WHERE id = :id")
    suspend fun delete(id: Long)
}
