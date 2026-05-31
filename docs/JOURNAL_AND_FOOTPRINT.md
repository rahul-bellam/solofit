# SoloFit v1.5 — Daily Journal + Footprint Hygiene

This release adds a **journaling/ledger layer** (morning goals + evening gratitude)
plus two footprint-minded systems: **DB maintenance** and a **perf dev screen**.

## 1. Journal (ledger / notepad)

A daily companion with two halves:

| Half | Prompt | UI |
|---|---|---|
| **🌅 Morning** | "Plan your day" (default 08:00) | Checklist of goals — add, tick off, delete; progress bar |
| **🌙 Evening** | "2-minute gratitude" (default 21:00) | Free-text reflection + a scrollable history of recent reflections |

- Storage: two tiny Room tables (`goal_items`, `gratitude_entries`) — **DB migration v3→v4**.
  - Goals: text + `done` flag, keyed by date, ordered.
  - Gratitude: one canonical row per date (unique index → re-saving updates it).
- Reachable from the **Dashboard** ("Open Journal") and **Settings → Tracking → Journal**.

### Silent prompts reuse the reminder engine
Two new self-rescheduling daily workers (`MorningGoalsWorker`, `EveningGratitudeWorker`)
post on a new **silent** `journal_prompts` channel (`IMPORTANCE_LOW` — no sound/buzz).
Toggles + time pickers live on the Reminders screen; everything persists to DataStore
and re-arms on reboot.

## 2. Footprint hygiene — `DbMaintenanceWorker`

A server-ops principle applied locally. Over time SQLite accumulates free pages
(from deletes/updates) and a growing `-wal` file. A **weekly** worker, constrained to
**device-idle + battery-not-low** (so it's invisible), runs:

```
PRAGMA wal_checkpoint(TRUNCATE)   -- fold WAL back, shrink -wal
VACUUM                            -- rebuild + reclaim free pages
```

Result: smaller on-disk footprint and tighter index scans. Scheduled idempotently
on app start (`KEEP` policy) and re-armed by the boot receiver. Best-effort — never
fails loudly.

## 3. Perf dev screen (`PerfScreen`)

A debug-only screen (Settings → "Performance (debug)", gated on `BuildConfig.DEBUG`)
surfacing `PerfTrace` **p50/p95/max** for instrumented hot paths
(`barcode.lookup`, `food.warmUp`, `db.maintenance`, `db.search`). Lets you *measure*
the optimizations from v1.4 on a real device instead of guessing.

## Files added in v1.5
```
data/local/entity/JournalEntities.kt        (GoalItemEntity, GratitudeEntryEntity)
data/local/dao/JournalDao.kt
data/repository/JournalRepositoryImpl.kt     (+ interface, binding, DAO provider)
ui/journal/JournalViewModel.kt + JournalScreen.kt
reminders/MorningGoalsWorker.kt
reminders/EveningGratitudeWorker.kt
reminders/DbMaintenanceWorker.kt
ui/devtools/PerfScreen.kt
```
Plus: DB v3→v4 migration, journal notification channel, scheduler methods, reminder
settings fields + UI toggles, dashboard & settings entry points.

## Footprint notes
- Journal data is text-only → kilobytes even after months of use.
- No new third-party libraries added — reuses Room, WorkManager, DataStore already present.
- Maintenance keeps the DB file from bloating, complementing the bounded LRU+TTL
  cache from v1.4 (RAM) — disk *and* memory stay lean.
