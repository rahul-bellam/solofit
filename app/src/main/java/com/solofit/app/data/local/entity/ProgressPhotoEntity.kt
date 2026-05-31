package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A progress photo. Only a relative file name + metadata is stored in the DB; the
 * image bytes live in the app's private internal storage (never uploaded).
 *
 * @param pose one of "FRONT" / "SIDE" / "BACK"
 * @param fileName file name inside filesDir/progress_photos/
 */
@Entity(
    tableName = "progress_photos",
    indices = [Index(value = ["date"]), Index(value = ["pose"])]
)
data class ProgressPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,           // ISO yyyy-MM-dd
    val pose: String,           // FRONT / SIDE / BACK
    val fileName: String,
    val createdAt: Long = System.currentTimeMillis()
)
