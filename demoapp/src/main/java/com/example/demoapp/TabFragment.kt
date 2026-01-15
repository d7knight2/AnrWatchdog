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

/**
 * A simple fragment used for tab demonstration in the ANR Watchdog demo app.
 * 
 * Each tab fragment displays:
 * - A text view showing the tab name with animated alpha fading
 * - A button to simulate an ANR condition for testing purposes
 * 
 * The fragment demonstrates how ANR events can be triggered and detected in different
 * parts of the application. When the "Simulate ANR" button is clicked, it intentionally
 * blocks the main thread to trigger ANR detection and logging.
 * 
 * ## Features:
 * - Tab-specific content display
 * - ANR simulation for testing
 * - Stack trace capture and logging
 * - Integration with DebugInfoCollector
 * - Alpha animation on text view
 * 
 * @see DebugInfoCollector
 * @see MainActivity
 */
class TabFragment : Fragment() {
    private var animator: ObjectAnimator? = null
    
    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * 
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state
     * @return The View for the fragment's UI
     */
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

    /**
     * Called when the fragment is visible to the user and actively running.
     * Starts the alpha animation on the text view.
     */
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

    /**
     * Called when the Fragment is no longer resumed.
     * Cancels and cleans up the alpha animation.
     */
    override fun onPause() {
        super.onPause()
        animator?.cancel()
        animator = null
    }
    
    /**
     * Simulates a main thread block to test ANR detection.
     * 
     * This method intentionally blocks the main thread for 2 seconds to simulate an ANR condition.
     * It captures the stack trace before blocking and records the event with DebugInfoCollector
     * after the block completes.
     * 
     * **WARNING**: This method uses Thread.sleep() on the main thread, which is normally
     * not recommended. This is done specifically for testing and demonstration purposes only.
     * 
     * @see DebugInfoCollector.recordMainThreadBlock
     */
    private fun simulateMainThreadBlock() {
        val startTime = System.currentTimeMillis()
        
        // Capture stack trace
        val stackTrace = Thread.currentThread().stackTrace.joinToString("\n") { 
            "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
        }
        
        // Block the main thread for 2 seconds (intentional for testing ANR scenarios)
        Thread.sleep(2000)
        
        val duration = System.currentTimeMillis() - startTime
        
        // Record the block
        DebugInfoCollector.recordMainThreadBlock(duration, stackTrace)
    }

    companion object {
        /**
         * Factory method to create a new instance of TabFragment with the specified tab name.
         * 
         * @param tabName The name to display in this tab
         * @return A new instance of TabFragment configured with the given tab name
         */
        fun newInstance(tabName: String): TabFragment {
            val fragment = TabFragment()
            fragment.arguments = Bundle().apply {
                putString("tabName", tabName)
            }
            return fragment
        }
    }
}
