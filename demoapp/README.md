# demoapp

This is a demo Android app that demonstrates the usage of the anrwatchdog library and LeakCanary memory leak detection.

## How to Build and Run

1. Make sure you have a valid Android SDK and set up your `local.properties` file:
   
   ```
   echo "sdk.dir=/path/to/your/android/sdk" > local.properties
   ```

2. Build the demo app:
   
   ```
   ./gradlew :demoapp:assembleDebug
   ```

3. Install the demo app on a connected device or emulator:
   
   ```
   ./gradlew :demoapp:installDebug
   ```

## Features

- Integrates [LeakCanary](https://square.github.io/leakcanary/) (latest alpha) for memory leak and growth detection.
- Demonstrates tab switching in `MainActivity` with three tabs.
- Each tab switch replaces a fragment (`TabFragment`) that runs an endless `ObjectAnimator`.
- LeakCanary will detect leaks if fragments or animators are not properly cleaned up.

## Example Usage

The demo app initializes ANRWatchdog and LeakCanary in its `Application` class. See `LeakWatcherApp.kt` for details.

---
