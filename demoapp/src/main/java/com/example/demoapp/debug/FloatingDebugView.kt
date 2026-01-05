package com.example.demoapp.debug

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.abs

/**
 * Floating debug view that displays debug information in a draggable overlay within the activity.
 * 
 * This view shows:
 * - Active threads with their names and statuses
 * - Recent main thread blocks
 * - General debug information
 * 
 * The view can be dragged around the screen and toggled between expanded and collapsed states.
 */
@SuppressLint("ClickableViewAccessibility")
class FloatingDebugView(private val context: Context) {
    
    private var floatingView: View? = null
    private var parentView: ViewGroup? = null
    private var isExpanded = false
    private var initialX = 0f
    private var initialY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    private lateinit var contentLayout: LinearLayout
    private lateinit var toggleButton: Button
    
    // Constants for UI dimensions and touch handling
    private companion object {
        const val CONTENT_WIDTH = 800
        const val CONTENT_HEIGHT = 600
        const val TOUCH_THRESHOLD = 10
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
        
        // Clear previous content
        contentLayout.removeAllViews()
        
        // Add section titles and data
        addSection("Active Threads", getActiveThreadsText())
        addSection("Recent Main Thread Blocks", getMainThreadBlocksText())
        addSection("General Debug Info", getGeneralDebugInfoText())
    }
    
    private fun createFloatingView(): View {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xCC000000.toInt())
            setPadding(16, 16, 16, 16)
            elevation = 10f
        }
        
        // Toggle button
        toggleButton = Button(context).apply {
            text = "Debug Tool 🔧"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF2196F3.toInt())
            setPadding(20, 10, 20, 10)
            setOnClickListener {
                toggleExpanded()
            }
        }
        layout.addView(toggleButton)
        
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
        layout.addView(scrollView)
        
        // Add touch listener for dragging
        layout.setOnTouchListener { view, event ->
            handleTouch(view, event)
        }
        
        return layout
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
        toggleButton.text = if (isExpanded) "Debug Tool 🔧 ▼" else "Debug Tool 🔧 ▶"
        
        if (isExpanded) {
            updateDebugInfo()
        }
    }
    
    private fun addSection(title: String, content: String) {
        // Section title
        val titleView = TextView(context).apply {
            text = title
            textSize = 16f
            setTextColor(0xFF4CAF50.toInt())
            setPadding(0, 20, 0, 10)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        contentLayout.addView(titleView)
        
        // Section content
        val contentView = TextView(context).apply {
            text = content
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
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
            setBackgroundColor(0xFF555555.toInt())
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
}
