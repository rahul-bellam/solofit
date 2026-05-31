package com.solofit.app.data

import com.solofit.app.data.remote.dto.OffNutriments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Mirrors the kcal-resolution logic used in BarcodeRepositoryImpl to guard the
 * Open Food Facts field mapping (prefer kcal, else convert kJ).
 */
class OffNutrimentMappingTest {

    private fun OffNutriments.resolvedKcal(): Double? =
        energyKcal100g ?: energyKj100g?.let { it / 4.184 }

    @Test
    fun `prefers explicit kcal field`() {
        val n = OffNutriments(energyKcal100g = 250.0, energyKj100g = 9999.0)
        assertEquals(250.0, n.resolvedKcal()!!, 0.001)
    }

    @Test
    fun `falls back to kJ conversion`() {
        // 1000 kJ / 4.184 = 239.0 kcal
        val n = OffNutriments(energyKcal100g = null, energyKj100g = 1000.0)
        assertEquals(239.005, n.resolvedKcal()!!, 0.01)
    }

    @Test
    fun `null when no energy provided`() {
        assertNull(OffNutriments().resolvedKcal())
    }
}
