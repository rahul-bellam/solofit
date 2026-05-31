# SoloFit — Barcode Scanning (Open Food Facts)

The one network-using feature: scan a product barcode to fetch exact macros.
This is a **lookup, not guessing** — no ML, no photo inference.

> The photo food-classification and on-device portion estimator that previously
> lived here were **removed** in the v2.2 "fitness OS" pivot in favour of manual
> entry against the local food dataset (see `docs/FITNESS_OS.md`).

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
- Networking is restricted to `openfoodfacts.org` via `res/xml/network_security_config.xml`
  (cleartext disabled).
- A scanned product is cached in the local food table, so repeat scans are fully offline.

## Key files
- `data/scanner/BarcodeScanner.kt` — wraps `GmsBarcodeScanning` (Play Services).
- `data/remote/OpenFoodFactsService.kt`, `data/remote/dto/OffProductDto.kt` — OFF API.
- `data/repository/BarcodeRepositoryImpl.kt` — tiered cache-aside lookup.
- `ui/scan/ScanScreen.kt`, `ui/scan/ScanViewModel.kt` — UI + manual fallback.
