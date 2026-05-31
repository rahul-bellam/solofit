package com.solofit.app.domain.repository

import com.solofit.app.domain.model.BarcodeLookupResult
import com.solofit.app.domain.model.ScannedFood

interface BarcodeRepository {
    /**
     * Resolve a barcode to a food. Strategy:
     *  1) check the local Room cache (instant, offline);
     *  2) otherwise query Open Food Facts;
     *  3) if not found / missing macros -> NotFound (triggers manual form).
     */
    suspend fun lookup(barcode: String): BarcodeLookupResult

    /** Persist a scanned/manually-entered product into the local food DB. */
    suspend fun saveScannedFood(food: ScannedFood, category: String = "Scanned"): Long
}
