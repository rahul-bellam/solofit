package com.solofit.app.data.scanner

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Outcome of an interactive barcode scan. */
sealed interface ScanOutcome {
    data class Success(val rawValue: String) : ScanOutcome
    data object Cancelled : ScanOutcome
    data class Failure(val message: String) : ScanOutcome
}

@Singleton
class BarcodeScanner @Inject constructor() {

    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128
        )
        .enableAutoZoom()
        .build()

    fun isAvailable(context: Context): String? {
        val result = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context)
        return when (result) {
            ConnectionResult.SUCCESS -> null
            ConnectionResult.SERVICE_MISSING ->
                "Google Play Services is not installed on this device."
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ->
                "Google Play Services needs to be updated."
            ConnectionResult.SERVICE_DISABLED ->
                "Google Play Services is disabled."
            ConnectionResult.SERVICE_INVALID ->
                "Google Play Services is not functioning correctly."
            else -> "Scanner unavailable (code $result)."
        }
    }

    suspend fun scan(context: Context): ScanOutcome = suspendCancellableCoroutine { cont ->
        try {
            val scanner = GmsBarcodeScanning.getClient(context, options)
            var resumed = false
            fun once(result: ScanOutcome) {
                if (!resumed) { resumed = true; cont.resume(result) }
            }
            cont.invokeOnCancellation { once(ScanOutcome.Cancelled) }
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val value = barcode.rawValue
                    if (value.isNullOrBlank()) once(ScanOutcome.Failure("Could not read barcode."))
                    else once(ScanOutcome.Success(value))
                }
                .addOnCanceledListener { once(ScanOutcome.Cancelled) }
                .addOnFailureListener { e ->
                    val msg = e.message ?: ""
                    val friendly = when {
                        msg.contains("module", ignoreCase = true) &&
                            msg.contains("download", ignoreCase = true) ->
                            "Barcode scanner module could not be downloaded. Check your internet and try again."
                        msg.contains("network", ignoreCase = true) ||
                            msg.contains("timeout", ignoreCase = true) ->
                            "Network error. Check your connection and try again."
                        msg.contains("permission", ignoreCase = true) ||
                            msg.contains("camera", ignoreCase = true) ->
                            "Camera permission is required for scanning."
                        else -> "Scanner unavailable. Tap Retry to try again."
                    }
                    once(ScanOutcome.Failure(friendly))
                }
        } catch (e: Exception) {
            if (!cont.isCancelled) cont.resume(
                ScanOutcome.Failure("Scanner unavailable. Tap Retry to try again.")
            )
        }
    }
}
