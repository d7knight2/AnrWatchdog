package com.d7knight.anrwatchdog.blocking

import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import com.d7knight.anrwatchdog.network.FakeOkHttpRepository
import com.d7knight.anrwatchdog.graphics.FakeGlideRepository
import com.d7knight.anrwatchdog.experimental.ExperimentCheckRepository

object BlockingRxJavaInteroptRepository {
    init {
        DebugProbes.install() // Install DebugProbes globally
        DebugProbes.enableCreationStackTraces = true // Enable capturing of creation stack traces
    }

    fun performBlockingOperation(index: Int) = runBlocking {
        launch(Dispatchers.Default + CoroutineName("FakeJob-$index")) {
            println("Started FakeJob-$index")
            delay(50)
            println("Finished FakeJob-$index")
        }.join()
    }
}

fun main() = runBlocking {
    val mainJob = launch(Dispatchers.Default + CoroutineName("Main")) {
        println("Main coroutine started")
        BlockingRxJavaInteroptRepository.performBlockingOperation(1)
    }

    val okhttpJob = launch(Dispatchers.Default + CoroutineName("OkHttp")) {
        println("OkHttp coroutine started")
        com.d7knight.anrwatchdog.okhttp.FakeOkHttpRepository.performBlockingOperation(2)
    }

    val glideJob = launch(Dispatchers.Default + CoroutineName("Glide")) {
        println("Glide coroutine started")
        com.d7knight.anrwatchdog.glide.FakeGlideRepository.performBlockingOperation(3)
    }

    val experimentJob = launch(Dispatchers.Default + CoroutineName("Experiment")) {
        println("Experiment coroutine started")
        runBlocking {
            ExperimentCheckRepository.performNonBlockingOperation(4)
        }
    }

    listOf(mainJob, okhttpJob, glideJob, experimentJob).forEach { it.join() }
    println("All coroutines completed")
}