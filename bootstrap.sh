#!/usr/bin/env bash
# Bootstrap eTaxi KMP project on a fresh machine.
# Generates the Gradle wrapper (too big to ship in a zip) and the iOS Xcode project.
# Run once after unzipping.
set -euo pipefail

echo "🚀 Bootstrapping eTaxi KMP…"

if ! command -v gradle >/dev/null 2>&1; then
  echo "❌ 'gradle' is not on PATH."
  echo "   Install with: brew install gradle"
  exit 1
fi

# ---------- Gradle wrapper ----------
echo "📦 Generating Gradle wrapper…"
gradle wrapper --gradle-version 8.11.1 --distribution-type all

echo "✅ Gradle wrapper ready: ./gradlew"
echo ""
echo "➡  Android: open this folder in Android Studio (Giraffe+ / Koala / Ladybug)."
echo "   It will detect the KMP project and sync."
echo ""
echo "➡  iOS: open iosApp/iosApp.xcodeproj in Xcode 15+."
echo "   If the xcodeproj doesn't exist yet, see docs/IOS_SETUP.md."
echo ""
echo "Or just run:  ./gradlew :composeApp:installDebug   (Android emulator must be running)"
