package com.example.demoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.demoapp.debug.DebugInfoCollector
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
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
        // Clear all logs before each test
        DebugInfoCollector.clearAllLogs()
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
        assertTrue(debugInfo.any { it.key.contains("Memory") }, "Expected memory info")
        assertTrue(debugInfo.any { it.key.contains("Available Processors") }, "Expected processor info")
        assertTrue(debugInfo.containsKey("Total Threads"), "Expected thread count info")
    }

    @Test
    fun testMainThreadBlockRecording() {
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
    
    @Test
    fun testCpuUsageTracking() {
        // Record some CPU usage samples
        DebugInfoCollector.recordCpuUsage(25.5f)
        DebugInfoCollector.recordCpuUsage(45.2f)
        DebugInfoCollector.recordCpuUsage(60.0f)
        
        // Verify they were recorded
        val history = DebugInfoCollector.getCpuUsageHistory()
        assertTrue(history.size >= 3, "Expected at least 3 CPU snapshots")
        
        // Verify values are within valid range
        history.forEach { snapshot ->
            assertTrue(snapshot.cpuUsagePercent in 0f..100f, 
                "CPU usage should be between 0 and 100")
        }
    }
    
    @Test
    fun testUiInteractionLogging() {
        // Record some UI interactions
        DebugInfoCollector.recordUiInteraction(
            DebugInfoCollector.InteractionType.TAP, 100f, 200f, "test tap"
        )
        DebugInfoCollector.recordUiInteraction(
            DebugInfoCollector.InteractionType.SCROLL, 150f, 250f, "test scroll"
        )
        
        // Verify they were recorded
        val interactions = DebugInfoCollector.getUiInteractions()
        assertTrue(interactions.size >= 2, "Expected at least 2 UI interactions")
        
        // Verify the interactions contain expected data
        val tapInteraction = interactions.find { it.type == DebugInfoCollector.InteractionType.TAP }
        assertNotNull(tapInteraction, "Expected to find TAP interaction")
        assertEquals(100f, tapInteraction.x, "TAP x coordinate mismatch")
        assertEquals(200f, tapInteraction.y, "TAP y coordinate mismatch")
    }
    
    @Test
    fun testClearAllLogs() {
        // Add some data
        DebugInfoCollector.recordMainThreadBlock(1000, "test stack trace")
        DebugInfoCollector.recordCpuUsage(50f)
        DebugInfoCollector.recordUiInteraction(
            DebugInfoCollector.InteractionType.TAP, 100f, 200f
        )
        
        // Verify data exists
        assertTrue(DebugInfoCollector.getRecentMainThreadBlocks().isNotEmpty())
        assertTrue(DebugInfoCollector.getCpuUsageHistory().isNotEmpty())
        assertTrue(DebugInfoCollector.getUiInteractions().isNotEmpty())
        
        // Clear all logs
        DebugInfoCollector.clearAllLogs()
        
        // Verify all data is cleared
        assertTrue(DebugInfoCollector.getRecentMainThreadBlocks().isEmpty(), 
            "Main thread blocks should be empty")
        assertTrue(DebugInfoCollector.getCpuUsageHistory().isEmpty(), 
            "CPU history should be empty")
        assertTrue(DebugInfoCollector.getUiInteractions().isEmpty(), 
            "UI interactions should be empty")
    }
    
    @Test
    fun testMaxBlocksConfiguration() {
        // Set max blocks to 5
        DebugInfoCollector.maxBlocks = 5
        
        // Add 10 blocks
        for (i in 1..10) {
            DebugInfoCollector.recordMainThreadBlock(100L * i, "stack trace $i")
        }
        
        // Verify only 5 are kept
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertEquals(5, blocks.size, "Should only keep max 5 blocks")
        
        // Verify most recent blocks are kept (10, 9, 8, 7, 6)
        assertEquals(1000L, blocks[0].duration, "Most recent block should be first")
        
        // Reset to default
        DebugInfoCollector.maxBlocks = 20
    }
    
    @Test
    fun testExportLogsToFile() {
        // Add some test data
        DebugInfoCollector.recordMainThreadBlock(2000, "test block stack trace")
        DebugInfoCollector.recordCpuUsage(75f)
        DebugInfoCollector.recordUiInteraction(
            DebugInfoCollector.InteractionType.TAP, 100f, 200f, "export test"
        )
        
        // Export logs
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = DebugInfoCollector.exportLogsToFile(context, "test_export.txt")
        
        // Verify file was created
        assertNotNull(file, "Export file should not be null")
        assertTrue(file.exists(), "Export file should exist")
        assertTrue(file.length() > 0, "Export file should not be empty")
        
        // Verify file contains expected content
        val content = file.readText()
        assertTrue(content.contains("Main Thread Blocks"), "Should contain blocks section")
        assertTrue(content.contains("CPU Usage History"), "Should contain CPU section")
        assertTrue(content.contains("UI Interactions"), "Should contain UI section")
        assertTrue(content.contains("test block stack trace"), "Should contain block data")
        
        // Clean up
        file.delete()
    }
    
    @Test
    fun testMultipleThreadSimulation() {
        // Create multiple threads
        val threads = mutableListOf<Thread>()
        for (i in 1..5) {
            val thread = Thread {
                Thread.sleep(500)
            }
            thread.name = "TestThread-$i"
            thread.start()
            threads.add(thread)
        }
        
        // Wait a bit for threads to start
        Thread.sleep(200)
        
        // Get active threads
        val activeThreads = DebugInfoCollector.getActiveThreads()
        
        // Verify our test threads are present
        val testThreads = activeThreads.filter { it.name.startsWith("TestThread-") }
        assertTrue(testThreads.isNotEmpty(), "Expected to find test threads")
        
        // Wait for threads to complete
        threads.forEach { it.join() }
    }
}
