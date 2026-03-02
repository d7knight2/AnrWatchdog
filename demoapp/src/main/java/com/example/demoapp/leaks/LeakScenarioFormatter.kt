package com.example.demoapp.leaks

object LeakScenarioFormatter {
    fun toDisplayText(scenario: LeakScenarioCatalog.LeakScenario): String {
        return buildString {
            appendLine("📌 ${scenario.title}")
            appendLine("Category: ${scenario.category}")
            appendLine("Symptom: ${scenario.symptom}")
            appendLine("Prevention: ${scenario.prevention}")
            append("Quick check: ${scenario.quickCheck}")
        }
    }
}
