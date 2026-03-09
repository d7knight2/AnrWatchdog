package com.example.demoapp

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.demoapp.debug.DebugInfoCollector
import com.example.demoapp.leaks.LeakScenarioCatalog
import com.example.demoapp.leaks.LeakScenarioFormatter

class TabFragment : Fragment() {
    private var animator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val tabName = arguments?.getString("tabName") ?: "Unknown"

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val textView = TextView(requireContext()).apply {
            text = "Tab: $tabName"
            textSize = 20f
            setPadding(0, 0, 0, 12)
            setTextColor(Color.parseColor("#263238"))
        }
        layout.addView(textView)

        val descriptionView = TextView(requireContext()).apply {
            text = "Use this tab to simulate ANRs and review common memory leak examples."
            textSize = 14f
            setTextColor(Color.parseColor("#546E7A"))
            setPadding(0, 0, 0, 20)
        }
        layout.addView(descriptionView)

        val blockButton = Button(requireContext()).apply {
            text = "Simulate ANR (Block Main Thread)"
            setBackgroundColor(Color.parseColor("#EF5350"))
            setTextColor(Color.WHITE)
            setOnClickListener { simulateMainThreadBlock() }
        }
        layout.addView(blockButton)

        val leakSectionTitle = TextView(requireContext()).apply {
            text = "Memory leak examples"
            textSize = 18f
            setTextColor(Color.parseColor("#1E88E5"))
            setPadding(0, 24, 0, 12)
        }
        layout.addView(leakSectionTitle)

        val leakDetailsView = TextView(requireContext()).apply {
            text = LeakScenarioFormatter.toDisplayText(LeakScenarioCatalog.allScenarios().first())
            textSize = 13f
            setTextColor(Color.parseColor("#37474F"))
            setBackgroundColor(Color.parseColor("#ECEFF1"))
            setPadding(16, 16, 16, 16)
        }
        layout.addView(leakDetailsView)

        LeakScenarioCatalog.allScenarios().forEachIndexed { index, scenario ->
            val scenarioButton = Button(requireContext()).apply {
                text = "Example ${index + 1}: ${scenario.title}"
                setOnClickListener {
                    leakDetailsView.text = LeakScenarioFormatter.toDisplayText(scenario)
                    DebugInfoCollector.recordUiInteraction(
                        DebugInfoCollector.InteractionType.TAP,
                        0f,
                        0f,
                        "Selected leak example: ${scenario.title}"
                    )
                }
            }
            layout.addView(scenarioButton)
        }

        return layout
    }

    override fun onResume() {
        super.onResume()
        val textView = (view as? LinearLayout)?.getChildAt(0) as? TextView ?: return
        animator = ObjectAnimator.ofFloat(textView, "alpha", 0.35f, 1f).apply {
            duration = 1400
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

    /**
     * Simulates a main thread block to test ANR detection.
     *
     * WARNING: This method intentionally blocks the main thread using Thread.sleep(),
     * which is normally not recommended. This is done specifically to simulate an ANR
     * condition for testing and demonstration purposes.
     */
    private fun simulateMainThreadBlock() {
        val startTime = System.currentTimeMillis()

        val stackTrace = Thread.currentThread().stackTrace.joinToString("\n") {
            "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
        }

        Thread.sleep(2000)

        val duration = System.currentTimeMillis() - startTime
        DebugInfoCollector.recordMainThreadBlock(duration, stackTrace)
    }

    companion object {
        fun newInstance(tabName: String): TabFragment {
            return TabFragment().apply {
                arguments = Bundle().apply {
                    putString("tabName", tabName)
                }
            }
        }
    }
}
