# Verification and Testing Guide

This document provides steps to verify that the CI/CD and branch protection setup is working correctly.

## Pre-requisites

- Repository administrator access (for configuring branch protection)
- Access to GitHub Actions (to view workflow runs)

## Verification Steps

### Step 1: Verify Workflow Files Exist

Confirm the following files are present:

```bash
ls -la .github/workflows/pr-validation.yml
ls -la .github/workflows/android-ci.yml
```

Expected: Both files should exist and be readable.

**Note**: `pr-validation.yml` is the primary workflow for PR validation and branch protection. `android-ci.yml` continues to provide CI coverage for pushes.

### Step 2: Validate Workflow Syntax

The workflow syntax should be valid YAML:

```bash
# Using Python
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/pr-validation.yml'))"
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/android-ci.yml'))"

# Or using yamllint (if installed)
yamllint .github/workflows/pr-validation.yml
yamllint .github/workflows/android-ci.yml
```

Expected: No errors should be reported.

**Note**: While both workflows are validated here, `pr-validation.yml` is the critical one for branch protection.

### Step 3: Review Workflow Configuration

Check the PR validation workflow:

```bash
cat .github/workflows/pr-validation.yml
```

Verify:
- ✅ Triggers on: `pull_request` for branches `main` and `develop`
- ✅ Has job: `unit-tests`
- ✅ Has job: `build`
- ✅ Has job: `ui-tests`
- ✅ Has job: `all-tests-passed` (depends on all previous jobs)

### Step 4: Configure Branch Protection (Administrator Only)

Follow the instructions in `BRANCH_PROTECTION_QUICKSTART.md`:

1. Navigate to: `https://github.com/<owner>/<repository>/settings/branches` (replace with your repository)
2. Add rule for `main`:
   - Branch name pattern: `main`
   - ☑️ Require a pull request before merging
   - ☑️ Require status checks to pass before merging
   - ☑️ Require branches to be up to date before merging
   - Required status check: `All Tests Passed`
3. Repeat for `develop` branch

### Step 5: Create a Test Pull Request

Create a test PR to verify the workflow:

```bash
# Create a test branch
git checkout -b test/verify-ci-workflow

# Make a trivial change
echo "# Test Change" >> TEST_VERIFICATION.md
git add TEST_VERIFICATION.md
git commit -m "Test: Verify CI workflow"
git push origin test/verify-ci-workflow
```

Then create a PR through GitHub UI targeting `main` or `develop`.

### Step 6: Verify Workflow Execution

1. Go to the PR page on GitHub
2. Look for the "Checks" section
3. Verify the following checks are running:
   - Unit Tests
   - Build APK
   - UI Tests (Instrumented)
   - All Tests Passed

### Step 7: Verify Status Checks

While the workflow is running or after completion:

1. Check the PR merge button status
2. If tests are passing:
   - ✅ "All checks have passed"
   - Merge button should be enabled (if branch protection is configured)
3. If tests are failing:
   - ❌ "Some checks were not successful"
   - Merge button should be disabled (if branch protection is configured)

### Step 8: Test with Failing Tests (Optional)

To verify that failing tests actually block merging, create a branch with a failing test:

```bash
# Create a branch with a failing test
git checkout -b test/failing-test

# Create the test directory if it doesn't exist
mkdir -p anrwatchdog/src/test/kotlin/com/example/anrwatchdog

# Create a test file with a failing test
# (You can use your editor or the commands below)
```

Create a file `anrwatchdog/src/test/kotlin/com/example/anrwatchdog/VerificationFailTest.kt` with:

```kotlin
package com.example.anrwatchdog

import org.junit.Test
import kotlin.test.assertEquals

class VerificationFailTest {
    @Test
    fun testFailureForVerification() {
        // This test intentionally fails for verification
        assertEquals(1, 2, "Expected failure for CI verification")
    }
}
```

Then commit and push:

```bash
git add .
git commit -m "Test: Add failing test to verify CI blocking"
git push origin test/failing-test
```

Create a PR and verify:
- ❌ "Unit Tests" check fails
- ❌ "All Tests Passed" check fails
- Merge button is disabled with message about required status checks

### Step 9: Verify Test Reports

After workflow completion:

1. Go to the Actions tab in GitHub
2. Click on the workflow run
3. Check that test report artifacts are uploaded:
   - `unit-test-reports`
   - `ui-test-reports`
   - `debug-apk`

### Step 10: Clean Up Test PRs

After verification, close and delete test PRs and branches:

```bash
# Delete local branches
git branch -D test/verify-ci-workflow
git branch -D test/failing-test  # if created

# Delete remote branches (after closing PRs)
git push origin --delete test/verify-ci-workflow
git push origin --delete test/failing-test  # if created
```

## Expected Results

### When Everything Works Correctly

1. **PR Created**: Workflow triggers automatically
2. **Tests Run**: All three test jobs execute
3. **All Pass**: "All Tests Passed" check succeeds
4. **Merge Enabled**: PR can be merged (with branch protection)

### When Tests Fail

1. **PR Created**: Workflow triggers automatically
2. **Tests Run**: All jobs execute
3. **Some Fail**: Failed jobs show red X
4. **Merge Blocked**: PR cannot be merged (with branch protection)
5. **Clear Feedback**: Error logs available in workflow details

## Troubleshooting

### Workflow Not Triggering

**Problem**: PR created but workflow doesn't run.

**Solutions**:
- Verify PR targets `main` or `develop` branch
- Check `.github/workflows/pr-validation.yml` exists
- Ensure no YAML syntax errors
- Check repository Actions settings are enabled

### Status Check Not Appearing in Branch Protection

**Problem**: Cannot add "All Tests Passed" as required check.

**Solutions**:
- Create a test PR first to trigger the workflow
- Wait for workflow to complete at least once
- Refresh the branch protection settings page
- The status check name must match exactly: `All Tests Passed`

### Tests Pass Locally But Fail in CI

**Problem**: Tests succeed on developer machine but fail in GitHub Actions.

**Common Causes**:
- Java/JDK version differences (CI uses JDK 17 as configured in workflow files)
- Android SDK version differences
- Timezone or locale differences
- Missing dependencies or environment variables
- Flaky UI tests with timing issues

**Solutions**:
- Check workflow logs for specific error messages
- Match local environment to CI (JDK 17, same Gradle version)
- Use IdlingResource for UI test timing
- Add retry logic for flaky tests

### UI Tests Timing Out

**Problem**: Instrumented tests fail with timeout or emulator issues.

**Solutions**:
- Check emulator startup logs in workflow output
- Verify emulator API level matches test requirements
- Increase timeout values if needed
- Check for KVM permissions (already configured in workflow)

## Success Criteria

✅ Workflow file syntax is valid
✅ PR triggers workflow automatically
✅ All three test jobs execute successfully
✅ "All Tests Passed" check appears in PR
✅ Branch protection prevents merge when tests fail
✅ Branch protection allows merge when tests pass
✅ Test reports are uploaded as artifacts
✅ Workflow provides clear feedback on failures

## Additional Resources

- [GitHub Actions Troubleshooting](https://docs.github.com/en/actions/monitoring-and-troubleshooting-workflows)
- [Branch Protection Troubleshooting](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/troubleshooting-required-status-checks)
- [Android Emulator in GitHub Actions](https://github.com/ReactiveCircus/android-emulator-runner)

## Contact

If you encounter issues not covered here, please:
1. Check the GitHub Actions workflow logs for detailed error messages
2. Review the documentation files in this repository
3. Open an issue with the workflow run ID and error details
