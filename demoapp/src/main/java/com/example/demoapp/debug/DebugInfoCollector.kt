package com.example.demoapp.debug

import android.os.Looper
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
 */
object DebugInfoCollector {
    
    private val mainThreadBlocks = CopyOnWriteArrayList<MainThreadBlock>()
    private const val MAX_BLOCKS = 20
    
    // Cache for active threads to avoid expensive getAllStackTraces() calls
    private var cachedThreads: List<ThreadInfo> = emptyList()
    private var lastThreadCacheTime: Long = 0
    private const val THREAD_CACHE_DURATION_MS = 1000 // Cache for 1 second
    
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
        
        // Keep only the most recent blocks
        while (mainThreadBlocks.size > MAX_BLOCKS) {
            mainThreadBlocks.removeLast()
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
     * Gets information about all active threads
     * Results are cached for 1 second to avoid expensive getAllStackTraces() calls
     * 
     * @return List of ThreadInfo objects
     */
    fun getActiveThreads(): List<ThreadInfo> {
        val currentTime = System.currentTimeMillis()
        
        // Return cached result if still valid
        if (currentTime - lastThreadCacheTime < THREAD_CACHE_DURATION_MS) {
            return cachedThreads
        }
        
        // Update cache
        val threadSet = Thread.getAllStackTraces().keys
        cachedThreads = threadSet.map { thread ->
            ThreadInfo(
                name = thread.name,
                state = thread.state,
                id = thread.id,
                priority = thread.priority,
                isDaemon = thread.isDaemon
            )
        }.sortedBy { it.name }
        
        lastThreadCacheTime = currentTime
        return cachedThreads
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
            "Total Blocks Recorded" to mainThreadBlocks.size.toString()
        )
    }
    
    /**
     * Formats a timestamp to a readable date/time string
     * Thread-safe implementation using ThreadLocal for better performance
     */
    private val dateFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    }
    
    fun formatTimestamp(timestamp: Long): String {
        return dateFormatThreadLocal.get().format(Date(timestamp))
    }
}
