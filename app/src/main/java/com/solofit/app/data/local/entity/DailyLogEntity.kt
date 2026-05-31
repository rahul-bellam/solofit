package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One logged food entry on a given day.
 * @param date stored as ISO yyyy-MM-dd string for easy daily grouping/queries.
 */
@Entity(
    tableName = "daily_log",
    foreignKeys = [
        ForeignKey(
            entity = FoodItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("foodId"), Index("date")]
)
data class DailyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val foodId: Long,
    val gramsConsumed: Double,
    val mealCategory: String,
    val loggedAt: Long = System.currentTimeMillis()
)
