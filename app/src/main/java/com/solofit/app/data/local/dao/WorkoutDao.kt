package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.solofit.app.data.local.entity.ExerciseEntity
import com.solofit.app.data.local.entity.ExerciseSetEntity
import com.solofit.app.data.local.entity.RoutineEntity
import com.solofit.app.data.local.entity.WorkoutSessionEntity
import com.solofit.app.data.local.relation.RoutineWithExercises
import com.solofit.app.data.local.relation.SessionWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ---- Routines ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Insert
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM exercises WHERE routineId = :routineId")
    suspend fun clearExercises(routineId: Long)

    @Transaction
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun observeRoutines(): Flow<List<RoutineWithExercises>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineWithExercises(id: Long): RoutineWithExercises?

    // ---- Sessions ----
    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Insert
    suspend fun insertSets(sets: List<ExerciseSetEntity>)

    @Update
    suspend fun updateSet(set: ExerciseSetEntity)

    @Insert
    suspend fun insertSet(set: ExerciseSetEntity): Long

    @Delete
    suspend fun deleteSet(set: ExerciseSetEntity)

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionWithSets(id: Long): SessionWithSets?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeSessionWithSets(id: Long): Flow<SessionWithSets?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY date DESC, completedAt DESC")
    fun observeCompletedSessions(): Flow<List<SessionWithSets>>

    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY orderIndex ASC, setNumber ASC")
    fun observeSetsForSession(sessionId: Long): Flow<List<ExerciseSetEntity>>

    /** All completed sets with the date of their session — for per-lift 1RM trends. */
    @Query(
        """
        SELECT es.exerciseName AS exerciseName, es.weightKg AS weightKg, es.reps AS reps,
               ws.date AS date
        FROM exercise_sets es
        INNER JOIN workout_sessions ws ON ws.id = es.sessionId
        WHERE es.isCompleted = 1 AND es.weightKg > 0 AND es.reps > 0 AND ws.isCompleted = 1
        ORDER BY ws.date ASC
        """
    )
    fun observeCompletedSetRows(): Flow<List<CompletedSetRow>>
}

/** A completed set flattened with its session date (for strength-progress charts). */
data class CompletedSetRow(
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val date: String
)
