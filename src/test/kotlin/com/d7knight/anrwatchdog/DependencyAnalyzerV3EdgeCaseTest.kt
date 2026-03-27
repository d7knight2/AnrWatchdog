package com.d7knight.anrwatchdog

import kotlinx.coroutines.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue
import kotlin.test.assertEquals

/**
 * Edge case unit tests for DependencyAnalyzerV3 following TDD principles.
 * Tests cover thread safety, concurrent access, and boundary conditions.
 */
class DependencyAnalyzerV3EdgeCaseTest {

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
        
        // Clear any previous state by creating a fresh instance
        // Note: Since it's an object, we can't truly reset it, but we'll work with cumulative logs
    }

    @After
    fun tearDown() {
        // Restore standard output
        System.setOut(originalOut)
    }

    @Test
    fun testLogEventWithEmptyString() {
        // Given
        val event = ""
        
        // When
        DependencyAnalyzerV3.logEvent(event)
        DependencyAnalyzerV3.dump()
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Dependency Analyzer Dump Start"), "Should show dump start")
        assertTrue(output.contains("Dependency Analyzer Dump End"), "Should show dump end")
    }

    @Test
    fun testLogEventWithSpecialCharacters() {
        // Given
        val event = "Event with special chars: !@#\$%^&*()"
        
        // When
        DependencyAnalyzerV3.logEvent(event)
        DependencyAnalyzerV3.dump()
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains(event), "Should log event with special characters")
        assertTrue(output.contains("["), "Should include thread name in brackets")
    }

    @Test
    fun testLogEventFromMultipleThreadsConcurrently() = runBlocking {
        // Given
        val threadCount = 10
        val eventsPerThread = 5
        val latch = CountDownLatch(threadCount)
        
        // When - Launch multiple threads logging concurrently
        repeat(threadCount) { threadIndex ->
            launch(Dispatchers.Default) {
                repeat(eventsPerThread) { eventIndex ->
                    DependencyAnalyzerV3.logEvent("Thread-$threadIndex Event-$eventIndex")
                }
                latch.countDown()
            }
        }
        
        // Wait for all threads to complete
        latch.await(5, TimeUnit.SECONDS)
        
        // Then
        DependencyAnalyzerV3.dump()
        val output = outputStream.toString()
        
        // Verify all events were logged (might not be in order due to concurrency)
        repeat(threadCount) { threadIndex ->
            repeat(eventsPerThread) { eventIndex ->
                assertTrue(
                    output.contains("Thread-$threadIndex Event-$eventIndex"),
                    "Should contain all logged events from all threads"
                )
            }
        }
    }

    @Test
    fun testLogEventPreservesThreadName() {
        // Given
        val eventText = "Test event"
        val currentThreadName = Thread.currentThread().name
        
        // When
        DependencyAnalyzerV3.logEvent(eventText)
        DependencyAnalyzerV3.dump()
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("[$currentThreadName]"), "Should include current thread name")
        assertTrue(output.contains(eventText), "Should include event text")
    }

    @Test
    fun testLogEventWithLongString() {
        // Given
        val longEvent = "A".repeat(10000)
        
        // When
        DependencyAnalyzerV3.logEvent(longEvent)
        DependencyAnalyzerV3.dump()
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains(longEvent), "Should handle long event strings")
    }

    @Test
    fun testDumpWithNoEvents() {
        // Given - No events logged yet (in a fresh state)
        
        // When
        DependencyAnalyzerV3.dump()
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Dependency Analyzer Dump Start"), "Should show dump start marker")
        assertTrue(output.contains("Dependency Analyzer Dump End"), "Should show dump end marker")
    }

    @Test
    fun testMultipleDumpsShowAccumulatedEvents() {
        // Given
        val event1 = "First event"
        val event2 = "Second event"
        
        // When
        DependencyAnalyzerV3.logEvent(event1)
        DependencyAnalyzerV3.dump()
        
        outputStream.reset() // Clear output buffer
        
        DependencyAnalyzerV3.logEvent(event2)
        DependencyAnalyzerV3.dump()
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains(event1), "Second dump should show first event")
        assertTrue(output.contains(event2), "Second dump should show second event")
    }

    @Test
    fun testLogEventWithMultilineString() {
        // Given
        val multilineEvent = "Line 1\nLine 2\nLine 3"
        
        // When
        DependencyAnalyzerV3.logEvent(multilineEvent)
        DependencyAnalyzerV3.dump()
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains("Line 1"), "Should contain first line")
        assertTrue(output.contains("Line 2"), "Should contain second line")
        assertTrue(output.contains("Line 3"), "Should contain third line")
    }

    @Test
    fun testLogEventFromNamedCoroutine() = runBlocking {
        // Given
        val eventText = "Coroutine event"
        
        // When
        launch(CoroutineName("CustomCoroutine")) {
            DependencyAnalyzerV3.logEvent(eventText)
        }.join()
        
        DependencyAnalyzerV3.dump()
        
        // Then
        val output = outputStream.toString()
        assertTrue(output.contains(eventText), "Should log event from named coroutine")
        // The thread name will include coroutine context but may vary
        assertTrue(output.contains("["), "Should include thread name brackets")
    }
}
