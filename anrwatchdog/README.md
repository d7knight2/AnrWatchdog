# anrwatchdog Library

This is the ANR Watchdog library module. It provides ANR (Application Not Responding) detection for Android apps with comprehensive debugging capabilities.

## Overview

ANRWatchdog is a lightweight, easy-to-integrate library that monitors your Android application for ANR conditions. It provides:

- **Real-time ANR Detection**: Monitors the main thread for blocking operations
- **Coroutine Debug Support**: Leverages Kotlin coroutine debug probes for enhanced debugging
- **Customizable Callbacks**: Respond to ANR events with custom logic
- **Flexible Configuration**: Adjust timeout thresholds and logging levels
- **Fluent API**: Chain configuration methods for clean, readable code

## Installation

### Local Module

Add this module as a dependency in your Android app module:

```gradle
dependencies {
    implementation(project(":anrwatchdog"))
}
```

### Future: Maven/JCenter Distribution

```gradle
dependencies {
    implementation 'com.example:anr-watchdog:1.0.0'
}
```

## Quick Start

### Basic Setup

Initialize the watchdog in your Application class:

```kotlin
import com.example.anrwatchdog.ANRWatchdog
import android.app.Application
import android.util.Log

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

```kotlin
ANRWatchdog.initialize(this)
    .setTimeout(5000L)              // Set ANR detection timeout to 5 seconds
    .setLogLevel(Log.DEBUG)         // Enable debug logging
    .setCallback { thread ->
        // Handle ANR detection
        Log.e("MyApp", "ANR detected on thread: ${thread.name}")
        // Send to crash reporting service
        reportAnrToCrashlytics(thread)
    }
    .start()
```

## API Reference

### Initialization

```kotlin
ANRWatchdog.initialize(application: Application): ANRWatchdog
```

Initializes the ANRWatchdog singleton instance. Must be called before any other methods.

**Parameters:**
- `application` - Your Android Application instance

**Returns:** The ANRWatchdog singleton instance

**Note:** Automatically enables coroutine debug probes for enhanced debugging.

### Configuration Methods

All configuration methods return the ANRWatchdog instance for method chaining.

#### setTimeout

```kotlin
setTimeout(timeout: Long): ANRWatchdog
```

Sets the ANR detection timeout duration in milliseconds.

**Parameters:**
- `timeout` - Duration in milliseconds (default: 5000ms)

**Recommendations:**
- **5000ms (5 seconds)**: Default, matches Android system ANR threshold
- **3000ms (3 seconds)**: More aggressive, catches brief ANRs
- **10000ms (10 seconds)**: More tolerant, reduces false positives

#### setLogLevel

```kotlin
setLogLevel(level: Int): ANRWatchdog
```

Sets the logging verbosity level using Android Log constants.

**Parameters:**
- `level` - Log level (Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR)

**Default:** Log.INFO

#### setCallback

```kotlin
setCallback(callback: (Thread) -> Unit): ANRWatchdog
```

Sets a callback function invoked when an ANR is detected.

**Parameters:**
- `callback` - Lambda function receiving the Thread where ANR was detected

**Use Cases:**
- Send ANR reports to crash reporting services (Crashlytics, Sentry)
- Display user-facing notifications
- Log additional diagnostic information
- Trigger recovery mechanisms

### Lifecycle Methods

#### start

```kotlin
start(): ANRWatchdog
```

Starts ANR monitoring in a background thread.

**Returns:** The ANRWatchdog instance for method chaining

**Important:** 
- Safe to call multiple times (subsequent calls are ignored)
- Always call `stop()` when monitoring is no longer needed

#### stop

```kotlin
stop()
```

Stops ANR monitoring and cleans up resources.

**Important:**
- Safe to call multiple times
- Should be called to prevent resource leaks
- Can restart monitoring by calling `start()` again

## Complete Example

```kotlin
class MyApplication : Application() {
    private lateinit var anrWatchdog: ANRWatchdog
    
    override fun onCreate() {
        super.onCreate()
        
        anrWatchdog = ANRWatchdog.initialize(this)
            .setTimeout(5000L)
            .setLogLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .setCallback { thread ->
                // Log ANR with detailed information
                Log.e("ANR", "ANR detected on thread: ${thread.name}")
                Log.e("ANR", "Thread state: ${thread.state}")
                Log.e("ANR", "Stack trace: ${thread.stackTrace.joinToString("\n")}")
                
                // Send to analytics/crash reporting
                if (!BuildConfig.DEBUG) {
                    FirebaseCrashlytics.getInstance().recordException(
                        ANRException("ANR detected on ${thread.name}")
                    )
                }
            }
            .start()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        anrWatchdog.stop()
    }
}
```

## Coroutine Debugging

To enable coroutine debugging features, add the following to your app's `gradle.properties`:

```properties
kotlin.coroutines.debug=on
```

This enables:
- Creation stack traces for active coroutines
- Enhanced coroutine state information
- Better debugging of coroutine-related ANRs

## Best Practices

1. **Initialize Early**: Initialize in your Application.onCreate() before any other initialization
2. **Use Appropriate Timeout**: Match your timeout to your app's responsiveness requirements
3. **Handle ANRs Gracefully**: Use callbacks to report to crash services and potentially recover
4. **Clean Up**: Call stop() when monitoring is no longer needed
5. **Test Thoroughly**: Test ANR detection in debug builds before releasing
6. **Consider Performance**: ANR monitoring has minimal overhead but runs continuously

## Testing

The library includes comprehensive unit tests. To run them:

```bash
# Test ANRWatchdog library
./gradlew anrwatchdog:test

# View test reports
open anrwatchdog/build/reports/tests/test/index.html
```

## Common Issues

### Issue: ANRWatchdog not detecting ANRs

**Solutions:**
- Ensure `start()` was called
- Check that timeout is appropriate for your use case
- Verify main thread is actually blocked (use systrace or profiler)

### Issue: False positive ANR detections

**Solutions:**
- Increase timeout value
- Verify your main thread work is actually taking too long
- Consider using more lenient thresholds for slower devices

### Issue: Callback not being invoked

**Solutions:**
- Ensure callback was set before calling `start()`
- Check that watchdog is actually running
- Verify no exceptions in callback are preventing execution

## Contributing

Contributions are welcome! Please:
1. Write tests for new features
2. Follow existing code style
3. Update documentation
4. Submit a pull request

## License

This library is part of the ANR Watchdog project and is licensed under the Apache License 2.0.

---
