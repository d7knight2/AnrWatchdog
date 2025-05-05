# ANR Watchdog

ANR Watchdog is a powerful tool designed to detect Application Not Responding (ANR) states at runtime in Android applications. It provides detailed logs of all thread states and traces, including CPU and memory utilization. Additionally, it can leverage coroutine debug probes to print the creation stack traces for active coroutines, making it an invaluable resource for debugging and performance monitoring.

## Features

- **Runtime ANR Detection**: Monitors the app for ANR events and logs detailed diagnostic information.
- **Thread State and Traces Logging**: Captures the states and stack traces of all threads at the moment of detection.
- **Resource Monitoring**: Prints the CPU and memory utilization at runtime for better understanding of resource usage.
- **Coroutine Debugging**: Utilizes coroutine debug probes to print the creation stack traces of active coroutines, aiding in coroutine lifecycle analysis.

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

## License

This project is free software and licensed under the [Apache License 2.0](LICENSE). You are free to use, modify, and distribute it in accordance with the terms of this license.

---

Happy debugging!
