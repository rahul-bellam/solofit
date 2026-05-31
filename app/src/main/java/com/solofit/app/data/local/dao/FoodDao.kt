package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solofit.app.data.local.entity.FoodItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<FoodItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FoodItemEntity): Long

    @Query("SELECT * FROM food_items ORDER BY name ASC")
    fun observeAll(): Flow<List<FoodItemEntity>>

    @Query(
        "SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' " +
            "ORDER BY CASE WHEN name LIKE :query || '%' THEN 0 ELSE 1 END, name ASC LIMIT 50"
    )
    fun search(query: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getById(id: Long): FoodItemEntity?

    /** Local barcode cache lookup — resolves repeat scans without networking. */
    @Query("SELECT * FROM food_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): FoodItemEntity?

    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun count(): Int
}
