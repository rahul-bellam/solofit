package com.solofit.app.data.repository

import com.solofit.app.data.local.dao.DailyLogDao
import com.solofit.app.data.local.dao.DailyTotalsWithDate
import com.solofit.app.data.local.dao.LoggedFoodRow
import com.solofit.app.domain.model.MacroTotals
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.domain.repository.DailyLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DailyLogRepositoryImpl @Inject constructor(
    private val dao: DailyLogDao
) : DailyLogRepository {
    override fun observeForDate(date: String): Flow<List<LoggedFoodRow>> = dao.observeForDate(date)

    override fun observeTotalsForDate(date: String): Flow<MacroTotals> =
        dao.observeTotalsForDate(date).map { t ->
            MacroTotals(
                calories = t.calories ?: 0.0,
                proteinG = t.proteinG ?: 0.0,
                carbsG = t.carbsG ?: 0.0,
                fatsG = t.fatsG ?: 0.0,
                fiberG = t.fiberG ?: 0.0
            )
        }
    override suspend fun logFood(entry: DailyLogEntity): Long = dao.insert(entry)
    override suspend fun removeEntry(entry: DailyLogEntity) = dao.delete(entry)

    override suspend fun getDailyTotalsSince(startDate: String): List<Pair<String, MacroTotals>> =
        dao.getDailyTotalsSince(startDate).map { t ->
            t.date to MacroTotals(
                calories = t.calories ?: 0.0,
                proteinG = t.proteinG ?: 0.0,
                carbsG = t.carbsG ?: 0.0,
                fatsG = t.fatsG ?: 0.0,
                fiberG = t.fiberG ?: 0.0
            )
        }
}
