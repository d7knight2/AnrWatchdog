package com.d7knight.anrwatchdog.experimental

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertTrue

/**
 * Unit tests for ExperimentCheckRepository following TDD principles.
 * Tests cover basic functionality, coroutine behavior, and concurrent operations.
 */
class ExperimentCheckRepositoryTest {

    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var printStream: PrintStream
    private lateinit var originalOut: PrintStream

    @Before
    fun setup() {
        // Capture standard output
        outputStream = ByteArrayOutputStream()
        printStream = PrintStream(outputStream)
        originalOut = System.out
        System.setOut(printStream)
    }

    @Test
    fun testPerformNonBlockingOperationCompletesSuccessfully() = runTest {
        // Given
        val index = 1
        
        // When
        ExperimentCheckRepository.performNonBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started ExperimentJob-$index"), "Should log start message")
        assertTrue(output.contains("Finished ExperimentJob-$index"), "Should log finish message")
    }

    @Test
    fun testPerformNonBlockingOperationWithDifferentIndices() = runTest {
        // Given
        val indices = listOf(1, 2, 3, 10, 50)
        
        // When
        indices.forEach { index ->
            ExperimentCheckRepository.performNonBlockingOperation(index)
        }
        
        // Then
        val output = outputStream.toString()
        indices.forEach { index ->
            assertTrue(output.contains("Started ExperimentJob-$index"), "Should log start for index $index")
            assertTrue(output.contains("Finished ExperimentJob-$index"), "Should log finish for index $index")
        }
    }

    @Test
    fun testPerformNonBlockingOperationWithZeroIndex() = runTest {
        // Given
        val index = 0
        
        // When
        ExperimentCheckRepository.performNonBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started ExperimentJob-0"), "Should handle zero index")
        assertTrue(output.contains("Finished ExperimentJob-0"), "Should complete with zero index")
    }

    @Test
    fun testPerformNonBlockingOperationWithNegativeIndex() = runTest {
        // Given
        val index = -10
        
        // When
        ExperimentCheckRepository.performNonBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started ExperimentJob--10"), "Should handle negative index")
        assertTrue(output.contains("Finished ExperimentJob--10"), "Should complete with negative index")
    }

    @Test
    fun testConcurrentNonBlockingOperations() = runTest {
        // Given
        val operationCount = 8
        
        // When - Launch multiple operations concurrently
        val jobs = (1..operationCount).map { index ->
            async {
                ExperimentCheckRepository.performNonBlockingOperation(index)
            }
        }
        jobs.awaitAll()
        
        // Then
        val output = outputStream.toString()
        (1..operationCount).forEach { index ->
            assertTrue(output.contains("Started ExperimentJob-$index"), "Should start operation $index")
            assertTrue(output.contains("Finished ExperimentJob-$index"), "Should complete operation $index")
        }
    }

    @Test
    fun testOperationUsesNamedCoroutineContext() = runTest {
        // Given
        val index = 999
        
        // When
        ExperimentCheckRepository.performNonBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("ExperimentJob-$index"), "Should use named coroutine context")
    }

    @Test
    fun testOperationCompletesWithinReasonableTime() = runTest {
        // Given
        val index = 1
        val startTime = System.currentTimeMillis()
        
        // When
        ExperimentCheckRepository.performNonBlockingOperation(index)
        val duration = System.currentTimeMillis() - startTime
        
        // Then
        assertTrue(duration < 1000, "Operation should complete quickly (within 1 second)")
    }

    @Test
    fun testSequentialNonBlockingOperations() = runTest {
        // Given
        val indices = listOf(1, 2, 3, 4, 5)
        
        // When - Execute operations sequentially
        indices.forEach { index ->
            ExperimentCheckRepository.performNonBlockingOperation(index)
        }
        
        // Then
        val output = outputStream.toString()
        // Verify all operations completed
        indices.forEach { index ->
            assertTrue(output.contains("Started ExperimentJob-$index"))
            assertTrue(output.contains("Finished ExperimentJob-$index"))
        }
    }

    @Test
    fun testCoroutineScopeHandlesChildCoroutine() = runTest {
        // Given
        val index = 100
        
        // When
        ExperimentCheckRepository.performNonBlockingOperation(index)
        
        // Then - The operation should complete without exceptions
        val output = outputStream.toString()
        assertTrue(output.contains("Started ExperimentJob-$index"))
        assertTrue(output.contains("Finished ExperimentJob-$index"))
    }
}
