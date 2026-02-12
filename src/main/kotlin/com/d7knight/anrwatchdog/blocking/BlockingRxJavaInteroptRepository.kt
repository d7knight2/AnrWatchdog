package com.d7knight.anrwatchdog.blocking

import com.d7knight.anrwatchdog.rxjava.SlowRxExperimentEnabledRepository
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

/**
 * BlockingRxJavaInteroptRepository demonstrates integration between RxJava and Kotlin Coroutines.
 * It shows how to bridge RxJava Observables/Publishers with Kotlin Flow for seamless interoperability.
 */
object BlockingRxJavaInteroptRepository {
    /**
     * Performs a blocking RxJava operation and converts it to Kotlin Flow.
     * This method demonstrates the pattern of:
     * 1. Switching to IO dispatcher for blocking operations
     * 2. Converting RxJava Publisher to Kotlin Flow
     * 3. Collecting all emitted values
     * 4. Processing the results
     *
     * @param index Unique identifier for this operation
     */
    suspend fun performBlockingRxOperation(index: Int) {
        withContext(Dispatchers.IO) {
            // Convert RxJava Publisher to Kotlin Flow and collect all values
            val result = SlowRxExperimentEnabledRepository.performSlowOperation(index).asFlow().toList()
            // Print each emitted value
            result.forEach { item: String -> println(item) }
        }
    }
}