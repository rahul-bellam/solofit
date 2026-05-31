package com.solofit.app.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimationGatePolicyTest {

    @Test
    fun `disabled is always false regardless of lifecycle`() {
        assertFalse(AnimationGatePolicy.active(enabled = false, resumed = true))
        assertFalse(AnimationGatePolicy.active(enabled = false, resumed = false))
    }

    @Test
    fun `enabled but not resumed is false (paused off-screen)`() {
        assertFalse(AnimationGatePolicy.active(enabled = true, resumed = false))
    }

    @Test
    fun `enabled and resumed is true (runs on-screen)`() {
        assertTrue(AnimationGatePolicy.active(enabled = true, resumed = true))
    }
}
