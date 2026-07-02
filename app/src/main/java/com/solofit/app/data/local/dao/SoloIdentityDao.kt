package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solofit.app.data.local.entity.SoloIdentityEntity

@Dao
interface SoloIdentityDao {
    @Query("SELECT * FROM solo_identity WHERE id = 1")
    suspend fun get(): SoloIdentityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(identity: SoloIdentityEntity)

    @Query("DELETE FROM solo_identity")
    suspend fun delete()
}
