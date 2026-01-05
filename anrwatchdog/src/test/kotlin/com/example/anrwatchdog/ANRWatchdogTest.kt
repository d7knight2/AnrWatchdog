package com.example.anrwatchdog

import android.app.Application
import android.util.Log
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
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
}
