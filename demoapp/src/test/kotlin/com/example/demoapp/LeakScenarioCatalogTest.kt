package com.example.demoapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeakScenarioCatalogTest {

    @Test
    fun allScenarios_containsAtLeastFiveExamples() {
        val scenarios = LeakScenarioCatalog.allScenarios()
        assertTrue("Expected at least five examples", scenarios.size >= 5)
    }

    @Test
    fun allScenarios_containsDistinctTitles() {
        val scenarios = LeakScenarioCatalog.allScenarios()
        val distinctTitles = scenarios.map { it.title }.toSet()
        assertEquals(scenarios.size, distinctTitles.size)
    }

    @Test
    fun toDisplayText_includesAllCoreFields() {
        val scenario = LeakScenarioCatalog.allScenarios().first()

        val text = LeakScenarioCatalog.toDisplayText(scenario)

        assertTrue(text.contains(scenario.title))
        assertTrue(text.contains(scenario.category))
        assertTrue(text.contains(scenario.symptom))
        assertTrue(text.contains(scenario.prevention))
        assertTrue(text.contains(scenario.quickCheck))
    }
}
