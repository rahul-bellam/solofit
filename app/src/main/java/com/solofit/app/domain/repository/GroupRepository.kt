package com.solofit.app.domain.repository

import com.solofit.app.data.local.entity.FriendGroupEntity
import com.solofit.app.data.local.entity.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun observeGroups(): Flow<List<FriendGroupEntity>>
    suspend fun getGroup(id: Long): FriendGroupEntity?
    suspend fun createGroup(name: String): Long
    suspend fun deleteGroup(id: Long)
    fun observeMembers(groupId: Long): Flow<List<GroupMemberEntity>>
    suspend fun addMember(groupId: Long, friendId: Long)
    suspend fun removeMember(groupId: Long, friendId: Long)
}
