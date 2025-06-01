package com.example.demoapp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class TabFragment : Fragment() {
    private var animator: ObjectAnimator? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(requireContext())
        textView.text = "Tab: ${arguments?.getString("tabName")}" 
        return textView
    }

    override fun onResume() {
        super.onResume()
        val view = view as? TextView ?: return
        animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
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
