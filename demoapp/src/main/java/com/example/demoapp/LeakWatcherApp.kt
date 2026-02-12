package com.example.demoapp

import android.app.Application
import android.util.Log
import com.example.anrwatchdog.ANRWatchdog

/**
 * Main Application class for the ANR Watchdog demo app.
 * 
 * This application demonstrates the integration and usage of the ANRWatchdog library.
 * It initializes the ANR detection system on app startup and configures it with
 * appropriate settings for debugging and monitoring.
 * 
 * ## Features Demonstrated:
 * - ANRWatchdog initialization and configuration
 * - Custom callback handling for ANR events
 * - Integration with Android Application lifecycle
 * - Error handling for initialization failures
 * 
 * ## Note:
 * The app name "LeakWatcherApp" is historical and also relates to the integration
 * with LeakCanary for memory leak detection in debug builds.
 */
class LeakWatcherApp : Application() {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects have been created.
     * 
     * Initializes the ANRWatchdog with the following configuration:
     * - Log level: DEBUG for verbose logging
     * - Timeout: 5000ms (5 seconds) to match Android's ANR threshold
     * - Callback: Logs ANR detection events with thread information
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize ANRWatchdog
        try {
            ANRWatchdog.initialize(this)
                .setLogLevel(Log.DEBUG)
                .setTimeout(5000)
                .setCallback { thread ->
                    Log.w("LeakWatcherApp", "ANR detected on thread: ${thread.name}")
                }
                .start()
            Log.d("LeakWatcherApp", "Demo app started - ANRWatchdog initialized successfully")
        } catch (e: Exception) {
            Log.w("LeakWatcherApp", "ANRWatchdog initialization failed: ${e.message}")
        }
    }
}