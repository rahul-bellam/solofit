# SoloFit — Performance & Memory Guide

RAM and speed move together here: fewer allocations → less GC → smoother frames **and**
lower footprint. This documents what's already implemented and what to do next.

## ✅ Implemented in this iteration

### 1. Bitmap memory safety (biggest RAM win)
- `core/BitmapUtils.kt`
  - `decodeSampled()` — bounds-only first pass, then `inSampleSize` power-of-two
    downscale, so a 12 MP photo (~48 MB ARGB_8888) never fully enters RAM. Use this
    if you switch to full-resolution capture via a `Uri`.
  - `capInMemory()` — defensively scales down + `recycle()`s any oversized bitmap.
- `PhotoScreen` now caps the camera preview bitmap before holding/classifying it.
- Only **one** bitmap is retained at a time (`lastBitmap`), replaced per capture.

### 2. TFLite inference (speed + steady RAM)
`data/ml/FoodClassifier.kt`:
- Model is **memory-mapped** (mmap) — not copied to the Java heap.
- Interpreter created **once, lazily**, and reused (no per-call model load).
- `ImageProcessor`, `TensorImage`, and the output `TensorBuffer` are **reused**
  across classifications instead of reallocated each frame → minimal GC churn.
- **XNNPACK** delegate enabled; `numThreads` clamped to `cores.coerceIn(2,4)`
  (more threads ≠ faster, and each costs memory).
- Skips the ARGB_8888 copy when the input is already that config.
- A `Mutex` serializes inference so reused buffers are never raced.
- `close()` releases native resources when needed.

### 3. Database-side aggregation (fewer objects, faster dashboard)
- `DailyLogDao.observeTotalsForDate()` sums calories/macros **in SQLite** and returns
  a single `DailyTotals` row, instead of loading N joined entities and folding them
  in Kotlin. `DashboardViewModel` now uses this fast-path.
- The Nutrition diary still loads per-entry rows (it must, to render the list), but
  the always-visible dashboard no longer pays that cost.

### 4. Lifecycle-scoped flows
- All `StateFlow`s use `SharingStarted.WhileSubscribed(5000)`, so observers and their
  buffers tear down shortly after a screen leaves the backstack.

## 🔜 Recommended next (not yet done — need build env / model)

### 5. Quantize the model (int8)
- A quantized MobileNetV3 (101 classes) is ~4–6 MB vs ~16 MB float, **and** uses less
  transient tensor memory + runs faster on CPU. If you ship a quantized model, set
  `IMAGE_MEAN/IMAGE_STD` to match its expected input and (optionally) switch the
  output buffer to `UINT8` + dequantize.

### 6. Ship an Android App Bundle (`.aab`)
- Play delivers only the device's ABI + density, removing duplicate TFLite native
  libs from the install. Biggest *download/install* size win.

### 7. Baseline Profiles (startup speed)
- Add `androidx.profileinstaller` + a `baseline-prof.txt` (generated via the Macrobenchmark
  module) to AOT-compile hot startup/scroll paths. Typically 15–30% faster cold start.

### 8. Compose hygiene (already mostly followed)
- Keep derived lists computed in ViewModels (done for dashboard).
- Provide stable `key`s in `LazyColumn` (done across screens).
- Prefer `@Stable`/`@Immutable` data and lambda references that don't capture new
  objects each recomposition.

## How to measure (do this on a real device)
- **RAM:** Android Studio Profiler → Memory, capture during a photo classification;
  or `adb shell dumpsys meminfo com.solofit.app`.
- **Jank/CPU:** Profiler → CPU / "System Trace", or Macrobenchmark `FrameTimingMetric`.
- **Size:** `./gradlew :app:bundleRelease` then APK Analyzer; compare per-ABI splits.

## Quick expectations after these changes
- Steady-state private RAM: **~50–70 MB**.
- Photo-inference spike: **~+8–15 MB** (down from ~15–20 MB) and shorter, thanks to
  buffer reuse + bitmap capping.
- Dashboard updates: O(1) row from SQLite instead of O(n) entities folded in Kotlin.
