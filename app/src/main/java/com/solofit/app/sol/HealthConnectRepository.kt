package com.solofit.app.sol

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class HealthConnectData(
    val sleepHours: Double? = null,
    val steps: Int? = null,
    val activeCalories: Int? = null
)

@Singleton
class HealthConnectRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var client: HealthConnectClient? = null

    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    private fun initClient(): HealthConnectClient? {
        if (client == null) {
            client = runCatching {
                HealthConnectClient.getOrCreate(context, context.packageName)
            }.getOrNull()
        }
        return client
    }

    suspend fun hasAllPermissions(): Boolean {
        val hcc = initClient() ?: return false
        val granted = hcc.permissionController.getGrantedPermissions()
        val needed = setOf(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class)
        )
        return granted.containsAll(needed)
    }

    fun getPermissionIntent() = runCatching {
        val hcc = initClient() ?: return@runCatching null
        PermissionController.createRequestPermissionResultContract()
            .createIntent(
                context,
                setOf(
                    HealthPermission.getReadPermission(SleepSessionRecord::class),
                    HealthPermission.getReadPermission(StepsRecord::class),
                    HealthPermission.getReadPermission(ExerciseSessionRecord::class)
                )
            )
    }.getOrNull()

    suspend fun readTodayData(): HealthConnectData {
        val hcc = initClient() ?: return HealthConnectData()
        val now = Instant.now()
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()

        val sleep = runCatching {
            val resp = hcc.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            resp.records.maxOfOrNull { record ->
                ChronoUnit.MINUTES.between(record.startTime, record.endTime) / 60.0
            }
        }.getOrNull()

        val steps = runCatching {
            val resp = hcc.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            resp.records.sumOf { it.count.toLong() }.toInt()
        }.getOrNull()

        return HealthConnectData(
            sleepHours = sleep,
            steps = steps
        )
    }
}
