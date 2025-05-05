package com.dknight.anrwatchdog

object FakeLogger {
    fun log(message: String) {
        println("[FakeLogger] \$message")
    }
}