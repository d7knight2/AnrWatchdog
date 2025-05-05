import kotlinx.coroutines.debug.DebugProbes

object AnrWatchdog {
    init {
        DebugProbes.install() // Install DebugProbes globally
        DebugProbes.enableCreationStackTraces = true // Enable capturing of creation stack traces
    }
}