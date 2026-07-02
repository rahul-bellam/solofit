package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "friends",
    indices = [Index(value = ["soloId"], unique = true)]
)
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val soloId: String,
    val displayName: String,
    val publicKey: ByteArray,
    val status: String = "pending",
    val relationshipType: String = "accountability_partner",
    val addedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FriendEntity) return false
        return id == other.id && soloId == other.soloId && displayName == other.displayName &&
            publicKey.contentEquals(other.publicKey) && status == other.status &&
            relationshipType == other.relationshipType && addedAt == other.addedAt
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + soloId.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + relationshipType.hashCode()
        result = 31 * result + addedAt.hashCode()
        return result
    }
}

@Entity(
    tableName = "friend_permissions",
    foreignKeys = [ForeignKey(
        entity = FriendEntity::class,
        parentColumns = ["id"],
        childColumns = ["friendId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["friendId", "category"], unique = true)]
)
data class FriendPermissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val friendId: Long,
    val category: String,
    val level: String = "private"
)

@Entity(tableName = "friend_groups")
data class FriendGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "group_members",
    foreignKeys = [
        ForeignKey(
            entity = FriendGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FriendEntity::class,
            parentColumns = ["id"],
            childColumns = ["friendId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId", "friendId"], unique = true)]
)
data class GroupMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val friendId: Long,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "friend_events",
    foreignKeys = [ForeignKey(
        entity = FriendEntity::class,
        parentColumns = ["id"],
        childColumns = ["friendId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["friendId"]), Index(value = ["friendId", "createdAt"])]
)
data class FriendEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val friendId: Long,
    val type: String,
    val payload: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
