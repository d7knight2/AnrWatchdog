# demoapp

This is a demo Android app that demonstrates the usage of the anrwatchdog library and LeakCanary memory leak detection.

## Firebase Configuration

**IMPORTANT**: The `google-services.json` file in this directory is a **placeholder** and must be replaced with your actual Firebase project configuration before using Firebase App Distribution.

To set up Firebase:
1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create or select your Firebase project
3. Add an Android app with package name: `com.example.demoapp`
4. Download the real `google-services.json` file
5. Replace the placeholder file with your downloaded file

For detailed instructions, see [DISTRIBUTION.md](../DISTRIBUTION.md).

## How to Build and Run

1. Make sure you have a valid Android SDK and set up your `local.properties` file:

   ```
   echo "sdk.dir=/path/to/your/android/sdk" > local.properties
   ```

2. Build the demo app:

   ```
   ./gradlew :demoapp:assembleDebug
   ```

3. Run local unit tests for leak examples:

   ```
   ./gradlew :demoapp:testDebugUnitTest
   ```

4. Install the demo app on a connected device or emulator:

   ```
   ./gradlew :demoapp:installDebug
   ```

## Features

- Integrates [LeakCanary](https://square.github.io/leakcanary/) (latest alpha) for memory leak and growth detection.
- Demonstrates tab switching in `MainActivity` with three tabs.
- Adds an improved training UI in `TabFragment` with:
  - ANR simulation button
  - Rich memory leak scenario examples
  - Interactive scenario buttons that show symptom/prevention/check guidance
- Includes a reusable `LeakScenarioCatalog` with examples such as:
  - Static Activity references
  - Unregistered listeners
  - Long-running coroutines that capture views
  - Oversized bitmap caches
  - Fragment ViewBinding lifecycle leaks

## Documentation

- [MEMORY_LEAK_EXAMPLES.md](./MEMORY_LEAK_EXAMPLES.md) for practical leak patterns and remediation.
- [FLOATING_DEBUG_TOOL.md](./FLOATING_DEBUG_TOOL.md) for details on debug overlay functionality.
- [FLOATING_DEBUG_TOOL_VISUAL_GUIDE.md](./FLOATING_DEBUG_TOOL_VISUAL_GUIDE.md) for UI details.

## Example Usage

The demo app initializes ANRWatchdog and LeakCanary in its `Application` class. See `LeakWatcherApp.kt` for details.

---
