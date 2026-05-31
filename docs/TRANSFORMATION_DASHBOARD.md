# SoloFit v2.0 — Transformation Dashboard

Reframes the app around the metrics that actually drive a recomp, not daily scale
weight. Calories are *one* component; consistency + trends + strength + V-taper lead.

## New "headline metric" — Days Consistent
The dashboard now opens with a hero card:
```
Foundation Recomp
Day 18 / 365
```
Phase name + day count come from DataStore (`phaseName`, `phaseStartDate`,
`phaseTargetDays`); day = days since start + 1. Calories/macros sit *below* the hero.

## Body & Recovery screen  (Settings → Body & recovery, or the dashboard Recovery chip)
- **V-Taper Score** — shoulders ÷ waist, with a label (Building base → Elite) and the
  golden ≈ 1.62 target. Shows waist Δ vs your last entry (down = good).
- **Waist trend** chart (reuses `LineChart`).
- **Measurements form** (waist/chest/shoulders/arms/thighs/neck) — one row per date,
  stored in `body_measurements`.
- **Daily check-in** (sleep, steps, mood, energy) — stored in `daily_metrics`.

## Recovery / Readiness score (dashboard chip)
`FitnessMath.recoveryScore` blends sleep (40%), steps (20%), workout-done (15%),
water (15%), energy (10%) — normalized by whatever data is present, so partial input
still works. Labelled High / Moderate / Low / Rest day.

## Strength: estimated 1RM
Each exercise card in the active workout shows **Est. 1RM** from your best completed
set via the Epley formula (`weight × (1 + reps/30)`), updating live.

## Traffic-light calories/protein
The liquid macro bars already turn red when over; protein/calorie targets remain the
simple green/over signal (kept deliberately un-MyFitnessPal).

## Data model (DB v4 → v5)
| Table | Purpose |
|---|---|
| `body_measurements` | waist/chest/shoulders/arms/thighs/neck per date (nullable) |
| `daily_metrics` | sleep/steps/mood/energy per date |
Migration `MIGRATION_4_5` adds both with unique date indexes.

## Pure math (unit-tested) — `core/FitnessMath.kt`
- `epley1RM`, `vTaperRatio` + label, `recoveryScore` + `readinessLabel`.
- Covered by `FitnessMathTest` (1RM formula, V-taper buckets, weighted recovery incl.
  partial-input normalization, readiness labels).

## Design choices (matching an analytical user)
- **No heavy streak gamification** — streak/active-day chips stay, but the hero is
  "Days Consistent" + trends, which tell the real story.
- Manual entry for sleep/steps (zero deps, fully offline). Health Connect can be added
  later without changing the data model.

## Still on the roadmap (not in this round)
- Progress photos (front/side/back) with consistent-capture guidance + compare.
- Strength 1RM **trend** graphs per lift; measurement trends for all sites.
- Editable phase UI (currently defaults: "Foundation Recomp", 365 days; start date set
  when first written via `setPhase`).

---

## v2.1 — edit phase, strength trends, progress photos

### Edit Phase UI
Tap the dashboard hero card → `EditPhaseScreen`. Set phase **name** (with presets:
Foundation Recomp / Cut / Lean Bulk / Maintenance), **target length** (90/180/365 day
presets or custom), and **start date** (native date picker). Persists via
`ProfileRepository.setPhase`; the hero's "Day X / target" updates immediately.

### Strength Progress (per-lift 1RM trends)
`Body & Recovery → Strength trends` → `StrengthScreen`. For every exercise with
completed sets, computes the **best estimated 1RM per day** (Epley) and plots it with
`LineChart`, showing current best and total gain since you started. Backed by a new
`WorkoutDao.observeCompletedSetRows()` join (`exercise_sets` ⋈ `workout_sessions`).

### Progress Photos (front / side / back) — 100% on-device
`Body & Recovery → Progress photos` → `ProgressPhotosScreen`.
- Pose tabs (Front/Side/Back), capture via **camera or gallery**.
- **Consistency guidance** banner (same spot/lighting/time/distance).
- **Then-vs-Now** side-by-side (first vs latest) + a scrollable **timeline**.
- **Storage:** images saved to the app's **private internal storage**
  (`filesDir/progress_photos`, downscaled to ≤1080px, JPEG q85). The DB stores only
  file names + pose + date. **Nothing is uploaded** — consistent with the privacy model.
- Data model: `progress_photos` table, migration `MIGRATION_5_6` (DB v5 → v6).

### Footprint
No new dependencies. Photos are user-generated and downscaled; everything else is a
few KB of code. DB stays tiny (file names, not blobs).
