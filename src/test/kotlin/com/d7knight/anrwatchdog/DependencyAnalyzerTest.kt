package com.d7knight.anrwatchdog

import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlinx.coroutines.*

/**
 * Comprehensive test suite for DependencyAnalyzerV3.
 * Tests event logging, dump functionality, and thread-safe concurrent access.
 */
class DependencyAnalyzerTest {

    @Before
    fun setup() {
        // Note: We can't easily clear the dependencies list since it's private
        // Each test should use unique event names to avoid conflicts
    }

    /**
     * Tests basic event logging functionality.
     */
    @Test
    fun testLogEvent() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        DependencyAnalyzerV3.logEvent("Test event 1")
        DependencyAnalyzerV3.dump()

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("Test event 1"), "Event should be logged")
        assertTrue(output.contains("Dependency Analyzer Dump Start"), "Dump should have start marker")
        assertTrue(output.contains("Dependency Analyzer Dump End"), "Dump should have end marker")
    }

    /**
     * Tests that logged events include thread names.
     */
    @Test
    fun testEventIncludesThreadName() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        val threadName = Thread.currentThread().name
        DependencyAnalyzerV3.logEvent("Event with thread name")
        DependencyAnalyzerV3.dump()

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains("[$threadName]"), "Event should include thread name")
        assertTrue(output.contains("Event with thread name"), "Event content should be preserved")
    }

    /**
     * Tests concurrent event logging from multiple threads.
     */
    @Test
    fun testConcurrentEventLogging() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out

        // Log events from multiple coroutines concurrently
        val jobs = (1..10).map { index ->
            launch(Dispatchers.Default + CoroutineName("Worker-$index")) {
                DependencyAnalyzerV3.logEvent("Concurrent event $index")
                delay(10) // Small delay to simulate work
                DependencyAnalyzerV3.logEvent("Concurrent event $index completed")
            }
        }

        // Wait for all jobs to complete
        jobs.forEach { it.join() }

        System.setOut(printStream)
        DependencyAnalyzerV3.dump()
        System.setOut(originalOut)

        val output = outputStream.toString()

        // Verify all events were logged
        (1..10).forEach { index ->
            assertTrue(
                output.contains("Concurrent event $index"),
                "Should contain event from worker $index"
            )
        }
    }

    /**
     * Tests dump output format.
     */
    @Test
    fun testDumpFormat() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        DependencyAnalyzerV3.logEvent("Format test event")
        DependencyAnalyzerV3.dump()

        System.setOut(originalOut)
        val output = outputStream.toString()

        val lines = output.lines()
        
        // Check that output has proper markers
        assertTrue(
            lines.any { it.contains("Dependency Analyzer Dump Start") },
            "Should have start marker"
        )
        assertTrue(
            lines.any { it.contains("Dependency Analyzer Dump End") },
            "Should have end marker"
        )
    }

    /**
     * Tests multiple dump calls.
     */
    @Test
    fun testMultipleDumps() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out

        DependencyAnalyzerV3.logEvent("Multi dump test event")

        // First dump
        System.setOut(printStream)
        DependencyAnalyzerV3.dump()
        val output1 = outputStream.toString()
        outputStream.reset()

        // Second dump (should show same events)
        DependencyAnalyzerV3.dump()
        val output2 = outputStream.toString()

        System.setOut(originalOut)

        assertTrue(output1.contains("Multi dump test event"), "First dump should contain event")
        assertTrue(output2.contains("Multi dump test event"), "Second dump should contain event")
    }

    /**
     * Tests logging with special characters.
     */
    @Test
    fun testLogEventWithSpecialCharacters() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        val specialEvent = "Event with special chars: []{}()@#$%^&*"
        DependencyAnalyzerV3.logEvent(specialEvent)
        DependencyAnalyzerV3.dump()

        System.setOut(originalOut)
        val output = outputStream.toString()

        assertTrue(output.contains(specialEvent), "Special characters should be preserved")
    }

    /**
     * Tests logging empty strings.
     */
    @Test
    fun testLogEmptyString() {
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        DependencyAnalyzerV3.logEvent("")
        DependencyAnalyzerV3.dump()

        System.setOut(originalOut)
        val output = outputStream.toString()

        // Should still log thread name even with empty event
        assertTrue(output.contains("[${Thread.currentThread().name}]"), "Should log thread name")
    }

    /**
     * Tests thread-safety of the CopyOnWriteArrayList.
     */
    @Test(timeout = 10000)
    fun testThreadSafetyUnderLoad() = runBlocking {
        // Stress test with many concurrent operations
        val jobs = (1..100).map { index ->
            launch(Dispatchers.Default) {
                repeat(10) {
                    DependencyAnalyzerV3.logEvent("Load test $index-$it")
                    yield() // Give other coroutines a chance
                }
            }
        }

        jobs.forEach { it.join() }

        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        DependencyAnalyzerV3.dump()

        System.setOut(originalOut)
        val output = outputStream.toString()

        // Verify dump completed successfully (doesn't crash)
        assertTrue(output.contains("Dependency Analyzer Dump Start"), "Dump should complete successfully")
        assertTrue(output.contains("Dependency Analyzer Dump End"), "Dump should complete successfully")
    }
}
