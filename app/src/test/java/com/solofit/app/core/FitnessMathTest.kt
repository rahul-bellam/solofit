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
        // only sleep 4h -> value 0.5, weight .40 -> weighted .20 / totalWeight .40 = 0.5 -> 50
        val s = FitnessMath.recoveryScore(4.0, null, null, null, 3000, null)
        assertEquals(50, s)
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

class FitnessMathProgressionTest {
    @Test
    fun `all sets hit top with low rir -> increase`() {
        val p = FitnessMath.progression(
            repsPerSet = listOf(12, 12, 12),
            rirPerSet = listOf(1, 1, 0),
            topOfRange = 12
        )
        assertEquals(FitnessMath.Progression.INCREASE, p)
    }

    @Test
    fun `below range -> hold`() {
        val p = FitnessMath.progression(
            repsPerSet = listOf(8, 9, 8),
            rirPerSet = listOf(2, 2, 2),
            topOfRange = 12
        )
        assertEquals(FitnessMath.Progression.HOLD, p)
    }

    @Test
    fun `struggled badly with zero rir -> deload`() {
        val p = FitnessMath.progression(
            repsPerSet = listOf(5, 4),
            rirPerSet = listOf(0, 0),
            topOfRange = 12
        )
        assertEquals(FitnessMath.Progression.DELOAD, p)
    }

    @Test
    fun `empty -> none`() {
        assertEquals(FitnessMath.Progression.NONE, FitnessMath.progression(emptyList(), emptyList(), 12))
    }

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
