package com.solofit.app.core.perf

import android.os.SystemClock

import com.solofit.app.BuildConfig
import java.util.concurrent.ConcurrentHashMap

/**
 * Tiny latency-observability harness inspired by SRE / HFT practice: you can't
 * optimize what you don't measure (Theory of Constraints — find the real
 * bottleneck before tuning).
 *
 * Records per-operation timings and exposes rolling p50/p95/max. Effectively free
 * in release builds (gated on BuildConfig.DEBUG) so it never adds production cost.
 *
 * Usage:
 *   val r = PerfTrace.measure("barcode.lookup") { repo.lookup(code) }
 *   suspend variant: PerfTrace.measureSuspend("db.search") { dao.search(q) }
 */
object PerfTrace {
    private const val TAG = "SoloPerf"
    private const val WINDOW = 50 // keep last N samples per op

    private data class Stat(val samples: ArrayDeque<Long> = ArrayDeque())

    private val stats = ConcurrentHashMap<String, Stat>()

    suspend inline fun <T> measureSuspend(label: String, block: suspend () -> T): T {
        if (!BuildConfig.DEBUG) return block()
        val start = SystemClock.elapsedRealtimeNanos()
        try {
            return block()
        } finally {
            record(label, (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000)
        }
    }

    fun record(label: String, millis: Long) {
        if (!BuildConfig.DEBUG) return
        val stat = stats.getOrPut(label) { Stat() }
        synchronized(stat) {
            stat.samples.addLast(millis)
            while (stat.samples.size > WINDOW) stat.samples.removeFirst()
        }
    }

    /** Returns p50/p95/max (ms) for a label, or null if no samples yet. */
    fun summary(label: String): Triple<Long, Long, Long>? {
        val stat = stats[label] ?: return null
        val sorted = synchronized(stat) { stat.samples.sorted() }
        if (sorted.isEmpty()) return null
        fun pct(p: Double) = sorted[((sorted.size - 1) * p).toInt()]
        return Triple(pct(0.50), pct(0.95), sorted.last())
    }


}
