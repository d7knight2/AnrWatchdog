# ANR Watchdog

ANR Watchdog is a powerful tool designed to detect Application Not Responding (ANR) states at runtime in Android applications. It provides detailed logs of all thread states and traces, including CPU and memory utilization. Additionally, it can leverage coroutine debug probes to print the creation stack traces for active coroutines, making it an invaluable resource for debugging and performance monitoring.

## Features

- **Runtime ANR Detection**: Monitors the app for ANR events and logs detailed diagnostic information.
- **Thread State and Traces Logging**: Captures the states and stack traces of all threads at the moment of detection.
- **Resource Monitoring**: Prints the CPU and memory utilization at runtime for better understanding of resource usage.
- **Coroutine Debugging**: Utilizes coroutine debug probes to print the creation stack traces of active coroutines, aiding in coroutine lifecycle analysis.
- **Floating Debug Tool (Demo App)**: Interactive UI overlay for real-time monitoring of threads, main thread blocks, and system resources.

## Demo App

The demo app includes a **Floating Debug Tool** that provides an interactive way to monitor and debug the application in real-time. This comprehensive tool is perfect for developers who want to:

- View all active threads and their current states
- Track recent main thread blocks that could lead to ANRs
- Monitor CPU usage and memory trends over time
- Log and review UI interactions (taps, scrolls, drags)
- Export debug logs for analysis or sharing
- Test ANR scenarios with built-in simulation
- Switch between dark and light themes for better visibility

### Floating Debug Tool Features

The enhanced floating debug tool provides:

1. **Active Threads Display**: Real-time list of all threads with names, states, IDs, priorities, and daemon status
2. **Recent Main Thread Blocks**: Chronological history of main thread blocking events with timestamps, durations, and stack traces (configurable limit)
3. **CPU Usage Over Time**: Track CPU/memory usage trends with historical snapshots
4. **UI Interaction Logging**: Automatic recording of taps, scrolls, and other gestures with coordinates and timing
5. **General Debug Info**: System metrics including memory usage, processor count, thread statistics, and data counts
6. **Export Functionality**: Save all debug logs to a text file for persistent storage and sharing
7. **Clear Logs**: One-click button to clear all recorded data
8. **Theme Toggle**: Switch between dark and light modes

The tool is:
- **Draggable**: Move it anywhere on the screen
- **Collapsible**: Minimize to a small button when not in use
- **Configurable**: Adjust update frequency and history limits
- **Auto-updating**: Refreshes at configurable intervals when expanded (default: 2 seconds)
- **Accessible**: Meets accessibility standards with 48dp minimum touch targets
- **Non-intrusive**: Designed to not interfere with app functionality

For detailed documentation on the Floating Debug Tool, see [demoapp/FLOATING_DEBUG_TOOL.md](demoapp/FLOATING_DEBUG_TOOL.md).

## Installation

To integrate the ANR Watchdog into your project, follow these steps:

1. Add the dependency to your `build.gradle` file:
   ```gradle
   implementation 'com.example:anr-watchdog:1.0.0'
   ```
2. Enable coroutine debug mode by adding the following line to your app's `gradle.properties`:
   ```properties
   kotlin.coroutines.debug=on
   ```

## Usage

### Basic Setup

Initialize the ANR Watchdog in your application class or main activity:

```kotlin
import com.example.anrwatchdog.ANRWatchdog

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start the ANR Watchdog
        ANRWatchdog.initialize(this)
            .setLogLevel(Log.DEBUG)  // Optional: Set log level
            .start()
    }
}
```

### Logging Thread States and Traces

When an ANR is detected, the watchdog will automatically log the states and stack traces of all threads. These logs can be found in your app's console output.

### Printing CPU and Memory Utilization

The watchdog continuously monitors and logs the CPU and memory utilization of your app. This data is included in the diagnostic logs.

### Debugging Coroutines

To print the creation stack traces of active coroutines, ensure that coroutine debug mode is enabled in your project. The watchdog will automatically include these stack traces in its logs when an ANR occurs.

## Customization

You can customize the behavior of the ANR Watchdog by using the following configuration options:

```kotlin
ANRWatchdog.initialize(this)
    .setTimeout(5000) // Set the ANR detection timeout in milliseconds (default: 5000ms)
    .setLogLevel(Log.INFO) // Set the desired log level
    .setCallback { threadInfo ->
        // Handle ANR detection event
        Log.e("ANRWatchdog", "ANR detected on thread: ${threadInfo.name}")
    }
    .start()
```

## Contributing

Contributions are welcome! If you have ideas for new features or improvements, feel free to open an issue or submit a pull request.

## Testing

This project includes comprehensive test coverage:
- **Unit tests** for the ANRWatchdog library
- **Instrumented UI tests** for the demo app (ANR simulation, memory leaks, UI interactions)
- **Continuous Integration** via GitHub Actions

See [TESTING.md](TESTING.md) for detailed information on running tests and CI configuration.

## License

This project is free software and licensed under the [Apache License 2.0](LICENSE). You are free to use, modify, and distribute it in accordance with the terms of this license.

---

Happy debugging!
