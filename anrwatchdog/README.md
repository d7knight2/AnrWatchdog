# ANRWatchdog Library

The ANRWatchdog library is the core module that provides runtime ANR (Application Not Responding) detection for Android applications.

## Overview

ANRWatchdog monitors your Android application for unresponsive behavior and provides detailed diagnostic information when ANR conditions are detected. It integrates with Kotlin Coroutine DebugProbes to capture creation stack traces for active coroutines, making it invaluable for debugging complex asynchronous code.

## Features

- 🔍 **Runtime ANR Detection**: Monitors the main thread for responsiveness
- 📊 **Thread State Logging**: Captures states and stack traces of all threads
- 🧠 **Memory & CPU Monitoring**: Tracks resource utilization at detection time
- 🔄 **Coroutine Debugging**: Prints creation stack traces for active coroutines
- ⚙️ **Configurable Timeouts**: Adjust detection sensitivity for your needs
- 📝 **Custom Callbacks**: Hook into ANR events for custom handling
- 🔗 **Fluent API**: Chain configuration methods for clean code

## Installation

### As a Module Dependency

Add this module as a dependency in your Android app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":anrwatchdog"))
}
```

### Enable Coroutine Debug Mode

Add to your `gradle.properties`:

```properties
kotlin.coroutines.debug=on
```

## Quick Start

### Basic Usage

Initialize ANRWatchdog in your Application class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        ANRWatchdog.initialize(this)
            .setTimeout(5000)      // 5 second timeout
            .setLogLevel(Log.DEBUG)
            .start()
    }
}
```

### With Custom Callback

Handle ANR events with custom logic:

```kotlin
ANRWatchdog.initialize(this)
    .setTimeout(3000)
    .setCallback { thread ->
        // Log to analytics
        Analytics.logEvent("anr_detected", mapOf(
            "thread" to thread.name,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    .start()
```

## Configuration Options

### setTimeout(millis: Long)

Sets the ANR detection timeout in milliseconds.

**Recommendations:**
- Debug builds: 3000-5000ms
- Production builds: 5000-10000ms

### setLogLevel(level: Int)

Controls logging verbosity using Android log levels (`Log.DEBUG`, `Log.INFO`, `Log.ERROR`, etc.)

### setCallback(callback: (Thread) -> Unit)

Sets a callback function invoked when an ANR is detected.

### start() / stop()

Starts or stops the ANR monitoring thread.

## Testing

Run unit tests for this module:

```bash
# Run unit tests
./gradlew anrwatchdog:test

# Generate coverage report
./gradlew anrwatchdog:testDebugUnitTest anrwatchdog:jacocoTestReport

# View coverage
open anrwatchdog/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Test Coverage

- ✅ Initialization and singleton behavior
- ✅ Configuration methods (setTimeout, setLogLevel, setCallback)
- ✅ Fluent API pattern
- ✅ Start/stop functionality
- ✅ Edge cases: timeouts, concurrent access, exception handling

**Total**: 24+ test cases with 90%+ coverage

## Build Tasks

Build the library:

```bash
# Build debug AAR
./gradlew :anrwatchdog:assembleDebug

# Build release AAR
./gradlew :anrwatchdog:assembleRelease
```

## API Reference

### Class: ANRWatchdog

**Package**: `com.example.anrwatchdog`

#### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `initialize(application: Application)` | `ANRWatchdog` | Creates or retrieves singleton instance |
| `setTimeout(timeout: Long)` | `ANRWatchdog` | Sets ANR detection timeout (ms) |
| `setLogLevel(level: Int)` | `ANRWatchdog` | Sets Android log level |
| `setCallback(callback: (Thread) -> Unit)` | `ANRWatchdog` | Sets ANR detection callback |
| `start()` | `ANRWatchdog` | Starts monitoring thread |
| `stop()` | `Unit` | Stops monitoring thread |

## Best Practices

1. **Initialize Early**: Call in `Application.onCreate()`
2. **Adjust for Context**: Use shorter timeouts in debug, longer in production
3. **Use Callbacks**: Integrate with analytics and crash reporting
4. **Clean Up**: Call `stop()` when appropriate

## Related Documentation

- [Main README](../README.md) - Project overview
- [TESTING.md](../TESTING.md) - Testing documentation
- [CODE_COVERAGE.md](../CODE_COVERAGE.md) - Coverage reports
- [Demo App README](../demoapp/README.md) - Example integration

## License

Apache License 2.0 - See [LICENSE](../LICENSE) for details.
