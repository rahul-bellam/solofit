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
    fun `put then get returns value`() {
        val cache = LruTtlCache<String, Int>(maxEntries = 4, ttlMillis = 10_000)
        cache.put("k", 42)
        assertEquals(42, cache.get("k"))
    }

    @Test
    fun `get returns null for unknown key`() {
        val cache = LruTtlCache<String, Int>(maxEntries = 4, ttlMillis = 10_000)
        assertNull(cache.get("missing"))
    }
}
