package com.d7knight.anrwatchdog.network

import kotlinx.coroutines.*

/**
 * FakeOkHttpRepository simulates network operations using coroutines.
 * This is a demonstration repository that mimics the behavior of OkHttp network calls
 * for testing ANR detection and coroutine debugging capabilities.
 */
object FakeOkHttpRepository {
    /**
     * Performs a simulated blocking network operation on a background dispatcher.
     * Uses a named coroutine context for easier debugging and tracking.
     *
     * @param index Unique identifier for this operation
     */
    suspend fun performBlockingOperation(index: Int) {
        withContext(Dispatchers.Default + CoroutineName("OkHttpJob-$index")) {
            println("Started OkHttpJob-$index")
            delay(50) // Simulate network latency
            println("Finished OkHttpJob-$index")
        }
    }
}