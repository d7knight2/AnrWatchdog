package com.dknight.anrwatchdog

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.debug.DebugProbes
import java.util.concurrent.CopyOnWriteArrayList

object DependencyAnalyzerV3 {
    private val dependencies = CopyOnWriteArrayList<String>()

    fun logEvent(event: String) {
        dependencies.add("[\${Thread.currentThread().name}] \$event")
    }

    fun dump() {
        println("----- Dependency Analyzer Dump Start -----")
        dependencies.forEach { println(it) }
        println("----- Dependency Analyzer Dump End -----")
    }
}