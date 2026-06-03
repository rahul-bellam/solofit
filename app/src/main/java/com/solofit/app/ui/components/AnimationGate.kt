package com.solofit.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

/**
 * Pure, testable decision for whether continuous animations should run.
 * No caching, no state — just (user-preference AND screen-actually-visible).
 */
object AnimationGatePolicy {
    /**
     * @param enabled the user's "playful animations" preference.
     * @param resumed whether the hosting screen's lifecycle is currently RESUMED.
     */
    fun active(enabled: Boolean, resumed: Boolean): Boolean = enabled && resumed
}

/**
 * Returns whether continuous (infinite) animations should run *right now*.
 *
 * Optimization: looping wave animations are pointless (and waste CPU/battery) when
 * the screen isn't actually in the foreground. This reads the lifecycle state
 * directly from the OS via lifecycle-runtime-compose's [currentStateAsState] — no
 * caching, no retained flags — so loops **fully stop** the moment the screen leaves
 * RESUMED and resume on return, with zero behavioral change while visible.
 *
 * @param enabled the user's "playful animations" preference.
 */
@Composable
fun rememberAnimationsActive(enabled: Boolean): Boolean {
    val state by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    if (!enabled) return false
    return AnimationGatePolicy.active(enabled = true, resumed = state.isAtLeast(Lifecycle.State.RESUMED))
}
