package com.example.demoapp.leaks

/**
 * Curated examples of memory leak patterns that are commonly seen in Android apps.
 */
object LeakScenarioCatalog {

    data class LeakScenario(
        val title: String,
        val category: String,
        val symptom: String,
        val prevention: String,
        val quickCheck: String
    )

    private val scenarios = listOf(
        LeakScenario(
            title = "Static Activity Reference",
            category = "Context Leak",
            symptom = "An Activity remains in memory after rotation because a static field still points to it.",
            prevention = "Store applicationContext only, or clear references in onDestroy().",
            quickCheck = "Rotate device repeatedly and inspect retained Activity instances in LeakCanary."
        ),
        LeakScenario(
            title = "Unregistered Listener",
            category = "Lifecycle Leak",
            symptom = "A Fragment view cannot be garbage collected because a listener remains registered.",
            prevention = "Register in onStart()/onResume() and unregister in onStop()/onPause().",
            quickCheck = "Navigate back and forth between screens while monitoring retained fragment views."
        ),
        LeakScenario(
            title = "Long-running Coroutine Capturing View",
            category = "Coroutine Leak",
            symptom = "A launched coroutine captures a TextView and keeps it alive after screen close.",
            prevention = "Use viewLifecycleOwner.lifecycleScope or cancel jobs in onDestroyView().",
            quickCheck = "Close the screen before job completion and verify no retained view references exist."
        ),
        LeakScenario(
            title = "Oversized Bitmap Cache",
            category = "Cache Leak",
            symptom = "Bitmaps are cached forever causing high memory usage and GC pressure.",
            prevention = "Use LruCache with sensible max size and explicit eviction policy.",
            quickCheck = "Scroll image-heavy list and confirm memory stabilizes after idle period."
        ),
        LeakScenario(
            title = "Fragment ViewBinding Not Cleared",
            category = "View Leak",
            symptom = "Binding survives destroyView() and keeps the entire view hierarchy in memory.",
            prevention = "Set binding = null in onDestroyView().",
            quickCheck = "Open and close Fragment quickly, then inspect retained objects for binding classes."
        )
    )

    fun allScenarios(): List<LeakScenario> = scenarios

    fun defaultScenario(): LeakScenario = scenarios.first()

    fun toDisplayText(scenario: LeakScenario): String = buildString {
        appendLine("📌 ${scenario.title}")
        appendLine("Category: ${scenario.category}")
        appendLine("Symptom: ${scenario.symptom}")
        appendLine("Prevention: ${scenario.prevention}")
        append("Quick check: ${scenario.quickCheck}")
    }
}
