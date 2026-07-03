package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.solofit.app.data.local.entity.FriendEntity
import com.solofit.app.data.local.entity.FriendGroupEntity
import com.solofit.app.data.local.entity.FriendPermissionEntity
import com.solofit.app.data.local.entity.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY displayName ASC")
    fun observeAll(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE id = :id")
    suspend fun getById(id: Long): FriendEntity?

    @Query("SELECT * FROM friends WHERE id = :id")
    fun observeById(id: Long): Flow<FriendEntity?>

    @Query("SELECT * FROM friends WHERE soloId = :soloId")
    suspend fun getBySoloId(soloId: String): FriendEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(friend: FriendEntity): Long

    @Delete
    suspend fun delete(friend: FriendEntity)

    @Query("UPDATE friends SET relationshipType = :type WHERE id = :id")
    suspend fun updateRelationshipType(id: Long, type: String)

    @Query("UPDATE friends SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT * FROM friends WHERE status = :status ORDER BY displayName ASC")
    fun observeByStatus(status: String): Flow<List<FriendEntity>>

    @Query("SELECT COUNT(*) FROM friends WHERE status = 'accepted'")
    fun observeAcceptedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermission(permission: FriendPermissionEntity): Long

    @Query("SELECT * FROM friend_permissions WHERE friendId = :friendId")
    fun observePermissions(friendId: Long): Flow<List<FriendPermissionEntity>>

    @Query("DELETE FROM friend_permissions WHERE friendId = :friendId AND category = :category")
    suspend fun deletePermission(friendId: Long, category: String)

    @Query("SELECT level FROM friend_permissions WHERE friendId = :friendId AND category = :category")
    suspend fun getPermissionLevel(friendId: Long, category: String): String?

    @Insert
    suspend fun insertGroup(group: FriendGroupEntity): Long

    @Query("SELECT * FROM friend_groups ORDER BY name ASC")
    fun observeGroups(): Flow<List<FriendGroupEntity>>

    @Delete
    suspend fun deleteGroup(group: FriendGroupEntity)

    @Query("SELECT * FROM friend_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): FriendGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMemberEntity): Long

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun observeGroupMembers(groupId: Long): Flow<List<GroupMemberEntity>>

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND friendId = :friendId")
    suspend fun removeGroupMember(groupId: Long, friendId: Long)

    @Transaction
    suspend fun deleteGroupWithMembers(group: FriendGroupEntity) {
        deleteGroup(group)
    }
}
