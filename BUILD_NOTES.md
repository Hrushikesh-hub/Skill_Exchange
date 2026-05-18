# Build Notes

## Environment
- Android Studio: Hedgehog (2023.1.1) or newer
- Gradle: 8.x
- Kotlin: 1.9.x
- Min SDK: 26 (Android 8.0)
- Target SDK: 34

## Build Commands

`ash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
`

## Known Issues
- Firebase google-services.json must be added manually (not included for security)
- First launch seeds demo data automatically
