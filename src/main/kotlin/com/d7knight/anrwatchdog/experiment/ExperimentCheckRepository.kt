package com.d7knight.anrwatchdog.experimental

import kotlinx.coroutines.*

/**
 * ExperimentCheckRepository provides non-blocking operations for testing
 * experimental features and A/B testing scenarios in the ANR watchdog context.
 *
 * This repository demonstrates proper coroutine scoping and structured concurrency,
 * ensuring that child coroutines complete before the parent returns, preventing
 * leaked coroutines and potential ANR issues.
 *
 * Key feature: Uses coroutineScope to create a new scope that waits for all
 * child coroutines to complete, maintaining structured concurrency guarantees.
 */
object ExperimentCheckRepository {
    /**
     * Performs a non-blocking experimental operation with proper coroutine scoping.
     *
     * This method uses coroutineScope to ensure structured concurrency:
     * - Child coroutines launched within the scope inherit context
     * - The function suspends until all child coroutines complete
     * - If any child fails, the entire scope is cancelled
     *
     * This pattern prevents common coroutine issues such as:
     * - Leaked coroutines continuing after parent completes
     * - Unhandled exceptions in background operations
     * - ANR-inducing blocking of the main thread
     *
     * @param index Unique identifier for this operation (used in logging and coroutine naming)
     */
    suspend fun performNonBlockingOperation(index: Int) {
        coroutineScope {
            launch(Dispatchers.Default + CoroutineName("ExperimentJob-$index")) {
                println("Started ExperimentJob-$index")
                delay(50) // Simulate non-blocking work (e.g., feature flag check)
                println("Finished ExperimentJob-$index")
            }
        }
    }
}