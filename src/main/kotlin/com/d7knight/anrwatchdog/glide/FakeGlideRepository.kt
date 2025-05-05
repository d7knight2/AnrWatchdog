package com.d7knight.anrwatchdog.graphics

import kotlinx.coroutines.*

object FakeGlideRepository {
    fun performBlockingOperation(index: Int) = runBlocking {
        launch(Dispatchers.Default + CoroutineName("GlideJob-$index")) {
            println("Started GlideJob-$index")
            delay(50)
            println("Finished GlideJob-$index")
        }.join()
    }
}