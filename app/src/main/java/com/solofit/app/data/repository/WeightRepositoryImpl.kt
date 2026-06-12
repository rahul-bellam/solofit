package com.solofit.app.data.repository

import com.solofit.app.data.local.dao.WeightDao
import com.solofit.app.data.local.entity.WeightEntryEntity
import com.solofit.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WeightRepositoryImpl @Inject constructor(
    private val dao: WeightDao
) : WeightRepository {
    override fun observeAll(): Flow<List<WeightEntryEntity>> = dao.observeAll()
    override suspend fun logWeight(date: String, weightKg: Double): Long =
        dao.upsert(WeightEntryEntity(date = date, weightKg = weightKg))
    override suspend fun latest(): WeightEntryEntity? = dao.latest()
    override suspend fun getEntriesSince(startDate: String): List<WeightEntryEntity> =
        dao.getEntriesSince(startDate)
    override suspend fun delete(id: Long) = dao.delete(id)
}
