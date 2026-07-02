package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solo_identity")
data class SoloIdentityEntity(
    @PrimaryKey val id: Int = 1,
    val soloId: String,
    val displayName: String,
    val publicKey: ByteArray,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SoloIdentityEntity) return false
        return id == other.id && soloId == other.soloId && displayName == other.displayName &&
            publicKey.contentEquals(other.publicKey) && createdAt == other.createdAt
    }
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + soloId.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
