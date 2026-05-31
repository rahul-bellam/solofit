package com.solofit.app.core.cache

import android.util.LruCache

/**
 * A small, thread-safe **cache-aside** store combining two classic ideas borrowed
 * from CDNs and database buffer pools:
 *
 *  - **LRU eviction** (bounded memory — never grows unbounded), and
 *  - **TTL expiry** (entries auto-stale so we don't serve outdated data).
 *
 * This is the same pattern Varnish/Redis/HTTP caches use: check cache → on miss,
 * load from source → populate. Keeps hot data in O(1) memory with predictable RAM.
 *
 * @param maxEntries hard cap on cached items (LRU evicts the coldest).
 * @param ttlMillis how long an entry stays fresh.
 * @param clock injectable time source (testability).
 */
class LruTtlCache<K : Any, V : Any>(
    maxEntries: Int = 64,
    private val ttlMillis: Long = 10 * 60 * 1000L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private data class Entry<V>(val value: V, val expiresAt: Long)

    private val lru = LruCache<K, Entry<V>>(maxEntries)

    @Synchronized
    fun get(key: K): V? {
        val e = lru.get(key) ?: return null
        if (clock() >= e.expiresAt) {
            lru.remove(key)
            return null
        }
        return e.value
    }

    @Synchronized
    fun put(key: K, value: V) {
        lru.put(key, Entry(value, clock() + ttlMillis))
    }

    /** Cache-aside helper: return cached value or compute, store, and return it. */
    inline fun getOrPut(key: K, compute: () -> V): V =
        get(key) ?: compute().also { put(key, it) }

    @Synchronized
    fun clear() = lru.evictAll()

    @Synchronized
    fun size(): Int = lru.size()
}
