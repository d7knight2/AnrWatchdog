package com.d7knight.anrwatchdog.okhttp

import kotlinx.coroutines.*

/**
 * FakeOkHttpRepository simulates network operations typically performed
 * by the OkHttp library in Android applications.
 *
 * This mock repository is used for testing and demonstrating proper handling
 * of network requests in coroutine-based architectures, ensuring they don't
 * block the main thread and cause ANR issues.
 *
 * Real-world use case: In production, OkHttp performs HTTP requests, which can
 * take significant time due to network latency. This simulation helps test ANR
 * detection when network calls are incorrectly performed on the main thread.
 */
object FakeOkHttpRepository {
    /**
     * Simulates a blocking network operation in a coroutine context.
     *
     * This method runs on [Dispatchers.Default] to avoid blocking the main thread,
     * demonstrating the proper pattern for handling network requests that would
     * otherwise cause ANR events.
     *
     * In production, this would represent operations such as:
     * - HTTP GET/POST requests
     * - Response parsing
     * - Connection establishment
     *
     * @param index Unique identifier for this operation (used in logging and coroutine naming)
     */
    suspend fun performBlockingOperation(index: Int) {
        withContext(Dispatchers.Default + CoroutineName("OkHttpJob-$index")) {
            println("Started OkHttpJob-$index")
            delay(50) // Simulates network latency
            println("Finished OkHttpJob-$index")
        }
    }
}