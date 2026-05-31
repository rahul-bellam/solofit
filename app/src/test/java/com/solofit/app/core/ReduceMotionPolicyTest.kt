package com.solofit.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReduceMotionPolicyTest {

    @Test
    fun `disables when OS reduce-motion on and not yet applied`() {
        assertTrue(
            ReduceMotionPolicy.shouldDisableAnimations(osAnimatorScale = 0f, alreadyApplied = false)
        )
    }

    @Test
    fun `does nothing when OS animations are on`() {
        assertFalse(
            ReduceMotionPolicy.shouldDisableAnimations(osAnimatorScale = 1f, alreadyApplied = false)
        )
        assertFalse(
            ReduceMotionPolicy.shouldDisableAnimations(osAnimatorScale = 0.5f, alreadyApplied = false)
        )
    }

    @Test
    fun `never re-applies once already applied (respects user choice)`() {
        // Even with OS reduce-motion on, if we've applied before we must not override.
        assertFalse(
            ReduceMotionPolicy.shouldDisableAnimations(osAnimatorScale = 0f, alreadyApplied = true)
        )
    }
}
