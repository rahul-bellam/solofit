package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single morning goal (checklist item) for a given day.
 * Lightweight by design — text + done flag, keyed by date.
 */
@Entity(
    tableName = "goal_items",
    indices = [Index(value = ["date"])]
)
data class GoalItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,            // ISO yyyy-MM-dd
    val text: String,
    val done: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * The evening gratitude note for a given day. One canonical entry per date
 * (date unique) so re-saving updates the same row.
 */
@Entity(
    tableName = "gratitude_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class GratitudeEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,            // ISO yyyy-MM-dd
    val text: String,
    val updatedAt: Long = System.currentTimeMillis()
)
