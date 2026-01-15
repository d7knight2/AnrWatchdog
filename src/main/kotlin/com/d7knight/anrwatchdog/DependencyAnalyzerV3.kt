package com.d7knight.anrwatchdog

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.debug.DebugProbes
import java.util.concurrent.CopyOnWriteArrayList

/**
 * DependencyAnalyzerV3 is a thread-safe singleton utility for tracking and analyzing
 * execution flow and dependencies across multiple threads and coroutines.
 *
 * This analyzer is particularly useful for:
 * - Debugging complex asynchronous operations
 * - Tracing execution order in concurrent systems
 * - Understanding dependency chains in coroutine-heavy code
 * - Identifying potential deadlocks or race conditions
 *
 * The analyzer uses a thread-safe CopyOnWriteArrayList to store events, ensuring
 * safe concurrent access from multiple threads without explicit synchronization.
 *
 * Example usage:
 * ```kotlin
 * DependencyAnalyzerV3.logEvent("Starting operation")
 * // ... perform operations ...
 * DependencyAnalyzerV3.logEvent("Operation completed")
 * DependencyAnalyzerV3.dump() // Print all logged events
 * ```
 */
object DependencyAnalyzerV3 {
    /**
     * Thread-safe list storing logged events with thread names.
     * CopyOnWriteArrayList is used to support concurrent reads and writes
     * without blocking, at the cost of higher memory usage during writes.
     */
    private val dependencies = CopyOnWriteArrayList<String>()

    /**
     * Logs an event with the current thread name.
     *
     * Events are stored with a timestamp in the format: [ThreadName] event
     * This method is thread-safe and can be called concurrently from multiple threads.
     *
     * @param event Description of the event to log
     */
    fun logEvent(event: String) {
        dependencies.add("[${Thread.currentThread().name}] $event")
    }

    /**
     * Dumps all logged events to standard output.
     *
     * Prints events in the order they were logged, with clear start and end markers.
     * This is useful for post-execution analysis of complex asynchronous flows.
     *
     * Output format:
     * ```
     * ----- Dependency Analyzer Dump Start -----
     * [Thread-1] Event 1
     * [Thread-2] Event 2
     * ...
     * ----- Dependency Analyzer Dump End -----
     * ```
     */
    fun dump() {
        println("----- Dependency Analyzer Dump Start -----")
        dependencies.forEach { println(it) }
        println("----- Dependency Analyzer Dump End -----")
    }
}