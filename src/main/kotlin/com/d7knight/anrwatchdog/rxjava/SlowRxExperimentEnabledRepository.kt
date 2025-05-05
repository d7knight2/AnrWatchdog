package com.d7knight.anrwatchdog.rxjava

import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.reactivestreams.Publisher

object SlowRxExperimentEnabledRepository {
    private val testScheduler = TestScheduler()

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