# ANRWatchdog Utilities and Test Support

This module contains utility classes, fake implementations, and test support code for the ANRWatchdog project. These classes are used for testing ANR detection, coroutine debugging, and repository pattern demonstrations.

## Overview

The utilities module provides:
- **Dependency tracing** for debugging execution flow
- **Fake repository implementations** for testing various blocking scenarios
- **RxJava integration** examples for legacy code interoperability
- **Coroutine debugging** utilities

## Module Structure

```
src/
├── main/kotlin/com/d7knight/anrwatchdog/
│   ├── AnrWatchdog.kt                    # DebugProbes initializer
│   ├── DependencyAnalyzerV3.kt           # Execution flow tracer
│   ├── FakeLogger.kt                     # Simple logger for testing
│   ├── FakeBlockingRepository.kt         # Main demo orchestrator
│   ├── blocking/
│   │   ├── BlockingRxJavaInteroptRepository.kt  # RxJava simulation
│   │   └── FakeBlockingRepository.kt     # Concurrent execution demo
│   ├── experiment/
│   │   └── ExperimentCheckRepository.kt  # A/B testing simulation
│   ├── glide/
│   │   └── FakeGlideRepository.kt        # Image loading simulation
│   ├── network/
│   │   └── FakeOkHttpRepository.kt       # Network call simulation
│   └── rxjava/
│       └── SlowRxExperimentEnabledRepository.kt  # RxJava with TestScheduler
└── test/kotlin/com/d7knight/anrwatchdog/
    ├── DependencyAnalyzerV3UnitTest.kt   # Integration test
    ├── DependencyAnalyzerTest.kt         # Unit tests (10 cases)
    ├── FakeRepositoriesTest.kt           # Repository tests (9 cases)
    ├── FakeLoggerTest.kt                 # Logger tests (7 cases)
    ├── SlowRxExperimentEnabledRepositoryTest.kt  # RxJava tests (10 cases)
    ├── ApkDiscoveryTest.kt
    ├── AppetizeUploadTest.kt
    └── FailingTest.kt
```

## Key Components

### 1. AnrWatchdog (DebugProbes Initializer)

**Purpose**: Initializes Kotlin Coroutine DebugProbes globally

**Usage**:
```kotlin
AnrWatchdog  // Simply reference to trigger initialization
```

**Features**:
- Installs DebugProbes on class load
- Enables creation stack trace capture
- Required for coroutine debugging during ANRs

### 2. DependencyAnalyzerV3

**Purpose**: Thread-safe execution flow tracer for debugging

**Usage**:
```kotlin
DependencyAnalyzerV3.logEvent("Operation started")
// ... perform operations ...
DependencyAnalyzerV3.logEvent("Operation completed")
DependencyAnalyzerV3.dump()  // Print all events
```

**Features**:
- Thread-safe event logging
- Automatic thread name capture
- Post-execution analysis support

**Example Output**:
```
----- Dependency Analyzer Dump Start -----
[main] Application started
[Worker-1] Background task initiated
[Worker-1] Background task completed
[main] Application finished
----- Dependency Analyzer Dump End -----
```

### 3. FakeLogger

**Purpose**: Simple logging utility for tests without Android dependencies

**Usage**:
```kotlin
FakeLogger.log("Test message")
// Output: [FakeLogger] Test message
```

**Features**:
- Works in unit tests
- No Android dependencies
- Simple println-based output

### 4. Repository Implementations

#### FakeOkHttpRepository (Network Operations)

Simulates HTTP requests with proper coroutine handling:

```kotlin
suspend fun example() {
    FakeOkHttpRepository.performBlockingOperation(1)
    // Simulates 50ms network delay
}
```

**Use Cases**:
- Testing network call handling
- Demonstrating proper dispatcher usage
- ANR detection scenarios

#### FakeGlideRepository (Image Loading)

Simulates image loading operations:

```kotlin
suspend fun loadImage() {
    FakeGlideRepository.performBlockingOperation(1)
    // Simulates image decoding
}
```

**Use Cases**:
- Image loading ANR scenarios
- Proper background thread usage
- Resource-intensive operations

#### ExperimentCheckRepository (A/B Testing)

Demonstrates structured concurrency with coroutineScope:

```kotlin
suspend fun checkExperiment() {
    ExperimentCheckRepository.performNonBlockingOperation(1)
    // Uses coroutineScope for structured concurrency
}
```

**Use Cases**:
- Feature flag checking
- A/B test evaluation
- Structured concurrency patterns

#### BlockingRxJavaInteroptRepository

RxJava/Coroutine interoperability demonstration:

```kotlin
suspend fun legacyOperation() {
    BlockingRxJavaInteroptRepository.performBlockingOperation(1)
    // Bridges RxJava and coroutines
}
```

**Use Cases**:
- Legacy RxJava integration
- Migration from RxJava to Coroutines
- Hybrid codebases

#### SlowRxExperimentEnabledRepository

RxJava with TestScheduler for deterministic testing:

```kotlin
val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(1)
publisher.subscribe { event -> println(event) }

// Control time in tests
SlowRxExperimentEnabledRepository.advanceTimeBy(100, TimeUnit.MILLISECONDS)
```

**Use Cases**:
- Testing time-dependent RxJava code
- Controlled timing without actual delays
- Deterministic test execution

## Running the Demo

### Main Function

The `FakeBlockingRepository.kt` file contains a main function demonstrating concurrent execution:

```bash
./gradlew run
```

This will:
1. Launch 4 concurrent repository operations
2. Display execution traces with thread names
3. Wait for all operations to complete

**Expected Output**:
```
Main coroutine started
OkHttp coroutine started
Glide coroutine started
Experiment coroutine started
Started FakeJob-1
Started OkHttpJob-2
Started GlideJob-3
Started ExperimentJob-4
Finished FakeJob-1
Finished OkHttpJob-2
Finished GlideJob-3
Finished ExperimentJob-4
All coroutines completed
```

## Testing

### Running Tests

```bash
# Run all tests in this module
./gradlew test

# Run specific test class
./gradlew test --tests "FakeRepositoriesTest"

# Generate coverage report
./gradlew test jacocoTestReport

# View coverage
open build/reports/jacoco/test/html/index.html
```

### Test Coverage

**Current Status**: 36+ test cases across 5 test files

| Test Class | Test Count | Coverage Focus |
|------------|------------|----------------|
| FakeRepositoriesTest | 9 | Repository operations, concurrency |
| DependencyAnalyzerTest | 10 | Event logging, thread safety |
| FakeLoggerTest | 7 | Logging functionality |
| SlowRxExperimentEnabledRepositoryTest | 10 | RxJava time control |
| DependencyAnalyzerV3UnitTest | 1 | Integration test |

**Target**: 90%+ code coverage

## Integration Examples

### Example 1: Testing ANR Detection

```kotlin
// In your test
@Test
fun testAnrDetection() = runBlocking {
    DependencyAnalyzerV3.logEvent("Test started")
    
    // Simulate potentially blocking operations
    launch { FakeOkHttpRepository.performBlockingOperation(1) }
    launch { FakeGlideRepository.performBlockingOperation(2) }
    
    delay(200)  // Wait for operations
    
    DependencyAnalyzerV3.logEvent("Test completed")
    DependencyAnalyzerV3.dump()
}
```

### Example 2: Testing Concurrent Operations

```kotlin
@Test
fun testConcurrentExecution() = runBlocking {
    val jobs = listOf(
        launch { FakeOkHttpRepository.performBlockingOperation(1) },
        launch { FakeGlideRepository.performBlockingOperation(2) },
        launch { ExperimentCheckRepository.performNonBlockingOperation(3) }
    )
    
    jobs.forEach { it.join() }
    
    // All operations should complete without blocking each other
    assertTrue(true, "Concurrent execution successful")
}
```

### Example 3: RxJava Timing Control

```kotlin
@Test
fun testRxJavaOperation() {
    val events = mutableListOf<String>()
    
    val publisher = SlowRxExperimentEnabledRepository.performSlowOperation(1)
    publisher.subscribe { event -> events.add(event) }
    
    // Advance virtual time
    SlowRxExperimentEnabledRepository.advanceTimeBy(100, TimeUnit.MILLISECONDS)
    
    assertEquals(2, events.size)
    assertTrue(events.contains("Started SlowRxExperimentJob-1"))
    assertTrue(events.contains("Finished SlowRxExperimentJob-1"))
}
```

## Design Patterns

### 1. Repository Pattern

All fake repositories follow the repository pattern:
- Single responsibility (network, images, experiments)
- Suspend functions for async operations
- Named coroutines for debugging
- Proper dispatcher usage

### 2. Singleton Pattern

Utilities use object declarations (singletons):
- Thread-safe by default
- Single instance per JVM
- Global state management

### 3. Observer Pattern

RxJava repositories use reactive streams:
- Event-driven architecture
- Backpressure handling
- Asynchronous data flow

## Best Practices Demonstrated

### ✅ Proper Dispatcher Usage

All blocking operations use `Dispatchers.Default`:

```kotlin
withContext(Dispatchers.Default + CoroutineName("Job-$index")) {
    // Blocking work here
}
```

### ✅ Named Coroutines

Every coroutine gets a descriptive name:

```kotlin
launch(Dispatchers.Default + CoroutineName("OkHttp")) {
    // Easy to identify in debug logs
}
```

### ✅ Structured Concurrency

Using `coroutineScope` for proper lifecycle management:

```kotlin
suspend fun operation() {
    coroutineScope {
        launch { /* Child completes before parent */ }
    }
}
```

### ✅ Thread-Safe Collections

Using `CopyOnWriteArrayList` for concurrent access:

```kotlin
private val dependencies = CopyOnWriteArrayList<String>()
```

## Common Use Cases

### Debugging ANR Issues

1. Add `DependencyAnalyzerV3.logEvent()` calls around suspicious code
2. Trigger the ANR
3. Call `DependencyAnalyzerV3.dump()` to see execution flow
4. Identify which operations were in progress

### Testing Timeout Scenarios

1. Use `SlowRxExperimentEnabledRepository` with `TestScheduler`
2. Control time advancement precisely
3. Verify timeout handling without real waits
4. Test edge cases deterministically

### Migrating from RxJava

1. Study `BlockingRxJavaInteroptRepository` patterns
2. Use suspend functions instead of Observable
3. Replace schedulers with Dispatchers
4. Maintain similar operation semantics

## Build Configuration

### Dependencies

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.7.3")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.7.3")
    
    testImplementation(kotlin("test"))
}
```

### JVM Toolchain

- **Target**: JVM 8
- **Kotlin Version**: 1.9.0

## Related Documentation

- [Main README](../README.md) - Project overview
- [ANRWatchdog Library README](../anrwatchdog/README.md) - Core library docs
- [TESTING.md](../TESTING.md) - Testing guidelines
- [CODE_COVERAGE.md](../CODE_COVERAGE.md) - Coverage reports

## Contributing

When adding new utilities:

1. Follow existing patterns (object declarations, suspend functions)
2. Add comprehensive KDoc comments
3. Include unit tests (aim for 90%+ coverage)
4. Use named coroutines for debugging
5. Document use cases and examples

## License

Apache License 2.0 - See [LICENSE](../LICENSE) for details.
