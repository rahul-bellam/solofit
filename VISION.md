# SoloFit Friends — Architecture & Vision

## Status
**V1 implemented**: On-device identity, key generation, local friend/groups/permissions/events database,
QR code generation, Sol awareness. **No server yet.**

## Core Philosophy

Friends are small trusted circles — never strangers, never public, never discoverable.
This feature is accountability, not a social network.

## Architecture

### Data Flow (Engine Architecture)

```
Room (friends, events, permissions)
         │
         ▼
  FriendRepository (exposes facts only)
         │
         ▼
  FriendIntelligenceEngine
  (accountability scores, encouragement,
   inactivity detection, shared habits,
   relationship strength, group momentum)
         │
         ▼
  CommunityState (into UserTwin)
         │
         ├──▶ Sol (briefing, circle health)
         ├──▶ Dashboard (circle card)
         ├──▶ Notifications (encouragement)
         └──▶ Identity Engine (mutual momentum)
```

**Rule enforced**: Repositories expose raw facts. Engines create meaning.
**Never** inject a Repository directly into Sol or UI.

### Current Flow (V1)

```
┌─────────────────────────────────────┐
│            Device A                  │
│  ┌───────────────────────────────┐  │
│  │    Room (solofit.db)          │  │
│  │  ┌────────────┐               │  │
│  │  │solo_identity│ ← Solo ID   │  │
│  │  │friends      │ ← Friend    │  │
│  │  │permissions  │ graph       │  │
│  │  │groups       │              │  │
│  │  │events       │              │  │
│  │  └────────────┘               │  │
│  │                               │  │
│  │  AndroidKeyStore              │  │
│  │  ┌──────────────────┐        │  │
│  │  │Ed25519 private key│        │  │
│  │  └──────────────────┘        │  │
│  └───────────────────────────────┘  │
│              │  ▲                   │
│              │  │ Events (encrypted)│
│              ▼  │                   │
│  ┌───────────────────────────────┐  │
│  │    Backend (future)           │  │
│  │  - Friend graph relay         │  │
│  │  - Event relay                │  │
│  │  - Notification routing       │  │
│  │  - Public key lookup          │  │
│  │  - NEVER stores health data   │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

## Implemented (V1)

### Identity
- **Solo ID generation**: `SF-XXXX-XXXX-XXXX` format via `CryptoUtil.generateSoloId()`
- **Ed25519 key pair**: Generated via `KeyPairGenerator("EC", "AndroidKeyStore")` with secp256r1 on `API 28+`
- **Private key**: Stored in `AndroidKeyStore` (never leaves device)
- **Public key**: Stored in `solo_identity` Room table alongside Solo ID and display name
- **Key generation**: Happens at onboarding in `OnboardingViewModel.finish()` via `SoloIdentityRepository.createIfNeeded()`

### Data Layer
| Entity | Table | Purpose |
|--------|-------|---------|
| `SoloIdentityEntity` | `solo_identity` | Singleton row for the user's own identity |
| `FriendEntity` | `friends` | Friend list with Solo ID, public key, status (pending/accepted) |
| `FriendPermissionEntity` | `friend_permissions` | Per-category (workout, nutrition, etc.) sharing level |
| `FriendGroupEntity` | `friend_groups` | Accountability groups |
| `GroupMemberEntity` | `group_members` | Group ↔ friend mapping |
| `FriendEventEntity` | `friend_events` | Event log (completed workout, meditation, etc.) |

Database version: **14** with `MIGRATION_13_14` creating all tables and indexes.

### Crypto
- `CryptoUtil` (singleton, injectable)
- Key generation, signing (`SHA256withECDSA`), verification
- Nonce generation (32 bytes secure random)
- SHA-256 hashing
- Base64 encode/decode (companion)
- QR content format: `SOLOFIT:{soloId}:{displayName}:{base64PublicKey}`

### UI
| Screen | Route | Purpose |
|--------|-------|---------|
| `FriendsScreen` | `friends` | List of accepted friends + pending requests + show Solo ID |
| `AddFriendScreen` | `add_friend` | Enter Solo ID manually or show QR code |
| `FriendDetailScreen` | `friend_detail/{id}` | Permissions per category (segmented button: Private/Share/Group) + Remove |
| `GroupsScreen` | `groups` | List groups, create/delete groups |

Entry point: **Settings > My Circle**

### FriendIntelligenceEngine
- **Pure engine** in `sol/FriendIntelligenceEngine.kt` — no repository, no side effects
- Computes per-friend `accountabilityScore` (0–100) based on event recency, frequency, shared activity
- Detects `needsEncouragement` (inactive >3 days + score < 40)
- Produces `CommunityState` → merged into `UserTwin.community`

### Relationship Types
| Type | Sol Behavior |
|------|-------------|
| `PARTNER` | "Both of you completed today's walk" |
| `COWORKER` | "Your office circle remained consistent this week" |
| `FAMILY` | "Your family group has checked in today" |
| `ACCOUNTABILITY_PARTNER` | "Your accountability partner was active" |
| `WORKOUT_BUDDY` | "Your workout buddy hit the gym today" |
| `CLOSE_FRIEND` | Generic circle awareness |
| `COACH` | "Coach" framing |
| `SUPPORT` | Supportive framing |

### Sol Integration
- `CommunityState` in `UserTwin.community` → consumed by Sol UI
- `friendActivitySummary`, `circleHealth`, `mutualMomentum`, `circleMomentumLabel`, `activeCircleCount`, `encouragementNeeded` in `SolUiState`
- Populated in `SolViewModel.refresh()` via `FriendIntelligenceEngine.compute()`
- Surfaces: "Your circle has been consistently active this week" / "3 active in your circle"

### Circle Health (Upcoming Iconic Feature)
Instead of a "friends list", the UI should eventually display **Circle Health**:
- "Your circle has been active this week."
- "Three people checked in today."
- "Everyone completed a walk yesterday."
- "This has been the strongest week for your group."

Mutual Momentum: when you and your circle are active together, Sol notices and feeds it back.

## Future (Not Yet Implemented)

### GroupEngine + CommunityEngine
Engines that operate at higher abstraction levels:
- **GroupEngine**: group-level momentum, shared consistency, encouragement routing
- **CommunityEngine**: aggregates all groups + individual relationships into single `CommunityState`

These are pure functions on top of `FriendIntelligenceEngine` — no repository access.

### Navigation Evolution
V1 puts "My Circle" in Settings. Eventually:
- Bottom nav: **Home → Workout → Nutrition → Circle → Profile**
- Dashboard has a **Circle Health card** instead of a settings entry
- Sol surfaces circle awareness without needing to navigate

### Circle Health (UI)
Full Circle Health screen showing:
- Active friends today/this week
- Encouragement suggestions
- Strongest partnership highlight
- Mutual momentum indicator
- "Your circle has been quietly building momentum" — cohesive, warm, never competitive

### Server Infrastructure
**Requires**: New backend service (Kotlin/Ktor, Go, or TypeScript)

| Component | Description |
|-----------|-------------|
| REST API | ~15 endpoints: auth, friend requests, event relay, group management |
| Database | PostgreSQL: users, friend_edges, groups, group_members, events (encrypted payloads) |
| Authentication | JWT-based session tokens |
| Push notifications | FCM integration for friend events |
| Rate limiting | Prevent abuse |
| Public key directory | SolO ID → public key lookup for verification |

### Cryptographic Verification
- **Challenge-response**: When adding a friend via QR, each device signs a random nonce
- **Replay protection**: Nonces are single-use, timestamp-bounded
- **Signature validation**: Both devices verify before accepting

### BLE / Wi-Fi Discovery (V2+)
- **BLE**: Requires `BLUETOOTH_SCAN` permission (API 31+), foreground service
- **Wi-Fi multicast**: Requires `CHANGE_WIFI_MULTICAST_STATE`, unreliable on captive networks
- **Recommendation**: Skip for V1, QR code + manual entry only

### Event Sharing
- Events stored locally in `friend_events` table
- When server exists: events pushed to server → relayed to connected friends
- Payloads encrypted end-to-end using recipient's public key
- Minimum data principle: only what's needed for the event type

### Device Migration
- Encrypted export of Room DB + AndroidKeyStore-wrapped private key
- Import on new device re-creates identity and friend graph
- QR code on old device scanned by new device to re-establish

### Groups V2
- Shared timeline (friend activity feed)
- Lightweight reactions (👏 💪 ❤️ 👍)
- No comments in V1
- Sol integration per-group

### No Leaderboards
Deliberately excluded: XP, ranks, global scores, weekly winners, competition.
Everything encourages consistency, not comparison.

### Data Flow (Future)
```
User completes workout
  → FriendEventEntity inserted locally
  → Event encrypted with friend's public key
  → Pushed to server
  → Server relays to connected friends
  → Friend's device receives, decrypts, stores locally
  → Sol sees: "Rahul completed today's workout"
```

## Key Design Decisions

1. **Phone is source of truth**, not server — server is a relay only
2. **Server NEVER stores**: UserTwin, nutrition, recovery, journal, workout, stress, Sol memory, AI insights
3. **Ed25519 signatures** prove identity without trusting the network
4. **Permissions per category** — nothing shared automatically, user controls everything
5. **Events not databases** — share minimal event payloads, not raw health data
6. **No chat, no comments, no social feed** — reactions only, accountability focus
7. **Sol stays calm and supportive** — never compares, never ranks, never shames
8. **Repositories expose facts, engines create meaning** — FriendRepository is never injected into Sol or UI
9. **UserTwin contains CommunityState** — all intelligence flows through a single source of truth
10. **Relationship types change Sol's framing** — Partner, Coworker, Family each get distinct Sol voice
11. **Groups are not chat rooms** — shared timeline of events + reactions, not message threads
12. **Mutual Momentum > Competition** — the system rewards collective consistency, not individual outperformance
