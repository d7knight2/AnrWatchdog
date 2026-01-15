import kotlinx.coroutines.debug.DebugProbes

/**
 * AnrWatchdog is a lightweight singleton that initializes Kotlin Coroutine DebugProbes
 * for global coroutine debugging and ANR analysis.
 *
 * This object automatically installs DebugProbes when the class is loaded, enabling:
 * - Creation stack trace capture for all active coroutines
 * - Coroutine state inspection during ANR events
 * - Enhanced debugging information in production environments
 *
 * The DebugProbes installation is idempotent and has minimal runtime overhead
 * when not actively dumping coroutine information.
 *
 * Usage:
 * Simply reference this object to ensure DebugProbes are installed:
 * ```kotlin
 * AnrWatchdog // Initialization happens automatically
 * ```
 *
 * Note: This is separate from the com.example.anrwatchdog.ANRWatchdog class
 * which provides full ANR detection and monitoring capabilities.
 */
object AnrWatchdog {
    init {
        // Install DebugProbes globally to enable coroutine debugging features
        DebugProbes.install()
        
        // Enable capturing of creation stack traces for all coroutines
        // This allows tracking where coroutines were created, which is
        // invaluable for debugging ANR issues involving suspended coroutines
        DebugProbes.enableCreationStackTraces = true
    }
}