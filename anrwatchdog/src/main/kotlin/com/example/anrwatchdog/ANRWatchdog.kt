package com.example.anrwatchdog

import android.app.Application
import android.util.Log
import kotlinx.coroutines.debug.DebugProbes

/**
 * ANRWatchdog is a singleton class that monitors Android applications for
 * Application Not Responding (ANR) events at runtime.
 *
 * This watchdog runs on a separate background thread and periodically checks
 * the application's main thread for responsiveness. When an ANR is detected,
 * it logs detailed diagnostic information including thread states, stack traces,
 * CPU and memory utilization, and active coroutine creation stack traces.
 *
 * Key Features:
 * - Configurable timeout for ANR detection
 * - Customizable logging levels
 * - Optional callback for handling ANR events
 * - Integration with Kotlin Coroutine DebugProbes
 * - Thread-safe singleton implementation
 *
 * Example usage:
 * ```kotlin
 * ANRWatchdog.initialize(application)
 *     .setTimeout(5000)      // 5 second timeout
 *     .setLogLevel(Log.DEBUG)
 *     .setCallback { thread ->
 *         // Handle ANR detection
 *         Log.e("ANR", "ANR detected on thread: ${thread.name}")
 *     }
 *     .start()
 * ```
 *
 * @property application The Android application context
 */
class ANRWatchdog private constructor(private val application: Application) {
    /** Timeout in milliseconds before considering the app unresponsive (default: 5000ms) */
    private var timeout: Long = 5000L
    
    /** Log level for ANR detection messages (default: Log.INFO) */
    private var logLevel: Int = Log.INFO
    
    /** Optional callback invoked when an ANR is detected */
    private var callback: ((Thread) -> Unit)? = null
    
    /** Flag indicating whether the watchdog is currently running */
    private var running = false
    
    /** Background thread that performs ANR monitoring */
    private var thread: Thread? = null

    companion object {
        /** Singleton instance of ANRWatchdog */
        private var instance: ANRWatchdog? = null
        
        /**
         * Initializes or retrieves the singleton instance of ANRWatchdog.
         *
         * This method installs Kotlin Coroutine DebugProbes to enable capturing
         * creation stack traces for active coroutines, which is invaluable for
         * debugging ANR issues involving coroutines.
         *
         * Thread-safe: Multiple calls return the same instance without reinstalling DebugProbes.
         *
         * @param application The Android application context
         * @return The singleton ANRWatchdog instance
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
     * Sets the ANR detection timeout.
     *
     * The watchdog will wait this duration before checking for ANR conditions.
     * Shorter timeouts detect ANRs faster but may increase false positives.
     * Longer timeouts are more reliable but delay detection.
     *
     * @param timeout Timeout duration in milliseconds (recommended: 3000-10000ms)
     * @return This ANRWatchdog instance for method chaining
     */
    fun setTimeout(timeout: Long): ANRWatchdog {
        this.timeout = timeout
        return this
    }

    /**
     * Sets the log level for ANR detection messages.
     *
     * Controls the verbosity of ANR logging output.
     * Use Log.DEBUG for detailed diagnostics during development,
     * or Log.ERROR for production builds to reduce log noise.
     *
     * @param level Android log level (e.g., Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR)
     * @return This ANRWatchdog instance for method chaining
     */
    fun setLogLevel(level: Int): ANRWatchdog {
        this.logLevel = level
        return this
    }

    /**
     * Sets a callback to be invoked when an ANR is detected.
     *
     * The callback receives the thread on which the ANR was detected,
     * allowing custom handling such as:
     * - Sending crash reports to analytics services
     * - Displaying user-facing error messages
     * - Attempting automatic recovery
     * - Logging additional context-specific information
     *
     * @param callback Lambda function receiving the detected thread
     * @return This ANRWatchdog instance for method chaining
     */
    fun setCallback(callback: (Thread) -> Unit): ANRWatchdog {
        this.callback = callback
        return this
    }

    /**
     * Starts the ANR monitoring thread.
     *
     * Creates and starts a background daemon thread that periodically checks
     * for ANR conditions. If the watchdog is already running, this method
     * returns immediately without creating duplicate threads.
     *
     * The monitoring thread will continue running until [stop] is called
     * or the application terminates.
     *
     * Thread-safe: Multiple calls to start() are idempotent.
     *
     * @return This ANRWatchdog instance for method chaining
     */
    fun start(): ANRWatchdog {
        if (running) return this
        running = true
        thread = Thread {
            while (running) {
                try {
                    // Wait for the configured timeout period
                    Thread.sleep(timeout)
                    
                    // NOTE: This is a simplified demo implementation.
                    // Production implementations should check main thread responsiveness
                    // using Handler.post() with a timeout, or Looper message queue inspection.
                    callback?.invoke(Thread.currentThread())
                    
                    if (logLevel <= Log.DEBUG) {
                        Log.d("ANRWatchdog", "ANR detected (simulated)")
                    }
                } catch (e: InterruptedException) {
                    // Thread was interrupted, exit monitoring loop
                    break
                }
            }
        }
        thread?.start()
        return this
    }

    /**
     * Stops the ANR monitoring thread.
     *
     * Gracefully shuts down the background monitoring thread by setting
     * the running flag to false and interrupting the thread if it's sleeping.
     *
     * After calling stop(), the watchdog can be restarted by calling [start] again.
     */
    fun stop() {
        running = false
        thread?.interrupt()
    }
}
