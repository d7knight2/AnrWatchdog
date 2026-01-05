# Testing Documentation

This document describes the testing infrastructure for the ANR Watchdog project.

## Test Structure

The project includes comprehensive testing at multiple levels:

### 1. Unit Tests

#### ANRWatchdog Library Tests
Location: `anrwatchdog/src/test/kotlin/com/example/anrwatchdog/`

- **ANRWatchdogTest.kt**: Tests the core ANRWatchdog functionality including:
  - Initialization and singleton behavior
  - Fluent API pattern (setTimeout, setLogLevel, setCallback)
  - Start/stop functionality
  - Multiple start call handling

#### Root Project Tests
Location: `src/test/kotlin/com/d7knight/anrwatchdog/`

- **DependencyAnalyzerV3UnitTest.kt**: Tests coroutine debugging and repository integrations
- **FailingTest.kt**: Basic test to verify test infrastructure (now passing)

### 2. Instrumented/UI Tests (Android Tests)

Location: `demoapp/src/androidTest/kotlin/com/example/demoapp/`

#### MainActivityTest.kt
Tests basic UI functionality:
- Activity launch verification
- Tab button display and functionality
- Tab switching between multiple tabs
- ANR simulation button existence and clickability
- Multiple tab switch scenarios

#### AnrSimulationTest.kt
Tests ANR simulation functionality:
- Main thread blocking detection
- Debug info recording
- ANR simulation from different tabs
- Multiple ANR simulations
- Stack trace capture and validation

#### FloatingDebugViewTest.kt
Tests debug view functionality:
- Active thread collection and display
- General debug info collection (memory, processor, thread count)
- Main thread block recording
- Debug info persistence across tab switches
- Debug info updates after UI interactions

#### MemoryLeakTest.kt
Tests memory leak detection integration:
- Activity lifecycle leak detection with LeakCanary
- Fragment lifecycle leak testing
- ANR simulation memory leak prevention
- Floating debug view leak prevention
- Multiple activity recreation scenarios

## Running Tests

### Run All Unit Tests
```bash
./gradlew test
```

### Run Specific Module Tests
```bash
# Test ANRWatchdog library
./gradlew anrwatchdog:test

# Test root project
./gradlew test
```

### Run Instrumented Tests (Requires Emulator or Device)
```bash
# Run all instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest --tests "com.example.demoapp.MainActivityTest"

# Run specific test method
./gradlew connectedAndroidTest --tests "com.example.demoapp.MainActivityTest.testTabSwitching"
```

### Build APK
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## Continuous Integration (CI)

The project uses GitHub Actions for CI/CD. See `.github/workflows/android-ci.yml`.

### CI Jobs

1. **test**: Runs all unit tests
   - Executes `./gradlew test`
   - Uploads test reports as artifacts

2. **build**: Builds the demo app APK
   - Executes `./gradlew assembleDebug`
   - Uploads APK as artifact

3. **instrumented-test**: Runs UI/instrumented tests
   - Uses Android emulator (API 29, x86_64)
   - Executes `./gradlew connectedAndroidTest`
   - Uploads test reports as artifacts

### Triggering CI

CI runs automatically on:
- Pushes to `main`, `develop`, or `copilot/**` branches
- Pull requests to `main` or `develop` branches

## Test Coverage

The test suite covers:

### Functionality Tested
- ✅ ANR detection and simulation
- ✅ Main thread blocking detection
- ✅ Debug info collection (threads, memory, stack traces)
- ✅ UI interactions (tab switching, button clicks)
- ✅ Memory leak detection integration
- ✅ Activity and fragment lifecycle
- ✅ Floating debug view functionality

### Test Types
- ✅ Unit tests (pure Kotlin/Java logic)
- ✅ Instrumented tests (Android UI and framework)
- ✅ Integration tests (multiple components)
- ✅ Lifecycle tests (activity/fragment creation and destruction)

## Test Dependencies

### ANRWatchdog Module
- JUnit 4.13.2
- Mockito 5.3.1
- Kotlin Test 1.9.0

### DemoApp Module
- AndroidX Test (JUnit, Espresso, Runner, Rules)
- Kotlin Test 1.9.0

### Debug Dependencies
- LeakCanary 2.15-alpha-2 (memory leak detection)

## Writing New Tests

### Unit Test Template
```kotlin
package com.example.anrwatchdog

import org.junit.Test
import kotlin.test.assertEquals

class MyNewTest {
    @Test
    fun testMyFeature() {
        // Arrange
        val expected = "value"
        
        // Act
        val actual = myFunction()
        
        // Assert
        assertEquals(expected, actual)
    }
}
```

### Instrumented Test Template
```kotlin
package com.example.demoapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MyInstrumentedTest {
    @Test
    fun testMyUIFeature() {
        // Test implementation
    }
}
```

## Troubleshooting

### Tests Not Running
- Ensure Gradle sync completed successfully
- Check that test dependencies are downloaded
- Verify JDK version (requires JDK 17 for CI)

### Instrumented Tests Failing
- Ensure emulator or device is connected: `adb devices`
- Check Android version compatibility (minSdk 21, targetSdk 34)
- Verify app permissions if needed

### CI Failures
- Check GitHub Actions logs for specific errors
- Verify workflow file syntax
- Ensure all dependencies are accessible

## Best Practices

1. **Keep tests focused**: Each test should verify one specific behavior
2. **Use descriptive names**: Test names should clearly indicate what they test
3. **Clean up resources**: Use `@Before` and `@After` for setup and teardown
4. **Avoid Thread.sleep**: Use IdlingResource or proper waits when possible
5. **Test edge cases**: Include tests for error conditions and boundary cases
6. **Mock external dependencies**: Use Mockito for unit tests to isolate behavior
7. **Run tests locally**: Before pushing, run tests to catch issues early

## Future Improvements

Potential areas for enhanced testing:
- Performance testing for ANR detection overhead
- Stress testing with multiple concurrent ANR events
- More comprehensive coroutine debugging tests
- Automated screenshot testing
- Code coverage reporting
- Integration with code quality tools (ktlint, detekt)
