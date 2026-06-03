package com.solofit.app.data.scanner

import android.content.Context
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

/**
 * Thin wrapper over Google Code Scanner (Play Services). It launches Google's own
 * fullscreen scanning UI (camera, autofocus, highlight all handled by the module),
 * so the app needs no custom camera preview. Scanning itself runs on-device; the
 * resulting string is then handed to the OFF lookup.
 *
 * Requires an Activity context — scanning uses startActivityForResult internally.
 */
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

    suspend fun scan(context: Context): ScanOutcome = suspendCancellableCoroutine { cont ->
        try {
            val scanner = GmsBarcodeScanning.getClient(context, options)
            scanner.startScan()
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
        } catch (e: Exception) {
            cont.resume(ScanOutcome.Failure(e.message ?: "Scanner unavailable."))
        }
    }
}
