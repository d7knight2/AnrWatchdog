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

class TabFragment : Fragment() {
    private var animator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = createRootLayout()
        val tabName = arguments?.getString(ARG_TAB_NAME) ?: "Unknown"

        layout.addView(createTitleView(tabName))
        layout.addView(createDescriptionView())
        layout.addView(createAnrSimulationButton())
        layout.addView(createLeakSectionTitle())

        val leakDetailsView = createLeakDetailsView()
        layout.addView(leakDetailsView)
        addLeakScenarioButtons(layout, leakDetailsView)

        return layout
    }

    override fun onResume() {
        super.onResume()
        val titleView = (view as? LinearLayout)?.getChildAt(0) as? TextView ?: return
        animator = ObjectAnimator.ofFloat(titleView, "alpha", 0.35f, 1f).apply {
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

    private fun createRootLayout(): LinearLayout = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE)
    }

    private fun createTitleView(tabName: String): TextView = TextView(requireContext()).apply {
        text = "Tab: $tabName"
        textSize = 20f
        setPadding(0, 0, 0, PADDING_MEDIUM)
        setTextColor(TITLE_COLOR)
    }

    private fun createDescriptionView(): TextView = TextView(requireContext()).apply {
        text = "Use this tab to simulate ANRs and review common memory leak examples."
        textSize = 14f
        setTextColor(SUBTITLE_COLOR)
        setPadding(0, 0, 0, PADDING_LARGE)
    }

    private fun createAnrSimulationButton(): Button = Button(requireContext()).apply {
        text = "Simulate ANR (Block Main Thread)"
        setBackgroundColor(BLOCK_BUTTON_COLOR)
        setTextColor(Color.WHITE)
        setOnClickListener { simulateMainThreadBlock() }
    }

    private fun createLeakSectionTitle(): TextView = TextView(requireContext()).apply {
        text = "Memory leak examples"
        textSize = 18f
        setTextColor(SECTION_TITLE_COLOR)
        setPadding(0, PADDING_LARGE, 0, PADDING_MEDIUM)
    }

    private fun createLeakDetailsView(): TextView = TextView(requireContext()).apply {
        text = LeakScenarioCatalog.toDisplayText(LeakScenarioCatalog.defaultScenario())
        textSize = 13f
        setTextColor(BODY_TEXT_COLOR)
        setBackgroundColor(CARD_BG_COLOR)
        setPadding(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
    }

    private fun addLeakScenarioButtons(layout: LinearLayout, leakDetailsView: TextView) {
        LeakScenarioCatalog.allScenarios().forEachIndexed { index, scenario ->
            val scenarioButton = Button(requireContext()).apply {
                text = "Example ${index + 1}: ${scenario.title}"
                setOnClickListener {
                    leakDetailsView.text = LeakScenarioCatalog.toDisplayText(scenario)
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
    }

    /**
     * Simulates a main thread block to test ANR detection.
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
        private const val ARG_TAB_NAME = "tabName"
        private const val PADDING_MEDIUM = 12
        private const val PADDING_LARGE = 24

        private val TITLE_COLOR = Color.parseColor("#263238")
        private val SUBTITLE_COLOR = Color.parseColor("#546E7A")
        private val SECTION_TITLE_COLOR = Color.parseColor("#1E88E5")
        private val BODY_TEXT_COLOR = Color.parseColor("#37474F")
        private val CARD_BG_COLOR = Color.parseColor("#ECEFF1")
        private val BLOCK_BUTTON_COLOR = Color.parseColor("#EF5350")

        fun newInstance(tabName: String): TabFragment {
            return TabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TAB_NAME, tabName)
                }
            }
        }
    }
}
