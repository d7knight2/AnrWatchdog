package com.d7knight.anrwatchdog.rxjava

import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.reactivestreams.Publisher

/**
 * SlowRxExperimentEnabledRepository demonstrates RxJava operations with
 * controlled time advancement using TestScheduler. This is particularly
 * useful for testing time-based operations without actual delays.
 */
object SlowRxExperimentEnabledRepository {
    // Test scheduler allows controlling time advancement manually
    private val testScheduler = TestScheduler()

    /**
     * Creates an RxJava Observable that performs a slow operation.
     * Uses TestScheduler to allow deterministic time control in tests.
     *
     * @param index Unique identifier for this operation
     * @return A Publisher that emits status messages about the operation
     */
    fun performSlowOperation(index: Int): Publisher<String> {
        return Observable.create<String> { emitter ->
            // Emit start message immediately
            emitter.onNext("Started SlowRxExperimentJob-$index")
            
            // Schedule completion message to be emitted after delay
            testScheduler.scheduleDirect({
                emitter.onNext("Finished SlowRxExperimentJob-$index")
                emitter.onComplete()
            }, 100, java.util.concurrent.TimeUnit.MILLISECONDS)
        }.subscribeOn(testScheduler).toFlowable(io.reactivex.BackpressureStrategy.BUFFER)
    }

    /**
     * Advances the test scheduler's virtual time by the specified amount.
     * This allows tests to simulate time passing without actual delays.
     *
     * @param delayTime Amount of time to advance
     * @param timeUnit Unit of time for the delay
     */
    fun advanceTimeBy(delayTime: Long, timeUnit: java.util.concurrent.TimeUnit) {
        testScheduler.advanceTimeBy(delayTime, timeUnit)
    }
}