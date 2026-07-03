package com.solofit.app.domain.repository

import com.solofit.app.data.local.entity.FriendEntity
import com.solofit.app.data.local.entity.FriendEventEntity
import com.solofit.app.data.local.entity.FriendPermissionEntity
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    fun observeAll(): Flow<List<FriendEntity>>
    fun observeAccepted(): Flow<List<FriendEntity>>
    fun observePending(): Flow<List<FriendEntity>>
    fun observeAcceptedCount(): Flow<Int>
    suspend fun getById(id: Long): FriendEntity?
    fun observeById(id: Long): Flow<FriendEntity?>
    suspend fun getBySoloId(soloId: String): FriendEntity?
    suspend fun addFriend(soloId: String, displayName: String, publicKey: ByteArray): Long
    suspend fun acceptFriend(id: Long)
    suspend fun rejectFriend(id: Long)
    suspend fun removeFriend(id: Long)
    suspend fun setPermission(friendId: Long, category: String, level: String)
    suspend fun removePermission(friendId: Long, category: String)
    fun observePermissions(friendId: Long): Flow<List<FriendPermissionEntity>>
    suspend fun getPermissionLevel(friendId: Long, category: String): String?
    suspend fun setRelationshipType(friendId: Long, type: String)

    fun observeEvents(friendId: Long): Flow<List<FriendEventEntity>>
    fun observeRecentEvents(limit: Int): Flow<List<FriendEventEntity>>
    suspend fun addEvent(friendId: Long, type: String, payload: String)
}
