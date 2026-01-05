package com.example.demoapp.debug

import android.content.Context
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Utility class to collect debug information for the floating debug tool.
 * 
 * This class provides methods to:
 * - Get a list of all active threads with their names and statuses
 * - Track recent main thread blocks
 * - Collect general debug information
 * - Track CPU usage over time
 * - Log UI interactions (taps and scrolls)
 * - Export debug logs to files
 */
object DebugInfoCollector {
    
    private val mainThreadBlocks = CopyOnWriteArrayList<MainThreadBlock>()
    private val cpuUsageHistory = CopyOnWriteArrayList<CpuUsageSnapshot>()
    private val uiInteractions = CopyOnWriteArrayList<UIInteraction>()
    
    // Configurable parameters
    var maxBlocks = 20
        set(value) {
            field = value.coerceAtLeast(1)
            trimMainThreadBlocks()
        }
    
    var maxCpuSnapshots = 50
        set(value) {
            field = value.coerceAtLeast(1)
            trimCpuHistory()
        }
    
    var maxUiInteractions = 100
        set(value) {
            field = value.coerceAtLeast(1)
            trimUiInteractions()
        }
    
    /**
     * Data class representing a main thread block event
     */
    data class MainThreadBlock(
        val timestamp: Long,
        val duration: Long,
        val stackTrace: String
    )
    
    /**
     * Data class representing thread information
     */
    data class ThreadInfo(
        val name: String,
        val state: Thread.State,
        val id: Long,
        val priority: Int,
        val isDaemon: Boolean
    )
    
    /**
     * Data class representing CPU usage at a point in time
     */
    data class CpuUsageSnapshot(
        val timestamp: Long,
        val cpuUsagePercent: Float,
        val totalThreads: Int
    )
    
    /**
     * Data class representing a UI interaction event
     */
    data class UIInteraction(
        val timestamp: Long,
        val type: InteractionType,
        val x: Float,
        val y: Float,
        val details: String = ""
    )
    
    enum class InteractionType {
        TAP, SCROLL, LONG_PRESS, DRAG
    }
    
    /**
     * Records a main thread block event
     * 
     * @param duration Duration of the block in milliseconds
     * @param stackTrace Stack trace at the time of the block
     */
    fun recordMainThreadBlock(duration: Long, stackTrace: String) {
        val block = MainThreadBlock(
            timestamp = System.currentTimeMillis(),
            duration = duration,
            stackTrace = stackTrace
        )
        mainThreadBlocks.add(0, block)
        trimMainThreadBlocks()
    }
    
    private fun trimMainThreadBlocks() {
        while (mainThreadBlocks.size > maxBlocks) {
            mainThreadBlocks.removeAt(mainThreadBlocks.size - 1)
        }
    }
    
    /**
     * Gets a list of recent main thread blocks
     * 
     * @return List of MainThreadBlock objects
     */
    fun getRecentMainThreadBlocks(): List<MainThreadBlock> {
        return mainThreadBlocks.toList()
    }
    
    /**
     * Clears all recorded main thread blocks
     */
    fun clearMainThreadBlocks() {
        mainThreadBlocks.clear()
    }
    
    /**
     * Records a CPU usage snapshot
     * 
     * @param cpuUsagePercent CPU usage percentage (0-100)
     */
    fun recordCpuUsage(cpuUsagePercent: Float) {
        val snapshot = CpuUsageSnapshot(
            timestamp = System.currentTimeMillis(),
            cpuUsagePercent = cpuUsagePercent.coerceIn(0f, 100f),
            totalThreads = Thread.activeCount()
        )
        cpuUsageHistory.add(snapshot)
        trimCpuHistory()
    }
    
    private fun trimCpuHistory() {
        while (cpuUsageHistory.size > maxCpuSnapshots) {
            cpuUsageHistory.removeAt(0)
        }
    }
    
    /**
     * Gets CPU usage history
     * 
     * @return List of CPU usage snapshots
     */
    fun getCpuUsageHistory(): List<CpuUsageSnapshot> {
        return cpuUsageHistory.toList()
    }
    
    /**
     * Clears all CPU usage history
     */
    fun clearCpuUsageHistory() {
        cpuUsageHistory.clear()
    }
    
    /**
     * Records a UI interaction event
     * 
     * @param type Type of interaction
     * @param x X coordinate
     * @param y Y coordinate
     * @param details Optional additional details
     */
    fun recordUiInteraction(type: InteractionType, x: Float, y: Float, details: String = "") {
        val interaction = UIInteraction(
            timestamp = System.currentTimeMillis(),
            type = type,
            x = x,
            y = y,
            details = details
        )
        uiInteractions.add(interaction)
        trimUiInteractions()
    }
    
    private fun trimUiInteractions() {
        while (uiInteractions.size > maxUiInteractions) {
            uiInteractions.removeAt(0)
        }
    }
    
    /**
     * Gets recent UI interactions
     * 
     * @return List of UI interaction events
     */
    fun getUiInteractions(): List<UIInteraction> {
        return uiInteractions.toList()
    }
    
    /**
     * Clears all UI interaction history
     */
    fun clearUiInteractions() {
        uiInteractions.clear()
    }
    
    /**
     * Clears all debug logs
     */
    fun clearAllLogs() {
        clearMainThreadBlocks()
        clearCpuUsageHistory()
        clearUiInteractions()
    }
    
    /**
     * Gets information about all active threads
     * 
     * @return List of ThreadInfo objects
     */
    fun getActiveThreads(): List<ThreadInfo> {
        val threadSet = Thread.getAllStackTraces().keys
        return threadSet.map { thread ->
            ThreadInfo(
                name = thread.name,
                state = thread.state,
                id = thread.id,
                priority = thread.priority,
                isDaemon = thread.isDaemon
            )
        }.sortedBy { it.name }
    }
    
    /**
     * Gets general debug information
     * 
     * @return Map of debug information key-value pairs
     */
    fun getGeneralDebugInfo(): Map<String, String> {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        val totalMemory = runtime.totalMemory() / (1024 * 1024)
        val freeMemory = runtime.freeMemory() / (1024 * 1024)
        val usedMemory = totalMemory - freeMemory
        
        return mapOf(
            "Total Threads" to Thread.activeCount().toString(),
            "Main Thread" to if (Looper.getMainLooper().thread == Thread.currentThread()) "YES" else "NO",
            "Memory Used" to "${usedMemory}MB",
            "Memory Free" to "${freeMemory}MB",
            "Memory Total" to "${totalMemory}MB",
            "Memory Max" to "${maxMemory}MB",
            "Available Processors" to runtime.availableProcessors().toString(),
            "Total Blocks Recorded" to mainThreadBlocks.size.toString(),
            "CPU Snapshots" to cpuUsageHistory.size.toString(),
            "UI Interactions" to uiInteractions.size.toString()
        )
    }
    
    /**
     * Exports debug logs to a file
     * 
     * @param context Android context for file access
     * @param filename Name of the file to export to
     * @return The exported file or null if export failed
     */
    fun exportLogsToFile(context: Context, filename: String = "debug_logs_${System.currentTimeMillis()}.txt"): File? {
        return try {
            val file = File(context.getExternalFilesDir(null), filename)
            file.bufferedWriter().use { writer ->
                writer.write("=== Debug Log Export ===\n")
                writer.write("Exported at: ${formatTimestamp(System.currentTimeMillis())}\n\n")
                
                // General info
                writer.write("=== General Debug Info ===\n")
                getGeneralDebugInfo().forEach { (key, value) ->
                    writer.write("$key: $value\n")
                }
                writer.write("\n")
                
                // Main thread blocks
                writer.write("=== Main Thread Blocks (${mainThreadBlocks.size}) ===\n")
                mainThreadBlocks.forEachIndexed { index, block ->
                    writer.write("\nBlock ${index + 1}:\n")
                    writer.write("  Time: ${formatTimestamp(block.timestamp)}\n")
                    writer.write("  Duration: ${block.duration}ms\n")
                    writer.write("  Stack Trace:\n")
                    writer.write("${block.stackTrace}\n")
                }
                writer.write("\n")
                
                // CPU usage history
                writer.write("=== CPU Usage History (${cpuUsageHistory.size}) ===\n")
                cpuUsageHistory.forEach { snapshot ->
                    writer.write("${formatTimestamp(snapshot.timestamp)}: ${String.format("%.1f", snapshot.cpuUsagePercent)}% (${snapshot.totalThreads} threads)\n")
                }
                writer.write("\n")
                
                // UI interactions
                writer.write("=== UI Interactions (${uiInteractions.size}) ===\n")
                uiInteractions.forEach { interaction ->
                    writer.write("${formatTimestamp(interaction.timestamp)}: ${interaction.type} at (${String.format("%.0f", interaction.x)}, ${String.format("%.0f", interaction.y)})")
                    if (interaction.details.isNotEmpty()) {
                        writer.write(" - ${interaction.details}")
                    }
                    writer.write("\n")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Formats a timestamp to a readable date/time string
     * Thread-safe implementation using SimpleDateFormat
     */
    fun formatTimestamp(timestamp: Long): String {
        // Create a new SimpleDateFormat instance for each call to ensure thread safety
        val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}
