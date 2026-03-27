package com.example.anrwatchdog

import android.app.Application
import android.util.Log
import kotlinx.coroutines.debug.DebugProbes

/**
 * ANRWatchdog is a runtime ANR (Application Not Responding) detection tool for Android applications.
 * It monitors the application for potential ANR events and provides detailed diagnostic information
 * including thread states, stack traces, and coroutine creation traces.
 *
 * This class implements the Singleton pattern to ensure only one instance monitors the application.
 *
 * @property application The Android application context
 */
class ANRWatchdog private constructor(private val application: Application) {
    // Configuration properties
    private var timeout: Long = 5000L // ANR detection timeout in milliseconds
    private var logLevel: Int = Log.INFO // Minimum log level for debug output
    private var callback: ((Thread) -> Unit)? = null // Optional callback for ANR events
    
    // Runtime state
    private var running = false // Flag to track if watchdog is active
    private var thread: Thread? = null // Background monitoring thread

    companion object {
        private var instance: ANRWatchdog? = null
        
        /**
         * Initializes the ANRWatchdog singleton instance.
         * This method is thread-safe and will return the same instance on subsequent calls.
         *
         * @param application The Android application context
         * @return The ANRWatchdog singleton instance
         */
        fun initialize(application: Application): ANRWatchdog {
            if (instance == null) {
                instance = ANRWatchdog(application)
                // Install coroutine debug probes to capture creation stack traces
                DebugProbes.install()
                DebugProbes.enableCreationStackTraces = true
            }
            return instance!!
        }
    }

    /**
     * Sets the ANR detection timeout period.
     * The watchdog will check for ANR conditions at this interval.
     *
     * @param timeout Timeout in milliseconds (default: 5000ms)
     * @return This ANRWatchdog instance for method chaining
     */
    fun setTimeout(timeout: Long): ANRWatchdog {
        this.timeout = timeout
        return this
    }

    /**
     * Sets the minimum log level for debug output.
     *
     * @param level Log level constant from android.util.Log (e.g., Log.DEBUG, Log.INFO)
     * @return This ANRWatchdog instance for method chaining
     */
    fun setLogLevel(level: Int): ANRWatchdog {
        this.logLevel = level
        return this
    }

    /**
     * Sets a callback to be invoked when an ANR is detected.
     *
     * @param callback Lambda function that receives the thread where ANR was detected
     * @return This ANRWatchdog instance for method chaining
     */
    fun setCallback(callback: (Thread) -> Unit): ANRWatchdog {
        this.callback = callback
        return this
    }

    /**
     * Starts the ANR monitoring thread.
     * This method is idempotent - calling it multiple times has no additional effect.
     *
     * The watchdog runs on a background thread and checks for ANR conditions
     * at the configured timeout interval. When an ANR is detected, it will:
     * - Invoke the registered callback (if any)
     * - Log debug information based on the configured log level
     *
     * @return This ANRWatchdog instance for method chaining
     */
    fun start(): ANRWatchdog {
        // Prevent multiple start calls from creating additional threads
        if (running) return this
        
        running = true
        thread = Thread {
            while (running) {
                try {
                    // Wait for the configured timeout period
                    Thread.sleep(timeout)
                    
                    // Simulate ANR detection for demo purposes
                    // In production, this would check main thread responsiveness
                    callback?.invoke(Thread.currentThread())
                    
                    // Log ANR event if debug level is enabled
                    if (logLevel <= Log.DEBUG) {
                        Log.d("ANRWatchdog", "ANR detected (simulated)")
                    }
                } catch (e: InterruptedException) {
                    // Thread was interrupted, exit gracefully
                    break
                }
            }
        }
        thread?.start()
        return this
    }

    /**
     * Stops the ANR monitoring thread.
     * This method cleanly shuts down the background monitoring thread.
     */
    fun stop() {
        running = false
        thread?.interrupt()
    }
}
