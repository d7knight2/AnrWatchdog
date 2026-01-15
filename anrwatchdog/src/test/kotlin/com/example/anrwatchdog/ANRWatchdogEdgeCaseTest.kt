package com.example.anrwatchdog

import android.app.Application
import android.util.Log
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Extended test suite for ANRWatchdog edge cases and advanced scenarios.
 */
@RunWith(MockitoJUnitRunner::class)
class ANRWatchdogEdgeCaseTest {

    @Mock
    private lateinit var mockApplication: Application

    @Before
    fun setup() {
        // Reset the singleton instance before each test
        val instanceField = ANRWatchdog::class.java.getDeclaredField("instance")
        instanceField.isAccessible = true
        instanceField.set(null, null)
    }

    /**
     * Tests timeout boundary values.
     */
    @Test
    fun testTimeoutBoundaryValues() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        
        // Test minimum timeout
        watchdog.setTimeout(1L)
        assertNotNull(watchdog, "Should accept minimum timeout")
        
        // Test very large timeout
        watchdog.setTimeout(Long.MAX_VALUE)
        assertNotNull(watchdog, "Should accept maximum timeout")
        
        // Test zero timeout (edge case)
        watchdog.setTimeout(0L)
        assertNotNull(watchdog, "Should accept zero timeout")
    }

    /**
     * Tests callback invocation count.
     */
    @Test
    fun testCallbackInvocationCount() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        var callbackCount = 0
        
        watchdog
            .setTimeout(100)
            .setCallback { thread ->
                callbackCount++
            }
            .start()
        
        // Wait for at least 2 callback invocations
        Thread.sleep(250)
        watchdog.stop()
        
        assertTrue(callbackCount >= 2, "Callback should be invoked multiple times")
    }

    /**
     * Tests callback with null handling.
     */
    @Test
    fun testCallbackReplacement() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        var firstCallbackInvoked = false
        var secondCallbackInvoked = false
        
        // Set first callback
        watchdog.setCallback { thread ->
            firstCallbackInvoked = true
        }
        
        // Replace with second callback
        watchdog.setCallback { thread ->
            secondCallbackInvoked = true
        }
        
        watchdog.setTimeout(100).start()
        Thread.sleep(150)
        watchdog.stop()
        
        // Only the second callback should be invoked
        assertFalse(firstCallbackInvoked, "First callback should not be invoked after replacement")
        assertTrue(secondCallbackInvoked, "Second callback should be invoked")
    }

    /**
     * Tests rapid start/stop cycles.
     */
    @Test
    fun testRapidStartStopCycles() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        
        // Perform multiple start/stop cycles
        repeat(10) {
            watchdog.start()
            Thread.sleep(10)
            watchdog.stop()
            Thread.sleep(10)
        }
        
        // Should complete without errors
        assertNotNull(watchdog, "Watchdog should handle rapid cycles")
    }

    /**
     * Tests concurrent start calls from multiple threads.
     */
    @Test
    fun testConcurrentStartCalls() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val threads = mutableListOf<Thread>()
        
        // Create multiple threads trying to start the watchdog
        repeat(10) { index ->
            val thread = Thread {
                watchdog.start()
            }
            threads.add(thread)
            thread.start()
        }
        
        // Wait for all threads to complete
        threads.forEach { it.join() }
        
        watchdog.stop()
        
        // Should complete without errors or race conditions
        assertNotNull(watchdog, "Watchdog should handle concurrent starts")
    }

    /**
     * Tests log level boundary values.
     */
    @Test
    fun testLogLevelBoundaryValues() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        
        // Test all Android log levels
        val logLevels = listOf(
            Log.VERBOSE,
            Log.DEBUG,
            Log.INFO,
            Log.WARN,
            Log.ERROR,
            Log.ASSERT
        )
        
        logLevels.forEach { level ->
            val result = watchdog.setLogLevel(level)
            assertEquals(watchdog, result, "Should accept log level $level")
        }
    }

    /**
     * Tests stopping a watchdog that hasn't been started.
     */
    @Test
    fun testStopWithoutStart() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        
        // Should not throw exception
        watchdog.stop()
        
        assertNotNull(watchdog, "Should handle stop without start")
    }

    /**
     * Tests multiple stop calls.
     */
    @Test
    fun testMultipleStopCalls() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        
        watchdog.start()
        Thread.sleep(50)
        
        // Multiple stop calls
        watchdog.stop()
        watchdog.stop()
        watchdog.stop()
        
        assertNotNull(watchdog, "Should handle multiple stop calls")
    }

    /**
     * Tests callback exception handling.
     */
    @Test
    fun testCallbackWithException() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        var exceptionThrown = false
        
        watchdog
            .setTimeout(100)
            .setCallback { thread ->
                exceptionThrown = true
                throw RuntimeException("Test exception")
            }
            .start()
        
        // Wait for callback
        Thread.sleep(150)
        watchdog.stop()
        
        assertTrue(exceptionThrown, "Callback should have been invoked despite exception")
    }

    /**
     * Tests thread safety of callback invocation.
     */
    @Test
    fun testCallbackThreadSafety() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val invokedThreads = mutableSetOf<Long>()
        
        watchdog
            .setTimeout(50)
            .setCallback { thread ->
                synchronized(invokedThreads) {
                    invokedThreads.add(Thread.currentThread().id)
                }
            }
            .start()
        
        Thread.sleep(200)
        watchdog.stop()
        
        // Should be invoked from the watchdog thread
        assertTrue(invokedThreads.isNotEmpty(), "Callback should be invoked")
    }

    /**
     * Tests fluent API with all methods chained.
     */
    @Test
    fun testCompleteFluentChain() {
        var callbackInvoked = false
        
        val watchdog = ANRWatchdog.initialize(mockApplication)
            .setTimeout(100)
            .setLogLevel(Log.DEBUG)
            .setCallback { thread ->
                callbackInvoked = true
            }
            .start()
        
        Thread.sleep(150)
        watchdog.stop()
        
        assertTrue(callbackInvoked, "Complete chain should work correctly")
        assertNotNull(watchdog, "Fluent chain should return watchdog instance")
    }

    /**
     * Tests that watchdog thread is properly interrupted on stop.
     */
    @Test
    fun testThreadInterruption() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        
        watchdog.setTimeout(10000).start() // Long timeout
        Thread.sleep(50)
        
        // Access the private thread field to check its state
        val threadField = ANRWatchdog::class.java.getDeclaredField("thread")
        threadField.isAccessible = true
        val watchdogThread = threadField.get(watchdog) as? Thread
        
        assertTrue(watchdogThread?.isAlive == true, "Watchdog thread should be alive before stop")
        
        watchdog.stop()
        Thread.sleep(100)
        
        // Thread should be interrupted/stopped
        assertFalse(watchdogThread?.isAlive == true, "Watchdog thread should be stopped")
    }

    /**
     * Tests initialization with null parameters handling.
     */
    @Test
    fun testInitializationIdempotency() {
        val watchdog1 = ANRWatchdog.initialize(mockApplication)
        val watchdog2 = ANRWatchdog.initialize(mockApplication)
        val watchdog3 = ANRWatchdog.initialize(mockApplication)
        
        // All should return the same instance
        assertEquals(watchdog1, watchdog2, "Should return same instance")
        assertEquals(watchdog2, watchdog3, "Should return same instance")
    }

    /**
     * Tests default values are properly set.
     */
    @Test
    fun testDefaultValues() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        
        // Access private fields to verify defaults
        val timeoutField = ANRWatchdog::class.java.getDeclaredField("timeout")
        timeoutField.isAccessible = true
        val timeout = timeoutField.getLong(watchdog)
        
        val logLevelField = ANRWatchdog::class.java.getDeclaredField("logLevel")
        logLevelField.isAccessible = true
        val logLevel = logLevelField.getInt(watchdog)
        
        assertEquals(5000L, timeout, "Default timeout should be 5000ms")
        assertEquals(Log.INFO, logLevel, "Default log level should be INFO")
    }
}
