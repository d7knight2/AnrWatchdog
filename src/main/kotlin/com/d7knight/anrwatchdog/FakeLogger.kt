package com.d7knight.anrwatchdog

/**
 * FakeLogger is a simple logging utility for testing and demonstration purposes.
 *
 * This logger provides a lightweight alternative to Android's Log class for
 * use in unit tests or standalone Kotlin applications where Android dependencies
 * are not available.
 *
 * Example usage:
 * ```kotlin
 * FakeLogger.log("Starting operation")
 * FakeLogger.log("Operation completed successfully")
 * ```
 *
 * Note: For production Android applications, use Android's Log class instead.
 */
object FakeLogger {
    /**
     * Logs a message to standard output with a [FakeLogger] prefix.
     *
     * @param message The message to log
     */
    fun log(message: String) {
        println("[FakeLogger] $message")
    }
}