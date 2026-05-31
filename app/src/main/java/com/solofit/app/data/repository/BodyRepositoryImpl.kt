package com.solofit.app.data.repository

import com.solofit.app.data.local.dao.BodyMeasurementDao
import com.solofit.app.data.local.dao.DailyMetricDao
import com.solofit.app.data.local.entity.BodyMeasurementEntity
import com.solofit.app.data.local.entity.DailyMetricEntity
import com.solofit.app.domain.repository.BodyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BodyRepositoryImpl @Inject constructor(
    private val measurementDao: BodyMeasurementDao,
    private val metricDao: DailyMetricDao
) : BodyRepository {

    override fun observeMeasurements(): Flow<List<BodyMeasurementEntity>> =
        measurementDao.observeAll()

    override fun observeLatestMeasurement(): Flow<BodyMeasurementEntity?> =
        measurementDao.observeLatest()

    override suspend fun getMeasurementForDate(date: String): BodyMeasurementEntity? =
        measurementDao.getForDate(date)

    override suspend fun saveMeasurement(entry: BodyMeasurementEntity): Long =
        measurementDao.upsert(entry)

    override suspend fun deleteMeasurement(id: Long) = measurementDao.delete(id)

    override fun observeMetric(date: String): Flow<DailyMetricEntity?> =
        metricDao.observeForDate(date)

    override suspend fun getMetric(date: String): DailyMetricEntity? =
        metricDao.getForDate(date)

    override suspend fun saveMetric(entry: DailyMetricEntity): Long = metricDao.upsert(entry)
}
