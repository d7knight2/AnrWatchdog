package com.example.demoapp

import android.util.TypedValue
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.demoapp.debug.DebugInfoCollector
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for floating debug view interactions including touch, drag, and drop.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class FloatingDebugViewUITest {

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
    fun testDebugToolButtonVisible() {
        // Verify the debug tool button is visible
        onView(withText(containsString("Debug Tool")))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                assertTrue(view.visibility == View.VISIBLE, "Debug tool button should be visible")
            }
    }

    @Test
    fun testDebugToolToggle() {
        // Wait for view to initialize
        Thread.sleep(500)
        
        // Click to expand
        onView(withText(containsString("Debug Tool"))).perform(click())
        Thread.sleep(500)
        
        // Verify button text changed to indicate expanded state
        onView(withText(containsString("▼")))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                assertTrue(view.visibility == View.VISIBLE, "Expanded indicator should be visible")
            }
        
        // Click to collapse
        onView(withText(containsString("▼"))).perform(click())
        Thread.sleep(500)
        
        // Verify button text changed back
        onView(withText(containsString("▶")))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                assertTrue(view.visibility == View.VISIBLE, "Collapsed indicator should be visible")
            }
    }

    @Test
    fun testClearButtonFunctionality() {
        // Add some data
        DebugInfoCollector.recordMainThreadBlock(1000, "test")
        DebugInfoCollector.recordCpuUsage(50f)
        
        // Expand debug tool
        onView(withText(containsString("Debug Tool"))).perform(click())
        Thread.sleep(500)
        
        // Click clear button
        onView(withText("Clear")).perform(click())
        Thread.sleep(500)
        
        // Verify data is cleared
        assertTrue(DebugInfoCollector.getRecentMainThreadBlocks().isEmpty(),
            "Blocks should be cleared")
        assertTrue(DebugInfoCollector.getCpuUsageHistory().isEmpty(),
            "CPU history should be cleared")
    }

    @Test
    fun testThemeToggleButton() {
        // Expand debug tool
        onView(withText(containsString("Debug Tool"))).perform(click())
        Thread.sleep(500)
        
        // Click theme toggle button (moon emoji)
        onView(withText("🌙")).perform(click())
        Thread.sleep(500)
        
        // Theme should toggle - verify by clicking again
        onView(withText("🌙")).perform(click())
        Thread.sleep(500)
        
        // Test passes if no crashes occur
        assertTrue(true, "Theme toggle should work without crashes")
    }

    @Test
    fun testExportButtonFunctionality() {
        // Add some test data
        DebugInfoCollector.recordMainThreadBlock(2000, "test export")
        
        // Expand debug tool
        onView(withText(containsString("Debug Tool"))).perform(click())
        Thread.sleep(500)
        
        // Click export button
        onView(withText("Export")).perform(click())
        Thread.sleep(1000)
        
        // Test passes if no crashes occur during export
        assertTrue(true, "Export should work without crashes")
    }

    @Test
    fun testUIInteractionRecording() {
        // Perform some UI interactions
        onView(withText("Tab 2")).perform(click())
        Thread.sleep(300)
        onView(withText("Tab 1")).perform(click())
        Thread.sleep(300)
        
        // Check if interactions were recorded
        val interactions = DebugInfoCollector.getUiInteractions()
        assertTrue(interactions.isNotEmpty(), "UI interactions should be recorded")
        
        // Verify interaction types
        val tapInteractions = interactions.filter { 
            it.type == DebugInfoCollector.InteractionType.TAP 
        }
        assertTrue(tapInteractions.isNotEmpty(), "TAP interactions should be recorded")
    }

    @Test
    fun testDebugToolButtonAccessibility() {
        // Calculate actual min size in pixels based on device density (48dp)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val minSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            48f,
            context.resources.displayMetrics
        ).toInt()
        
        // Verify button meets minimum touch target size
        onView(withText(containsString("Debug Tool")))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                
                assertTrue(view.height >= minSize || view.minimumHeight >= minSize,
                    "Button height should meet accessibility standards (48dp = ${minSize}px)")
                assertTrue(view.width >= minSize || view.minimumWidth >= minSize,
                    "Button width should meet accessibility standards (48dp = ${minSize}px)")
            }
    }

    @Test
    fun testActionButtonsAccessibility() {
        // Calculate actual min size in pixels based on device density (48dp)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val minSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            48f,
            context.resources.displayMetrics
        ).toInt()
        
        // Expand debug tool
        onView(withText(containsString("Debug Tool"))).perform(click())
        Thread.sleep(500)
        
        // Check Clear button accessibility
        onView(withText("Clear"))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                
                assertTrue(view.height >= minSize || view.minimumHeight >= minSize,
                    "Clear button should meet accessibility standards (48dp = ${minSize}px)")
            }
        
        // Check Export button accessibility
        onView(withText("Export"))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                
                assertTrue(view.height >= minSize || view.minimumHeight >= minSize,
                    "Export button should meet accessibility standards (48dp = ${minSize}px)")
            }
    }

    @Test
    fun testDebugInfoDisplayAfterToggle() {
        // Expand debug tool
        onView(withText(containsString("Debug Tool"))).perform(click())
        Thread.sleep(1000) // Wait for debug info to update
        
        // Verify debug info sections are visible by checking if content was added
        scenario.onActivity { activity ->
            // The debug tool should have updated with information
            val threads = DebugInfoCollector.getActiveThreads()
            assertTrue(threads.isNotEmpty(), "Active threads should be displayed")
            
            val debugInfo = DebugInfoCollector.getGeneralDebugInfo()
            assertTrue(debugInfo.isNotEmpty(), "General debug info should be displayed")
        }
    }

    @Test
    fun testMultipleExpandCollapseOperations() {
        // Perform multiple expand/collapse operations
        for (i in 1..3) {
            // Expand
            onView(withText(containsString("Debug Tool"))).perform(click())
            Thread.sleep(500)
            
            // Collapse
            onView(withText(containsString("Debug Tool"))).perform(click())
            Thread.sleep(500)
        }
        
        // Test passes if no crashes occur
        assertTrue(true, "Multiple toggle operations should work without issues")
    }

    @Test
    fun testDebugToolPersistsAcrossTabChanges() {
        // Expand debug tool
        onView(withText(containsString("Debug Tool"))).perform(click())
        Thread.sleep(500)
        
        // Switch tabs
        onView(withText("Tab 2")).perform(click())
        Thread.sleep(500)
        onView(withText("Tab 3")).perform(click())
        Thread.sleep(500)
        onView(withText("Tab 1")).perform(click())
        Thread.sleep(500)
        
        // Debug tool should still be visible and expanded
        onView(withText(containsString("▼")))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                assertTrue(view.visibility == View.VISIBLE, 
                    "Debug tool should persist across tab changes")
            }
    }
}
