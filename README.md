# eTaxi KMP — Patch G: Wire real backend + unified register form

## What this patch does

1. **Fixes Koin wiring** — `initKoin()` now loads `networkModule` + `repositoryModule`. When `USE_REMOTE_API = true`, SessionRepository and DriverRepository resolve to the real backend (http://192.168.0.181:5001/api/v1). That's why no requests were hitting your backend — mocks were still bound.

2. **Adds EtaxiApp.kt** — Android Application class. Registers the Context (needed for TokenManager's encrypted storage) and calls `initKoin`. This file likely doesn't exist in your project yet.

3. **Unified RegisterScreen** — Rider/Driver toggle at the top of the form. No more separate role-chooser screen. Normalizes phone to +27 format before submit, calls `session.register(...)` → backend → navigates to OTP screen on success.

## Files in this patch (3)

- `composeApp/src/commonMain/kotlin/za/co/etaxi/app/core/di/AppModule.kt`  (replaces existing)
- `composeApp/src/commonMain/kotlin/za/co/etaxi/app/feature/auth/register/RegisterScreen.kt`  (replaces existing)
- `composeApp/src/androidMain/kotlin/za/co/etaxi/app/EtaxiApp.kt`  (NEW file)

## Apply

```bash
cd "/Users/xolanindebele/Documents/Production Ready/etaxi/etaxi-kmp"
unzip -o ~/Downloads/etaxi-kmp-patch-G.zip
```

## REQUIRED manual step — AndroidManifest.xml

Open `composeApp/src/androidMain/AndroidManifest.xml` and make sure the
`<application>` tag references our new class AND has INTERNET permission
AND has the network security config.

Near the top of `<manifest>`, inside but NOT inside `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

On the `<application>` tag, add these attributes:

```xml
<application
    android:name=".EtaxiApp"
    android:usesCleartextTraffic="true"
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

If you already applied the F2 patch's manifest instructions, only the
`android:name=".EtaxiApp"` attribute is new.

**If you'd rather I send you a complete replacement AndroidManifest, paste me your current one and I'll ship it next turn. Without `android:name=".EtaxiApp"` the app WILL NOT call initKoin at all** and nothing will hit the backend.

## REQUIRED manual step — remove RoleSelectionScreen from nav

I don't know where in your navigation graph RoleSelectionScreen is
referenced. Most likely in your LoginScreen or a SplashScreen. Look for:

```kotlin
navigator.push(RoleSelectionScreen)
// or
navigator.replaceAll(RoleSelectionScreen)
```

Replace those references with `RegisterScreen` directly. The new
RegisterScreen has the role toggle built in.

You can also delete these two screen files entirely if nothing else
references them:
```bash
rm composeApp/src/commonMain/kotlin/za/co/etaxi/app/feature/auth/roleSelection/RoleSelectionScreen.kt
rm composeApp/src/commonMain/kotlin/za/co/etaxi/app/feature/onboarding/ServiceChooserScreen.kt
```

If you prefer to keep them for now (they may be wired elsewhere),
just change the `navigator.push(RoleSelectionScreen)` calls to
`navigator.push(RegisterScreen)` and leave the files alone.

## Build + run

```bash
./gradlew :composeApp:assembleDebug
```

## Test end-to-end

1. Start backend on Mac:
   ```bash
   cd /Users/xolanindebele/Documents/Production\ Ready/etaxi/etaxi-backend
   npm run dev
   ```

2. Run app on your Android device (same Wi-Fi as Mac).

3. On Register screen: pick **Rider**, fill in test details, tap Continue.

4. **Watch the Mac terminal.** You should see:
   ```
   POST /api/v1/auth/register 201
   [OTP] +27821112222 -> 834217
   ```

5. Enter that 6-digit code on OTP screen. App should log you in.

If no backend log appears after tapping Continue:
- Check that `android:name=".EtaxiApp"` is in AndroidManifest
- Check `USE_REMOTE_API = true` in AppModule.kt
- Check that device and Mac are on the same Wi-Fi
- Open logcat and filter by "EtaxiApi" — you'll see the HTTP attempts

## Known limits in this patch

- Role-based routing AFTER login/OTP (rider home vs driver home) isn't
  changed here. Depending on what your current `HomeScreen` does, it
  may or may not already route by role. If not, that's a tiny
  follow-up patch once we confirm auth works end-to-end.
- Session restore on app launch — existing behavior preserved. Next
  patch adds `restoreSession()` call on splash.
