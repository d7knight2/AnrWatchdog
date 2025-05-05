package com.dknight.anrwatchdog

import kotlinx.coroutines.*
import org.junit.Test

class DependencyAnalyzerV3UnitTest {

    @Test
    fun testDeepBlockingChain() = runBlocking {
        val jobs = (1..10).map { index ->
            launch(Dispatchers.Default + CoroutineName("Job-\$index")) {
                DependencyAnalyzerV3.logEvent("Started Job-\$index")
                delay(50)
                DependencyAnalyzerV3.logEvent("Finished Job-\$index")
            }
        }
        jobs.forEach { it.join() }

        DependencyAnalyzerV3.dump()
    }
}