package com.d7knight.anrwatchdog

import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DependencyAnalyzerV3UnitTest {

    @Before
    fun setup() {
        DebugProbes.install() // Ensure DebugProbes is installed before tests
        DebugProbes.enableCreationStackTraces = true // Enable creation stack traces
    }

    @Test
    fun testDeepBlockingChain() = runBlocking {
        val jobs = (1..10).map { index ->
            launch(Dispatchers.Default + CoroutineName("Job-$index")) {
                DependencyAnalyzerV3.logEvent("Started Job-$index")
                delay(50)
                DependencyAnalyzerV3.logEvent("Finished Job-$index")
            }
        }
        jobs.forEach { it.join() }

        println("Assertions and creation stack trace:")
        println("Jobs created: ${jobs.size}")
        jobs.forEachIndexed { index, job ->
            println("Job-$index creation stack trace:")

            // Redirect standard output to capture the coroutine dump
            val outputStream = ByteArrayOutputStream()
            val printStream = PrintStream(outputStream)
            val originalOut = System.out
            System.setOut(printStream)

            DebugProbes.dumpCoroutines() // Capture detailed coroutine information

            // Restore the original standard output
            System.setOut(originalOut)

            val stackTrace = outputStream.toString()
            val expectedMethod = "testDeepBlockingChain"
            val expectedClass = "DependencyAnalyzerV3UnitTest"
            assertTrue(stackTrace.contains(expectedMethod), "Expected stack trace to contain method: $expectedMethod")
            assertTrue(stackTrace.contains(expectedClass), "Expected stack trace to contain class: $expectedClass")
        }

        DebugProbes.dumpCoroutines() // Print detailed coroutine information for all active coroutines
        DependencyAnalyzerV3.dump()
    }
}