package com.d7knight.anrwatchdog.rxjava

import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.reactivestreams.Publisher

object SlowRxExperimentEnabledRepository {
    private val testScheduler = TestScheduler()

    // INTENTIONAL COMPILATION ERROR FOR TESTING FLYCI WINGMAN:
    // This line attempts to assign a String to an Int variable, causing a type mismatch error.
    // This error is deliberately introduced to test FlyCI Wingman's automated error detection
    // and fix suggestion capabilities. DO NOT MERGE - FOR TESTING ONLY.
    private val testErrorValue: Int = "This should be an Int, not a String"

    fun performSlowOperation(index: Int): Publisher<String> {
        return Observable.create<String> { emitter ->
            emitter.onNext("Started SlowRxExperimentJob-$index")
            testScheduler.scheduleDirect({
                emitter.onNext("Finished SlowRxExperimentJob-$index")
                emitter.onComplete()
            }, 100, java.util.concurrent.TimeUnit.MILLISECONDS)
        }.subscribeOn(testScheduler).toFlowable(io.reactivex.BackpressureStrategy.BUFFER)
    }

    fun advanceTimeBy(delayTime: Long, timeUnit: java.util.concurrent.TimeUnit) {
        testScheduler.advanceTimeBy(delayTime, timeUnit)
    }
}