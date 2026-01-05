package com.example.demoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.demoapp.debug.DebugInfoCollector
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * Instrumented test for ANR simulation functionality.
 * Tests the main thread blocking detection and debug info collection.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AnrSimulationTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        // Clear any previous main thread blocks
        DebugInfoCollector.getRecentMainThreadBlocks().clear()
    }

    @After
    fun cleanup() {
        scenario.close()
    }

    @Test
    fun testAnrSimulationRecordsBlock() {
        // Click the ANR simulation button
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        
        // Wait for the simulation to complete (it blocks for 2 seconds)
        Thread.sleep(3000)
        
        // Verify that the block was recorded in DebugInfoCollector
        val recentBlocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(recentBlocks.isNotEmpty(), "Expected at least one main thread block to be recorded")
        
        // Verify the block duration is approximately 2000ms
        val lastBlock = recentBlocks.last()
        assertTrue(lastBlock.duration >= 1900, "Expected block duration to be at least 1900ms, got ${lastBlock.duration}")
        assertTrue(lastBlock.duration <= 3000, "Expected block duration to be at most 3000ms, got ${lastBlock.duration}")
    }

    @Test
    fun testAnrSimulationFromDifferentTabs() {
        // Test ANR simulation from Tab 1
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        val blocksAfterTab1 = DebugInfoCollector.getRecentMainThreadBlocks().size
        assertTrue(blocksAfterTab1 >= 1, "Expected at least 1 block after Tab 1 simulation")
        
        // Switch to Tab 2
        onView(withText("Tab 2")).perform(click())
        Thread.sleep(500)
        
        // Test ANR simulation from Tab 2
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        val blocksAfterTab2 = DebugInfoCollector.getRecentMainThreadBlocks().size
        assertTrue(blocksAfterTab2 >= 2, "Expected at least 2 blocks after Tab 2 simulation")
    }

    @Test
    fun testMultipleAnrSimulations() {
        // Simulate ANR multiple times
        for (i in 1..3) {
            onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
            Thread.sleep(3000)
        }
        
        // Verify all blocks were recorded
        val recentBlocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(recentBlocks.size >= 3, "Expected at least 3 blocks to be recorded, got ${recentBlocks.size}")
    }

    @Test
    fun testAnrSimulationStackTrace() {
        // Click the ANR simulation button
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(3000)
        
        // Verify that the block was recorded with a stack trace
        val recentBlocks = DebugInfoCollector.getRecentMainThreadBlocks()
        assertTrue(recentBlocks.isNotEmpty(), "Expected at least one main thread block")
        
        val lastBlock = recentBlocks.last()
        assertTrue(lastBlock.stackTrace.isNotEmpty(), "Expected stack trace to be non-empty")
        assertTrue(lastBlock.stackTrace.contains("simulateMainThreadBlock"), 
            "Expected stack trace to contain 'simulateMainThreadBlock'")
    }
}
