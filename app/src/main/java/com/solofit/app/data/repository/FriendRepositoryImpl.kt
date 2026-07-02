package com.solofit.app.data.repository

import com.solofit.app.data.local.dao.FriendDao
import com.solofit.app.data.local.dao.FriendEventDao
import com.solofit.app.data.local.entity.FriendEntity
import com.solofit.app.data.local.entity.FriendEventEntity
import com.solofit.app.data.local.entity.FriendPermissionEntity
import com.solofit.app.domain.repository.FriendRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepositoryImpl @Inject constructor(
    private val friendDao: FriendDao,
    private val eventDao: FriendEventDao
) : FriendRepository {

    override fun observeAll(): Flow<List<FriendEntity>> = friendDao.observeAll()
    override fun observeAccepted(): Flow<List<FriendEntity>> = friendDao.observeByStatus("accepted")
    override fun observePending(): Flow<List<FriendEntity>> = friendDao.observeByStatus("pending")
    override fun observeAcceptedCount(): Flow<Int> = friendDao.observeAcceptedCount()

    override suspend fun getById(id: Long): FriendEntity? = friendDao.getById(id)
    override suspend fun getBySoloId(soloId: String): FriendEntity? = friendDao.getBySoloId(soloId)

    override suspend fun addFriend(
        soloId: String,
        displayName: String,
        publicKey: ByteArray
    ): Long {
        val existing = friendDao.getBySoloId(soloId)
        if (existing != null) return existing.id
        return friendDao.insert(
            FriendEntity(
                soloId = soloId,
                displayName = displayName,
                publicKey = publicKey,
                status = "pending"
            )
        )
    }

    override suspend fun acceptFriend(id: Long) = friendDao.updateStatus(id, "accepted")
    override suspend fun rejectFriend(id: Long) {
        friendDao.getById(id)?.let { friendDao.delete(it) }
    }

    override suspend fun removeFriend(id: Long) {
        friendDao.getById(id)?.let { friendDao.delete(it) }
    }

    override suspend fun setRelationshipType(friendId: Long, type: String) {
        friendDao.updateRelationshipType(friendId, type)
    }

    override suspend fun setPermission(friendId: Long, category: String, level: String) {
        friendDao.insertPermission(
            FriendPermissionEntity(
                friendId = friendId,
                category = category,
                level = level
            )
        )
    }

    override suspend fun removePermission(friendId: Long, category: String) {
        friendDao.deletePermission(friendId, category)
    }

    override fun observePermissions(friendId: Long): Flow<List<FriendPermissionEntity>> =
        friendDao.observePermissions(friendId)

    override suspend fun getPermissionLevel(friendId: Long, category: String): String? =
        friendDao.getPermissionLevel(friendId, category)

    override fun observeEvents(friendId: Long): Flow<List<FriendEventEntity>> =
        eventDao.observeByFriend(friendId)

    override fun observeRecentEvents(limit: Int): Flow<List<FriendEventEntity>> =
        eventDao.observeRecent(limit)

    override suspend fun addEvent(friendId: Long, type: String, payload: String) {
        eventDao.insert(FriendEventEntity(friendId = friendId, type = type, payload = payload))
    }
}
