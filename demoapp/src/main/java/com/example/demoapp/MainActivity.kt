package com.example.demoapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import android.widget.LinearLayout
import android.widget.Button
import android.widget.FrameLayout
import android.view.ViewGroup
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
            handler.postDelayed(this, 2000) // Update every 2 seconds
        }
    }

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
