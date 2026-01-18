package com.example.anrwatchdog

import android.app.Application
import android.util.Log
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class ANRWatchdogTest {

    @Mock
    private lateinit var mockApplication: Application

    @Before
    fun setup() {
        // Reset the singleton instance before each test
        val instanceField = ANRWatchdog::class.java.getDeclaredField("instance")
        instanceField.isAccessible = true
        instanceField.set(null, null)
    }

    @Test
    fun testInitialization() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        assertNotNull(watchdog)
    }

    @Test
    fun testSetTimeout() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val result = watchdog.setTimeout(10000L)
        assertEquals(watchdog, result) // Test fluent API
    }

    @Test
    fun testSetLogLevel() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val result = watchdog.setLogLevel(Log.DEBUG)
        assertEquals(watchdog, result) // Test fluent API
    }

    @Test
    fun testSetCallback() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        var callbackInvoked = false
        val result = watchdog.setCallback { thread ->
            callbackInvoked = true
        }
        assertEquals(watchdog, result) // Test fluent API
    }

    @Test
    fun testFluentAPI() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
            .setTimeout(5000L)
            .setLogLevel(Log.INFO)
            .setCallback { thread ->
                // Callback logic
            }
        assertNotNull(watchdog)
    }

    @Test
    fun testSingletonBehavior() {
        val watchdog1 = ANRWatchdog.initialize(mockApplication)
        val watchdog2 = ANRWatchdog.initialize(mockApplication)
        assertEquals(watchdog1, watchdog2) // Should return same instance
    }

    @Test
    fun testStartWatchdog() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val result = watchdog.start()
        assertEquals(watchdog, result) // Test fluent API
        watchdog.stop()
    }

    @Test
    fun testStopWatchdog() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        watchdog.start()
        // Should not throw exception
        watchdog.stop()
    }

    @Test
    fun testMultipleStartCalls() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        watchdog.start()
        watchdog.start() // Should handle gracefully
        watchdog.stop()
    }

    @Test
    fun testCallbackInvocation() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val latch = CountDownLatch(1)
        var capturedThread: Thread? = null
        
        watchdog.setCallback { thread ->
            capturedThread = thread
            latch.countDown()
        }
        .setTimeout(100L) // Short timeout for faster test
        .start()
        
        // Wait for callback to be invoked with timeout
        val invoked = latch.await(500, TimeUnit.MILLISECONDS)
        watchdog.stop()
        
        // Verify callback was invoked
        assertTrue(invoked, "Callback should be invoked within timeout")
        assertNotNull(capturedThread, "Captured thread should not be null")
    }

    @Test
    fun testStopBeforeStart() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        // Should not throw exception
        watchdog.stop()
    }

    @Test
    fun testMultipleStopCalls() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        watchdog.start()
        watchdog.stop()
        watchdog.stop() // Should handle gracefully
    }

    @Test
    fun testSetTimeoutWithPositiveValue() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val result = watchdog.setTimeout(10000L)
        assertEquals(watchdog, result)
    }

    @Test
    fun testSetTimeoutWithZero() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        // Should accept zero timeout (edge case)
        val result = watchdog.setTimeout(0L)
        assertEquals(watchdog, result)
    }

    @Test
    fun testSetTimeoutWithNegativeValue() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        // Should accept negative timeout (edge case, may cause immediate wake)
        val result = watchdog.setTimeout(-1L)
        assertEquals(watchdog, result)
    }

    @Test
    fun testSetLogLevelVerbose() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val result = watchdog.setLogLevel(Log.VERBOSE)
        assertEquals(watchdog, result)
    }

    @Test
    fun testSetLogLevelError() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val result = watchdog.setLogLevel(Log.ERROR)
        assertEquals(watchdog, result)
    }

    @Test
    fun testChainedConfiguration() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        var callbackInvoked = false
        
        // Test full fluent API chain
        val result = watchdog
            .setTimeout(1000L)
            .setLogLevel(Log.WARN)
            .setCallback { callbackInvoked = true }
            .start()
        
        assertNotNull(result)
        assertEquals(watchdog, result)
        watchdog.stop()
    }

    @Test
    fun testStartStopRestart() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val latch = CountDownLatch(1)
        
        // Start
        watchdog.start()
        latch.await(50, TimeUnit.MILLISECONDS)
        
        // Stop
        watchdog.stop()
        latch.await(50, TimeUnit.MILLISECONDS)
        
        // Restart
        watchdog.start()
        latch.await(50, TimeUnit.MILLISECONDS)
        
        // Stop again
        watchdog.stop()
    }

    @Test
    fun testCallbackNotSetDoesNotCrash() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val latch = CountDownLatch(1)
        
        watchdog.setTimeout(100L).start()
        
        // Wait a bit and ensure watchdog doesn't crash
        latch.await(250, TimeUnit.MILLISECONDS)
        watchdog.stop()
        // Should complete without throwing exception
    }

    @Test
    fun testCallbackWithException() {
        val watchdog = ANRWatchdog.initialize(mockApplication)
        val latch = CountDownLatch(1)
        
        watchdog.setCallback { 
            latch.countDown()
            throw RuntimeException("Test exception in callback")
        }
        .setTimeout(100L)
        .start()
        
        // Wait and ensure watchdog doesn't crash even if callback throws
        val invoked = latch.await(500, TimeUnit.MILLISECONDS)
        watchdog.stop()
        
        assertTrue(invoked, "Callback should be invoked even if it throws")
        // Should complete even if callback throws
    }
}
