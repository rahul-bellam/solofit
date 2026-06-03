package com.solofit.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.solofit.app.data.local.dao.DailyLogDao
import com.solofit.app.data.local.dao.FoodDao
import com.solofit.app.data.local.dao.UserProfileDao
import com.solofit.app.data.local.dao.JournalDao
import com.solofit.app.data.local.dao.BodyMeasurementDao
import com.solofit.app.data.local.dao.DailyMetricDao
import com.solofit.app.data.local.dao.ProgressPhotoDao
import com.solofit.app.data.local.dao.WeeklyPlanDao
import com.solofit.app.data.local.dao.WeightDao
import com.solofit.app.data.local.dao.WorkoutDao
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.data.local.entity.ExerciseEntity
import com.solofit.app.data.local.entity.ExerciseSetEntity
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.data.local.entity.PersonalRecordEntity
import com.solofit.app.data.local.entity.RoutineEntity
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
        PersonalRecordEntity::class
    ],
    version = 11,
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
                try { db.execSQL(
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
                ) } catch (_: Exception) {}
                try { db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_records_exerciseName ON personal_records(exerciseName)") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE exercise_sets ADD COLUMN isWarmUp INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE exercise_sets ADD COLUMN notes TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE exercise_sets ADD COLUMN supersetId INTEGER") } catch (_: Exception) {}
            }
        }

        fun build(context: Context, scope: CoroutineScope): SoloFitDatabase {
            var needsSeed = false
            val db = Room.databaseBuilder(
                context.applicationContext,
                SoloFitDatabase::class.java,
                DB_NAME
            )
                .addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                    MIGRATION_9_10, MIGRATION_10_11
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        needsSeed = true
                    }
                })
                .build()
            if (needsSeed) {
                scope.launch(Dispatchers.IO) {
                    db.foodDao().insertAll(FoodSeedData.items)
                }
            }
            return db
        }
    }
}
