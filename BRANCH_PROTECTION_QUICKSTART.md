# Quick Start: Branch Protection Configuration

This is a quick reference guide for repository administrators to configure branch protection rules.

## Required Status Check

The repository includes a workflow specifically designed for PR validation:
- **Workflow**: `.github/workflows/pr-validation.yml`
- **Required Status Check Name**: `All Tests Passed`

## Setup Steps (5 minutes)

### For Main Branch

1. Go to: `https://github.com/<owner>/<repository>/settings/branches` (replace with your repository)
2. Click **"Add rule"**
3. Branch name pattern: `main`
4. Enable these checkboxes:
   - ☑️ Require a pull request before merging
   - ☑️ Require status checks to pass before merging
   - ☑️ Require branches to be up to date before merging
5. In "Status checks that are required", search and add:
   - `All Tests Passed`
6. Click **"Create"**

### For Develop Branch

Repeat the same steps with branch name pattern: `develop`

## What This Does

- ✅ All PRs automatically run unit tests and UI tests
- ✅ PRs cannot be merged until tests pass
- ✅ Ensures code quality and stability
- ✅ Prevents broken code from entering main branches

## Verification

Create a test PR and verify:
1. "Pull Request Validation" workflow starts automatically
2. Merge button is blocked until tests pass
3. Status check "All Tests Passed" appears in PR

## Complete Documentation

See [BRANCH_PROTECTION_SETUP.md](BRANCH_PROTECTION_SETUP.md) for detailed instructions, troubleshooting, and best practices.

## Test Commands for Local Verification

```bash
# Run all tests locally before creating PR
./gradlew test                      # Unit tests
./gradlew assembleDebug             # Build
./gradlew connectedAndroidTest      # UI tests (requires emulator/device)
```
