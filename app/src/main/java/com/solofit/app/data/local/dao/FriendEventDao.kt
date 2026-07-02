package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.solofit.app.data.local.entity.FriendEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendEventDao {
    @Query("SELECT * FROM friend_events WHERE friendId = :friendId ORDER BY createdAt DESC")
    fun observeByFriend(friendId: Long): Flow<List<FriendEventEntity>>

    @Query("SELECT * FROM friend_events ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<FriendEventEntity>>

    @Insert
    suspend fun insert(event: FriendEventEntity): Long

    @Delete
    suspend fun delete(event: FriendEventEntity)

    @Query("DELETE FROM friend_events WHERE friendId = :friendId")
    suspend fun deleteByFriend(friendId: Long)
}
