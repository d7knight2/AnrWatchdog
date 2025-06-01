package com.example.demoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import android.widget.LinearLayout
import android.widget.Button
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {
    private val tabNames = listOf("Tab 1", "Tab 2", "Tab 3")
    private var currentTab = 0
    private lateinit var container: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setContentView(layout)
        if (savedInstanceState == null) {
            switchTab(0)
        }
    }

    private fun switchTab(index: Int) {
        currentTab = index
        supportFragmentManager.commit {
            replace(container.id, TabFragment.newInstance(tabNames[index]))
        }
    }
}
