# eTaxi — KMP app (rider + driver)

A role-based **Kotlin Multiplatform** taxi-hailing app for South Africa. One codebase, two roles (rider and driver), targeting **Android + iOS** with a shared Compose UI.

> **Stack:** Kotlin 2.1 · Compose Multiplatform 1.7 · Voyager nav · Koin DI · Material 3 · Coroutines · Clean architecture

---

## Screens

Ten screens, all hardcoded to mock data so the UI runs end-to-end before the backend is integrated.

**Shared:** Splash · Role Selection · Login · Register · OTP · Profile · Notifications
**Rider:** Home (map + bottom sheet) · Trip in progress (status banner + driver card) · Trip history
**Driver:** Home (online toggle + incoming request modal) · Active trip (state machine) · Earnings · Trips

---

## Architecture

```
composeApp/
├── src/commonMain/kotlin/za/co/etaxi/app/
│   ├── App.kt                              # root composable
│   ├── core/
│   │   ├── theme/                          # Color · Typography · Shape · Theme
│   │   ├── components/                     # Buttons · TextFields · Loaders · MockMapCanvas
│   │   ├── navigation/                     # HomeScreen router (routes by role)
│   │   ├── di/                             # Koin AppModule
│   │   └── util/                           # Format (ZAR)
│   ├── domain/
│   │   ├── model/                          # User · Trip · Place · Notification
│   │   └── repository/                     # Repository interfaces
│   ├── data/mock/                          # Mock repositories + Joburg sample data
│   └── feature/
│       ├── splash/
│       ├── auth/ (roleSelection, login, register, otp)
│       ├── rider/ (home, tripInProgress, history)
│       ├── driver/ (home, activeTrip, earnings, history)
│       ├── profile/
│       └── notifications/
├── src/androidMain/kotlin/za/co/etaxi/app/
│   ├── EtaxiApp.kt                         # Application class, Koin init
│   └── MainActivity.kt
└── src/iosMain/kotlin/za/co/etaxi/app/
    └── MainViewController.kt               # Swift entrypoint
```

**Clean architecture** enforces the swap: the `domain/repository` interfaces are hit from screens; `data/mock` is the only implementation today. When the backend is ready, we add a new `data/remote` module with Ktor and register it in Koin — zero screen changes.

---

## Theme

The palette is lifted from the reference slide (`POWER #C3D809` + `SLIDES #222022`) and calibrated into an 18-tone ramp:

- **Primary lime** (`Power`) for CTAs, accents, the online/active states
- **Near-black** (`Slides`, `SlidesDeep`, `SlidesRaised`, `SlidesEdge`, `SlidesSoft`) for background/surface/elevated layers
- **Ink** scale (Ink / InkMuted / InkFaint / InkOnPower) for text that always meets contrast
- Semantic: `Success` (green), `Warning` (amber), `Danger` (red) + container dims

Typography is display-heavy (Black/ExtraBold on large sizes) so headings feel like signage — matches the brand attitude.

Animations: spring-based button press, `AnimatedContent` fade-through on tab switches, infinite-transition pulses on the "searching" state and driver online indicator, slide-up sheets for incoming requests.

---

## Getting started

### Prerequisites

- **macOS** (Xcode is needed for iOS)
- **JDK 17+**
- **Android Studio** Koala or newer with the Kotlin Multiplatform plugin
- **Xcode 15+**
- **gradle** CLI for the bootstrap (`brew install gradle`)

### First-time setup

```bash
./bootstrap.sh
```

This generates the Gradle wrapper (`./gradlew`). From here:

### Android

```bash
./gradlew :composeApp:installDebug
```

…or open the folder in Android Studio and press ▶.

### iOS

Xcode project isn't included in the zip (too machine-specific). Follow `docs/IOS_SETUP.md` — two options, one takes ~5 minutes.

Once generated:

```bash
open iosApp/iosApp.xcodeproj
# then ▶ in Xcode against an iOS simulator
```

---

## Development flows (mock mode)

All flows work end-to-end without a backend.

### Rider flow

1. Splash → Role selection → pick "Rider"
2. Login or Register (any input ≥ 4 chars works)
3. OTP (any 4-6 digit numeric code)
4. **Home** — tap a saved place → estimate appears → pick vehicle class → request trip
5. **Trip in progress** — searching state pulses, then "Simulate driver accepted" advances through the state machine (ACCEPTED → ARRIVING → ARRIVED → IN_PROGRESS → COMPLETED)

### Driver flow

1. Splash → Role selection → "Driver"
2. Login / Register / OTP (same as rider)
3. **Drive** tab — big "Go online" button → 2.5s later a mock request slides up
4. Accept → pushed to **Active trip** screen → advance through states with the big primary CTA
5. **Earnings** tab shows today/week/month cards + wallet balance

---

## Integrating the backend

Mock repos in `data/mock/` implement the same interfaces as the real repos will. To plug in the existing `etaxi-backend`:

1. Add `data/remote/` with Ktor client, auth interceptor (reads JWT from a `TokenStorage`)
2. Implement each interface: `RemoteSessionRepository`, `RemoteRiderRepository`, etc.
3. Swap one line in `core/di/AppModule.kt`:
   ```kotlin
   single<SessionRepository> { RemoteSessionRepository(get()) }  // was MockSessionRepository()
   ```

The screens never reference the mock classes directly — only the interfaces — so no UI changes needed.

---

## What's NOT built yet

- Real map (Google Maps / MapKit) — `MockMapCanvas` draws a stylized placeholder. Swap later via `expect/actual`.
- Persistent storage (DataStore) — role + token are in-memory only.
- Networking layer (Ktor).
- Localization (en-ZA is the only string bundle).
- Unit tests.

---

## Conventions

- `expect/actual` kept to the minimum (only needed once we add platform storage + maps).
- Every screen is a Voyager `Screen` — navigation is type-safe and supports deep transitions.
- Repositories expose `Flow<T>` for reactive state, `suspend fun` for commands.
- Colors only reference `EtaxiColors.*` — don't hardcode hex in screens.
- Currencies are cents (Int) in the domain; `formatZar(cents)` renders `R1 234.50`.

---

## License

Internal project. Lime and black forever.
# eTaxi_kmp
