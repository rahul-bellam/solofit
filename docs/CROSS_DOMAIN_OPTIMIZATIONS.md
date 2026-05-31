# SoloFit v1.4 — Cross-Domain Performance Optimizations

Performance engineering is largely about **borrowing battle-tested principles from
other industries**. This release applies five, each mapped to its origin.

| Principle | Origin industry | Implementation | Effect |
|---|---|---|---|
| **Tiered cache-aside (L1/L2/L3)** | CPUs, CDNs, DB buffer pools | `BarcodeRepositoryImpl` | Repeat scans resolve in memory; offline scans hit Room; network only on true miss |
| **LRU + TTL eviction** | Varnish / Redis / HTTP caches | `core/cache/LruTtlCache` | Bounded RAM, auto-stale data |
| **Debounce / signal conditioning** | Telecom, control systems | `NutritionViewModel` search | ~1 query after typing settles vs one-per-keystroke |
| **Backpressure / conflation** | Reactive Streams, Kafka | `flatMapLatest` + `conflate` + `distinctUntilChanged` | Cancels stale work, keeps only freshest result |
| **Read-ahead / prefetch** | OS paging, storage controllers | `FoodRepository.warmUp()` on app start | First nutrition search hits warm SQLite pages |
| **Latency observability (p50/p95)** | SRE, HFT | `core/perf/PerfTrace` | Measure before optimizing (Theory of Constraints) |

---

## 1. Tiered cache-aside (CPU/CDN principle)

`BarcodeRepositoryImpl.lookup()` now resolves through three tiers, fastest first:

```
L1  in-memory LRU+TTL   (~ns)   survives the session
L2  Room (barcode index)(~µs)   survives launches, fully offline
L3  Open Food Facts     (~100ms+) only on a complete miss
```

This is the same hierarchy a CPU uses (L1/L2/L3 → RAM) and the web uses
(browser cache → CDN → origin). Each tier hit avoids every slower tier.

## 2. LRU + TTL cache (`LruTtlCache`)

Generic, thread-safe, dependency-free. Combines:
- **LRU** (bounded entries — predictable memory, evicts coldest), and
- **TTL** (entries auto-expire so we never serve stale macros).

`getOrPut {}` gives idiomatic cache-aside. Unit-tested for TTL expiry, LRU
eviction, and compute-once semantics.

## 3 & 4. Debounce + backpressure on search

The nutrition search `Flow`:
```
_query
  .debounce { if (blank) 0 else 250ms }   // signal conditioning
  .distinctUntilChanged()                  // dedupe identical inputs
  .flatMapLatest { search(it) }            // cancel superseded query
  .conflate()                              // keep only the freshest result
```
Fewer DB hits, no wasted work on inputs the user already typed past, and the UI
never processes an out-of-date result — straight from reactive-streams theory.

## 5. Read-ahead prefetch

`SoloFitApplication.onCreate()` kicks off `foodRepository.warmUp()` on `Dispatchers.IO`.
A cheap `COUNT(*)` forces SQLite to open the DB and load index pages, so the
user's first real search doesn't pay cold-start I/O — the same trick OS page
caches and disk controllers use to hide latency.

## 6. Observability first (`PerfTrace`)

Before tuning, you must find the real bottleneck (Theory of Constraints). `PerfTrace`
records rolling **p50 / p95 / max** per operation and is **compiled out in release**
(`BuildConfig.DEBUG` guards), so it's free in production. Wrap any hot path:

```kotlin
PerfTrace.measureSuspend("barcode.lookup") { repo.lookup(code) }
// later, from a dev menu: PerfTrace.dump()  -> logcat p50/p95/max
```

Already instrumenting `barcode.lookup` and `food.warmUp`.

---

## Why these are safe
- All are **additive** and behind interfaces; no behavior change for the user
  except "faster".
- `PerfTrace` is a no-op in release builds.
- The cache has bounded memory (LRU cap) and self-invalidates (TTL), so it can't
  leak or serve indefinitely-stale data.

## Measuring the wins (on device)
- `PerfTrace.dump()` → compare `barcode.lookup` p50 before/after caching.
- Android Studio Profiler (CPU/System Trace) on the search box to confirm fewer
  DB queries while typing.
- `adb shell dumpsys meminfo com.solofit.app` to confirm the cache's RAM stays bounded.
