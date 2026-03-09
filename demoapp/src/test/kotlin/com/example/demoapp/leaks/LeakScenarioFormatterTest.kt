package com.example.demoapp.leaks

import org.junit.Assert.assertTrue
import org.junit.Test

class LeakScenarioFormatterTest {

    @Test
    fun toDisplayText_hasReadableSections() {
        val scenario = LeakScenarioCatalog.LeakScenario(
            title = "Example",
            category = "Category",
            symptom = "Symptom",
            prevention = "Prevention",
            quickCheck = "Check"
        )

        val text = LeakScenarioFormatter.toDisplayText(scenario)

        assertTrue(text.contains("📌 Example"))
        assertTrue(text.contains("Category: Category"))
        assertTrue(text.contains("Symptom: Symptom"))
        assertTrue(text.contains("Prevention: Prevention"))
        assertTrue(text.contains("Quick check: Check"))
    }
}
