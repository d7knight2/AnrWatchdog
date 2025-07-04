package com.example.demoapp

import android.app.Application
import android.util.Log

class LeakWatcherApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize ANRWatchdog
        try {
            // Note: This will need to be implemented when the ANRWatchdog library is ready
            Log.d("LeakWatcherApp", "Demo app started - ANRWatchdog initialization would go here")
        } catch (e: Exception) {
            Log.w("LeakWatcherApp", "ANRWatchdog not available yet: ${e.message}")
        }
    }
}