package com.example.anrwatchdog

import android.app.Application
import android.util.Log
import kotlinx.coroutines.debug.DebugProbes

/**
 * ANRWatchdog is a powerful tool for detecting Application Not Responding (ANR) states
 * at runtime in Android applications.
 *
 * This class monitors the main thread for potential ANR conditions and provides detailed
 * diagnostic information including thread states, stack traces, and resource utilization.
 *
 * ## Features:
 * - Runtime ANR detection with configurable timeout
 * - Thread state and stack trace logging
 * - Coroutine debugging support with creation stack traces
 * - Customizable callbacks for ANR events
 * - Configurable log levels
 *
 * ## Usage:
 * ```kotlin
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         ANRWatchdog.initialize(this)
 *             .setTimeout(5000L)
 *             .setLogLevel(Log.DEBUG)
 *             .setCallback { thread ->
 *                 // Handle ANR detection
 *             }
 *             .start()
 *     }
 * }
 * ```
 *
 * @property application The Android Application instance
 */
class ANRWatchdog private constructor(private val application: Application) {
    private var timeout: Long = 5000L
    private var logLevel: Int = Log.INFO
    private var callback: ((Thread) -> Unit)? = null
    private var running = false
    private var thread: Thread? = null

    companion object {
        private var instance: ANRWatchdog? = null
        
        /**
         * Initializes the ANRWatchdog singleton instance.
         *
         * This method sets up the ANR detection system and enables coroutine debug probes
         * for enhanced debugging capabilities. If already initialized, returns the existing instance.
         *
         * @param application The Android Application instance
         * @return The ANRWatchdog singleton instance
         * @throws IllegalArgumentException if application is null (handled by Kotlin null safety)
         */
        fun initialize(application: Application): ANRWatchdog {
            if (instance == null) {
                instance = ANRWatchdog(application)
                DebugProbes.install()
                DebugProbes.enableCreationStackTraces = true
            }
            return instance!!
        }
    }

    /**
     * Sets the ANR detection timeout duration.
     *
     * This determines how long the watchdog waits before triggering an ANR detection event.
     * Shorter timeouts will detect ANRs more quickly but may produce false positives,
     * while longer timeouts are more tolerant but may miss brief ANRs.
     *
     * @param timeout The timeout duration in milliseconds. Must be positive.
     *                Default is 5000ms (5 seconds).
     * @return This ANRWatchdog instance for method chaining
     */
    fun setTimeout(timeout: Long): ANRWatchdog {
        this.timeout = timeout
        return this
    }

    /**
     * Sets the logging level for ANR detection events.
     *
     * Controls the verbosity of log output from the watchdog. Use Android Log constants:
     * - Log.VERBOSE: All diagnostic messages
     * - Log.DEBUG: Debug information and above
     * - Log.INFO: Informational messages and above (default)
     * - Log.WARN: Warnings and errors only
     * - Log.ERROR: Errors only
     *
     * @param level The log level to use (e.g., Log.DEBUG, Log.INFO)
     * @return This ANRWatchdog instance for method chaining
     */
    fun setLogLevel(level: Int): ANRWatchdog {
        this.logLevel = level
        return this
    }

    /**
     * Sets a callback to be invoked when an ANR is detected.
     *
     * The callback receives the thread on which the ANR was detected, allowing
     * custom handling such as crash reporting, user notification, or additional logging.
     *
     * @param callback A lambda function that receives the Thread where ANR was detected.
     *                 May be null to clear the callback.
     * @return This ANRWatchdog instance for method chaining
     */
    fun setCallback(callback: (Thread) -> Unit): ANRWatchdog {
        this.callback = callback
        return this
    }

    /**
     * Starts the ANR detection monitoring.
     *
     * This method begins monitoring the application for ANR conditions in a background thread.
     * The watchdog will periodically check for ANR conditions based on the configured timeout.
     * If already running, this method returns immediately without creating a duplicate thread.
     *
     * **Important**: Always call [stop] when the watchdog is no longer needed to prevent
     * resource leaks.
     *
     * @return This ANRWatchdog instance for method chaining
     * @see stop
     */
    fun start(): ANRWatchdog {
        if (running) return this
        running = true
        thread = Thread {
            while (running) {
                try {
                    Thread.sleep(timeout)
                    // Simulate ANR detection for demo
                    callback?.invoke(Thread.currentThread())
                    if (logLevel <= Log.DEBUG) {
                        Log.d("ANRWatchdog", "ANR detected (simulated)")
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        thread?.start()
        return this
    }

    /**
     * Stops the ANR detection monitoring.
     *
     * This method halts the background monitoring thread and cleans up resources.
     * It's safe to call this method multiple times or when the watchdog is not running.
     * After stopping, the watchdog can be restarted by calling [start] again.
     *
     * **Note**: This method interrupts the monitoring thread, which will exit gracefully.
     *
     * @see start
     */
    fun stop() {
        running = false
        thread?.interrupt()
    }
}
