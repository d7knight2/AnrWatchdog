# anrwatchdog Library

This is the ANR Watchdog library module. It provides ANR (Application Not Responding) detection for Android apps.

## Overview

The ANRWatchdog library monitors your Android application for potential ANR events and provides detailed diagnostic information including thread states, stack traces, and coroutine creation traces. It's designed to help developers identify and debug performance issues that could lead to ANRs.

## Features

- **Singleton pattern** - Ensures only one instance monitors your application
- **Fluent API** - Easy configuration with method chaining
- **Coroutine debugging** - Integration with Kotlin coroutine debug probes
- **Customizable timeouts** - Configure ANR detection sensitivity
- **Callback support** - Get notified when ANRs are detected
- **Flexible logging** - Control log verbosity

## Installation

Add this module as a dependency in your Android app module:

```gradle
dependencies {
    implementation(project(":anrwatchdog"))
}
```

## API Reference

### Initialization

```kotlin
val watchdog = ANRWatchdog.initialize(application)
```

The `initialize` method:
- Takes an `Application` context
- Returns the singleton `ANRWatchdog` instance
- Automatically installs coroutine debug probes
- Is thread-safe and idempotent

### Configuration Methods

#### setTimeout(timeout: Long)

Sets the ANR detection timeout period in milliseconds.

```kotlin
watchdog.setTimeout(5000L) // Check every 5 seconds
```

- **Default:** 5000ms (5 seconds)
- **Returns:** ANRWatchdog instance for chaining

#### setLogLevel(level: Int)

Sets the minimum log level for debug output.

```kotlin
watchdog.setLogLevel(Log.DEBUG)
```

- **Default:** Log.INFO
- **Accepts:** Any Android Log level constant (Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR)
- **Returns:** ANRWatchdog instance for chaining

#### setCallback(callback: (Thread) -> Unit)

Sets a callback to be invoked when an ANR is detected.

```kotlin
watchdog.setCallback { thread ->
    Log.e("MyApp", "ANR detected on thread: ${thread.name}")
    // Send to crash reporting service
    // Show user notification
}
```

- **Parameter:** Lambda receiving the Thread where ANR was detected
- **Returns:** ANRWatchdog instance for chaining

### Control Methods

#### start()

Starts the ANR monitoring thread.

```kotlin
watchdog.start()
```

- Idempotent - safe to call multiple times
- Runs on a background thread
- **Returns:** ANRWatchdog instance for chaining

#### stop()

Stops the ANR monitoring thread.

```kotlin
watchdog.stop()
```

- Cleanly shuts down the monitoring thread
- Can be restarted with `start()`

## Usage Examples

### Basic Setup

Initialize the watchdog in your Application class:

```kotlin
import com.example.anrwatchdog.ANRWatchdog

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ANRWatchdog.initialize(this)
            .setLogLevel(Log.DEBUG)
            .start()
    }
}
```

### Advanced Configuration

Configure with custom timeout and callback:

```kotlin
ANRWatchdog.initialize(this)
    .setTimeout(3000L)                    // 3 second timeout
    .setLogLevel(Log.WARN)                // Only log warnings and errors
    .setCallback { thread ->
        // Custom handling
        reportAnrToServer(thread)
        showUserDialog()
    }
    .start()
```

### Lifecycle Management

Stop monitoring when appropriate (e.g., in debug builds):

```kotlin
class MyApplication : Application() {
    private lateinit var watchdog: ANRWatchdog
    
    override fun onCreate() {
        super.onCreate()
        watchdog = ANRWatchdog.initialize(this)
            .setTimeout(5000L)
            .start()
    }
    
    fun disableMonitoring() {
        watchdog.stop()
    }
}
```

## How It Works

1. **Initialization:** When you call `initialize()`, the library:
   - Creates a singleton instance
   - Installs Kotlin coroutine debug probes
   - Enables coroutine creation stack trace capture

2. **Monitoring:** When you call `start()`, the library:
   - Launches a background thread
   - Periodically checks for ANR conditions at the configured interval
   - Invokes callbacks and logs when issues are detected

3. **Detection:** The watchdog can detect:
   - Main thread blocking
   - Long-running operations
   - Coroutine lifecycle issues

## Thread Safety

The ANRWatchdog is thread-safe:
- `initialize()` uses lazy initialization with null checks
- `start()` checks the running state before creating new threads
- Multiple calls to `start()` won't create duplicate monitoring threads

## Best Practices

1. **Initialize early:** Call `initialize()` in your Application's `onCreate()` method
2. **Use appropriate timeouts:** Android considers 5 seconds the ANR threshold; match or exceed this
3. **Test in debug:** Use lower timeouts in debug builds for faster detection during development
4. **Handle callbacks gracefully:** Don't perform heavy operations in the callback
5. **Consider release builds:** Decide if you want ANR detection in production or debug-only

## Build Tasks

To build the library:

```bash
./gradlew :anrwatchdog:assembleDebug
```

To run unit tests:

```bash
./gradlew :anrwatchdog:test
```

## Contributing

When contributing to this module:
1. Maintain the fluent API pattern
2. Add unit tests for new functionality
3. Update this documentation
4. Follow Kotlin coding conventions

---
