package com.d7knight.anrwatchdog.network

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertTrue

/**
 * Unit tests for FakeOkHttpRepository following TDD principles.
 * Tests cover basic functionality, coroutine behavior, and concurrent operations.
 */
class FakeOkHttpRepositoryTest {

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
    fun testPerformBlockingOperationCompletesSuccessfully() = runTest {
        // Given
        val index = 1
        
        // When
        FakeOkHttpRepository.performBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started OkHttpJob-$index"), "Should log start message")
        assertTrue(output.contains("Finished OkHttpJob-$index"), "Should log finish message")
    }

    @Test
    fun testPerformBlockingOperationWithDifferentIndices() = runTest {
        // Given
        val indices = listOf(1, 2, 3, 100, 999)
        
        // When
        indices.forEach { index ->
            FakeOkHttpRepository.performBlockingOperation(index)
        }
        
        // Then
        val output = outputStream.toString()
        indices.forEach { index ->
            assertTrue(output.contains("Started OkHttpJob-$index"), "Should log start for index $index")
            assertTrue(output.contains("Finished OkHttpJob-$index"), "Should log finish for index $index")
        }
    }

    @Test
    fun testPerformBlockingOperationWithZeroIndex() = runTest {
        // Given
        val index = 0
        
        // When
        FakeOkHttpRepository.performBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started OkHttpJob-0"), "Should handle zero index")
        assertTrue(output.contains("Finished OkHttpJob-0"), "Should complete with zero index")
    }

    @Test
    fun testPerformBlockingOperationWithNegativeIndex() = runTest {
        // Given
        val index = -1
        
        // When
        FakeOkHttpRepository.performBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started OkHttpJob--1"), "Should handle negative index")
        assertTrue(output.contains("Finished OkHttpJob--1"), "Should complete with negative index")
    }

    @Test
    fun testConcurrentOperations() = runTest {
        // Given
        val operationCount = 5
        
        // When - Launch multiple operations concurrently
        val jobs = (1..operationCount).map { index ->
            async {
                FakeOkHttpRepository.performBlockingOperation(index)
            }
        }
        jobs.awaitAll()
        
        // Then
        val output = outputStream.toString()
        (1..operationCount).forEach { index ->
            assertTrue(output.contains("Started OkHttpJob-$index"), "Should start operation $index")
            assertTrue(output.contains("Finished OkHttpJob-$index"), "Should complete operation $index")
        }
    }

    @Test
    fun testOperationUsesNamedCoroutineContext() = runTest {
        // Given
        val index = 42
        
        // When
        FakeOkHttpRepository.performBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("OkHttpJob-$index"), "Should use named coroutine context")
    }

    @Test
    fun testOperationCompletesWithinReasonableTime() = runTest {
        // Given
        val index = 1
        val startTime = System.currentTimeMillis()
        
        // When
        FakeOkHttpRepository.performBlockingOperation(index)
        val duration = System.currentTimeMillis() - startTime
        
        // Then
        assertTrue(duration < 1000, "Operation should complete quickly (within 1 second)")
    }
}
