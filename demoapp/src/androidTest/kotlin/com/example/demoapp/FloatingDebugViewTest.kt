package com.example.demoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.demoapp.debug.DebugInfoCollector
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Instrumented test for floating debug view functionality.
 * Tests the debug info collection and display.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class FloatingDebugViewTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun cleanup() {
        scenario.close()
    }

    @Test
    fun testDebugInfoCollectorActiveThreads() {
        // Get active threads info
        val threads = DebugInfoCollector.getActiveThreads()
        
        // Verify we have at least some threads running
        assertTrue(threads.isNotEmpty(), "Expected at least one active thread")
        
        // Verify thread info contains expected fields
        val firstThread = threads.first()
        assertNotNull(firstThread.name, "Thread name should not be null")
        assertNotNull(firstThread.state, "Thread state should not be null")
        assertTrue(firstThread.id > 0, "Thread ID should be positive")
    }

    @Test
    fun testDebugInfoCollectorGeneralInfo() {
        // Get general debug info
        val debugInfo = DebugInfoCollector.getGeneralDebugInfo()
        
        // Verify debug info is not empty
        assertTrue(debugInfo.isNotEmpty(), "Expected debug info to be non-empty")
        
        // Verify it contains expected information
        assertTrue(debugInfo.any { it.contains("Memory") }, "Expected memory info")
        assertTrue(debugInfo.any { it.contains("Available Processors") }, "Expected processor info")
        assertTrue(debugInfo.any { it.contains("Active Thread Count") }, "Expected thread count info")
    }

    @Test
    fun testMainThreadBlockRecording() {
        // Clear existing blocks
        DebugInfoCollector.getRecentMainThreadBlocks().clear()
        
        // Simulate a main thread block
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        // Verify the block was recorded
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(blocks.isNotEmpty(), "Expected at least one recorded block")
        
        // Verify block has required fields
        val block = blocks.first()
        assertTrue(block.duration > 0, "Block duration should be positive")
        assertTrue(block.stackTrace.isNotEmpty(), "Block should have stack trace")
        assertTrue(block.timestamp > 0, "Block should have timestamp")
    }

    @Test
    fun testDebugInfoPersistenceAcrossTabSwitches() {
        // Record a block in Tab 1
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        val blocksInTab1 = DebugInfoCollector.getRecentMainThreadBlocks().size
        
        // Switch to Tab 2
        onView(withText("Tab 2")).perform(click())
        Thread.sleep(500)
        
        // Verify blocks persist
        val blocksInTab2 = DebugInfoCollector.getRecentMainThreadBlocks().size
        assertTrue(blocksInTab2 == blocksInTab1, 
            "Debug info should persist across tab switches")
    }

    @Test
    fun testDebugInfoUpdateAfterActivity() {
        // Perform some actions
        onView(withText("Tab 2")).perform(click())
        Thread.sleep(500)
        onView(withText("Tab 1")).perform(click())
        Thread.sleep(500)
        
        // Get thread info
        val threads = DebugInfoCollector.getActiveThreads()
        
        // Should still have active threads
        assertTrue(threads.isNotEmpty(), "Expected active threads after UI interaction")
        
        // Should include main thread
        assertTrue(threads.any { it.name.contains("main", ignoreCase = true) }, 
            "Expected main thread to be present")
    }
}
