package com.d7knight.anrwatchdog.experimental

import kotlinx.coroutines.*

object ExperimentCheckRepository {
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