package com.solofit.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import android.database.sqlite.SQLiteException
import androidx.sqlite.db.SupportSQLiteDatabase
import com.solofit.app.data.local.dao.DailyLogDao
import com.solofit.app.data.local.dao.FoodDao
import com.solofit.app.data.local.dao.FriendDao
import com.solofit.app.data.local.dao.FriendEventDao
import com.solofit.app.data.local.dao.SoloIdentityDao
import com.solofit.app.data.local.dao.UserProfileDao
import com.solofit.app.data.local.dao.JournalDao
import com.solofit.app.data.local.dao.BodyMeasurementDao
import com.solofit.app.data.local.dao.DailyMetricDao
import com.solofit.app.data.local.dao.ProgressPhotoDao
import com.solofit.app.data.local.dao.WeeklyPlanDao
import com.solofit.app.data.local.dao.WeightDao
import com.solofit.app.data.local.dao.WorkoutDao
import com.solofit.app.data.local.dao.FrequentMealDao
import com.solofit.app.data.local.entity.FrequentMealEntity
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.data.local.entity.ExerciseEntity
import com.solofit.app.data.local.entity.ExerciseSetEntity
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.data.local.entity.FriendEntity
import com.solofit.app.data.local.entity.FriendEventEntity
import com.solofit.app.data.local.entity.FriendGroupEntity
import com.solofit.app.data.local.entity.FriendPermissionEntity
import com.solofit.app.data.local.entity.GroupMemberEntity
import com.solofit.app.data.local.entity.PersonalRecordEntity
import com.solofit.app.data.local.entity.RoutineEntity
import com.solofit.app.data.local.entity.SoloIdentityEntity
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.data.local.entity.GoalItemEntity
import com.solofit.app.data.local.entity.GratitudeEntryEntity
import com.solofit.app.data.local.entity.BodyMeasurementEntity
import com.solofit.app.data.local.entity.DailyMetricEntity
import com.solofit.app.data.local.entity.PlannedExerciseEntity
import com.solofit.app.data.local.entity.ProgressPhotoEntity
import com.solofit.app.data.local.entity.WeeklyPlanEntity
import com.solofit.app.data.local.entity.WeightEntryEntity
import com.solofit.app.data.local.entity.WorkoutSessionEntity
import com.solofit.app.data.local.seed.FoodSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        FoodItemEntity::class,
        DailyLogEntity::class,
        RoutineEntity::class,
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        ExerciseSetEntity::class,
        WeightEntryEntity::class,
        GoalItemEntity::class,
        GratitudeEntryEntity::class,
        BodyMeasurementEntity::class,
        DailyMetricEntity::class,
        ProgressPhotoEntity::class,
        WeeklyPlanEntity::class,
        PlannedExerciseEntity::class,
        PersonalRecordEntity::class,
        FrequentMealEntity::class,
        SoloIdentityEntity::class,
        FriendEntity::class,
        FriendPermissionEntity::class,
        FriendGroupEntity::class,
        GroupMemberEntity::class,
        FriendEventEntity::class
    ],
    version = 15,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SoloFitDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodDao(): FoodDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun weightDao(): WeightDao
    abstract fun journalDao(): JournalDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun dailyMetricDao(): DailyMetricDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun weeklyPlanDao(): WeeklyPlanDao
    abstract fun frequentMealDao(): FrequentMealDao
    abstract fun soloIdentityDao(): SoloIdentityDao
    abstract fun friendDao(): FriendDao
    abstract fun friendEventDao(): FriendEventDao

    companion object {
        const val DB_NAME = "solofit.db"

        /** v1 -> v2: add nullable `barcode` column + unique index for scan caching. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_items ADD COLUMN barcode TEXT")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_food_items_barcode " +
                        "ON food_items(barcode)"
                )
            }
        }

        /** v2 -> v3: add weight_entries table for body-weight monitoring. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS weight_entries (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        date TEXT NOT NULL,
                        weightKg REAL NOT NULL,
                        loggedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_weight_entries_date " +
                        "ON weight_entries(date)"
                )
            }
        }

        /** v3 -> v4: add journal tables (morning goals + evening gratitude). */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS goal_items (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        date TEXT NOT NULL,
                        text TEXT NOT NULL,
                        done INTEGER NOT NULL DEFAULT 0,
                        orderIndex INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goal_items_date ON goal_items(date)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS gratitude_entries (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        date TEXT NOT NULL,
                        text TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_gratitude_entries_date " +
                        "ON gratitude_entries(date)"
                )
            }
        }

        /** v4 -> v5: add body_measurements + daily_metrics (transformation dashboard). */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS body_measurements (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        date TEXT NOT NULL,
                        waistCm REAL, chestCm REAL, shouldersCm REAL,
                        armsCm REAL, thighsCm REAL, neckCm REAL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_body_measurements_date ON body_measurements(date)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_metrics (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        date TEXT NOT NULL,
                        sleepHours REAL, steps INTEGER,
                        moodScore INTEGER, energyScore INTEGER,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_metrics_date ON daily_metrics(date)")
            }
        }

        /** v5 -> v6: add progress_photos (file names only; images stay in private storage). */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS progress_photos (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        date TEXT NOT NULL,
                        pose TEXT NOT NULL,
                        fileName TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_progress_photos_date ON progress_photos(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_progress_photos_pose ON progress_photos(pose)")
            }
        }

        /**
         * v6 -> v7: add serving-size columns for count-based manual entry, and
         * backfill sensible per-unit servings for the seeded foods so existing
         * installs (not just fresh ones) can log by count. Only fills rows where
         * servingGrams IS NULL, so user-edited/custom foods are never overwritten.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_items ADD COLUMN servingGrams REAL")
                db.execSQL("ALTER TABLE food_items ADD COLUMN servingLabel TEXT")
                db.execSQL("UPDATE food_items SET servingGrams = 120.0, servingLabel = 'breast' WHERE name = 'Chicken Breast (cooked)' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 90.0, servingLabel = 'thigh' WHERE name = 'Chicken Thigh (cooked)' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 8.0, servingLabel = 'slice' WHERE name = 'Bacon (cooked)' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 25.0, servingLabel = 'sardine' WHERE name = 'Sardines (canned)' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 50.0, servingLabel = 'egg' WHERE name = 'Whole Egg' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 33.0, servingLabel = 'egg white' WHERE name = 'Egg White' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 30.0, servingLabel = 'scoop' WHERE name = 'Whey Protein Powder' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 30.0, servingLabel = 'scoop' WHERE name = 'Casein Protein Powder' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 30.0, servingLabel = 'scoop' WHERE name = 'Plant Protein Powder' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 30.0, servingLabel = 'slice' WHERE name = 'Whole Wheat Bread' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 30.0, servingLabel = 'slice' WHERE name = 'White Bread' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 85.0, servingLabel = 'bagel' WHERE name = 'Bagel (plain)' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 50.0, servingLabel = 'tortilla' WHERE name = 'Tortilla (flour)' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 50.0, servingLabel = 'tortilla' WHERE name = 'Corn Tortilla' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 9.0, servingLabel = 'cake' WHERE name = 'Rice Cakes' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 170.0, servingLabel = 'potato' WHERE name = 'Potato (boiled)' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 170.0, servingLabel = 'potato' WHERE name = 'Sweet Potato (baked)' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 118.0, servingLabel = 'banana' WHERE name = 'Banana' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 182.0, servingLabel = 'apple' WHERE name = 'Apple' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 131.0, servingLabel = 'orange' WHERE name = 'Orange' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 200.0, servingLabel = 'mango' WHERE name = 'Mango' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 150.0, servingLabel = 'avocado' WHERE name = 'Avocado' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 69.0, servingLabel = 'kiwi' WHERE name = 'Kiwi' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 178.0, servingLabel = 'pear' WHERE name = 'Pear' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 7.0, servingLabel = 'date' WHERE name = 'Dates' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 1.2, servingLabel = 'almond' WHERE name = 'Almonds' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 8.0, servingLabel = 'half' WHERE name = 'Walnuts' AND servingGrams IS NULL")
                db.execSQL("UPDATE food_items SET servingGrams = 1.6, servingLabel = 'cashew' WHERE name = 'Cashews' AND servingGrams IS NULL")
            }
        }

        /** v7 -> v8: add RIR (reps in reserve) to exercise sets. */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercise_sets ADD COLUMN rir INTEGER")
            }
        }

        /** v8 -> v9: add fiberPer100g column to food_items. */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_items ADD COLUMN fiberPer100g REAL NOT NULL DEFAULT 0.0")
            }
        }

        /** v9 -> v10: weekly plan tables. */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weekly_plans (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        dayOfWeek INTEGER NOT NULL,
                        name TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS planned_exercises (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        planId INTEGER NOT NULL,
                        exerciseName TEXT NOT NULL,
                        sets INTEGER NOT NULL DEFAULT 3,
                        reps INTEGER NOT NULL DEFAULT 10,
                        weight REAL NOT NULL DEFAULT 0.0,
                        weightUnit TEXT NOT NULL DEFAULT 'kg',
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (planId) REFERENCES weekly_plans(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_planned_exercises_planId ON planned_exercises(planId)")
            }
        }

        /** v10 -> v11: add personal_records table + warm-up/notes/superset columns on exercise_sets. */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS personal_records (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        exerciseName TEXT NOT NULL,
                        bestWeightKg REAL NOT NULL,
                        bestReps INTEGER NOT NULL,
                        estimated1RM REAL NOT NULL,
                        date TEXT NOT NULL,
                        sessionId INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_records_exerciseName ON personal_records(exerciseName)")
                try {
                    db.execSQL("ALTER TABLE exercise_sets ADD COLUMN isWarmUp INTEGER NOT NULL DEFAULT 0")
                } catch (e: SQLiteException) {
                    if ("duplicate column" !in (e.message ?: "")) throw e
                }
                try {
                    db.execSQL("ALTER TABLE exercise_sets ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                } catch (e: SQLiteException) {
                    if ("duplicate column" !in (e.message ?: "")) throw e
                }
                try {
                    db.execSQL("ALTER TABLE exercise_sets ADD COLUMN supersetId INTEGER")
                } catch (e: SQLiteException) {
                    if ("duplicate column" !in (e.message ?: "")) throw e
                }
            }
        }

        /** v11 -> v12: add frequent_meals table for meal memory. */
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS frequent_meals (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        normalizedName TEXT NOT NULL,
                        caloriesPer100g REAL NOT NULL,
                        proteinPer100g REAL NOT NULL,
                        carbsPer100g REAL NOT NULL,
                        fatsPer100g REAL NOT NULL,
                        fiberPer100g REAL NOT NULL DEFAULT 0.0,
                        logCount INTEGER NOT NULL DEFAULT 1,
                        lastLoggedAt INTEGER NOT NULL,
                        confidence TEXT NOT NULL DEFAULT 'MEDIUM'
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_frequent_meals_normalizedName
                    ON frequent_meals(normalizedName)
                """.trimIndent())
            }
        }

        /** v12 -> v13: add missing database indexes for query performance. */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_workout_sessions_isCompleted ON workout_sessions(isCompleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_exercise_sets_isCompleted ON exercise_sets(isCompleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_exercise_sets_exerciseName ON exercise_sets(exerciseName)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_personal_records_exerciseName ON personal_records(exerciseName)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_weekly_plans_dayOfWeek ON weekly_plans(dayOfWeek)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_planned_exercises_isCompleted ON planned_exercises(isCompleted)")
            }
        }

        /** v14 -> v15: add relationshipType to friends. */
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE friends ADD COLUMN relationshipType TEXT NOT NULL DEFAULT 'accountability_partner'")
            }
        }

        /** v13 -> v14: friend system tables. */
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS solo_identity (
                        id INTEGER NOT NULL PRIMARY KEY,
                        soloId TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        publicKey BLOB NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS friends (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        soloId TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        publicKey BLOB NOT NULL,
                        status TEXT NOT NULL DEFAULT 'pending',
                        addedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_friends_soloId ON friends(soloId)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS friend_permissions (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        friendId INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        level TEXT NOT NULL DEFAULT 'private',
                        FOREIGN KEY (friendId) REFERENCES friends(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_friend_permissions_friend_category ON friend_permissions(friendId, category)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS friend_groups (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS group_members (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        groupId INTEGER NOT NULL,
                        friendId INTEGER NOT NULL,
                        joinedAt INTEGER NOT NULL,
                        FOREIGN KEY (groupId) REFERENCES friend_groups(id) ON DELETE CASCADE,
                        FOREIGN KEY (friendId) REFERENCES friends(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_group_members_group_friend ON group_members(groupId, friendId)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS friend_events (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        friendId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        payload TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY (friendId) REFERENCES friends(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_friend_events_friendId ON friend_events(friendId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_friend_events_friend_created ON friend_events(friendId, createdAt)")
            }
        }

        fun build(context: Context, scope: CoroutineScope): SoloFitDatabase {
            val db = Room.databaseBuilder(
                context.applicationContext,
                SoloFitDatabase::class.java,
                DB_NAME
            )
                .addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                    MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12,
                    MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15
                )

                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        scope.launch(Dispatchers.IO) {
                            runCatching { dbHelper?.foodDao()?.insertAll(FoodSeedData.items) }
                        }
                    }
                })
                .build()
            // Expose the built instance for the callback to use.
            dbHelper = db
            return db
        }

        /** Holder so Callback.onCreate can access DAOs without a circular build(). */
        private var dbHelper: SoloFitDatabase? = null
    }
}
