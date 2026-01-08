package com.example.demoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.demoapp.debug.DebugInfoCollector
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * Enhanced instrumented test for various ANR simulation scenarios.
 * Tests different conditions that can lead to ANRs.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class EnhancedAnrSimulationTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        DebugInfoCollector.clearAllLogs()
    }

    @After
    fun cleanup() {
        scenario.close()
    }

    @Test
    fun testShortBlockDetection() {
        // Simulate a short block (< 1 second)
        val startTime = System.currentTimeMillis()
        DebugInfoCollector.recordMainThreadBlock(500, "Short block stack trace")
        val endTime = System.currentTimeMillis()
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(blocks.isNotEmpty(), "Short block should be recorded")
        
        val block = blocks.first()
        assertTrue(block.duration == 500L, "Block duration should be 500ms")
    }

    @Test
    fun testMediumBlockDetection() {
        // Simulate a medium block (1-3 seconds)
        DebugInfoCollector.recordMainThreadBlock(1500, "Medium block stack trace")
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(blocks.isNotEmpty(), "Medium block should be recorded")
        
        val block = blocks.first()
        assertTrue(block.duration == 1500L, "Block duration should be 1500ms")
    }

    @Test
    fun testLongBlockDetection() {
        // Simulate actual ANR with button click
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(blocks.isNotEmpty(), "Long block should be recorded")
        
        val block = blocks.first()
        assertTrue(block.duration >= 1900, "Block duration should be at least 1900ms")
    }

    @Test
    fun testMultipleConsecutiveBlocks() {
        // Simulate multiple blocks in succession
        for (i in 1..3) {
            onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
            Thread.sleep(3000)
        }
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(blocks.size >= 3, "Should record at least 3 consecutive blocks")
        
        // Verify they are in chronological order (most recent first)
        for (i in 0 until blocks.size - 1) {
            assertTrue(blocks[i].timestamp >= blocks[i + 1].timestamp,
                "Blocks should be ordered by timestamp (newest first)")
        }
    }

    @Test
    fun testBlocksDuringTabSwitching() {
        // Record blocks while switching tabs
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        val blocksBeforeSwitch = DebugInfoCollector.getRecentMainThreadBlocks().size
        
        // Switch tabs
        onView(withText("Tab 2")).perform(click())
        Thread.sleep(500)
        
        // Record another block
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        val blocksAfterSwitch = DebugInfoCollector.getRecentMainThreadBlocks().size
        assertTrue(blocksAfterSwitch == blocksBeforeSwitch + 1,
            "Should have one more block after second simulation")
    }

    @Test
    fun testBlockStackTraceContent() {
        // Simulate a block
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(blocks.isNotEmpty(), "Block should be recorded")
        
        val block = blocks.first()
        val stackTrace = block.stackTrace
        
        // Verify stack trace contains expected method
        assertTrue(stackTrace.contains("simulateMainThreadBlock"),
            "Stack trace should contain the blocking method")
        
        // Verify stack trace format
        assertTrue(stackTrace.contains("at "),
            "Stack trace should be properly formatted")
    }

    @Test
    fun testBlockTimestampAccuracy() {
        val beforeTime = System.currentTimeMillis()
        
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        val afterTime = System.currentTimeMillis()
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(blocks.isNotEmpty(), "Block should be recorded")
        
        val block = blocks.first()
        assertTrue(block.timestamp >= beforeTime,
            "Block timestamp should be after start time")
        assertTrue(block.timestamp <= afterTime,
            "Block timestamp should be before end time")
    }

    @Test
    fun testRapidBlockSequence() {
        // Record multiple blocks rapidly without waiting
        for (i in 1..5) {
            DebugInfoCollector.recordMainThreadBlock(100L * i, "Rapid block $i")
        }
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(blocks.size == 5, "Should record all 5 rapid blocks")
        
        // Verify all blocks are unique
        val durations = blocks.map { it.duration }.toSet()
        assertTrue(durations.size == 5, "All blocks should have unique durations")
    }

    @Test
    fun testBlocksWithDifferentStackTraces() {
        // Record blocks with different stack traces
        DebugInfoCollector.recordMainThreadBlock(1000, "Stack trace from method A")
        DebugInfoCollector.recordMainThreadBlock(2000, "Stack trace from method B")
        DebugInfoCollector.recordMainThreadBlock(3000, "Stack trace from method C")
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(blocks.size >= 3, "Should record all blocks")
        
        // Verify stack traces are different
        val stackTraces = blocks.take(3).map { it.stackTrace }.toSet()
        assertTrue(stackTraces.size == 3, "All blocks should have unique stack traces")
    }
}
