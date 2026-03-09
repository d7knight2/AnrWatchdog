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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@LargeTest
class WatchdogIntegrationUiTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        DebugInfoCollector.clearAllLogs()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
        DebugInfoCollector.clearAllLogs()
    }

    @Test
    fun watchdogCallback_isRecordedAfterAppStart() {
        waitForWatchdogEvents(minimumCount = 1)
        assertTrue(DebugInfoCollector.getWatchdogEvents().isNotEmpty())
    }

    @Test
    fun watchdogEventCount_growsDuringIdle() {
        waitForWatchdogEvents(minimumCount = 1)
        val firstCount = DebugInfoCollector.getWatchdogEvents().size

        waitForWatchdogEvents(minimumCount = firstCount + 1)
        val secondCount = DebugInfoCollector.getWatchdogEvents().size

        assertTrue(secondCount > firstCount)
    }

    @Test
    fun watchdogAndMainThreadBlock_areBothCapturedForProblematicCode() {
        onView(withText("Simulate ANR (Block Main Thread)")).perform(click())
        Thread.sleep(2300)

        waitForWatchdogEvents(minimumCount = 1)
        assertTrue(DebugInfoCollector.getRecentMainThreadBlocks().isNotEmpty())
        assertTrue(DebugInfoCollector.getWatchdogEvents().isNotEmpty())
    }

    @Test
    fun watchdogEvents_canBeClearedForNextTestRun() {
        waitForWatchdogEvents(minimumCount = 1)
        assertTrue(DebugInfoCollector.getWatchdogEvents().isNotEmpty())

        DebugInfoCollector.clearWatchdogEvents()

        assertEquals(0, DebugInfoCollector.getWatchdogEvents().size)
    }

    private fun waitForWatchdogEvents(minimumCount: Int, timeoutMs: Long = 8000L) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start <= timeoutMs) {
            if (DebugInfoCollector.getWatchdogEvents().size >= minimumCount) {
                return
            }
            Thread.sleep(100)
        }
        throw AssertionError("Timed out waiting for at least $minimumCount watchdog events")
    }
}
