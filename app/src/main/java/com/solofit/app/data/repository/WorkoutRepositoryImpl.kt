package com.solofit.app.data.repository

import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.dao.WorkoutDao
import com.solofit.app.data.local.entity.ExerciseEntity
import com.solofit.app.data.local.entity.ExerciseSetEntity
import com.solofit.app.data.local.entity.PersonalRecordEntity
import com.solofit.app.data.local.entity.RoutineEntity
import com.solofit.app.data.local.entity.WorkoutSessionEntity
import com.solofit.app.data.local.relation.RoutineWithExercises
import com.solofit.app.data.local.relation.SessionWithSets
import com.solofit.app.domain.model.ExercisePlan
import com.solofit.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao
) : WorkoutRepository {

    override fun observeRoutines(): Flow<List<RoutineWithExercises>> = dao.observeRoutines()

    override suspend fun getRoutine(id: Long): RoutineWithExercises? =
        dao.getRoutineWithExercises(id)

    override suspend fun saveRoutine(
        name: String,
        notes: String,
        exercises: List<ExercisePlan>,
        routineId: Long?
    ): Long {
        val entities = exercises.mapIndexed { index, plan ->
            ExerciseEntity(
                routineId = 0,               // overwritten with the real id inside the transaction
                name = plan.name,
                muscleGroup = plan.muscleGroup,
                orderIndex = index
            )
        }
        // Single transaction: insert/update the routine and (re)write its exercises
        // atomically so a crash can't leave the routine without its exercises.
        return dao.saveRoutineWithExercises(
            RoutineEntity(id = routineId ?: 0L, name = name, notes = notes),
            entities
        )
    }

    override suspend fun deleteRoutine(routine: RoutineEntity) = dao.deleteRoutine(routine)

    override suspend fun startSession(routineId: Long): Long {
        val routine = dao.getRoutineWithExercises(routineId)
            ?: error("Routine not found")
        val sessionId = dao.insertSession(
            WorkoutSessionEntity(
                routineId = routine.routine.id,
                routineName = routine.routine.name,
                date = DateUtils.today()
            )
        )
        val sets = mutableListOf<ExerciseSetEntity>()
        routine.exercises.sortedBy { it.orderIndex }.forEach { ex ->
            for (setNum in 1..3) {
                sets += ExerciseSetEntity(
                    sessionId = sessionId,
                    exerciseName = ex.name,
                    muscleGroup = ex.muscleGroup,
                    orderIndex = ex.orderIndex,
                    setNumber = setNum,
                    weightKg = 0.0,
                    reps = 0
                )
            }
        }
        dao.insertSets(sets)
        return sessionId
    }

    override fun observeSession(sessionId: Long): Flow<SessionWithSets?> =
        dao.observeSessionWithSets(sessionId)

    override suspend fun updateSet(set: ExerciseSetEntity) = dao.updateSet(set)

    override suspend fun addSet(
        sessionId: Long,
        exerciseName: String,
        muscleGroup: String,
        orderIndex: Int
    ): Long {
        val session = dao.getSessionWithSets(sessionId) ?: error("Session not found")
        val nextSetNumber = session.sets
            .filter { it.exerciseName == exerciseName }
            .maxOfOrNull { it.setNumber }?.plus(1) ?: 1
        return dao.insertSet(
            ExerciseSetEntity(
                sessionId = sessionId,
                exerciseName = exerciseName,
                muscleGroup = muscleGroup,
                orderIndex = orderIndex,
                setNumber = nextSetNumber,
                weightKg = 0.0,
                reps = 0
            )
        )
    }

    override suspend fun deleteSet(set: ExerciseSetEntity) = dao.deleteSet(set)

    override suspend fun completeSession(session: WorkoutSessionEntity) {
        dao.updateSession(
            session.copy(isCompleted = true, completedAt = System.currentTimeMillis())
        )
    }

    override fun observeHistory(): Flow<List<SessionWithSets>> = dao.observeCompletedSessions()

    override fun observeCompletedSetRows(): Flow<List<com.solofit.app.data.local.dao.CompletedSetRow>> =
        dao.observeCompletedSetRows()

    override suspend fun getPersonalRecord(exerciseName: String): PersonalRecordEntity? =
        dao.getPersonalRecord(exerciseName)

    override suspend fun savePersonalRecord(pr: PersonalRecordEntity) =
        dao.insertPersonalRecord(pr)

    override fun observePRs(): Flow<List<com.solofit.app.data.local.dao.ExercisePR>> =
        dao.observePRs()

    override fun observeVolumeSince(sinceDate: String): Flow<List<com.solofit.app.data.local.dao.ExerciseVolume>> =
        dao.observeVolumeSince(sinceDate)
}
