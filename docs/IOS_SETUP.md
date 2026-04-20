# iOS setup

The `iosApp/iosApp.xcodeproj` is not included in the zip (Xcode project files are huge and machine-specific). Generate it on first boot:

## Option 1 — Xcode "New project from existing code" (5 min)

1. Open Xcode → File → New → Project → **App** (iOS, Swift, SwiftUI).
2. Product Name: `iosApp`. Save the new project **inside** the `iosApp/` folder (overwrite/merge the existing `iosApp/` directory).
3. Replace the auto-generated `iOSApp.swift` with the one already in this repo at `iosApp/iosApp/iOSApp.swift`.
4. In Xcode → Build Phases → New Run Script Phase — add this BEFORE "Compile Sources":
   ```sh
   cd "$SRCROOT/.."
   ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
   ```
5. Build Settings → Framework Search Paths: add `$(SRCROOT)/../composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`.
6. General → Frameworks, Libraries, and Embedded Content → add `ComposeApp.framework` (from the path above).
7. Build & run on a simulator.

## Option 2 — XcodeGen (recommended, one-command regen)

```bash
brew install xcodegen
cd iosApp
xcodegen generate       # uses project.yml (add one per XcodeGen docs)
```

Example `project.yml` to put in `iosApp/`:

```yaml
name: iosApp
options:
  bundleIdPrefix: za.co.etaxi
targets:
  iosApp:
    type: application
    platform: iOS
    deploymentTarget: "15.0"
    sources:
      - iosApp
    settings:
      base:
        PRODUCT_BUNDLE_IDENTIFIER: za.co.etaxi.ios
        FRAMEWORK_SEARCH_PATHS:
          - $(SRCROOT)/../composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)
    dependencies:
      - framework: $(SRCROOT)/../composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)/ComposeApp.framework
        embed: true
    preBuildScripts:
      - name: Compile Kotlin Framework
        script: |
          cd "$SRCROOT/.."
          ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

## Troubleshooting

- **"Invalid bitcode" on device**: Compose MP iOS doesn't support bitcode. Build Settings → Enable Bitcode → No.
- **Slow first build**: Normal. Kotlin/Native link step is heavy. Subsequent builds are fast.
- **Architecture mismatch on M-series Macs**: make sure the simulator target is `iosSimulatorArm64` (already configured in `composeApp/build.gradle.kts`).
