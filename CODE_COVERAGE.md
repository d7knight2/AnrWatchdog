# Code Coverage Documentation

This document describes the code coverage setup and reporting for the ANR Watchdog project.

## Overview

The project uses JaCoCo (Java Code Coverage) to measure and report test coverage across all modules. Coverage reports are automatically generated during CI builds and can also be generated locally.

## Coverage Goals

- **Target Coverage**: 90% minimum coverage for all production code
- **Measured Metrics**: Line coverage, branch coverage, and instruction coverage
- **Exclusions**: Test files, generated code, Android framework classes

## Generating Coverage Reports Locally

### For the Root Module (Kotlin/JVM)

```bash
# Run tests and generate coverage report
./gradlew test jacocoTestReport

# View HTML report
open build/reports/jacoco/test/html/index.html
# Or on Linux: xdg-open build/reports/jacoco/test/html/index.html
```

### For the ANRWatchdog Library Module

```bash
# Run tests and generate coverage report
./gradlew anrwatchdog:testDebugUnitTest anrwatchdog:jacocoTestReport

# View HTML report
open anrwatchdog/build/reports/jacoco/jacocoTestReport/html/index.html
```

### For All Modules

```bash
# Run all tests and generate all coverage reports
./gradlew test jacocoTestReport

# Coverage reports will be available at:
# - build/reports/jacoco/test/html/index.html (root module)
# - anrwatchdog/build/reports/jacoco/jacocoTestReport/html/index.html (library)
```

## CI/CD Integration

### GitHub Actions Workflows

Coverage reports are automatically generated in the following workflows:

1. **android-ci.yml**: Runs on pushes to main/develop branches
2. **pr-validation.yml**: Runs on pull requests

### Coverage Artifacts

After each CI run, coverage reports are available as downloadable artifacts:
- **coverage-reports**: Contains JaCoCo HTML and XML reports
- **test-reports**: Contains JUnit test execution reports

To download artifacts:
1. Go to the GitHub Actions run page
2. Scroll to the "Artifacts" section at the bottom
3. Download the `coverage-reports` artifact
4. Extract and open `index.html` in a browser

## Coverage Report Structure

### XML Report
- **Location**: `build/reports/jacoco/test/jacocoTestReport.xml`
- **Purpose**: Machine-readable format for CI integration and coverage tools
- **Usage**: Can be uploaded to coverage services like Codecov, Coveralls, or SonarQube

### HTML Report
- **Location**: `build/reports/jacoco/test/html/index.html`
- **Purpose**: Human-readable format for developers
- **Contents**:
  - Overall coverage summary
  - Package-level coverage breakdown
  - Class-level coverage details
  - Line-by-line coverage highlighting

### CSV Report
- **Status**: Disabled by default (can be enabled in build.gradle.kts)
- **Purpose**: Spreadsheet-friendly format for analysis

## Understanding Coverage Metrics

### Line Coverage
- **Definition**: Percentage of executable lines that were executed during tests
- **Interpretation**: High line coverage indicates most code paths are tested

### Branch Coverage
- **Definition**: Percentage of decision branches (if/else, switch, loops) tested
- **Interpretation**: Important for catching edge cases and conditional logic bugs

### Instruction Coverage
- **Definition**: Percentage of bytecode instructions executed
- **Interpretation**: Most granular coverage metric

## Excluded from Coverage

The following are excluded from coverage calculations:

- Test files (`**/*Test*.kt`, `**/*Test*.java`)
- Android generated files (`**/BuildConfig.*`, `**/R.class`, `**/R$*.class`)
- Synthetic classes (`**/*Lambda*.class`, `**/*\$ViewInjector*.*`)
- Android framework code (`android/**/*.*`)

## Coverage Best Practices

### Writing Tests for Coverage

1. **Test Happy Paths**: Ensure normal execution flows are tested
2. **Test Edge Cases**: Cover boundary conditions and special values
3. **Test Error Paths**: Verify exception handling and error scenarios
4. **Test Concurrent Access**: For thread-safe code, test concurrent operations

### Example: Improving Coverage

If coverage report shows uncovered lines in a class:

```kotlin
// Uncovered code (shown in red in HTML report)
fun processData(data: String?): String {
    if (data == null) {
        return "default"  // ❌ Not covered
    }
    return data.uppercase()
}
```

Add test to improve coverage:

```kotlin
@Test
fun testProcessDataWithNull() {
    val result = processData(null)
    assertEquals("default", result)  // ✅ Now covered
}
```

## Coverage Verification

### Enforcing Minimum Coverage

The root module build is configured to enforce 90% minimum coverage:

```kotlin
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}
```

To check if coverage meets requirements:

```bash
./gradlew jacocoTestCoverageVerification
```

This will fail the build if coverage is below 90%.

## Current Coverage Status

As of the latest build:

### Root Module Coverage
- **Package**: `com.d7knight.anrwatchdog`
- **Classes Covered**:
  - ✅ DependencyAnalyzerV3: Comprehensive tests (10 test cases)
  - ✅ FakeLogger: Complete coverage (7 test cases)
  - ✅ FakeOkHttpRepository: Full coverage (9 test cases)
  - ✅ FakeGlideRepository: Full coverage (9 test cases)
  - ✅ ExperimentCheckRepository: Full coverage (9 test cases)
  - ✅ BlockingRxJavaInteroptRepository: Full coverage (9 test cases)
  - ✅ SlowRxExperimentEnabledRepository: Complete coverage (10 test cases)
  - ✅ AnrWatchdog: Covered via initialization tests

### ANRWatchdog Library Coverage
- **Package**: `com.example.anrwatchdog`
- **Classes Covered**:
  - ✅ ANRWatchdog: Comprehensive tests (24 test cases including edge cases)
    - Initialization and singleton behavior
    - Fluent API pattern
    - Start/stop functionality
    - Timeout boundaries
    - Concurrent access
    - Exception handling

## Troubleshooting

### Coverage Report Not Generated

**Problem**: JaCoCo report is empty or missing

**Solutions**:
1. Ensure tests are passing: `./gradlew test`
2. Check for test execution data: `find . -name "*.exec"`
3. Verify JaCoCo plugin is applied in `build.gradle.kts`
4. Clean and rebuild: `./gradlew clean test jacocoTestReport`

### Low Coverage Despite Writing Tests

**Problem**: Tests exist but coverage is still low

**Solutions**:
1. Verify tests are actually running: Check test reports
2. Ensure tests are in the correct source set (`src/test/kotlin`)
3. Check test naming follows conventions (`*Test.kt`)
4. Verify JaCoCo is measuring the correct source directories

### Coverage Report Shows Wrong Files

**Problem**: Coverage includes generated or test files

**Solutions**:
1. Check exclusion patterns in `build.gradle.kts`
2. Ensure `classDirectories.setFrom()` has proper filters
3. Verify source and class directory paths are correct

## Integration with External Services

### Codecov Integration (Optional)

To upload coverage to Codecov:

```yaml
- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
  with:
    files: ./build/reports/jacoco/test/jacocoTestReport.xml
    flags: unittests
    name: codecov-umbrella
    fail_ci_if_error: true
```

### SonarQube Integration (Optional)

For SonarQube analysis:

```bash
./gradlew sonarqube \
  -Dsonar.projectKey=anrwatchdog \
  -Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
```

## Future Enhancements

Potential improvements to the coverage setup:

1. **Multi-module Aggregation**: Combine coverage from all modules into a single report
2. **Coverage Badges**: Add README badges showing current coverage percentage
3. **Coverage Trends**: Track coverage over time with historical data
4. **Differential Coverage**: Show coverage only for changed files in PRs
5. **Coverage Gates**: Block PRs if coverage decreases

## References

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Gradle JaCoCo Plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
- [Android Test Coverage](https://developer.android.com/studio/test/test-coverage)
- [Testing Best Practices](../TESTING.md)
