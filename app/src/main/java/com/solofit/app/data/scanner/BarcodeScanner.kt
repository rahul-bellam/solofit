package com.solofit.app.data.scanner

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.qualifiers.ApplicationContext
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

/**
 * Thin wrapper over Google Code Scanner (Play Services). It launches Google's own
 * fullscreen scanning UI (camera, autofocus, highlight all handled by the module),
 * so the app needs no custom camera preview. Scanning itself runs on-device; the
 * resulting string is then handed to the OFF lookup.
 */
@Singleton
class BarcodeScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scanner by lazy {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128
            )
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }

    suspend fun scan(): ScanOutcome = suspendCancellableCoroutine { cont ->
        val task: Task<Barcode> = scanner.startScan()
        task
            .addOnSuccessListener { barcode ->
                val value = barcode.rawValue
                if (value.isNullOrBlank()) {
                    cont.resume(ScanOutcome.Failure("Could not read barcode."))
                } else {
                    cont.resume(ScanOutcome.Success(value))
                }
            }
            .addOnCanceledListener { cont.resume(ScanOutcome.Cancelled) }
            .addOnFailureListener { e ->
                cont.resume(ScanOutcome.Failure(e.message ?: "Scanner error."))
            }
    }
}
