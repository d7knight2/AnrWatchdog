package com.d7knight.anrwatchdog.blocking

import kotlinx.coroutines.*
import com.d7knight.anrwatchdog.okhttp.FakeOkHttpRepository
import com.d7knight.anrwatchdog.glide.FakeGlideRepository
import com.d7knight.anrwatchdog.experiment.ExperimentCheckRepository

/**
 * BlockingRxJavaInteroptRepository simulates blocking operations in a coroutine context
 * to test interoperability between RxJava-style blocking calls and Kotlin Coroutines.
 *
 * This repository is used for testing and demonstrating how blocking operations from
 * legacy RxJava code can be safely integrated into coroutine-based Android applications
 * without causing ANR issues.
 *
 * The operations are run on [Dispatchers.Default] to avoid blocking the main thread.
 */
object BlockingRxJavaInteroptRepository {
    /**
     * Performs a simulated blocking operation in a coroutine context.
     *
     * This method demonstrates proper handling of potentially blocking operations
     * by executing them in a background dispatcher with a named coroutine for
     * easier debugging with DebugProbes.
     *
     * @param index Unique identifier for this operation (used in logging and coroutine naming)
     */
    suspend fun performBlockingOperation(index: Int) {
        withContext(Dispatchers.Default + CoroutineName("FakeJob-$index")) {
            println("Started FakeJob-$index")
            delay(50) // Simulates I/O or computation time
            println("Finished FakeJob-$index")
        }
    }
}

/**
 * Demonstration main function showing concurrent execution of multiple repository operations.
 *
 * This example demonstrates:
 * - Launching multiple coroutines concurrently
 * - Using named coroutines for debugging
 * - Coordinating completion with join()
 * - Integration between different repository implementations
 */
fun main() = runBlocking {
    // Launch main blocking operation
    val mainJob = launch(Dispatchers.Default + CoroutineName("Main")) {
        println("Main coroutine started")
        BlockingRxJavaInteroptRepository.performBlockingOperation(1)
    }

    // Launch OkHttp simulation
    val okhttpJob = launch(Dispatchers.Default + CoroutineName("OkHttp")) {
        println("OkHttp coroutine started")
        com.d7knight.anrwatchdog.okhttp.FakeOkHttpRepository.performBlockingOperation(2)
    }

    // Launch Glide image loading simulation
    val glideJob = launch(Dispatchers.Default + CoroutineName("Glide")) {
        println("Glide coroutine started")
        com.d7knight.anrwatchdog.glide.FakeGlideRepository.performBlockingOperation(3)
    }

    // Launch experimental feature operation
    val experimentJob = launch(Dispatchers.Default + CoroutineName("Experiment")) {
        println("Experiment coroutine started")
        ExperimentCheckRepository.performNonBlockingOperation(4)
    }

    // Wait for all operations to complete
    listOf(mainJob, okhttpJob, glideJob, experimentJob).forEach { it.join() }
    println("All coroutines completed")
}