# SoloFit v1.3 — Silent Reminders & Body-Weight Monitoring

This release turns SoloFit into a daily companion: gentle **silent** nudges plus a
**weight-monitoring** loop that tells you whether you're tracking toward your goal.

## 1. Silent notifications — how the silence works

Android notification *sound/vibration* is controlled by the **channel importance**,
not per-notification flags (on API 26+). SoloFit creates its reminder channels at
`IMPORTANCE_LOW`:

```
NotificationChannel(id, name, IMPORTANCE_LOW).apply {
    setSound(null, null)   // no sound
    enableVibration(false) // no buzz
    enableLights(false)
}
```

`IMPORTANCE_LOW` = appears in the shade and as a badge, **no sound, no vibration,
no heads-up pop-up**. We also set `NotificationCompat.setSilent(true)` and
`PRIORITY_LOW` to cover pre-O devices. See `reminders/SoloNotifier.kt`.

Two channels: **Hydration reminders** and **Workout reminders**.

## 2. Scheduling (reliable, battery-friendly)

`reminders/ReminderScheduler.kt` uses **WorkManager** (no foreground service):

| Reminder | Mechanism | Notes |
|---|---|---|
| Hydration | `PeriodicWorkRequest` (unique) | Interval from settings (clamped to WM's 15-min min). |
| Workout   | `OneTimeWorkRequest` at the chosen time, **self-reschedules** for the next day | Skipped if you already trained today. |

Workers (`HydrationWorker`, `WorkoutWorker`) are `@HiltWorker`s; the app is a
`Configuration.Provider` supplying the `HiltWorkerFactory`, and the default
WorkManager initializer is disabled in the manifest.

### Suppression rules (no noise = no annoyance)
- **Quiet hours** (`QuietHours.kt`, unit-tested incl. midnight-wrap) — nothing fires overnight.
- **Goal-aware** — hydration skips if today's water goal is already met; workout
  reminder skips if a session is already logged today.
- **Reboot-safe** — `BootReceiver` re-arms everything after restart
  (`RECEIVE_BOOT_COMPLETED`).

## 3. Permissions

- **POST_NOTIFICATIONS** (Android 13+) requested in-context on the Reminders screen,
  with a fallback to system notification settings on older flows.
- No exact-alarm permission needed — WorkManager handles timing within OS windows
  (fine for gentle reminders; not meant to be to-the-second alarms).

## 4. Reminders settings UI

`ui/reminders/RemindersScreen.kt`: master toggles, hydration frequency chips,
workout time + quiet-hours time pickers (native `TimePickerDialog`), and a
permission card. Every change persists to DataStore and immediately re-applies the
WorkManager schedule.

## 5. Weight monitoring (close the loop)

- New Room table `weight_entries` (one row per day; DB **migration v2→v3**).
- `ui/weight/WeightScreen.kt`: quick log, a dependency-free **`LineChart`** trend,
  history list, and an **on-track / off-track** verdict computed against the user's
  `FitnessGoal` (lose → down, gain → up, maintain → within ±1 kg).

## Files added in v1.3
```
domain/model/ReminderSettings.kt
reminders/SoloNotifier.kt
reminders/QuietHours.kt              (+ test)
reminders/HydrationWorker.kt
reminders/WorkoutWorker.kt
reminders/ReminderScheduler.kt
reminders/BootReceiver.kt
ui/reminders/RemindersViewModel.kt + RemindersScreen.kt
data/local/entity/WeightEntryEntity.kt
data/local/dao/WeightDao.kt
data/repository/WeightRepositoryImpl.kt   (+ interface)
ui/weight/WeightViewModel.kt + WeightScreen.kt
ui/components/LineChart.kt
res/drawable/ic_stat_solofit.xml
```

## Honest notes / limits
- WorkManager timing is **inexact by design** (respects Doze/battery). For gentle
  reminders that's ideal; it is *not* a precise alarm clock. If you ever need
  to-the-minute alarms, switch the workout reminder to `AlarmManager`
  `setExactAndAllowWhileIdle` + the `SCHEDULE_EXACT_ALARM` permission.
- Some OEMs (aggressive battery managers) may delay background work; that's an
  Android-ecosystem reality, not a code bug.
