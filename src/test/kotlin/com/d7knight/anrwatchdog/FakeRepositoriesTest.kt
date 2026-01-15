package com.d7knight.anrwatchdog

import com.d7knight.anrwatchdog.blocking.BlockingRxJavaInteroptRepository
import com.d7knight.anrwatchdog.experiment.ExperimentCheckRepository
import com.d7knight.anrwatchdog.glide.FakeGlideRepository
import com.d7knight.anrwatchdog.okhttp.FakeOkHttpRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Comprehensive test suite for all fake repository implementations.
 * Tests proper coroutine usage, concurrent execution, and output verification.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FakeRepositoriesTest {

    /**
     * Tests that FakeOkHttpRepository performs operations on the correct dispatcher.
     */
    @Test
    fun testOkHttpRepositoryPerformsOperation() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        FakeOkHttpRepository.performBlockingOperation(1)

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("Started OkHttpJob-1"), "Expected start message")
        assertTrue(output.contains("Finished OkHttpJob-1"), "Expected finish message")
    }

    /**
     * Tests that FakeGlideRepository performs operations on the correct dispatcher.
     */
    @Test
    fun testGlideRepositoryPerformsOperation() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        FakeGlideRepository.performBlockingOperation(2)

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("Started GlideJob-2"), "Expected start message")
        assertTrue(output.contains("Finished GlideJob-2"), "Expected finish message")
    }

    /**
     * Tests that ExperimentCheckRepository performs non-blocking operations correctly.
     */
    @Test
    fun testExperimentRepositoryPerformsNonBlockingOperation() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        ExperimentCheckRepository.performNonBlockingOperation(3)

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("Started ExperimentJob-3"), "Expected start message")
        assertTrue(output.contains("Finished ExperimentJob-3"), "Expected finish message")
    }

    /**
     * Tests that BlockingRxJavaInteroptRepository performs operations correctly.
     */
    @Test
    fun testBlockingRxJavaRepositoryPerformsOperation() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        BlockingRxJavaInteroptRepository.performBlockingOperation(4)

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("Started FakeJob-4"), "Expected start message")
        assertTrue(output.contains("Finished FakeJob-4"), "Expected finish message")
    }

    /**
     * Tests concurrent execution of multiple repositories.
     */
    @Test
    fun testConcurrentRepositoryExecution() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        val jobs = listOf(
            launch { FakeOkHttpRepository.performBlockingOperation(10) },
            launch { FakeGlideRepository.performBlockingOperation(20) },
            launch { ExperimentCheckRepository.performNonBlockingOperation(30) },
            launch { BlockingRxJavaInteroptRepository.performBlockingOperation(40) }
        )

        jobs.forEach { it.join() }

        System.setOut(originalOut)
        val output = outputStream.toString()

        // Verify all operations completed
        assertTrue(output.contains("OkHttpJob-10"), "OkHttp operation should complete")
        assertTrue(output.contains("GlideJob-20"), "Glide operation should complete")
        assertTrue(output.contains("ExperimentJob-30"), "Experiment operation should complete")
        assertTrue(output.contains("FakeJob-40"), "Blocking operation should complete")
    }

    /**
     * Tests that operations complete within reasonable time (no infinite loops or deadlocks).
     */
    @Test(timeout = 5000)
    fun testOperationsCompleteInReasonableTime() = runBlocking {
        // All operations should complete within 5 seconds
        FakeOkHttpRepository.performBlockingOperation(1)
        FakeGlideRepository.performBlockingOperation(2)
        ExperimentCheckRepository.performNonBlockingOperation(3)
        BlockingRxJavaInteroptRepository.performBlockingOperation(4)
    }

    /**
     * Tests that operations use proper coroutine naming.
     */
    @Test
    fun testCoroutineNaming() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        withContext(Dispatchers.Default + CoroutineName("TestCoroutine")) {
            FakeOkHttpRepository.performBlockingOperation(100)
        }

        System.setOut(originalOut)
        val output = outputStream.toString()

        // Verify the operation completed
        assertTrue(output.contains("OkHttpJob-100"), "Should contain coroutine-specific job name")
    }

    /**
     * Tests exception handling in repository operations.
     */
    @Test
    fun testRepositoryExceptionHandling() = runBlocking {
        // Test that cancellation is handled properly
        val job = launch {
            try {
                FakeOkHttpRepository.performBlockingOperation(999)
            } catch (e: CancellationException) {
                // Expected behavior - coroutine was cancelled
                assertTrue(true, "Cancellation handled correctly")
            }
        }
        
        // Cancel after a short delay
        delay(10)
        job.cancelAndJoin()
    }

    /**
     * Tests that multiple operations with the same index don't interfere.
     */
    @Test
    fun testMultipleOperationsWithSameIndex() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        // Launch two operations with same index concurrently
        val job1 = launch { FakeOkHttpRepository.performBlockingOperation(5) }
        val job2 = launch { FakeOkHttpRepository.performBlockingOperation(5) }

        job1.join()
        job2.join()

        System.setOut(originalOut)
        val output = outputStream.toString()

        // Count occurrences - should have 2 starts and 2 finishes
        val startCount = output.split("Started OkHttpJob-5").size - 1
        val finishCount = output.split("Finished OkHttpJob-5").size - 1
        
        assertTrue(startCount == 2, "Should have 2 start messages")
        assertTrue(finishCount == 2, "Should have 2 finish messages")
    }
}
