package com.solofit.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FitnessMathTest {

    @Test
    fun `epley 1RM matches formula`() {
        // 100kg x 5 reps -> 100 * (1 + 5/30) = 116.67
        assertEquals(116.67, FitnessMath.epley1RM(100.0, 5), 0.01)
        // single rep returns the weight
        assertEquals(80.0, FitnessMath.epley1RM(80.0, 1), 1e-9)
        // invalid -> 0
        assertEquals(0.0, FitnessMath.epley1RM(0.0, 5), 1e-9)
        assertEquals(0.0, FitnessMath.epley1RM(100.0, 0), 1e-9)
    }

    @Test
    fun `subjective readiness blends check-in inputs to 0-100`() {
        // Perfect day: 8h sleep, lowest stress (level 1), max mood, max energy -> 100.
        assertEquals(100, FitnessMath.subjectiveReadinessScore(8f, 1, 5, 5))
        // Worst day: no sleep, max stress (5), min mood/energy. mood(1/5*20=4) and
        // energy(1/5*30=6) can't hit zero at level 1, so the floor is 10.
        assertEquals(10, FitnessMath.subjectiveReadinessScore(0f, 5, 1, 1))
        // Sleep is capped at the 8h target (oversleeping doesn't overflow the band).
        assertEquals(
            FitnessMath.subjectiveReadinessScore(8f, 3, 3, 3),
            FitnessMath.subjectiveReadinessScore(12f, 3, 3, 3)
        )
    }

    @Test
    fun `vtaper ratio divides shoulders by waist`() {
        assertEquals(1.6, FitnessMath.vTaperRatio(128.0, 80.0)!!, 1e-9)
        assertNull(FitnessMath.vTaperRatio(null, 80.0))
        assertNull(FitnessMath.vTaperRatio(120.0, 0.0))
    }

    @Test
    fun `vtaper labels bucket correctly`() {
        assertEquals("Elite V-taper", FitnessMath.vTaperLabel(1.62))
        assertEquals("Strong V-taper", FitnessMath.vTaperLabel(1.5))
        assertEquals("Developing", FitnessMath.vTaperLabel(1.35))
        assertEquals("Building base", FitnessMath.vTaperLabel(1.1))
        assertEquals("—", FitnessMath.vTaperLabel(null))
    }

    @Test
    fun `recovery score full inputs`() {
        // sleep 8h(1.0*.40) steps 8000(1.0*.20) workout true(.15) water 3000/3000(.15) energy 10(.10)
        // = 100
        val s = FitnessMath.recoveryScore(8.0, 8000, true, 3000, 3000, 10)
        assertEquals(100, s)
    }

    @Test
    fun `recovery score partial inputs normalize by available weights`() {
        // only sleep 4h -> value 4/7 ≈ 0.571, weight .40 -> score = 57
        val s = FitnessMath.recoveryScore(4.0, null, null, null, 3000, null)
        assertEquals(57, s)
    }

    @Test
    fun `recovery null when no inputs`() {
        assertNull(FitnessMath.recoveryScore(null, null, null, null, 3000, null))
    }

    @Test
    fun `readiness labels`() {
        assertEquals("High", FitnessMath.readinessLabel(85))
        assertEquals("Moderate", FitnessMath.readinessLabel(65))
        assertEquals("Low", FitnessMath.readinessLabel(45))
        assertEquals("Rest day", FitnessMath.readinessLabel(20))
    }
}

/**
 * NOTE: the RIR-based `FitnessMath.progression` API previously covered here was
 * removed from the app, so those tests were dropped; the transformation-score
 * coverage below remains valid.
 */
class FitnessMathTransformationTest {
    @Test
    fun `transformation score weights and normalizes`() {
        // all components 1.0 -> 100 regardless of weights
        val s = FitnessMath.transformationScore(1.0, 1.0, 1.0, 1.0, 0.5, 0.3, 0.15, 0.05)
        assertEquals(100, s)
        // only strength 1.0, others 0, with strength weight dominating
        val s2 = FitnessMath.transformationScore(1.0, 0.0, 0.0, 0.0, 0.5, 0.15, 0.25, 0.10)
        // = (0.5*1) / (1.0) * 100 = 50
        assertEquals(50, s2)
    }
}
