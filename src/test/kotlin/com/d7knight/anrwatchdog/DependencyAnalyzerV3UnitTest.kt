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
    fun shouldInvokeAllRepositoriesAndVerifyStackTraces() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

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
            com.d7knight.anrwatchdog.experiment.ExperimentCheckRepository.performNonBlockingOperation(4)
        }

        listOf(mainJob, okhttpJob, glideJob, experimentJob).forEach { it.join() }

        System.setOut(originalOut)

        val output = outputStream.toString()

        val expectedTraces = listOf(
            "BlockingRxJavaInteroptRepository",
            "FakeOkHttpRepository",
            "FakeGlideRepository",
            "ExperimentCheckRepository"
        )

        expectedTraces.forEach { trace ->
            assertTrue(output.contains(trace), "Expected output to contain trace: $trace")
        }
    }
}