package com.example.demoapp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.demoapp.debug.DebugInfoCollector

class TabFragment : Fragment() {
    private var animator: ObjectAnimator? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
        }
        
        val textView = TextView(requireContext()).apply {
            text = "Tab: ${arguments?.getString("tabName")}"
            textSize = 18f
            setPadding(0, 0, 0, 20)
        }
        layout.addView(textView)
        
        // Add a button to simulate main thread block
        val blockButton = Button(requireContext()).apply {
            text = "Simulate ANR (Block Main Thread)"
            setOnClickListener {
                simulateMainThreadBlock()
            }
        }
        layout.addView(blockButton)
        
        return layout
    }

    override fun onResume() {
        super.onResume()
        val textView = (view as? LinearLayout)?.getChildAt(0) as? TextView ?: return
        animator = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        animator?.cancel()
        animator = null
    }
    
    private fun simulateMainThreadBlock() {
        val startTime = System.currentTimeMillis()
        
        // Capture stack trace
        val stackTrace = Thread.currentThread().stackTrace.joinToString("\n") { 
            "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
        }
        
        // Block the main thread for 2 seconds
        Thread.sleep(2000)
        
        val duration = System.currentTimeMillis() - startTime
        
        // Record the block
        DebugInfoCollector.recordMainThreadBlock(duration, stackTrace)
    }

    companion object {
        fun newInstance(tabName: String): TabFragment {
            val fragment = TabFragment()
            fragment.arguments = Bundle().apply {
                putString("tabName", tabName)
            }
            return fragment
        }
    }
}
