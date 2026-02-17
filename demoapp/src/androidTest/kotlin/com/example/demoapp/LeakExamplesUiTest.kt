package com.example.demoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LeakExamplesUiTest {

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
    fun leakExamplesSection_isVisibleOnLaunch() {
        onView(withText("Memory leak examples")).check(matches(isDisplayed()))
    }

    @Test
    fun clickingLeakExample_updatesDetailPanel() {
        onView(withText("Example 2: Unregistered Listener")).perform(click())
        onView(withText(containsString("Category: Lifecycle Leak"))).check(matches(isDisplayed()))

        onView(withText("Example 4: Oversized Bitmap Cache")).perform(click())
        onView(withText(containsString("Category: Cache Leak"))).check(matches(isDisplayed()))
    }
}
