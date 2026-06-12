# SoloFit: Free → Premium Transition Plan

---

## 1. Premium Tiers

### Free Tier (existing)
- Core tracking: nutrition logging, workout sessions, water, weight
- Weekly plans (1 active plan)
- Barcode scanning
- AI food scan (5 scans/day)
- Basic dashboard

### Premium Tier ($4.99/mo or $39.99/yr)
All of Free, plus:

| Feature | Value Prop |
|---|---|
| Unlimited AI food scans | Remove 5/day cap |
| Multiple weekly plans | Auto-rotate plans (Push/Pull/Legs on different days) |
| Meal prep mode | Batch-log future meals, export grocery lists |
| Advanced analytics | Weekly/monthly trend charts, macro adherence scores |
| Custom goals | Set different macros per day (training vs rest day) |
| Progress photos | Unlimited photo storage with timeline view |
| Body measurements | Track neck, shoulders, chest, waist, hips, etc. |
| Export data | CSV/PDF export of all logs |
| No ads | Removes bottom banner |

### Pro Tier ($9.99/mo or $79.99/yr)
Everything in Premium, plus:

| Feature | Value Prop |
|---|---|
| AI meal planner | Generate a week of meals from calorie/macro targets |
| Smart recipe scanner | Photo of a recipe → per-serving macros |
| Custom AI training plans | Generate workout routines from goals + equipment |
| Form check | Upload video, AI analyzes exercise form |
| Coach mode | Share data with a real coach via read-only link |
| Meal prep optimization | Minimize cost or prep time given macro targets |

---

## 2. Feature Gate Architecture

### Approach: Feature flags with Paywall overlay

```kotlin
enum class PremiumFeature {
    UNLIMITED_SCANS,
    MULTIPLE_PLANS,
    MEAL_PREP,
    ANALYTICS,
    EXPORT,
    PROGRESS_PHOTOS,
    BODY_MEASUREMENTS,
    NO_ADS
}

fun isFeatureAvailable(feature: PremiumFeature): Boolean {
    return when (feature) {
        PremiumFeature.UNLIMITED_SCANS -> isPremium()
        // ...
    }
}
```

- Check `isFeatureAvailable()` at the entry point of the gated feature
- If not available, show a bottom sheet paywall instead of the feature
- Track paywall views and conversion in Firebase Analytics

### What NOT to gate (keep free to retain users)
- Nutrition logging (core loop)
- Workout tracking (core loop)
- Dashboard (daily engagement)
- Barcode scanning (utility, drives food logging)
- 5 AI scans/day (taste — enough to hook users)

---

## 3. Backend Infrastructure

### What needs a backend
| Service | Why | Options |
|---|---|---|
| Subscription management | Store receipt, expiry, platform | RevenueCat (recommended), or Billing Library direct |
| Progress photos | Cloud storage, not local | Firebase Storage, Cloudinary |
| Multiple plans sync | Cross-device plan storage | Firestore (no server needed) |
| AI features (Pro) | Server-side LLM calls | Cloud Run + Gemini API |

### What stays local
- All food logs, workout sessions, water, weight — Room DB
- Weekly plans (cached locally, synced to Firestore if Premium)

### RevenueCat integration
One-time setup, handles:
- Google Play Billing
- Free trial offers
- Introductory pricing
- Receipt validation
- Subscription status caching (offline-safe)

---

## 4. Implementation Phases

### Phase 0 — Foundation (2 weeks)
- [ ] Set up RevenueCat SDK + Google Play Billing
- [ ] Create `PremiumManager` with offline caching of subscription state
- [ ] Add `isFeatureAvailable()` plumbing
- [ ] Design paywall bottom sheet component
- [ ] Add analytics events (paywall viewed, converted, etc.)

### Phase 1 — Core Premium (3 weeks)
- [ ] Remove AI scan rate limit for premium users
- [ ] Add multiple weekly plans (DB migration: add `plan_order`, UI: plan selector)
- [ ] Build analytics screen (weekly trend charts, macro adherence)
- [ ] Build body measurements screen + history chart
- [ ] Build export screen (CSV generation)
- [ ] Deliver paywall at feature entry points

### Phase 2 — Engagement (2 weeks)
- [ ] Progress photos: camera + gallery picker + timeline view
- [ ] Firestore sync for premium users (backup + restore)
- [ ] Meal prep mode: multi-day meal planning view
- [ ] Grocery list generation from meal prep

### Phase 3 — Pro (post-launch, 3 weeks)
- [ ] AI meal planner prompt engineering + UI
- [ ] Recipe scanner (Gemini + structured output)
- [ ] Custom AI workout generator
- [ ] Form check video upload + analysis flow
- [ ] Coach share link + read-only view

---

## 5. Revenue Projections

Conservative estimate (1% conversion of 10k MAU):

| Tier | Price | Subscribers | Monthly Revenue |
|---|---|---|---|
| Premium | $4.99 | 100 | $499 |
| Pro | $9.99 | 20 | $199 |
| **Total** | | **120** | **$698** |

Reasonable target (3% conversion of 10k MAU):

| Tier | Price | Subscribers | Monthly Revenue |
|---|---|---|---|
| Premium | $4.99 | 300 | $1,497 |
| Pro | $9.99 | 50 | $499 |
| **Total** | | **350** | **$1,996** |

---

## 6. Retention Tactics

- **7-day free trial** for Premium — highest conversion rate for fitness apps
- **End-of-day notification**: "You used 3/5 AI scans today. Upgrade for unlimited."
- **Monthly digest**: "You tracked 22 meals and 8 workouts this month. Here's your trend."
- **Lapsed user**: After 7 days inactive, send "Your transformation score is waiting" with a link back
- **Referral**: 1 month free for referring a friend who converts

---

## 7. Code Changes Required

### New files needed
```
domain/payment/
├── PremiumFeature.kt          ← enum of gated features
├── PremiumManager.kt          ← interface
├── SubscriptionState.kt       ← data class (active, expiry, tier)
data/payment/
├── RevenueCatManager.kt       ← RevenueCat SDK integration
├── PremiumRepositoryImpl.kt
ui/components/
├── PaywallBottomSheet.kt      ← reusable paywall UI
├── PremiumBadge.kt            ← small crown/lock icon
ui/paywall/
├── PaywallScreen.kt           ← full-screen paywall
├── PaywallViewModel.kt
```

### Existing files to modify
| File | Change |
|---|---|
| `AiFoodScanViewModel.kt` | Check `isFeatureAvailable(UNLIMITED_SCANS)` before rate limit |
| `NutritionScreen.kt` | Show paywall on AI scan if over free limit |
| `WorkoutPlannerScreen.kt` | Gate multiple plans behind Premium |
| `DashboardScreen.kt` | Add analytics card (Premium); progress photos card (Premium) |
| `SoloFitApp.kt` nav graph | Add PaywallScreen route |
| `build.gradle.kts` | Add RevenueCat, Firebase Storage dependencies |

---

## 8. Risks & Mitigation

| Risk | Mitigation |
|---|---|
| Users reject subscription model | Keep generous free tier; trial before purchase |
| RevenueCat adds complexity | Single SDK handles Play Billing + receipts + expiry — worth it |
| Backend costs for Pro AI features | Use Gemini with rate limiting per user; cap at 50 requests/day |
| Low conversion rate | A/B test paywall design, trial length, pricing |
| App review rejection | Ensure no broken features behind paywall (free tier must be functional) |

---

## 9. Launch Checklist

- [ ] RevenueCat configured in Google Play Console
- [ ] All gated features degrade gracefully (show paywall, not crash)
- [ ] Offline subscription cache works (no network → show cached status)
- [ ] Analytics events fire on paywall view, conversion, feature access
- [ ] FREE TIER REMAINS FULLY FUNCTIONAL — no regression
- [ ] Test purchase flow, restore purchases, subscription expiry
- [ ] GDPR/CCPA compliance for analytics
- [ ] Localization: paywall copy strings in `strings.xml`
