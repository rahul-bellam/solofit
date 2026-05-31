package com.solofit.app.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies SoloFitDatabase migrations, focusing on the v6 -> v7 serving-size
 * backfill: seeded foods get servingGrams/servingLabel; custom rows are untouched.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SoloFitDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate6To7_backfillsSeededServings_andLeavesCustomUntouched() {
        // Create a v6 database and seed two food rows WITHOUT serving columns.
        helper.createDatabase(dbName, 6).use { db ->
            db.execSQL(
                "INSERT INTO food_items (name, category, caloriesPer100g, proteinPer100g, " +
                    "carbsPer100g, fatsPer100g, isCustom, barcode) VALUES " +
                    "('Whole Egg', 'Protein', 143.0, 13.0, 1.1, 9.5, 0, NULL)"
            )
            db.execSQL(
                "INSERT INTO food_items (name, category, caloriesPer100g, proteinPer100g, " +
                    "carbsPer100g, fatsPer100g, isCustom, barcode) VALUES " +
                    "('My Custom Shake', 'Custom', 200.0, 30.0, 5.0, 4.0, 1, NULL)"
            )
        }

        // Run the real migration up to v7.
        helper.runMigrationsAndValidate(
            dbName, 7, true, SoloFitDatabase.MIGRATION_6_7
        ).use { db ->
            db.query(
                "SELECT servingGrams, servingLabel FROM food_items WHERE name = 'Whole Egg'"
            ).use { c ->
                assertTrue(c.moveToFirst())
                assertEquals(50.0, c.getDouble(0), 0.001)
                assertEquals("egg", c.getString(1))
            }
            db.query(
                "SELECT servingGrams, servingLabel FROM food_items WHERE name = 'My Custom Shake'"
            ).use { c ->
                assertTrue(c.moveToFirst())
                assertTrue("custom row should keep NULL serving", c.isNull(0))
                assertNull(c.getString(1))
            }
        }
    }

    @Test
    fun migrateAll_1_to_latest_succeeds() {
        helper.createDatabase(dbName, 1).close()
        // Validates the entire chain up to the current schema (catches drift).
        helper.runMigrationsAndValidate(
            dbName,
            8,
            true,
            SoloFitDatabase.MIGRATION_1_2,
            SoloFitDatabase.MIGRATION_2_3,
            SoloFitDatabase.MIGRATION_3_4,
            SoloFitDatabase.MIGRATION_4_5,
            SoloFitDatabase.MIGRATION_5_6,
            SoloFitDatabase.MIGRATION_6_7,
            SoloFitDatabase.MIGRATION_7_8
        ).close()
    }
}
