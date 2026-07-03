package com.solofit.app.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solofit.app.BuildConfig
import com.solofit.app.core.perf.PerfTrace
import com.solofit.app.data.local.SoloFitDatabase
import com.solofit.app.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic database hygiene — a server-ops principle applied locally: over time
 * SQLite accumulates free pages (from deletes/updates) and a growing WAL file.
 *
 *  - `wal_checkpoint(TRUNCATE)` folds the write-ahead log back into the main DB
 *    and shrinks the -wal file.
 *  - `VACUUM` rebuilds the DB file, reclaiming freed pages → smaller on-disk
 *    footprint and tighter, faster index scans.
 *
 * Runs rarely (scheduled ~weekly with battery/idle constraints) so it never
 * competes with foreground work.
 */
@HiltWorker
class DbMaintenanceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val database: SoloFitDatabase,
    private val userPreferences: UserPreferences
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = PerfTrace.measureSuspend("db.maintenance") {
        try {
            withContext(Dispatchers.IO) {
                val db = database.openHelper.writableDatabase
                db.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
                db.execSQL("VACUUM")
            }
            userPreferences.pruneOldData()
            Result.success()
        } catch (e: Exception) {
            // Maintenance is best-effort; never fail the chain, but surface the
            // cause in debug so silent VACUUM/prune failures are diagnosable.
            if (BuildConfig.DEBUG) Log.w("DbMaintenanceWorker", "maintenance failed", e)
            Result.success()
        }
    }
}
