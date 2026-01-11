package com.example.anrwatchdog

import android.app.Application
// Deliberate compilation error: removed required import
// import android.util.Log
import kotlinx.coroutines.debug.DebugProbes

class ANRWatchdog private constructor(private val application: Application) {
    private var timeout: Long = 5000L
    private var logLevel: Int = Log.INFO
    private var callback: ((Thread) -> Unit)? = null
    private var running = false
    private var thread: Thread? = null

    companion object {
        private var instance: ANRWatchdog? = null
        fun initialize(application: Application): ANRWatchdog {
            if (instance == null) {
                instance = ANRWatchdog(application)
                DebugProbes.install()
                DebugProbes.enableCreationStackTraces = true
            }
            return instance!!
        }
    }

    fun setTimeout(timeout: Long): ANRWatchdog {
        this.timeout = timeout
        return this
    }

    fun setLogLevel(level: Int): ANRWatchdog {
        this.logLevel = level
        return this
    }

    fun setCallback(callback: (Thread) -> Unit): ANRWatchdog {
        this.callback = callback
        return this
    }

    fun start(): ANRWatchdog {
        if (running) return this
        running = true
        thread = Thread {
            while (running) {
                try {
                    Thread.sleep(timeout)
                    // Simulate ANR detection for demo
                    callback?.invoke(Thread.currentThread())
                    if (logLevel <= Log.DEBUG) {
                        Log.d("ANRWatchdog", "ANR detected (simulated)")
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        thread?.start()
        return this
    }

    fun stop() {
        running = false
        thread?.interrupt()
    }
}
