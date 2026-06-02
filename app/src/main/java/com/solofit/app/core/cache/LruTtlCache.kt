package com.solofit.app.core.cache

class LruTtlCache<K : Any, V : Any>(
    maxEntries: Int = 64,
    private val ttlMillis: Long = 10 * 60 * 1000L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private data class Entry<V>(val value: V, val expiresAt: Long)

    private val map = object : LinkedHashMap<K, Entry<V>>(0, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, Entry<V>>): Boolean =
            size > maxEntries
    }

    @Synchronized
    fun get(key: K): V? {
        val e = map[key] ?: return null
        if (clock() >= e.expiresAt) {
            map.remove(key)
            return null
        }
        return e.value
    }

    @Synchronized
    fun put(key: K, value: V) {
        map[key] = Entry(value, clock() + ttlMillis)
    }

    inline fun getOrPut(key: K, compute: () -> V): V =
        get(key) ?: compute().also { put(key, it) }

    @Synchronized
    fun clear() = map.clear()

    @Synchronized
    fun size(): Int = map.size
}
