package com.solofit.app.data.repository

import com.solofit.app.data.local.dao.FoodDao
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.core.perf.PerfTrace
import com.solofit.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val dao: FoodDao
) : FoodRepository {
    override fun observeAll(): Flow<List<FoodItemEntity>> = dao.observeAll()
    override fun search(query: String): Flow<List<FoodItemEntity>> = dao.search(query.trim())
    override suspend fun getById(id: Long): FoodItemEntity? = dao.getById(id)
    override suspend fun addCustomFood(item: FoodItemEntity): Long = dao.insert(item)

    override suspend fun warmUp() {
        // Prefetch: a cheap query forces SQLite to open the DB + load index pages,
        // so the user's first real search hits a warm cache (OS read-ahead idea).
        PerfTrace.measureSuspend("food.warmUp") { dao.count() }
    }
}
