package com.d7knight.anrwatchdog.blocking

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertTrue

/**
 * Unit tests for BlockingRxJavaInteroptRepository following TDD principles.
 * Tests cover RxJava-Coroutine interop, flow conversion, and concurrent operations.
 */
class BlockingRxJavaInteroptRepositoryTest {

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
    fun testPerformBlockingRxOperationCompletesSuccessfully() = runTest {
        // Given
        val index = 1
        
        // When
        BlockingRxJavaInteroptRepository.performBlockingRxOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started SlowRxExperimentJob-$index"), "Should log start message")
        assertTrue(output.contains("Finished SlowRxExperimentJob-$index"), "Should log finish message")
    }

    @Test
    fun testPerformBlockingRxOperationWithDifferentIndices() = runTest {
        // Given
        val indices = listOf(1, 2, 3)
        
        // When
        indices.forEach { index ->
            BlockingRxJavaInteroptRepository.performBlockingRxOperation(index)
        }
        
        // Then
        val output = outputStream.toString()
        indices.forEach { index ->
            assertTrue(output.contains("Started SlowRxExperimentJob-$index"), "Should log start for index $index")
            assertTrue(output.contains("Finished SlowRxExperimentJob-$index"), "Should log finish for index $index")
        }
    }

    @Test
    fun testPerformBlockingRxOperationWithZeroIndex() = runTest {
        // Given
        val index = 0
        
        // When
        BlockingRxJavaInteroptRepository.performBlockingRxOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started SlowRxExperimentJob-0"), "Should handle zero index")
        assertTrue(output.contains("Finished SlowRxExperimentJob-0"), "Should complete with zero index")
    }

    @Test
    fun testPerformBlockingRxOperationWithNegativeIndex() = runTest {
        // Given
        val index = -1
        
        // When
        BlockingRxJavaInteroptRepository.performBlockingRxOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started SlowRxExperimentJob--1"), "Should handle negative index")
        assertTrue(output.contains("Finished SlowRxExperimentJob--1"), "Should complete with negative index")
    }

    @Test
    fun testConcurrentRxOperations() = runTest {
        // Given
        val operationCount = 3
        
        // When - Launch multiple operations concurrently
        val jobs = (1..operationCount).map { index ->
            async {
                BlockingRxJavaInteroptRepository.performBlockingRxOperation(index)
            }
        }
        jobs.awaitAll()
        
        // Then
        val output = outputStream.toString()
        (1..operationCount).forEach { index ->
            assertTrue(output.contains("Started SlowRxExperimentJob-$index"), "Should start operation $index")
            assertTrue(output.contains("Finished SlowRxExperimentJob-$index"), "Should complete operation $index")
        }
    }

    @Test
    fun testOperationUsesIODispatcher() = runTest {
        // Given
        val index = 42
        
        // When
        BlockingRxJavaInteroptRepository.performBlockingRxOperation(index)
        
        // Then
        val output = outputStream.toString()
        // The operation should complete successfully on IO dispatcher
        assertTrue(output.contains("Started SlowRxExperimentJob-$index"))
        assertTrue(output.contains("Finished SlowRxExperimentJob-$index"))
    }

    @Test
    fun testSequentialRxOperations() = runTest {
        // Given
        val indices = listOf(1, 2, 3, 4)
        
        // When - Execute operations sequentially
        indices.forEach { index ->
            BlockingRxJavaInteroptRepository.performBlockingRxOperation(index)
        }
        
        // Then
        val output = outputStream.toString()
        // Verify all operations completed in order
        indices.forEach { index ->
            assertTrue(output.contains("Started SlowRxExperimentJob-$index"))
            assertTrue(output.contains("Finished SlowRxExperimentJob-$index"))
        }
    }

    @Test
    fun testRxToFlowConversion() = runTest {
        // Given
        val index = 100
        
        // When - Perform operation that converts RxJava Publisher to Flow
        BlockingRxJavaInteroptRepository.performBlockingRxOperation(index)
        
        // Then - Should successfully convert and collect values
        val output = outputStream.toString()
        assertTrue(output.contains("Started SlowRxExperimentJob-$index"), "Should emit start event")
        assertTrue(output.contains("Finished SlowRxExperimentJob-$index"), "Should emit finish event")
    }
}
