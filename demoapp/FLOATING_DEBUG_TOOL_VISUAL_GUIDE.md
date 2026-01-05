# Floating Debug Tool - Visual Guide

## User Interface Overview

### Collapsed State
```
┌─────────────────────────┐
│  Debug Tool 🔧 ▶        │
└─────────────────────────┘
```
When collapsed, the debug tool appears as a small button that can be moved around the screen.

### Expanded State
```
┌─────────────────────────────────────────────────────────┐
│  Debug Tool 🔧 ▼                                        │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Active Threads                                          │
│  ───────────────────────────────────────────────────    │
│  Name: main                                              │
│  State: RUNNABLE                                         │
│  ID: 1                                                   │
│  Priority: 5                                             │
│  Daemon: false                                           │
│                                                          │
│  Name: HeapTaskDaemon                                    │
│  State: WAITING                                          │
│  ID: 12                                                  │
│  Priority: 5                                             │
│  Daemon: true                                            │
│  ───────────────────────────────────────────────────    │
│                                                          │
│  Recent Main Thread Blocks                               │
│  ───────────────────────────────────────────────────    │
│  Time: 14:32:15.234                                      │
│  Duration: 2000ms                                        │
│  Stack Trace (first 3 lines):                            │
│    at TabFragment.simulateMainThreadBlock(...)           │
│    at android.view.View$OnClickListener.onClick(...)     │
│    at android.view.View.performClick(...)                │
│  ───────────────────────────────────────────────────    │
│                                                          │
│  General Debug Info                                      │
│  ───────────────────────────────────────────────────    │
│  Total Threads: 24                                       │
│  Main Thread: YES                                        │
│  Memory Used: 45MB                                       │
│  Memory Free: 78MB                                       │
│  Memory Total: 123MB                                     │
│  Memory Max: 512MB                                       │
│  Available Processors: 8                                 │
│  Total Blocks Recorded: 1                                │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

## Component Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        MainActivity                           │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                                                         │  │
│  │  Tab Navigation (Tab 1, Tab 2, Tab 3)                  │  │
│  │                                                         │  │
│  │  ┌───────────────────────────────────────────────┐     │  │
│  │  │                                                │     │  │
│  │  │           TabFragment Content                  │     │  │
│  │  │                                                │     │  │
│  │  │  • Tab-specific content                        │     │  │
│  │  │  • "Simulate ANR" button                       │     │  │
│  │  │                                                │     │  │
│  │  └───────────────────────────────────────────────┘     │  │
│  │                                                         │  │
│  │  ╔═══════════════════════════════╗  ← Draggable        │  │
│  │  ║  Debug Tool 🔧 ▶             ║                      │  │
│  │  ╚═══════════════════════════════╝                     │  │
│  │                                    Floating Debug View  │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

## Data Flow

```
┌──────────────────────┐
│   TabFragment        │
│  "Simulate ANR"      │
│     Button           │
└──────────┬───────────┘
           │
           │ 1. User clicks button
           │
           ▼
┌──────────────────────┐
│ simulateMainThread   │
│      Block()         │
│                      │
│ • Captures stack     │
│ • Blocks for 2s      │
│ • Calculates duration│
└──────────┬───────────┘
           │
           │ 2. Records block event
           │
           ▼
┌──────────────────────┐
│  DebugInfoCollector  │
│                      │
│ • Stores blocks      │
│ • Tracks threads     │
│ • Collects system    │
│   information        │
└──────────┬───────────┘
           │
           │ 3. Queries debug info
           │    (every 2 seconds)
           │
           ▼
┌──────────────────────┐
│  FloatingDebugView   │
│                      │
│ • Displays threads   │
│ • Shows blocks       │
│ • Updates UI         │
└──────────────────────┘
```

## Interaction Flow

### Opening the Debug Tool
```
User Action: Tap "Debug Tool 🔧 ▶"
     ↓
View expands to show full content
     ↓
Button text changes to "Debug Tool 🔧 ▼"
     ↓
Auto-update begins (every 2 seconds)
     ↓
Display: Active threads, blocks, system info
```

### Dragging the Debug Tool
```
User Action: Touch and hold anywhere on the tool
     ↓
Initial position captured
     ↓
User Action: Move finger
     ↓
View follows finger movement
     ↓
User Action: Release finger
     ↓
View stays at new position
```

### Simulating an ANR
```
User Action: Tap "Simulate ANR" button
     ↓
Main thread blocks for 2 seconds
     ↓
Stack trace captured
     ↓
Block event recorded to DebugInfoCollector
     ↓
User Action: Open debug tool
     ↓
Recent block appears in "Recent Main Thread Blocks" section
```

## Color Scheme

- **Background**: Semi-transparent black (0xCC000000)
- **Button**: Blue (0xFF2196F3)
- **Section Titles**: Green (0xFF4CAF50) - Bold
- **Text Content**: White (0xFFFFFFFF)
- **Dividers**: Gray (0xFF555555)

## Responsive Behavior

- **Collapsed**: ~200x50 pixels
- **Expanded**: 800x600 pixels (scrollable if content exceeds)
- **Position**: Preserved when toggling between collapsed/expanded
- **Touch Target**: Large enough for easy tapping (20px padding)

## Use Cases

### 1. Monitoring Thread Activity
Developer wants to see which threads are active:
1. Open the floating debug tool
2. Scroll to "Active Threads" section
3. View all threads with their current states

### 2. Investigating ANR Issues
Developer experiences an ANR:
1. Trigger the ANR (or it happens naturally)
2. Open the floating debug tool
3. Check "Recent Main Thread Blocks" section
4. Review timestamp, duration, and stack trace

### 3. Memory Monitoring
Developer suspects memory issues:
1. Open the floating debug tool
2. Check "General Debug Info" section
3. Monitor "Memory Used", "Memory Free", "Memory Total"
4. Observe changes over time

### 4. Testing ANR Detection
Developer wants to test ANR handling:
1. Navigate to any tab
2. Tap "Simulate ANR (Block Main Thread)"
3. Wait for app to freeze (2 seconds)
4. Open debug tool to verify block was recorded
