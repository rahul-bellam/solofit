# SoloFit — Food Scanning (Barcode + AI photo)

Two opt-in scan paths:
1. **Barcode lookup** — scan a product barcode to fetch exact macros. This is a
   **lookup, not guessing** (no inference): the barcode is read on-device and the
   macros come from Open Food Facts / the local cache.
2. **AI food-photo estimation** — the user photographs a plated dish and the image
   is **uploaded to Google's Gemini API**, which returns an estimated
   per-100g macro breakdown. This only runs on an explicit "AI scan" tap and
   requires a configured `GEMINI_API_KEY`.

> The earlier on-device TFLite photo-classifier was **removed** in the v2.2
> "fitness OS" pivot. AI photo estimation was later re-introduced as the
> **cloud** Gemini path above — it is not on-device (see `ui/scan/AiFoodScanViewModel.kt`).

## Flow
```
Camera ─▶ Google Code Scanner (Play Services, on-device)
        ─▶ extracts barcode string (e.g. "737628011226")
        ─▶ BarcodeRepository.lookup():
             L1 in-memory LRU+TTL cache  → L2 local Room cache → L3 Open Food Facts (HTTPS)
        ─▶ found: prefill macros; not found: manual "add this product" form
        ─▶ writes to the daily log; caches the product locally for instant repeat scans
```

## Privacy
- Networking is restricted to three allowlisted hosts via
  `res/xml/network_security_config.xml` (cleartext disabled, all other domains blocked):
  `openfoodfacts.org`, `nal.usda.gov`, and `googleapis.com` (Gemini).
- Barcode scanning is on-device (Google Code Scanner); a scanned product is cached
  in the local food table, so repeat barcode scans are fully offline.
- **AI food-photo scan uploads the captured image to Google Gemini.** This is the
  only feature that transmits user-captured images off the device, and it is opt-in.

## Key files
- `data/scanner/BarcodeScanner.kt` — wraps `GmsBarcodeScanning` (Play Services).
- `data/remote/OpenFoodFactsService.kt`, `data/remote/dto/OffProductDto.kt` — OFF API.
- `data/repository/BarcodeRepositoryImpl.kt` — tiered cache-aside lookup.
- `ui/scan/ScanScreen.kt`, `ui/scan/ScanViewModel.kt` — UI + manual fallback.
