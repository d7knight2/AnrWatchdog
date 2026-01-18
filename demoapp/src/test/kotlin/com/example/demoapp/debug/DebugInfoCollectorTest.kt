package com.example.demoapp.debug

import android.content.Context
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for DebugInfoCollector.
 * Tests the utility functions for collecting and managing debug information.
 */
@RunWith(MockitoJUnitRunner::class)
class DebugInfoCollectorTest {

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        // Clear all logs before each test
        DebugInfoCollector.clearAllLogs()
        
        // Reset configurable parameters to defaults
        DebugInfoCollector.maxBlocks = 20
        DebugInfoCollector.maxCpuSnapshots = 50
        DebugInfoCollector.maxUiInteractions = 100
    }

    @After
    fun tearDown() {
        // Clean up after tests
        DebugInfoCollector.clearAllLogs()
    }

    @Test
    fun testRecordMainThreadBlock() {
        val duration = 1000L
        val stackTrace = "Test stack trace"
        
        DebugInfoCollector.recordMainThreadBlock(duration, stackTrace)
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertEquals(1, blocks.size)
        assertEquals(duration, blocks[0].duration)
        assertEquals(stackTrace, blocks[0].stackTrace)
        assertTrue(blocks[0].timestamp > 0)
    }

    @Test
    fun testRecordMultipleMainThreadBlocks() {
        for (i in 1..5) {
            DebugInfoCollector.recordMainThreadBlock(i * 100L, "Stack trace $i")
        }
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertEquals(5, blocks.size)
        
        // Verify order (most recent first, as blocks are added at index 0)
        assertEquals("Stack trace 5", blocks[0].stackTrace)
        assertEquals("Stack trace 1", blocks[4].stackTrace)
        
        // Verify timestamps are in descending order (most recent first)
        for (i in 0 until blocks.size - 1) {
            assertTrue(blocks[i].timestamp >= blocks[i + 1].timestamp,
                "Timestamps should be in descending order (most recent first)")
        }
    }

    @Test
    fun testMainThreadBlockMaxLimit() {
        DebugInfoCollector.maxBlocks = 10
        
        // Record more than max
        for (i in 1..15) {
            DebugInfoCollector.recordMainThreadBlock(i * 100L, "Stack trace $i")
        }
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertEquals(10, blocks.size)
        
        // Should keep most recent blocks
        assertEquals("Stack trace 15", blocks[0].stackTrace)
    }

    @Test
    fun testClearMainThreadBlocks() {
        DebugInfoCollector.recordMainThreadBlock(1000L, "Test")
        DebugInfoCollector.recordMainThreadBlock(2000L, "Test2")
        
        DebugInfoCollector.clearMainThreadBlocks()
        
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertEquals(0, blocks.size)
    }

    @Test
    fun testRecordCpuUsage() {
        DebugInfoCollector.recordCpuUsage(75.5f)
        
        val history = DebugInfoCollector.getCpuUsageHistory()
        assertEquals(1, history.size)
        assertEquals(75.5f, history[0].cpuUsagePercent, 0.01f)
        assertTrue(history[0].totalThreads > 0)
    }

    @Test
    fun testRecordCpuUsageClampingLow() {
        DebugInfoCollector.recordCpuUsage(-10f)
        
        val history = DebugInfoCollector.getCpuUsageHistory()
        assertEquals(1, history.size)
        assertEquals(0f, history[0].cpuUsagePercent, 0.01f)
    }

    @Test
    fun testRecordCpuUsageClampingHigh() {
        DebugInfoCollector.recordCpuUsage(150f)
        
        val history = DebugInfoCollector.getCpuUsageHistory()
        assertEquals(1, history.size)
        assertEquals(100f, history[0].cpuUsagePercent, 0.01f)
    }

    @Test
    fun testCpuUsageMaxLimit() {
        DebugInfoCollector.maxCpuSnapshots = 10
        
        // Record more than max
        for (i in 1..15) {
            DebugInfoCollector.recordCpuUsage(i.toFloat())
        }
        
        val history = DebugInfoCollector.getCpuUsageHistory()
        assertEquals(10, history.size)
        
        // CPU history trims from the beginning (FIFO), so oldest entries are removed
        // Most recent snapshots (6-15) should be kept
        assertEquals(6f, history[0].cpuUsagePercent, 0.01f)
        assertEquals(15f, history[9].cpuUsagePercent, 0.01f)
    }

    @Test
    fun testClearCpuUsageHistory() {
        DebugInfoCollector.recordCpuUsage(50f)
        DebugInfoCollector.recordCpuUsage(60f)
        
        DebugInfoCollector.clearCpuUsageHistory()
        
        val history = DebugInfoCollector.getCpuUsageHistory()
        assertEquals(0, history.size)
    }

    @Test
    fun testRecordUiInteraction() {
        DebugInfoCollector.recordUiInteraction(
            DebugInfoCollector.InteractionType.TAP,
            100f,
            200f,
            "Test tap"
        )
        
        val interactions = DebugInfoCollector.getUiInteractions()
        assertEquals(1, interactions.size)
        assertEquals(DebugInfoCollector.InteractionType.TAP, interactions[0].type)
        assertEquals(100f, interactions[0].x, 0.01f)
        assertEquals(200f, interactions[0].y, 0.01f)
        assertEquals("Test tap", interactions[0].details)
    }

    @Test
    fun testRecordMultipleUiInteractions() {
        val types = listOf(
            DebugInfoCollector.InteractionType.TAP,
            DebugInfoCollector.InteractionType.SCROLL,
            DebugInfoCollector.InteractionType.LONG_PRESS,
            DebugInfoCollector.InteractionType.DRAG
        )
        
        types.forEachIndexed { index, type ->
            DebugInfoCollector.recordUiInteraction(type, index.toFloat(), index.toFloat() * 2)
        }
        
        val interactions = DebugInfoCollector.getUiInteractions()
        assertEquals(4, interactions.size)
        
        types.forEachIndexed { index, type ->
            assertEquals(type, interactions[index].type)
        }
    }

    @Test
    fun testUiInteractionMaxLimit() {
        DebugInfoCollector.maxUiInteractions = 10
        
        // Record more than max
        for (i in 1..15) {
            DebugInfoCollector.recordUiInteraction(
                DebugInfoCollector.InteractionType.TAP,
                i.toFloat(),
                i.toFloat()
            )
        }
        
        val interactions = DebugInfoCollector.getUiInteractions()
        assertEquals(10, interactions.size)
        
        // UI interactions use FIFO trimming (removeAt(0)), so oldest entries (1-5) are removed
        // Kept interactions are 6-15
        assertEquals(6f, interactions[0].x, 0.01f)  // First kept interaction
        assertEquals(15f, interactions[9].x, 0.01f) // Last interaction
    }

    @Test
    fun testClearUiInteractions() {
        DebugInfoCollector.recordUiInteraction(DebugInfoCollector.InteractionType.TAP, 0f, 0f)
        DebugInfoCollector.recordUiInteraction(DebugInfoCollector.InteractionType.SCROLL, 10f, 10f)
        
        DebugInfoCollector.clearUiInteractions()
        
        val interactions = DebugInfoCollector.getUiInteractions()
        assertEquals(0, interactions.size)
    }

    @Test
    fun testClearAllLogs() {
        DebugInfoCollector.recordMainThreadBlock(1000L, "Test")
        DebugInfoCollector.recordCpuUsage(50f)
        DebugInfoCollector.recordUiInteraction(DebugInfoCollector.InteractionType.TAP, 0f, 0f)
        
        DebugInfoCollector.clearAllLogs()
        
        assertEquals(0, DebugInfoCollector.getRecentMainThreadBlocks().size)
        assertEquals(0, DebugInfoCollector.getCpuUsageHistory().size)
        assertEquals(0, DebugInfoCollector.getUiInteractions().size)
    }

    @Test
    fun testGetActiveThreads() {
        val threads = DebugInfoCollector.getActiveThreads()
        
        assertNotNull(threads)
        assertTrue(threads.isNotEmpty(), "Should have at least one active thread")
        
        // Verify thread info structure
        val firstThread = threads[0]
        assertNotNull(firstThread.name)
        assertNotNull(firstThread.state)
        assertTrue(firstThread.id > 0)
        assertTrue(firstThread.priority > 0)
    }

    @Test
    fun testGetActiveThreadsCaching() {
        val threads1 = DebugInfoCollector.getActiveThreads()
        val threads2 = DebugInfoCollector.getActiveThreads()
        
        // Should return same instance from cache
        assertEquals(threads1.size, threads2.size)
    }

    @Test
    fun testGetGeneralDebugInfo() {
        val info = DebugInfoCollector.getGeneralDebugInfo()
        
        assertNotNull(info)
        assertTrue(info.containsKey("Total Threads"))
        assertTrue(info.containsKey("Memory Used"))
        assertTrue(info.containsKey("Memory Free"))
        assertTrue(info.containsKey("Memory Total"))
        assertTrue(info.containsKey("Memory Max"))
        assertTrue(info.containsKey("Available Processors"))
        
        // Verify values are reasonable
        val totalThreads = info["Total Threads"]?.toIntOrNull()
        assertNotNull(totalThreads)
        assertTrue(totalThreads!! > 0)
    }

    @Test
    fun testFormatTimestamp() {
        val timestamp = System.currentTimeMillis()
        val formatted = DebugInfoCollector.formatTimestamp(timestamp)
        
        assertNotNull(formatted)
        assertTrue(formatted.isNotEmpty())
        // Should contain colons for time format
        assertTrue(formatted.contains(":"))
    }

    @Test
    fun testMaxBlocksSetter() {
        DebugInfoCollector.maxBlocks = 5
        assertEquals(5, DebugInfoCollector.maxBlocks)
        
        // Test minimum constraint
        DebugInfoCollector.maxBlocks = -1
        assertEquals(1, DebugInfoCollector.maxBlocks)
        
        DebugInfoCollector.maxBlocks = 0
        assertEquals(1, DebugInfoCollector.maxBlocks)
    }

    @Test
    fun testMaxCpuSnapshotsSetter() {
        DebugInfoCollector.maxCpuSnapshots = 25
        assertEquals(25, DebugInfoCollector.maxCpuSnapshots)
        
        // Test minimum constraint
        DebugInfoCollector.maxCpuSnapshots = -1
        assertEquals(1, DebugInfoCollector.maxCpuSnapshots)
    }

    @Test
    fun testMaxUiInteractionsSetter() {
        DebugInfoCollector.maxUiInteractions = 50
        assertEquals(50, DebugInfoCollector.maxUiInteractions)
        
        // Test minimum constraint
        DebugInfoCollector.maxUiInteractions = 0
        assertEquals(1, DebugInfoCollector.maxUiInteractions)
    }

    @Test
    fun testSettersTrimmingExistingData() {
        // Add 15 blocks
        for (i in 1..15) {
            DebugInfoCollector.recordMainThreadBlock(i * 100L, "Block $i")
        }
        
        // Set max to 5
        DebugInfoCollector.maxBlocks = 5
        
        // Verify trimming occurred
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertEquals(5, blocks.size)
    }

    @Test
    fun testDataClassMainThreadBlock() {
        val block = DebugInfoCollector.MainThreadBlock(
            timestamp = 123456789L,
            duration = 5000L,
            stackTrace = "Test stack"
        )
        
        assertEquals(123456789L, block.timestamp)
        assertEquals(5000L, block.duration)
        assertEquals("Test stack", block.stackTrace)
    }

    @Test
    fun testDataClassThreadInfo() {
        val threadInfo = DebugInfoCollector.ThreadInfo(
            name = "TestThread",
            state = Thread.State.RUNNABLE,
            id = 12345L,
            priority = 5,
            isDaemon = false
        )
        
        assertEquals("TestThread", threadInfo.name)
        assertEquals(Thread.State.RUNNABLE, threadInfo.state)
        assertEquals(12345L, threadInfo.id)
        assertEquals(5, threadInfo.priority)
        assertEquals(false, threadInfo.isDaemon)
    }

    @Test
    fun testDataClassCpuUsageSnapshot() {
        val snapshot = DebugInfoCollector.CpuUsageSnapshot(
            timestamp = 123456789L,
            cpuUsagePercent = 75.5f,
            totalThreads = 42
        )
        
        assertEquals(123456789L, snapshot.timestamp)
        assertEquals(75.5f, snapshot.cpuUsagePercent, 0.01f)
        assertEquals(42, snapshot.totalThreads)
    }

    @Test
    fun testDataClassUIInteraction() {
        val interaction = DebugInfoCollector.UIInteraction(
            timestamp = 123456789L,
            type = DebugInfoCollector.InteractionType.TAP,
            x = 100f,
            y = 200f,
            details = "Test details"
        )
        
        assertEquals(123456789L, interaction.timestamp)
        assertEquals(DebugInfoCollector.InteractionType.TAP, interaction.type)
        assertEquals(100f, interaction.x, 0.01f)
        assertEquals(200f, interaction.y, 0.01f)
        assertEquals("Test details", interaction.details)
    }

    @Test
    fun testInteractionTypeEnum() {
        // Verify all enum values exist
        assertEquals(DebugInfoCollector.InteractionType.TAP, DebugInfoCollector.InteractionType.valueOf("TAP"))
        assertEquals(DebugInfoCollector.InteractionType.SCROLL, DebugInfoCollector.InteractionType.valueOf("SCROLL"))
        assertEquals(DebugInfoCollector.InteractionType.LONG_PRESS, DebugInfoCollector.InteractionType.valueOf("LONG_PRESS"))
        assertEquals(DebugInfoCollector.InteractionType.DRAG, DebugInfoCollector.InteractionType.valueOf("DRAG"))
    }
}
