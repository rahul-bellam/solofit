# SoloFit — Kotlin Local Edition 🏋️

A **privacy-first, local-first** Android app for daily calorie goals, nutrition logging, and gym workout tracking. The core engine (calories, food DB, workouts) runs **100% offline**.

**v1.1** adds two *opt-in* intake pipelines that use the camera:
**barcode scanning** (Open Food Facts lookup over HTTPS, domain-locked) and
**on-device photo classification** (TensorFlow Lite — no LLM, images never leave the phone).
See [`docs/BARCODE_SCANNING.md`](docs/BARCODE_SCANNING.md).

> Built with Kotlin · Jetpack Compose · Room · Hilt · MVVM + Clean Architecture.

---


## v2.2 — "Fitness OS" pivot (current)

Nutrition is now **manual entry** against the local food dataset (log "100 g chicken
breast" or "5 eggs" → macros). The on-device ML photo classifier + portion estimator
were **removed** (smaller APK, no TFLite). Barcode lookup (Open Food Facts) stays.
Added RIR, auto-progression, a goal engine, and a **Transformation Score**.
See [`docs/FITNESS_OS.md`](docs/FITNESS_OS.md) and [`docs/TRANSFORMATION_DASHBOARD.md`](docs/TRANSFORMATION_DASHBOARD.md).


## v1.6.2 — finishing touches

- Water-goal **ripple + haptic** celebration when you hit your daily target.
- **Liquid calorie ring** — the dashboard ring fills with the same wave as the water glass.
- **OS reduce-motion respected** — if system animations are off, playful animations
  default off on first launch (still user-overridable in Settings).


## v1.6 — Playful (optional) exercise animations

Gym-flavored micro-interactions in the Journal, all drawn with Compose Canvas
(**no Lottie/GIF/asset — zero extra footprint**) and fully optional:
- **Dumbbell check-offs** — ticking a goal curls a dumbbell up with a rep-bounce.
- **Scroll = overhead press** — scrolling the journal makes a figure press a barbell overhead.
- **Lat-pulldown celebration** when all goals are done.
- **Settings → Appearance → "Playful animations"** switch turns them all off (calm static UI).

See [`docs/PLAYFUL_ANIMATIONS.md`](docs/PLAYFUL_ANIMATIONS.md).


## v1.5 — Daily journal & footprint hygiene

- **Journal (ledger/notepad)** — morning **goals checklist** and evening **2-minute
  gratitude** note, with recent-reflections history (new Room tables, migration v3→v4).
- **Silent journal prompts** — morning "plan your day" + evening "gratitude" nudges on
  a silent channel, with time pickers, reboot-safe, reusing the reminder engine.
- **DB maintenance** — weekly `wal_checkpoint(TRUNCATE)` + `VACUUM` (idle + battery-not-low
  constrained) to keep the on-disk footprint lean.
- **Perf dev screen** — debug-only p50/p95/max for instrumented hot paths.

See [`docs/JOURNAL_AND_FOOTPRINT.md`](docs/JOURNAL_AND_FOOTPRINT.md).


## v1.4 — Cross-domain performance optimizations

Borrowed principles from CDNs, telecom, reactive streams, OS paging and SRE:
- **Tiered cache-aside** for barcode lookups (memory → Room → network).
- **`LruTtlCache`** — bounded-memory LRU + TTL (CDN/buffer-pool style), unit-tested.
- **Debounce + conflation + distinctUntilChanged** on nutrition search (fewer DB hits, freshest-only results).
- **DB prefetch/warm-up** on launch so the first search hits warm SQLite pages.
- **`PerfTrace`** p50/p95/max latency harness (free in release builds).

See [`docs/CROSS_DOMAIN_OPTIMIZATIONS.md`](docs/CROSS_DOMAIN_OPTIMIZATIONS.md).


## v1.3 — Silent reminders & weight monitoring

- **Silent notifications** — hydration + daily-workout nudges on `IMPORTANCE_LOW`
  channels (appear in the shade, **no sound/vibration/heads-up**).
- **Smart suppression** — quiet hours (midnight-wrap aware), skips when the day's
  water goal is met or a workout is already logged; reboot-safe via WorkManager +
  `BootReceiver`.
- **Reminders settings** — toggles, frequency chips, time pickers, Android 13+
  notification-permission flow.
- **Weight monitor** — daily weigh-in log, dependency-free trend chart, and an
  on-track/off-track verdict vs your goal (new Room table, migration v2→v3).
- Tests added: `QuietHoursTest` (incl. midnight wrap).

See [`docs/REMINDERS_AND_MONITORING.md`](docs/REMINDERS_AND_MONITORING.md).


## v1.2 — Settings, profile & dashboard polish

- **Settings screen** — theme mode (System / Light / Dark, persisted via DataStore),
  a privacy summary, and an entry point to edit your profile.
- **Editable profile** — change weight/age/activity/goal and your calorie & macro
  targets **recalculate live** (reuses the Mifflin-St Jeor engine) and persist.
- **Dashboard polish** — daily **water tracker** (+/- a glass), a **workout streak**
  chip and a **7-day active-days** chip, a settings shortcut, and an accessibility
  description on the calorie ring.
- **Reusable `EmptyState`** used on the Workout & History screens.
- **More tests** — `StreakCalculatorTest` (streak + weekly window logic) added
  alongside the existing calorie-engine and Open Food Facts mapping tests.


## ✨ Features

| Module | What it does |
|---|---|
| **Onboarding & Profile** | Collects age, gender, weight, height, activity level, goal → computes **BMR (Mifflin-St Jeor)**, **TDEE**, calorie target (±offset by goal) and a hardcoded macro split (2 g protein/kg, 25% kcal fat, rest carbs). Live preview as you type. |
| **Dashboard** | Glanceable day summary: animated **circular calorie ring** + three linear **macro bars** (protein/carbs/fats) + quick actions. |
| **Nutrition Log** | Search a **105-item local food DB** (per-100g macros), enter grams → exact macros computed `(grams/100)*base`, logged by meal (Breakfast/Lunch/Dinner/Snacks). |
| **Workout Tracker** | **Routine Builder** (pick from 47 seeded exercises), **Active Workout** screen with weight/reps inputs + per-set completion checkboxes, and a **calendar History** of completed sessions with volume stats. |

## 🔒 Non-Functional Requirements (met)

- **Privacy:** core app stays on-device. v1.1 declares `INTERNET` (used **only** for Open Food Facts, enforced by `network_security_config.xml`) and `CAMERA` (barcode + on-device classifier; photos are never uploaded).
- **Size:** No bitmap assets bundled. Launcher icon + all imagery use **vector drawables / Material icons**, and release build enables R8 minify + resource shrinking to stay well under 40 MB.
- **Performance:** All DB access goes through **Room + Kotlin Coroutines/Flow** on background dispatchers; UI observes `StateFlow` so the main thread never blocks.

---

## 🏛️ Architecture

```
ui (Compose screens + ViewModels)      ← presentation
        │  observes StateFlow / calls
        ▼
domain (models, repository interfaces, use cases)   ← pure Kotlin, testable
        ▲  implemented by
        │
data (Room entities/DAOs, repositories, seed data, DataStore)   ← framework
```

- **DI:** Hilt (`AppModule` provides DB/DAOs/scope; `RepositoryModule` binds interfaces).
- **Single source of truth:** Room `Flow`s drive the UI reactively.

### Room schema

```
user_profile        food_items            daily_log (FK → food_items)
routines ──1:N──► exercises
workout_sessions ──1:N──► exercise_sets
```

See `data/local/entity/*` and `data/local/relation/Relations.kt`.

---

## ▶️ Building & Running

Requirements: **Android Studio (Koala or newer)**, **JDK 17**, Android SDK 34.

```bash
# 1. Open the SoloFit/ folder in Android Studio (it will sync Gradle), OR from CLI:
./gradlew :app:assembleDebug      # build APK
./gradlew :app:installDebug       # install to a connected device/emulator
./gradlew test                    # run unit tests (calorie engine)
```

> The first sync downloads Gradle 8.9, AGP 8.5.2 and dependencies — that step needs
> internet **on your dev machine**. The *app itself* never uses the network.

If `local.properties` is missing, Android Studio creates it; or copy the template:

```
cp local.properties.example local.properties   # then set sdk.dir
```

---

## ✅ Tests

`app/src/test/.../CalculateNutritionTargetsUseCaseTest.kt` verifies the BMR/TDEE/offset
and macro math against hand-computed Mifflin-St Jeor values (male maintain, female cut,
gain offset, and input validation).

---

## 📁 Key paths

```
app/src/main/java/com/solofit/app/
├─ domain/usecase/CalculateNutritionTargetsUseCase.kt   # the offline math engine
├─ data/local/seed/FoodSeedData.kt                      # 105 foods
├─ data/local/seed/ExerciseSeedData.kt                  # 47 exercises
├─ data/local/SoloFitDatabase.kt                        # Room DB + first-run seeding
├─ ui/onboarding | dashboard | nutrition | workout      # the four modules
└─ ui/SoloFitApp.kt                                      # nav graph + bottom bar
```

## 🛣️ Possible next steps

- Export/import the local DB as an encrypted file for device migration.
- Progressive-overload charts per exercise from `exercise_sets` history.
