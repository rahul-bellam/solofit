package com.solofit.app.core

/**
 * Pure, testable decision for the one-time OS reduce-motion auto-detect.
 * Kept Android-free so it can be unit-tested directly.
 */
object ReduceMotionPolicy {

    /**
     * Decide whether to auto-disable playful animations on first launch.
     *
     * @param osAnimatorScale value of Settings.Global.ANIMATOR_DURATION_SCALE
     *        (0f means the user turned animations off system-wide).
     * @param alreadyApplied whether we've already run this one-time check.
     * @return true only when we should flip the in-app animations switch OFF.
     */
    fun shouldDisableAnimations(osAnimatorScale: Float, alreadyApplied: Boolean): Boolean =
        !alreadyApplied && osAnimatorScale == 0f
}
