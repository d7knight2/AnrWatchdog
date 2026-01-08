package com.d7knight.anrwatchdog.graphics

import kotlinx.coroutines.*

object FakeGlideRepository {
    suspend fun performBlockingOperation(index: Int) {
        withContext(Dispatchers.Default + CoroutineName("GlideJob-$index")) {
            println("Started GlideJob-$index")
            delay(50)
            println("Finished GlideJob-$index")
        }
    }
}