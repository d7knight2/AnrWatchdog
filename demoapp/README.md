# demoapp

This is a demo Android app that demonstrates the usage of the anrwatchdog library.

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

## Example Usage

The demo app initializes ANRWatchdog in its `Application` class. See `MyApplication.kt` for details.

---
