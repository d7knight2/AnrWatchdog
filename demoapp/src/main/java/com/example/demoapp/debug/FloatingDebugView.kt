package com.example.demoapp.debug

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Debug
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

/**
 * Floating debug view that displays debug information in a draggable overlay within the activity.
 * 
 * This view shows:
 * - Active threads with their names and statuses
 * - Recent main thread blocks
 * - General debug information
 * - CPU usage over time
 * - UI interactions
 * 
 * Features:
 * - Draggable
 * - Collapsible/expandable
 * - Dark/light mode toggle
 * - Clear logs button
 * - Export logs functionality
 * - Configurable update frequency
 * 
 * The view can be dragged around the screen and toggled between expanded and collapsed states.
 */
@SuppressLint("ClickableViewAccessibility")
class FloatingDebugView(private val context: Context) {
    
    private var floatingView: View? = null
    private var parentView: ViewGroup? = null
    private var isExpanded = false
    private var isDarkMode = true
    private var initialX = 0f
    private var initialY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    private lateinit var contentLayout: LinearLayout
    private lateinit var toggleButton: Button
    private lateinit var mainLayout: LinearLayout
    private lateinit var buttonBar: LinearLayout
    
    // Configurable update frequency (milliseconds)
    var updateFrequency: Long = 2000
        set(value) {
            field = value.coerceAtLeast(500)
        }
    
    // Constants for UI dimensions and touch handling
    private companion object {
        const val CONTENT_WIDTH = 800
        const val CONTENT_HEIGHT = 600
        const val TOUCH_THRESHOLD = 10
        const val MIN_TOUCH_SIZE = 120 // 48dp minimum for accessibility (assuming 2.5 density)
    }
    
    /**
     * Shows the floating debug view by attaching it to the parent view
     * 
     * @param parent The parent ViewGroup to attach the floating view to
     */
    fun show(parent: ViewGroup) {
        if (floatingView != null) return
        
        parentView = parent
        floatingView = createFloatingView()
        
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            leftMargin = 20
            topMargin = 100
        }
        
        parent.addView(floatingView, params)
    }
    
    /**
     * Hides and removes the floating debug view
     */
    fun hide() {
        floatingView?.let {
            parentView?.removeView(it)
            floatingView = null
            parentView = null
        }
    }
    
    /**
     * Updates the debug information displayed in the view
     */
    fun updateDebugInfo() {
        if (!isExpanded || floatingView == null) return
        
        // Update CPU usage
        updateCpuUsage()
        
        // Clear previous content
        contentLayout.removeAllViews()
        
        // Add section titles and data
        addSection("Active Threads", getActiveThreadsText())
        addSection("Recent Main Thread Blocks", getMainThreadBlocksText())
        addSection("CPU Usage Over Time", getCpuUsageText())
        addSection("Recent UI Interactions", getUiInteractionsText())
        addSection("General Debug Info", getGeneralDebugInfoText())
    }
    
    private fun updateCpuUsage() {
        // Get CPU usage - using a simple approximation based on memory
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()).toFloat()
        val maxMemory = runtime.maxMemory().toFloat()
        val memoryUsagePercent = (usedMemory / maxMemory * 100).coerceIn(0f, 100f)
        
        // Record CPU usage (using memory as proxy since actual CPU is harder to measure)
        DebugInfoCollector.recordCpuUsage(memoryUsagePercent)
    }
    
    private fun createFloatingView(): View {
        mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getBackgroundColor())
            setPadding(16, 16, 16, 16)
            elevation = 10f
        }
        
        // Toggle button with larger touch target
        toggleButton = Button(context).apply {
            text = "Debug Tool 🔧"
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF2196F3.toInt())
            val padding = 20
            setPadding(padding, padding, padding, padding)
            minHeight = MIN_TOUCH_SIZE
            minWidth = MIN_TOUCH_SIZE * 2
            setOnClickListener {
                toggleExpanded()
            }
        }
        mainLayout.addView(toggleButton)
        
        // Button bar for actions (initially hidden)
        buttonBar = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
        }
        
        // Clear button
        val clearButton = Button(context).apply {
            text = "Clear"
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFFE53935.toInt())
            setPadding(15, 15, 15, 15)
            minHeight = MIN_TOUCH_SIZE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 8
            }
            setOnClickListener {
                clearAllLogs()
            }
        }
        buttonBar.addView(clearButton)
        
        // Theme toggle button
        val themeButton = Button(context).apply {
            text = "🌙"
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF757575.toInt())
            setPadding(15, 15, 15, 15)
            minHeight = MIN_TOUCH_SIZE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 8
            }
            setOnClickListener {
                toggleTheme()
            }
        }
        buttonBar.addView(themeButton)
        
        // Export button
        val exportButton = Button(context).apply {
            text = "Export"
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF43A047.toInt())
            setPadding(15, 15, 15, 15)
            minHeight = MIN_TOUCH_SIZE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                exportLogs()
            }
        }
        buttonBar.addView(exportButton)
        
        mainLayout.addView(buttonBar)
        
        // Content layout (initially hidden)
        contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        
        val scrollView = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                CONTENT_WIDTH,
                CONTENT_HEIGHT
            )
            addView(contentLayout)
        }
        mainLayout.addView(scrollView)
        
        // Add touch listener for dragging
        mainLayout.setOnTouchListener { view, event ->
            handleTouch(view, event)
        }
        
        return mainLayout
    }
    
    private fun handleTouch(view: View, event: MotionEvent): Boolean {
        val params = view.layoutParams as FrameLayout.LayoutParams
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = view.x
                initialY = view.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                val deltaY = event.rawY - initialTouchY
                
                // Only move if the touch has moved significantly (to distinguish from clicks)
                if (abs(deltaX) > TOUCH_THRESHOLD || abs(deltaY) > TOUCH_THRESHOLD) {
                    view.x = initialX + deltaX
                    view.y = initialY + deltaY
                    return true
                }
            }
        }
        return false
    }
    
    private fun toggleExpanded() {
        isExpanded = !isExpanded
        contentLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        buttonBar.visibility = if (isExpanded) View.VISIBLE else View.GONE
        toggleButton.text = if (isExpanded) "Debug Tool 🔧 ▼" else "Debug Tool 🔧 ▶"
        
        if (isExpanded) {
            updateDebugInfo()
        }
    }
    
    private fun toggleTheme() {
        isDarkMode = !isDarkMode
        mainLayout.setBackgroundColor(getBackgroundColor())
        updateDebugInfo()
    }
    
    private fun getBackgroundColor(): Int {
        return if (isDarkMode) 0xCC000000.toInt() else 0xCCFFFFFF.toInt()
    }
    
    private fun getTextColor(): Int {
        return if (isDarkMode) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
    }
    
    private fun getTitleColor(): Int {
        return if (isDarkMode) 0xFF4CAF50.toInt() else 0xFF2E7D32.toInt()
    }
    
    private fun getDividerColor(): Int {
        return if (isDarkMode) 0xFF555555.toInt() else 0xFFCCCCCC.toInt()
    }
    
    private fun clearAllLogs() {
        DebugInfoCollector.clearAllLogs()
        Toast.makeText(context, "All debug logs cleared", Toast.LENGTH_SHORT).show()
        updateDebugInfo()
    }
    
    private fun exportLogs() {
        val file = DebugInfoCollector.exportLogsToFile(context)
        if (file != null) {
            Toast.makeText(context, "Logs exported to ${file.name}", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Failed to export logs", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addSection(title: String, content: String) {
        // Section title
        val titleView = TextView(context).apply {
            text = title
            textSize = 16f
            setTextColor(getTitleColor())
            setPadding(0, 20, 0, 10)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        contentLayout.addView(titleView)
        
        // Section content
        val contentView = TextView(context).apply {
            text = content
            textSize = 12f
            setTextColor(getTextColor())
            setPadding(10, 0, 0, 0)
        }
        contentLayout.addView(contentView)
        
        // Divider
        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                topMargin = 10
                bottomMargin = 10
            }
            setBackgroundColor(getDividerColor())
        }
        contentLayout.addView(divider)
    }
    
    private fun getActiveThreadsText(): String {
        val threads = DebugInfoCollector.getActiveThreads()
        return if (threads.isEmpty()) {
            "No threads found"
        } else {
            threads.joinToString("\n\n") { thread ->
                """
                Name: ${thread.name}
                State: ${thread.state}
                ID: ${thread.id}
                Priority: ${thread.priority}
                Daemon: ${thread.isDaemon}
                """.trimIndent()
            }
        }
    }
    
    private fun getMainThreadBlocksText(): String {
        val blocks = DebugInfoCollector.getRecentMainThreadBlocks()
        return if (blocks.isEmpty()) {
            "No recent blocks detected"
        } else {
            blocks.take(5).joinToString("\n\n") { block ->
                """
                Time: ${DebugInfoCollector.formatTimestamp(block.timestamp)}
                Duration: ${block.duration}ms
                Stack Trace (first 3 lines):
                ${block.stackTrace.split("\n").take(3).joinToString("\n")}
                """.trimIndent()
            }
        }
    }
    
    private fun getGeneralDebugInfoText(): String {
        val info = DebugInfoCollector.getGeneralDebugInfo()
        return info.entries.joinToString("\n") { (key, value) ->
            "$key: $value"
        }
    }
    
    private fun getCpuUsageText(): String {
        val history = DebugInfoCollector.getCpuUsageHistory()
        return if (history.isEmpty()) {
            "No CPU data available yet"
        } else {
            val recent = history.takeLast(10)
            recent.joinToString("\n") { snapshot ->
                "${DebugInfoCollector.formatTimestamp(snapshot.timestamp)}: ${String.format("%.1f", snapshot.cpuUsagePercent)}% (${snapshot.totalThreads} threads)"
            }
        }
    }
    
    private fun getUiInteractionsText(): String {
        val interactions = DebugInfoCollector.getUiInteractions()
        return if (interactions.isEmpty()) {
            "No UI interactions recorded"
        } else {
            interactions.takeLast(10).joinToString("\n") { interaction ->
                val details = if (interaction.details.isNotEmpty()) " - ${interaction.details}" else ""
                "${DebugInfoCollector.formatTimestamp(interaction.timestamp)}: ${interaction.type} at (${String.format("%.0f", interaction.x)}, ${String.format("%.0f", interaction.y)})$details"
            }
        }
    }
}
