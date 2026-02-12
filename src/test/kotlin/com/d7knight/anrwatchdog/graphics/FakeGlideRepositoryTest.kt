package com.d7knight.anrwatchdog.graphics

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertTrue

/**
 * Unit tests for FakeGlideRepository following TDD principles.
 * Tests cover basic functionality, coroutine behavior, and concurrent operations.
 */
class FakeGlideRepositoryTest {

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
        FakeGlideRepository.performBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started GlideJob-$index"), "Should log start message")
        assertTrue(output.contains("Finished GlideJob-$index"), "Should log finish message")
    }

    @Test
    fun testPerformBlockingOperationWithDifferentIndices() = runTest {
        // Given
        val indices = listOf(1, 2, 3, 50, 100)
        
        // When
        indices.forEach { index ->
            FakeGlideRepository.performBlockingOperation(index)
        }
        
        // Then
        val output = outputStream.toString()
        indices.forEach { index ->
            assertTrue(output.contains("Started GlideJob-$index"), "Should log start for index $index")
            assertTrue(output.contains("Finished GlideJob-$index"), "Should log finish for index $index")
        }
    }

    @Test
    fun testPerformBlockingOperationWithZeroIndex() = runTest {
        // Given
        val index = 0
        
        // When
        FakeGlideRepository.performBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started GlideJob-0"), "Should handle zero index")
        assertTrue(output.contains("Finished GlideJob-0"), "Should complete with zero index")
    }

    @Test
    fun testPerformBlockingOperationWithNegativeIndex() = runTest {
        // Given
        val index = -5
        
        // When
        FakeGlideRepository.performBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Started GlideJob--5"), "Should handle negative index")
        assertTrue(output.contains("Finished GlideJob--5"), "Should complete with negative index")
    }

    @Test
    fun testConcurrentOperations() = runTest {
        // Given
        val operationCount = 10
        
        // When - Launch multiple operations concurrently
        val jobs = (1..operationCount).map { index ->
            async {
                FakeGlideRepository.performBlockingOperation(index)
            }
        }
        jobs.awaitAll()
        
        // Then
        val output = outputStream.toString()
        (1..operationCount).forEach { index ->
            assertTrue(output.contains("Started GlideJob-$index"), "Should start operation $index")
            assertTrue(output.contains("Finished GlideJob-$index"), "Should complete operation $index")
        }
    }

    @Test
    fun testOperationUsesNamedCoroutineContext() = runTest {
        // Given
        val index = 123
        
        // When
        FakeGlideRepository.performBlockingOperation(index)
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("GlideJob-$index"), "Should use named coroutine context")
    }

    @Test
    fun testOperationCompletesWithinReasonableTime() = runTest {
        // Given
        val index = 1
        val startTime = System.currentTimeMillis()
        
        // When
        FakeGlideRepository.performBlockingOperation(index)
        val duration = System.currentTimeMillis() - startTime
        
        // Then
        assertTrue(duration < 1000, "Operation should complete quickly (within 1 second)")
    }

    @Test
    fun testSequentialOperations() = runTest {
        // Given
        val indices = listOf(1, 2, 3)
        
        // When - Execute operations sequentially
        indices.forEach { index ->
            FakeGlideRepository.performBlockingOperation(index)
        }
        
        // Then
        val output = outputStream.toString()
        // Verify all operations completed
        indices.forEach { index ->
            assertTrue(output.contains("Started GlideJob-$index"))
            assertTrue(output.contains("Finished GlideJob-$index"))
        }
    }
}
