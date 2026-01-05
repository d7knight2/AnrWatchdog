package com.example.demoapp

import android.app.Application
import android.util.Log
import com.example.anrwatchdog.ANRWatchdog

class LeakWatcherApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize ANRWatchdog
        try {
            ANRWatchdog.initialize(this)
                .setLogLevel(Log.DEBUG)
                .setTimeout(5000)
                .setCallback { thread ->
                    Log.w("LeakWatcherApp", "ANR detected on thread: ${thread.name}")
                }
                .start()
            Log.d("LeakWatcherApp", "Demo app started - ANRWatchdog initialized successfully")
        } catch (e: Exception) {
            Log.w("LeakWatcherApp", "ANRWatchdog initialization failed: ${e.message}")
        }
    }
}