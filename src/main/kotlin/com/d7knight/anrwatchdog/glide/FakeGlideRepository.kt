package com.d7knight.anrwatchdog.graphics

import kotlinx.coroutines.*

/**
 * FakeGlideRepository simulates image loading operations using coroutines.
 * This is a demonstration repository that mimics the behavior of Glide image loading
 * for testing ANR detection and coroutine debugging capabilities.
 */
object FakeGlideRepository {
    /**
     * Performs a simulated blocking image loading operation on a background dispatcher.
     * Uses a named coroutine context for easier debugging and tracking.
     *
     * @param index Unique identifier for this operation
     */
    suspend fun performBlockingOperation(index: Int) {
        withContext(Dispatchers.Default + CoroutineName("GlideJob-$index")) {
            println("Started GlideJob-$index")
            delay(50) // Simulate image loading time
            println("Finished GlideJob-$index")
        }
    }
}