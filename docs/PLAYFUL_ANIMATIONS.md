# SoloFit v1.6 — Playful (but optional) Exercise Micro-Animations

The journal should feel like a coach, not a chore — never boring, never making the
user feel judged. This release adds gym-flavored micro-interactions that are
**delightful, optional, and zero-footprint**.

## Design principles
- **No extra footprint.** Everything is drawn with **Compose Canvas + the animation
  APIs already in the project** — no Lottie, no GIFs, no bitmaps, no new dependency,
  no asset files. `ui/components/ExerciseAnimations.kt`.
- **Optional & respectful.** A "Playful animations" switch in **Settings → Appearance**
  turns them all off for a calm, still UI. Every animator takes `animate=false` and
  renders a tasteful static state — good for battery, accessibility, and reduce-motion
  preferences.
- **Encouraging tone.** Copy is supportive ("every rep counts", "All goals smashed!"),
  never punishing.

## The three interactions (in the Journal)

| Interaction | Where | What it does |
|---|---|---|
| **`DumbbellCheck`** | Each goal's checkbox | Ticking a goal "curls" a dumbbell up with a little overshoot **rep-bounce** (Animatable spring-like settle). |
| **`OverheadPressHeader`** | Top of the journal | A stick-figure presses a barbell. **Scrolling drives the rep** — `derivedStateOf` maps the first ~600px of scroll offset to press 0→1, so scrolling literally performs an overhead shoulder press. |
| **`LatPulldownCelebration`** | When all goals are done | An `AnimatedVisibility` card appears with a **lat-pulldown bar** that sweeps down on a gentle infinite reverse loop. |

## How it's wired
- `UserPreferences.animationsEnabled` (DataStore, default **on**) → exposed via
  `ProfileRepository` → read by `JournalViewModel` and `SettingsViewModel`.
- `JournalScreen` collects the flag and passes `animate` into each component; it also
  feeds the `LazyListState` scroll offset into `OverheadPressHeader`.

## Performance notes
- Canvas drawing is cheap (a handful of `drawLine`/`drawRoundRect` calls).
- Scroll mapping uses `derivedStateOf` so it only recomposes the tiny header when the
  derived press value actually changes — not on every scroll pixel.
- Infinite animation (celebration) only exists while the "all done" card is visible.
- With the switch off, there are **no** running animations at all.

## Files
```
ui/components/ExerciseAnimations.kt   (DumbbellCheck, OverheadPressHeader, LatPulldownCelebration)
```
Plus: `animationsEnabled` in UserPreferences / ProfileRepository(+impl);
Journal + Settings ViewModels & screens updated; Settings "Playful animations" switch.

---

## v1.6.1 — extended to workouts + water, with haptics

- **Dumbbell tick on workout sets** — the Active Workout set-completion checkbox now
  uses `DumbbellCheck` too (the most natural home for it).
- **Haptics** — completing a goal *or* a workout set fires a light
  `HapticFeedbackType.LongPress`. Haptics are an OS service — **no library, no asset,
  no measurable RAM/disk cost**.
- **Animated water glass** — `WaterTracker` now shows a `WaterGlass` that fills
  bottom-up with a gentle surface wave as you log water (replaces the flat bar).
- All three respect the same **"Playful animations" switch** (`animate=false` →
  still glass, plain dumbbell, no wave loop).

## Footprint impact (measured)

**Disk / APK**
- New code: `ExerciseAnimations.kt` ≈ **10 KB of source** (compiles to a few KB of
  DEX). `WaterTracker`/screen edits are net-neutral.
- **No new dependencies** (build.gradle/libs.versions.toml unchanged) and **no new
  asset/resource files** — no Lottie JSON, no GIF/PNG, no `res/raw`. Compare: a single
  Lottie animation is typically 20–150 KB *plus* the ~1 MB Lottie runtime library.
  We add **~0** of that. Net APK delta: **well under ~20 KB**.

**RAM**
- The animators allocate only a couple of `Animatable`/`Path`/`Offset` objects while
  on screen (a few KB), freed when you navigate away (`WhileSubscribed` + Compose
  disposal). No bitmaps are decoded, so there's **no heap spike** like image-based
  animation would cause.
- Infinite loops (water wave, lat-pulldown) run **only while their composable is
  visible**; off-screen = zero animation cost. With the switch off, **no animations
  run at all**.

**CPU / battery**
- Canvas draws are a handful of vector ops per frame; the scroll→press mapping uses
  `derivedStateOf` so it recomputes only when the derived value changes, not per pixel.

Net: the "delight" layer is effectively free on all three axes — the deliberate
payoff of using Compose Canvas instead of an animation library.

---

## v1.6.2 — goal-hit ripple, liquid calorie ring, OS reduce-motion auto-detect

- **(a) Water goal celebration** — when daily water first reaches the goal, the glass
  emits a one-shot expanding **ripple** + a light haptic (`WaterTracker`). Detected via
  a below-goal → at-goal transition (`LaunchedEffect(goalHit)`), so it fires once, not
  every recomposition.
- **(b) Liquid calorie ring** — `CalorieRing` now fills its interior with the same
  bottom-up **wave** used by the water glass (`waveFill=true`), giving the dashboard a
  cohesive "fill up" language. Still respects `animate=false`.
- **(c) Reduce-motion auto-detect** — on first launch, `MainActivity` reads the OS
  `Settings.Global.ANIMATOR_DURATION_SCALE`; if it's `0` (user turned animations off
  system-wide), the playful-animations switch is flipped **off once**
  (`RootViewModel.applyReducedMotionOnce`, guarded by a `reducedMotionApplied` flag so
  the user's later choice is never overridden).

Footprint impact of this round: **still zero new dependencies and zero asset files** —
all Canvas + animation APIs already in the project. Net source added is a few KB; no
bitmaps, so no heap/APK growth of note.

---

## v1.6.3 — liquid macro bars + reduce-motion unit test

- **(b) Liquid macro bars** — `MacroBar` now renders on a Canvas with the same wave
  language as the water glass / calorie ring (a wavy leading edge sweeping vertically),
  driven by the shared `animate` flag. Replaces the old Box-based fill (removed now-dead
  `background`/`RoundedCornerShape` imports). Dashboard passes `animate` to all three.
- **(c) Testability** — the reduce-motion decision is extracted to a pure
  `core/ReduceMotionPolicy.shouldDisableAnimations(scale, alreadyApplied)` and covered by
  `ReduceMotionPolicyTest` (OS-off → disable once; OS-on → no-op; already-applied →
  never override the user). `RootViewModel` now delegates to it.

Footprint: unchanged — no new deps, no assets.

---

## v1.6.4 — live Settings preview + off-screen animation gating

- **Live preview** — the "Playful animations" card in Settings now embeds a real,
  self-contained `AnimationPreview` (mini calorie ring + 3 liquid macro bars + water
  glass) that reflects the toggle **instantly**, so users see the effect before
  committing. Uses fixed demo values (no DB/flow reads) → zero data cost.
- **Optimization — lifecycle-gated loops** — new `rememberAnimationsActive(enabled)`
  (`ui/components/AnimationGate.kt`) gates the user's animation preference on the
  lifecycle being **RESUMED**, via lifecycle-runtime-compose's built-in
  `currentStateAsState()` (already a dependency, no leak). Infinite wave/ring loops
  now **pause when the screen is backgrounded** and resume on return — saving CPU/
  battery with **no visible change** while on screen. Applied on Dashboard, Journal,
  Active Workout, and the Settings preview.

Footprint/cost: no new dependencies, no assets. Net effect is *less* runtime work
(loops stop off-screen) at the cost of a few KB of source.

---

## v1.6.5 — shared liquid bar, leaner gate, gate unit test

- **(b) Journal goals bar** now uses the shared `LiquidProgressBar` (extracted from
  `MacroBar`) — the morning checklist progress fills with the same wave language as
  macros / calorie ring / water glass. One component, full consistency, less code.
- **Leaner gate (no caching)** — `rememberAnimationsActive` reads the OS lifecycle
  state directly via `currentStateAsState()` and ANDs it with the user preference
  through the pure `AnimationGatePolicy.active(enabled, resumed)`. No retained flags,
  no cache; loops **fully stop** the moment the screen leaves RESUMED.
- **(c) Unit test** — `AnimationGatePolicyTest` asserts: disabled → always false;
  enabled+backgrounded → false; enabled+resumed → true.

No new dependencies, no assets.
