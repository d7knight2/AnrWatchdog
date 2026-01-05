package com.d7knight.anrwatchdog.network

import kotlinx.coroutines.*

object FakeOkHttpRepository {
    suspend fun performBlockingOperation(index: Int) {
        withContext(Dispatchers.Default + CoroutineName("OkHttpJob-$index")) {
            println("Started OkHttpJob-$index")
            delay(50)
            println("Finished OkHttpJob-$index")
        }
    }
}