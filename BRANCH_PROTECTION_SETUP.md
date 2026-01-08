# Branch Protection Setup Guide

This guide provides step-by-step instructions for configuring branch protection rules to ensure all pull requests run and pass both unit tests and UI tests before they can be merged.

## Overview

The repository has been configured with a comprehensive CI/CD workflow that automatically runs:
- **Unit Tests**: JUnit-based tests for the ANRWatchdog library and other components
- **UI Tests**: Espresso-based instrumented tests for the demo app
- **Build Validation**: Ensures the application builds successfully

These tests run automatically via GitHub Actions whenever a pull request is created or updated.

## CI/CD Workflow Structure

### Workflow Files

1. **`.github/workflows/pr-validation.yml`** (Primary PR validation)
   - Runs on all pull requests to `main` and `develop` branches
   - Executes three main jobs:
     - `unit-tests`: Runs all unit tests using `./gradlew test`
     - `build`: Builds the debug APK using `./gradlew assembleDebug`
     - `ui-tests`: Runs instrumented tests using `./gradlew connectedAndroidTest`
   - Final job `all-tests-passed`: Consolidates results of all jobs
   - **This workflow is specifically designed to be used as the required status check**

2. **`.github/workflows/android-ci.yml`** (Continuous integration)
   - Runs on pushes to `main`, `develop`, and `copilot/**` branches
   - Also runs on pull requests
   - Similar test suite for continuous validation

### Test Frameworks

- **Unit Tests**: JUnit 4.13.2, Mockito 5.3.1, Kotlin Test 1.9.0
- **UI Tests**: AndroidX Test (JUnit, Espresso, Runner, Rules)
- **Test Runner**: AndroidJUnitRunner for instrumented tests

## Configuring Branch Protection Rules

Branch protection rules must be configured through the GitHub web interface by a repository administrator. Follow these steps:

### Step 1: Access Branch Protection Settings

1. Navigate to your repository on GitHub: `https://github.com/d7knight2/AnrWatchdog`
2. Click on **Settings** (top navigation bar)
3. In the left sidebar, click **Branches** (under "Code and automation")
4. Under "Branch protection rules", click **Add rule** or **Add branch protection rule**

### Step 2: Configure Protection for Main Branch

1. **Branch name pattern**: Enter `main`

2. **Protect matching branches** - Enable the following settings:

   ✅ **Require a pull request before merging**
   - This ensures all changes go through a PR process
   - Optionally: "Require approvals" (set to 1 or more reviewers)
   - Optionally: "Dismiss stale pull request approvals when new commits are pushed"

   ✅ **Require status checks to pass before merging**
   - This is the critical setting that enforces test passing
   - Check "Require branches to be up to date before merging"
   
   ✅ **Status checks that are required** - Search for and select:
   - `All Tests Passed` (from pr-validation.yml workflow)
   
   Or alternatively, select all three individual jobs:
   - `Unit Tests`
   - `Build APK`
   - `UI Tests (Instrumented)`
   
   **Recommendation**: Use the `All Tests Passed` check as it provides a single consolidated status check that depends on all others.

   ✅ **Require conversation resolution before merging** (Optional but recommended)
   - Ensures all PR comments are addressed

   ✅ **Do not allow bypassing the above settings** (Optional)
   - Enforces rules even for administrators

3. Click **Create** or **Save changes**

### Step 3: Configure Protection for Develop Branch (Optional)

Repeat Step 2 for the `develop` branch to maintain quality across all main branches.

1. Click **Add rule** again
2. **Branch name pattern**: Enter `develop`
3. Apply the same settings as configured for `main` branch
4. Click **Create** or **Save changes**

### Step 4: Verify Configuration

1. Create a test pull request with a trivial change
2. Verify that the "Pull Request Validation" workflow starts automatically
3. Check that the PR cannot be merged until all status checks pass
4. Verify the merge button shows: "Merging is blocked - Required status check 'All Tests Passed' must pass"

## Understanding the Required Status Checks

### What Happens When Tests Fail?

- If **unit tests fail**: The `Unit Tests` job will fail, preventing merge
- If **build fails**: The `Build APK` job will fail, preventing merge  
- If **UI tests fail**: The `UI Tests (Instrumented)` job will fail, preventing merge
- The `All Tests Passed` job will only succeed if all previous jobs succeed

### What Happens When Tests Pass?

- All three jobs (unit-tests, build, ui-tests) complete successfully
- The `All Tests Passed` job runs and succeeds
- The status check shows as green ✓
- The "Merge pull request" button becomes available
- PR can be merged into the protected branch

## Test Execution Details

### Unit Tests
```bash
./gradlew test --stacktrace
```
- Tests in: `anrwatchdog/src/test/`, `src/test/`
- Includes: ANRWatchdog functionality, dependency analysis, etc.

### Build Validation
```bash
./gradlew assembleDebug --stacktrace
```
- Builds: Debug APK for the demo app
- Validates: Compilation, resource processing, packaging

### UI/Instrumented Tests
```bash
./gradlew connectedAndroidTest --stacktrace
```
- Tests in: `demoapp/src/androidTest/`
- Includes: MainActivityTest, AnrSimulationTest, FloatingDebugViewTest, MemoryLeakTest
- Runs on: Android Emulator (API 29, x86_64, Nexus 6 profile)

## Running Tests Locally

Before creating a pull request, developers should run tests locally:

```bash
# Run all unit tests
./gradlew test

# Build the debug APK
./gradlew assembleDebug

# Run instrumented tests (requires emulator or device)
./gradlew connectedAndroidTest

# Or run all checks at once
./gradlew test assembleDebug connectedAndroidTest
```

## Troubleshooting

### Status Check Not Appearing

If the required status check doesn't appear in the PR:
1. Ensure the workflow file exists: `.github/workflows/pr-validation.yml`
2. Check the Actions tab for workflow runs
3. Verify the PR is targeting `main` or `develop` branch
4. Check for YAML syntax errors in the workflow file

### Tests Passing Locally But Failing in CI

Common causes:
- Environment differences (Java version, Android SDK)
- Dependency resolution issues
- Timing issues in UI tests
- Check the workflow logs in the Actions tab for detailed error messages

### Cannot Merge Despite Passing Tests

Verify:
- All required status checks are listed and passing
- No unresolved conversations (if that rule is enabled)
- Branch is up to date with base branch (if that rule is enabled)
- You have write permissions to the repository

## Best Practices

1. **Run Tests Locally First**: Always run the test suite locally before pushing
2. **Keep Tests Fast**: Optimize test execution time to avoid CI bottlenecks
3. **Fix Broken Tests Immediately**: Don't let broken tests accumulate
4. **Review Test Failures**: Understand why tests failed before making changes
5. **Update Tests with Code**: Keep tests in sync with code changes
6. **Monitor CI Performance**: Track test execution time and optimize as needed

## Additional Resources

- [GitHub Branch Protection Documentation](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Repository Testing Documentation](TESTING.md)
- [Android Testing Guide](https://developer.android.com/training/testing)

## Summary

With these branch protection rules in place:
- ✅ All PRs automatically trigger unit tests and UI tests
- ✅ PRs cannot be merged until all tests pass
- ✅ Code quality and stability are maintained
- ✅ The CI/CD pipeline provides fast feedback to developers
- ✅ The codebase remains in a consistently working state

This setup ensures that only well-tested, high-quality code makes it into the `main` and `develop` branches, maintaining the stability and reliability of the ANR Watchdog library and demo application.
