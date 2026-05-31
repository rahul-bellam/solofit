package com.solofit.app.data.repository

import com.solofit.app.data.local.dao.JournalDao
import com.solofit.app.data.local.entity.GoalItemEntity
import com.solofit.app.data.local.entity.GratitudeEntryEntity
import com.solofit.app.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val dao: JournalDao
) : JournalRepository {

    override fun observeGoals(date: String): Flow<List<GoalItemEntity>> = dao.observeGoals(date)

    override suspend fun addGoal(date: String, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val order = dao.totalCount(date)
        dao.insertGoal(GoalItemEntity(date = date, text = clean, orderIndex = order))
    }

    override suspend fun toggleGoal(goal: GoalItemEntity) {
        dao.updateGoal(goal.copy(done = !goal.done))
    }

    override suspend fun deleteGoal(id: Long) = dao.deleteGoal(id)

    override fun observeGratitude(date: String): Flow<GratitudeEntryEntity?> =
        dao.observeGratitude(date)

    override fun observeRecentGratitude(limit: Int): Flow<List<GratitudeEntryEntity>> =
        dao.observeRecentGratitude(limit)

    override suspend fun saveGratitude(date: String, text: String) {
        dao.upsertGratitude(GratitudeEntryEntity(date = date, text = text.trim()))
    }
}
