package com.example.demoapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import android.widget.LinearLayout
import android.widget.Button
import android.widget.FrameLayout
import android.view.ViewGroup
import com.example.demoapp.debug.DebugInfoCollector
import com.example.demoapp.debug.FloatingDebugView

class MainActivity : AppCompatActivity() {
    private val tabNames = listOf("Tab 1", "Tab 2", "Tab 3")
    private var currentTab = 0
    private lateinit var container: ViewGroup
    private lateinit var floatingDebugView: FloatingDebugView
    private lateinit var rootLayout: FrameLayout
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            floatingDebugView.updateDebugInfo()
            handler.postDelayed(this, floatingDebugView.updateFrequency)
        }
    }
    
    private var lastTouchTime = 0L
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create root layout as FrameLayout to support floating view
        rootLayout = FrameLayout(this)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val buttonLayout = LinearLayout(this)
        tabNames.forEachIndexed { index, name ->
            val button = Button(this).apply {
                text = name
                setOnClickListener { switchTab(index) }
            }
            buttonLayout.addView(button)
        }
        layout.addView(buttonLayout)
        container = LinearLayout(this).apply {
            id = ViewGroup.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        }
        layout.addView(container, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        
        rootLayout.addView(layout)
        setContentView(rootLayout)
        
        if (savedInstanceState == null) {
            switchTab(0)
        }
        
        // Initialize and show floating debug view
        floatingDebugView = FloatingDebugView(this)
        floatingDebugView.show(rootLayout)
        
        // Start periodic updates
        handler.post(updateRunnable)
    }
    
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Log UI interactions
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchTime = System.currentTimeMillis()
                lastTouchX = ev.rawX
                lastTouchY = ev.rawY
            }
            MotionEvent.ACTION_UP -> {
                val duration = System.currentTimeMillis() - lastTouchTime
                if (duration < 500) {
                    DebugInfoCollector.recordUiInteraction(
                        DebugInfoCollector.InteractionType.TAP,
                        ev.rawX, ev.rawY,
                        "duration: ${duration}ms"
                    )
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = kotlin.math.abs(ev.rawX - lastTouchX)
                val deltaY = kotlin.math.abs(ev.rawY - lastTouchY)
                if (deltaX > 10 || deltaY > 10) {
                    DebugInfoCollector.recordUiInteraction(
                        DebugInfoCollector.InteractionType.SCROLL,
                        ev.rawX, ev.rawY,
                        "delta: (${String.format("%.0f", deltaX)}, ${String.format("%.0f", deltaY)})"
                    )
                    lastTouchX = ev.rawX
                    lastTouchY = ev.rawY
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        floatingDebugView.hide()
    }

    private fun switchTab(index: Int) {
        currentTab = index
        supportFragmentManager.commit {
            replace(container.id, TabFragment.newInstance(tabNames[index]))
        }
    }
}
