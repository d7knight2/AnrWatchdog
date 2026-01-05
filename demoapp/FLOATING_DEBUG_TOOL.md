# Floating Debug Tool

## Overview

The Floating Debug Tool is a developer-friendly feature integrated into the demo app that provides real-time debugging information in a draggable, non-intrusive overlay. This tool is designed to help developers monitor and debug ANR (Application Not Responding) issues and understand thread behavior in the application.

## Features

### 1. **Active Threads Display**
Shows a comprehensive list of all currently active threads in the application, including:
- Thread name
- Thread state (RUNNABLE, WAITING, BLOCKED, etc.)
- Thread ID
- Thread priority
- Whether the thread is a daemon thread

### 2. **Recent Main Thread Blocks**
Displays a chronological list of recent main thread blocking events that could lead to ANRs, including:
- Timestamp of when the block occurred
- Duration of the block in milliseconds
- Stack trace showing where the block happened

### 3. **General Debug Information**
Provides system-level debugging information:
- Total number of active threads
- Current thread context (Main thread or not)
- Memory usage statistics (used, free, total, max)
- Available processor count
- Total number of blocks recorded

## Architecture

The floating debug tool consists of three main components:

### 1. **FloatingDebugView.kt**
The main UI component that creates and manages the draggable debug overlay.

**Key Features:**
- Draggable anywhere on the screen
- Expandable/collapsible interface
- Auto-updates every 2 seconds when expanded
- Non-intrusive design with minimal footprint when collapsed

**Usage:**
```kotlin
val floatingDebugView = FloatingDebugView(context)
floatingDebugView.show(rootLayout) // Attach to parent ViewGroup
floatingDebugView.hide() // Remove the view
```

### 2. **DebugInfoCollector.kt**
A utility object that collects and manages debug information from various sources.

**Key Methods:**
- `recordMainThreadBlock(duration, stackTrace)`: Records a main thread block event
- `getRecentMainThreadBlocks()`: Returns list of recent blocks
- `getActiveThreads()`: Returns list of all active threads with their info
- `getGeneralDebugInfo()`: Returns system-level debug information

**Usage:**
```kotlin
// Record a main thread block
DebugInfoCollector.recordMainThreadBlock(2000, stackTrace)

// Get active threads
val threads = DebugInfoCollector.getActiveThreads()

// Get debug info
val info = DebugInfoCollector.getGeneralDebugInfo()
```

### 3. **Integration with MainActivity**
The MainActivity is updated to:
- Create and show the floating debug view on startup
- Automatically update the debug information every 2 seconds
- Clean up resources on activity destruction

## How to Use

### Starting the Debug Tool

The floating debug tool automatically starts when you launch the demo app. You'll see a small button labeled "Debug Tool 🔧" in the top-left area of the screen.

### Expanding the Debug Tool

1. Tap on the "Debug Tool 🔧" button
2. The view will expand to show all debug information
3. The button text changes to "Debug Tool 🔧 ▼" when expanded

### Dragging the Debug Tool

1. Touch and hold the debug tool view
2. Drag it to any position on the screen
3. Release to place it in the new position

### Collapsing the Debug Tool

1. Tap on the "Debug Tool 🔧 ▼" button again
2. The view will collapse back to just the button
3. This minimizes screen real estate usage while keeping the tool accessible

### Simulating a Main Thread Block

To test the main thread block tracking:
1. Switch to any tab in the demo app
2. Tap the "Simulate ANR (Block Main Thread)" button
3. The app will freeze for 2 seconds (simulating an ANR)
4. Open the debug tool to see the recorded block event

## Implementation Details

### Thread Safety
- Uses `CopyOnWriteArrayList` for thread-safe storage of block events
- All public methods in `DebugInfoCollector` are thread-safe

### Memory Management
- Limits stored block events to the most recent 20 entries
- Automatically cleans up old entries
- Minimal memory footprint

### Performance
- Updates run on the main thread but are lightweight
- Update interval is configurable (default: 2 seconds)
- View updates only when expanded to minimize overhead

### UI Design
- Semi-transparent dark background for visibility
- Color-coded sections (green for titles)
- Scrollable content area for large data sets
- Touch-friendly button size

## Code Examples

### Recording a Custom Block Event

```kotlin
import com.example.demoapp.debug.DebugInfoCollector

// Simulate or capture a block
val startTime = System.currentTimeMillis()
// ... some blocking operation ...
val duration = System.currentTimeMillis() - startTime

// Get stack trace
val stackTrace = Thread.currentThread().stackTrace.joinToString("\n") { 
    "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
}

// Record it
DebugInfoCollector.recordMainThreadBlock(duration, stackTrace)
```

### Customizing the Update Interval

In MainActivity.kt:
```kotlin
private val updateRunnable = object : Runnable {
    override fun run() {
        floatingDebugView.updateDebugInfo()
        handler.postDelayed(this, 1000) // Update every 1 second instead of 2
    }
}
```

## Future Enhancements

Potential improvements for the floating debug tool:

1. **Filtering Options**: Add filters to show only specific thread states
2. **Export Functionality**: Allow exporting debug logs to a file
3. **Real-time ANR Detection**: Integrate with ANRWatchdog to automatically record blocks
4. **Performance Graphs**: Add visual graphs for CPU and memory usage over time
5. **Network Monitoring**: Display network request information
6. **Custom Metrics**: Allow developers to add custom debug metrics

## Troubleshooting

### The debug tool doesn't appear
- Check that the MainActivity properly initializes the FloatingDebugView
- Ensure the rootLayout is a FrameLayout or similar ViewGroup that supports overlays

### The debug tool can't be dragged
- Verify touch events are not being intercepted by other views
- Check that the view's parent allows touch event propagation

### Debug information is not updating
- Confirm the Handler is properly posting the update runnable
- Check that the view is expanded when expecting updates

## License

This feature is part of the ANR Watchdog project and follows the same Apache License 2.0.
