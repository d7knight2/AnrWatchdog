package com.example.demoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test for memory leak detection with LeakCanary integration.
 * 
 * Note: LeakCanary is configured as debugImplementation, so these tests
 * verify the integration doesn't break the app functionality. Actual leak
 * detection happens at runtime in debug builds.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MemoryLeakTest {

    @Test
    fun testActivityLifecycleWithLeakCanary() {
        // Launch and close activity multiple times to test for leaks
        repeat(3) {
            val scenario = ActivityScenario.launch(MainActivity::class.java)
            Thread.sleep(1000)
            scenario.close()
            Thread.sleep(500)
        }
        // If there are memory leaks, LeakCanary will detect them in debug builds
    }

    @Test
    fun testFragmentLifecycleWithLeakCanary() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Switch tabs multiple times to test fragment lifecycle
        repeat(5) {
            onView(withText("Tab 1")).perform(click())
            Thread.sleep(300)
            onView(withText("Tab 2")).perform(click())
            Thread.sleep(300)
            onView(withText("Tab 3")).perform(click())
            Thread.sleep(300)
        }
        
        scenario.close()
        // LeakCanary will detect fragment leaks if any exist
    }

    @Test
    fun testAnrSimulationDoesNotCauseLeak() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Trigger ANR simulation multiple times
        repeat(3) {
            onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
            Thread.sleep(3000)
        }
        
        scenario.close()
        // Verify ANR simulation doesn't cause memory leaks
    }

    @Test
    fun testFloatingDebugViewDoesNotCauseLeak() {
        // Launch activity (which creates floating debug view)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        
        // Let the debug view update a few times
        Thread.sleep(5000)
        
        // Close activity (should properly clean up debug view)
        scenario.close()
        
        // LeakCanary will detect if FloatingDebugView causes leaks
    }

    @Test
    fun testMultipleActivityRecreations() {
        // Test recreating activity multiple times (simulating config changes)
        repeat(3) {
            val scenario = ActivityScenario.launch(MainActivity::class.java)
            
            // Interact with UI
            onView(withText("Tab 2")).perform(click())
            Thread.sleep(500)
            
            // Recreate activity
            scenario.recreate()
            Thread.sleep(1000)
            
            scenario.close()
            Thread.sleep(500)
        }
    }
}
