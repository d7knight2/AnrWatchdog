package com.d7knight.anrwatchdog.graphics

import kotlinx.coroutines.*

/**
 * FakeGlideRepository simulates image loading operations typically performed
 * by the Glide library in Android applications.
 *
 * This mock repository is used for testing and demonstrating proper handling
 * of image loading operations in coroutine-based architectures, ensuring they
 * don't block the main thread and cause ANR issues.
 *
 * Real-world use case: In production, Glide performs image decoding, caching,
 * and network requests. This simulation helps test ANR detection when such
 * operations are incorrectly performed on the main thread.
 */
object FakeGlideRepository {
    /**
     * Simulates a blocking image loading operation in a coroutine context.
     *
     * This method runs on [Dispatchers.Default] to avoid blocking the main thread,
     * demonstrating the proper pattern for handling potentially expensive image
     * processing operations.
     *
     * @param index Unique identifier for this operation (used in logging and coroutine naming)
     */
    suspend fun performBlockingOperation(index: Int) {
        withContext(Dispatchers.Default + CoroutineName("GlideJob-$index")) {
            println("Started GlideJob-$index")
            delay(50) // Simulates image decoding/loading time
            println("Finished GlideJob-$index")
        }
    }
}