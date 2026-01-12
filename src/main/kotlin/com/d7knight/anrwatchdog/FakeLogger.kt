package com.d7knight.anrwatchdog

/**
 * FakeLogger provides a simple logging utility for demonstration purposes.
 * This logger prefixes all messages with "[FakeLogger]" for easy identification.
 */
object FakeLogger {
    /**
     * Logs a message to standard output with a FakeLogger prefix.
     *
     * @param message The message to log
     */
    fun log(message: String) {
        println("[FakeLogger] \$message")
    }
}