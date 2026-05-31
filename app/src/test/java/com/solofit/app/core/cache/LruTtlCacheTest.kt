package com.solofit.app.core.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LruTtlCacheTest {

    @Test
    fun `returns stored value before ttl`() {
        var now = 0L
        val cache = LruTtlCache<String, Int>(maxEntries = 4, ttlMillis = 100, clock = { now })
        cache.put("a", 1)
        now = 50
        assertEquals(1, cache.get("a"))
    }

    @Test
    fun `expires value after ttl`() {
        var now = 0L
        val cache = LruTtlCache<String, Int>(maxEntries = 4, ttlMillis = 100, clock = { now })
        cache.put("a", 1)
        now = 150
        assertNull(cache.get("a"))
    }

    @Test
    fun `lru evicts coldest when over capacity`() {
        val cache = LruTtlCache<String, Int>(maxEntries = 2, ttlMillis = 10_000)
        cache.put("a", 1)
        cache.put("b", 2)
        cache.get("a")          // touch a -> b is now coldest
        cache.put("c", 3)       // exceeds cap -> evicts b
        assertEquals(1, cache.get("a"))
        assertNull(cache.get("b"))
        assertEquals(3, cache.get("c"))
    }

    @Test
    fun `getOrPut computes once then caches`() {
        var computeCount = 0
        val cache = LruTtlCache<String, Int>(maxEntries = 4, ttlMillis = 10_000)
        val v1 = cache.getOrPut("k") { computeCount++; 42 }
        val v2 = cache.getOrPut("k") { computeCount++; 99 }
        assertEquals(42, v1)
        assertEquals(42, v2)        // served from cache, not recomputed
        assertEquals(1, computeCount)
    }
}
