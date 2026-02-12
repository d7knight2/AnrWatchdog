package com.d7knight.anrwatchdog.experimental

import kotlinx.coroutines.*

/**
 * ExperimentCheckRepository demonstrates non-blocking coroutine operations.
 * This repository is used for testing and demonstration of proper coroutine
 * patterns that don't cause ANR issues.
 */
object ExperimentCheckRepository {
    /**
     * Performs a non-blocking operation using coroutineScope.
     * This method launches a child coroutine and waits for it to complete
     * without blocking the calling thread.
     *
     * @param index Unique identifier for this operation
     */
    suspend fun performNonBlockingOperation(index: Int) {
        coroutineScope {
            launch(Dispatchers.Default + CoroutineName("ExperimentJob-$index")) {
                println("Started ExperimentJob-$index")
                delay(50) // Simulate non-blocking work
                println("Finished ExperimentJob-$index")
            }
        }
    }
}