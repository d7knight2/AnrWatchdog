package com.d7knight.anrwatchdog

object FakeLogger {
    fun log(message: String) {
        println("[FakeLogger] \$message")
    }
}