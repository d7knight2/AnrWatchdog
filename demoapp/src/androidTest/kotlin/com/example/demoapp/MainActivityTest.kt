package com.example.demoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for MainActivity.
 * Tests basic UI functionality including tab switching and button interactions.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

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
    fun testActivityLaunches() {
        // Verify activity is launched and displayed
        onView(withText("Tab 1")).check(matches(isDisplayed()))
    }

    @Test
    fun testTabButtonsDisplayed() {
        // Verify all tab buttons are displayed
        onView(withText("Tab 1")).check(matches(isDisplayed()))
        onView(withText("Tab 2")).check(matches(isDisplayed()))
        onView(withText("Tab 3")).check(matches(isDisplayed()))
    }

    @Test
    fun testTabSwitching() {
        // Click Tab 2 button and verify content changes
        onView(withText("Tab 2")).perform(click())
        Thread.sleep(500) // Wait for fragment transaction
        onView(withText(containsString("Tab: Tab 2"))).check(matches(isDisplayed()))

        // Click Tab 3 button and verify content changes
        onView(withText("Tab 3")).perform(click())
        Thread.sleep(500) // Wait for fragment transaction
        onView(withText(containsString("Tab: Tab 3"))).check(matches(isDisplayed()))

        // Go back to Tab 1
        onView(withText("Tab 1")).perform(click())
        Thread.sleep(500) // Wait for fragment transaction
        onView(withText(containsString("Tab: Tab 1"))).check(matches(isDisplayed()))
    }

    @Test
    fun testAnrSimulationButtonExists() {
        // Verify the ANR simulation button exists in the fragment
        onView(withText("Simulate ANR (Block Main Thread)")).check(matches(isDisplayed()))
    }

    @Test
    fun testAnrSimulationButtonClickable() {
        // Verify the ANR simulation button is clickable
        onView(withText("Simulate ANR (Block Main Thread)"))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testMultipleTabSwitches() {
        // Test switching between tabs multiple times
        for (i in 1..3) {
            onView(withText("Tab 1")).perform(click())
            Thread.sleep(300)
            onView(withText("Tab 2")).perform(click())
            Thread.sleep(300)
            onView(withText("Tab 3")).perform(click())
            Thread.sleep(300)
        }
        // Verify we end on Tab 3
        onView(withText(containsString("Tab: Tab 3"))).check(matches(isDisplayed()))
    }
}
