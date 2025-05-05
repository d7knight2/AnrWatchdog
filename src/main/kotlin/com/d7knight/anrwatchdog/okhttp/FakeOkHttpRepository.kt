package com.d7knight.anrwatchdog.network

import kotlinx.coroutines.*

object FakeOkHttpRepository {
    fun performBlockingOperation(index: Int) = runBlocking {
        launch(Dispatchers.Default + CoroutineName("OkHttpJob-$index")) {
            println("Started OkHttpJob-$index")
            delay(50)
            println("Finished OkHttpJob-$index")
        }.join()
    }
}