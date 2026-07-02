package com.solofit.app.data.repository

import com.solofit.app.data.local.dao.FriendDao
import com.solofit.app.data.local.entity.FriendGroupEntity
import com.solofit.app.data.local.entity.GroupMemberEntity
import com.solofit.app.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val friendDao: FriendDao
) : GroupRepository {

    override fun observeGroups(): Flow<List<FriendGroupEntity>> = friendDao.observeGroups()

    override suspend fun getGroup(id: Long): FriendGroupEntity? = friendDao.getGroupById(id)

    override suspend fun createGroup(name: String): Long =
        friendDao.insertGroup(FriendGroupEntity(name = name))

    override suspend fun deleteGroup(id: Long) {
        friendDao.getGroupById(id)?.let { friendDao.deleteGroupWithMembers(it) }
    }

    override fun observeMembers(groupId: Long): Flow<List<GroupMemberEntity>> =
        friendDao.observeGroupMembers(groupId)

    override suspend fun addMember(groupId: Long, friendId: Long) {
        friendDao.insertGroupMember(GroupMemberEntity(groupId = groupId, friendId = friendId))
    }

    override suspend fun removeMember(groupId: Long, friendId: Long) {
        friendDao.removeGroupMember(groupId, friendId)
    }
}
