package com.d7knight.anrwatchdog

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.debug.DebugProbes
import java.util.concurrent.CopyOnWriteArrayList

/**
 * DependencyAnalyzerV3 is a thread-safe event logging utility for tracking
 * and analyzing dependency interactions across multiple threads and coroutines.
 *
 * This analyzer uses a CopyOnWriteArrayList to ensure thread-safe access
 * without requiring explicit synchronization, making it suitable for
 * high-concurrency scenarios.
 *
 * Typical usage:
 * ```
 * DependencyAnalyzerV3.logEvent("Repository operation started")
 * // ... perform operation ...
 * DependencyAnalyzerV3.logEvent("Repository operation completed")
 * DependencyAnalyzerV3.dump() // Print all logged events
 * ```
 */
object DependencyAnalyzerV3 {
    // Thread-safe list that stores events with thread information
    private val dependencies = CopyOnWriteArrayList<String>()

    /**
     * Logs an event with the current thread name as context.
     * This method is thread-safe and can be called from any thread or coroutine.
     *
     * @param event The event description to log
     */
    fun logEvent(event: String) {
        dependencies.add("[${Thread.currentThread().name}] $event")
    }

    /**
     * Prints all logged events to standard output.
     * Events are displayed with delimiters for easy identification.
     * This method is thread-safe and will print the events in the order they were logged.
     */
    fun dump() {
        println("----- Dependency Analyzer Dump Start -----")
        dependencies.forEach { println(it) }
        println("----- Dependency Analyzer Dump End -----")
    }
}