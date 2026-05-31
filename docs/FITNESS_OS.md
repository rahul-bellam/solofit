# SoloFit v2.2 — "Fitness OS" pivot

Removed the ML/photo guessing; nutrition is now pure **manual entry** against the
local food dataset. Added the high-leverage "operating system" modules.

## Removed (smaller footprint, less complexity)
- TensorFlow Lite classifier, portion estimator, food-area estimator, photo-classification
  screen, dish-baseline + density seeds, classification domain models, model assets,
  and **both TFLite dependencies**. (Progress Photos — camera only, no ML — kept.)
- Barcode scanner + Open Food Facts lookup **kept** (exact lookup, not guessing).

## Nutrition — manual entry ("khana" dataset)
- Foods carry optional `servingGrams` + `servingLabel` (DB v6→v7).
- Log dialog supports **grams OR count**: type "5 eggs" / "2 slices" and it converts
  (5 × 50g) → macros. Grams-only foods (oils) just use grams.

## Workout engine
- **RIR (Reps In Reserve)** per set (DB v7→v8) — new column + input in the active workout.
- **Estimated 1RM** per exercise (Epley), live.
- **Auto-progression** coaching from completed sets (`FitnessMath.progression`,
  double-progression, top-of-range 12): "Increase weight" / "Repeat" / "Deload".

## Goal engine
- `TrainingGoal`: Fat Loss / Athletic / Strength / Bodybuilding, each with component
  weights. Selected in Edit Phase; persisted in DataStore.

## Transformation Score (the headline number)
- `FitnessMath.transformationScore` blends **strength progress + waist reduction +
  consistency + recovery**, weighted by the chosen `TrainingGoal`, normalized to 0–100.
- Shown on the dashboard hero next to "Day X / target".
- Inputs computed live: waist drop vs start (5cm→max), avg lift 1RM gain vs 20% target,
  active-days/7, recovery/100.

## Tests
`FitnessMathTest` (+ progression/score classes): Epley, V-taper, recovery, readiness,
progression rules, and transformation-score weighting/normalization. All verified.

## Footprint impact
- **2 dependencies removed** (TFLite runtime + support, ~2–3 MB/ABI) and model assets gone.
- Net: smaller APK, lower peak RAM (no interpreter), simpler code — exactly the
  "10 variables that matter" direction.
