package com.solofit.app.sol

import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Locale

class SetbackPredictorTest {

    private val originalLocale: Locale = Locale.getDefault()

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `weights round-trip through serialization`() {
        val weights = SetbackPredictor.defaultWeights()
        val restored = SetbackPredictor.deserializeWeights(SetbackPredictor.serializeWeights(weights))
        assertNotNull(restored)
        assertArrayEquals(weights, restored!!, 1e-9)
    }

    @Test
    fun `serialization is locale-independent (comma-decimal locales)`() {
        // Regression: German/French etc. use ',' as the decimal separator. A
        // locale-sensitive format would emit "0,35" and corrupt the comma-joined
        // CSV, making deserialization return null and silently reset the model.
        Locale.setDefault(Locale.GERMANY)
        val weights = doubleArrayOf(0.35, -0.25, 1.5, 0.0, 0.125)
        val serialized = SetbackPredictor.serializeWeights(weights)
        val restored = SetbackPredictor.deserializeWeights(serialized)
        assertNotNull("comma-decimal locale must not corrupt weights", restored)
        assertArrayEquals(weights, restored!!, 1e-9)
    }

    @Test
    fun `sigmoid output stays within probability bounds`() {
        val weights = SetbackPredictor.defaultWeights()
        val features = SetbackPredictor.extractFeatures(
            daysSinceLastWorkout = 14,
            avgRecovery7d = 20,
            avgSleep3d = 4.0,
            avgProteinAdherence7d = 0.3,
            avgSteps3d = 1000,
            meditatedAny7d = false,
            journaledAny7d = false,
            isWeekend = true,
            momentumDirection = 0
        )
        val p = SetbackPredictor.predict(weights, 0.0, features)
        assertEquals("probability in [0,1]", true, p in 0.0..1.0)
    }
}
