package com.d7knight.anrwatchdog.rxjava

import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.reactivestreams.Publisher

/**
 * SlowRxExperimentEnabledRepository simulates slow RxJava operations for testing
 * ANR detection and coroutine debugging in RxJava-based legacy code.
 *
 * This repository uses RxJava's TestScheduler to provide deterministic timing control
 * for test scenarios, allowing precise testing of timeout and ANR detection logic
 * without relying on actual delays.
 *
 * Key features:
 * - Controlled timing via TestScheduler
 * - Conversion from RxJava Observable to Reactive Streams Publisher
 * - Backpressure handling with BUFFER strategy
 *
 * Example usage:
 * ```kotlin
 * val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(1)
 * // Simulate passage of time
 * SlowRxExperimentEnabledRepository.advanceTimeBy(100, TimeUnit.MILLISECONDS)
 * ```
 */
object SlowRxExperimentEnabledRepository {
    /** TestScheduler for deterministic time control in tests */
    private val testScheduler = TestScheduler()

    /**
     * Performs a simulated slow operation using RxJava Observable.
     *
     * The operation emits two events:
     * 1. Immediate "Started" event
     * 2. Delayed "Finished" event after 100ms (test scheduler time)
     *
     * @param index Unique identifier for this operation
     * @return Publisher that emits operation status messages
     */
    fun performSlowOperation(index: Int): Publisher<String> {
        return Observable.create<String> { emitter ->
            emitter.onNext("Started SlowRxExperimentJob-$index")
            // Schedule completion after 100ms (virtual time)
            testScheduler.scheduleDirect({
                emitter.onNext("Finished SlowRxExperimentJob-$index")
                emitter.onComplete()
            }, 100, java.util.concurrent.TimeUnit.MILLISECONDS)
        }.subscribeOn(testScheduler).toFlowable(io.reactivex.BackpressureStrategy.BUFFER)
    }

    /**
     * Advances the test scheduler's virtual time by the specified amount.
     *
     * This method is essential for testing time-dependent RxJava operations
     * without actually waiting. It triggers all scheduled actions whose
     * execution time has been reached.
     *
     * @param delayTime Amount of time to advance
     * @param timeUnit Unit of time (MILLISECONDS, SECONDS, etc.)
     */
    fun advanceTimeBy(delayTime: Long, timeUnit: java.util.concurrent.TimeUnit) {
        testScheduler.advanceTimeBy(delayTime, timeUnit)
    }
}