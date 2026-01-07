# Scripts Directory

This directory contains utility scripts for the ANR Watchdog project.

## Available Scripts

### resolve-pr-conflicts.sh

Automatically detects and resolves merge conflicts in open pull requests by merging the base branch into the PR branch.

**Usage:**
```bash
# Set required environment variables
export GITHUB_TOKEN="your_github_token_here"
export GITHUB_REPOSITORY="owner/repo"

# Run the script
./scripts/resolve-pr-conflicts.sh
```

**Requirements:**
- `GITHUB_TOKEN` environment variable must be set (with repo and PR permissions)
- `GITHUB_REPOSITORY` environment variable must be set (format: "owner/repo")
- `git` command available
- `curl` command available
- `jq` command available

**Features:**
- **Intelligent PR Detection**: Detects all open pull requests in the repository
- **Smart Conflict Identification**: Properly handles PRs with merge conflicts, including:
  - `mergeable: false` - Has conflicts, attempts resolution
  - `mergeable: null` - GitHub hasn't computed status yet, skips and reports
  - `mergeable: true` - No conflicts, logs and continues
- **Automatic Conflict Resolution**: Attempts resolution by merging the base branch
- **Robust Error Handling**: 
  - Retry logic for API calls (3 attempts with 2s delays)
  - Detailed error messages with timestamps
  - Proper cleanup on failure
- **Detailed Logging**:
  - Timestamped log entries with severity levels (INFO, WARN, ERROR, SUCCESS)
  - Bash debug tracing enabled (set -x)
  - Comprehensive conflict file detection with counts
- **Smart Notifications**:
  - Only takes action when conflicts are detected
  - Adds comments to PRs about resolution status
  - Tracks meaningful actions to reduce notification spam
- **Fork Handling**: Skips PRs from forks (cannot push to fork branches)

**Output:**
- Console: Detailed timestamped progress and results for each PR
- `/tmp/conflict-resolution-summary.txt`: Summary report for GitHub Actions
- GitHub PR comments: Notifications about resolution attempts with retry logic

**Exit Codes:**
- `0`: Success (conflicts resolved or no action needed)
- `1`: Failure (conflict resolution failed)

**GitHub Actions Integration:**
This script is automatically run every 6 hours by the `.github/workflows/pr-conflict-resolver.yml` workflow. The reduced frequency (from hourly to every 6 hours) minimizes notification spam while still providing timely conflict resolution.

**Example Output:**
```
============================================
PR Conflict Auto-Resolver
============================================

Repository: owner/repo
Found 3 open pull request(s)

[2026-01-07 08:00:00 UTC] [INFO] ----------------------------------------
[2026-01-07 08:00:00 UTC] [INFO] Processing PR #123: Feature implementation
[2026-01-07 08:00:00 UTC] [INFO] Head: feature-branch (abc123)
[2026-01-07 08:00:00 UTC] [INFO] Base: main
[2026-01-07 08:00:00 UTC] [INFO] Mergeable status: false
[2026-01-07 08:00:00 UTC] [WARN] PR #123 has merge conflicts - attempting resolution
[2026-01-07 08:00:01 UTC] [INFO] Successfully fetched branch feature-branch
[2026-01-07 08:00:02 UTC] [INFO] Successfully fetched base branch main
[2026-01-07 08:00:03 UTC] [INFO] Merging main into feature-branch...
[2026-01-07 08:00:04 UTC] [SUCCESS] Merge successful for PR #123
[2026-01-07 08:00:05 UTC] [SUCCESS] Successfully resolved conflicts for PR #123

[2026-01-07 08:00:05 UTC] [INFO] ============================================
[2026-01-07 08:00:05 UTC] [INFO] Summary
[2026-01-07 08:00:05 UTC] [INFO] ============================================
[2026-01-07 08:00:05 UTC] [INFO] Total PRs checked: 3
[2026-01-07 08:00:05 UTC] [INFO] PRs with null mergeable status: 1
[2026-01-07 08:00:05 UTC] [INFO] Conflicts detected: 1
[2026-01-07 08:00:05 UTC] [INFO] Conflicts resolved: 1
[2026-01-07 08:00:05 UTC] [INFO] Conflicts failed: 0
[2026-01-07 08:00:05 UTC] [INFO] Meaningful action taken: true
[2026-01-07 08:00:05 UTC] [INFO] ============================================
```

### upload-to-appetize.sh

Uploads APK files to Appetize.io for browser-based testing.

**Usage:**
```bash
# Set your API token
export APPETIZE_API_TOKEN="your_api_token_here"

# Upload a new app
./scripts/upload-to-appetize.sh path/to/app.apk

# Update an existing app
./scripts/upload-to-appetize.sh path/to/app.apk your_public_key
```

**Requirements:**
- `APPETIZE_API_TOKEN` environment variable must be set
- Valid APK file path
- curl command available

**Output:**
- Console: App URL and public key
- `appetize-outputs/public-key.txt`: The app's public key
- `appetize-outputs/app-url.txt`: The app's public URL
- GitHub Actions output variables (if running in CI)

**Example:**
```bash
export APPETIZE_API_TOKEN="abc123..."
./scripts/upload-to-appetize.sh demoapp/build/outputs/apk/debug/demoapp-debug.apk

# Output:
# ============================================
# Appetize.io App Details:
# ============================================
# Public Key: abc123xyz
# App URL: https://appetize.io/app/abc123xyz
# Embed URL: https://appetize.io/embed/abc123xyz
# ============================================
```

**Error Handling:**
- Exits with code 1 if `APPETIZE_API_TOKEN` is not set
- Exits with code 1 if APK file is not found
- Exits with code 1 if upload fails

**Integration with GitHub Actions:**
The script automatically outputs variables for GitHub Actions:
- `public_key`: The app's public key
- `app_url`: The app's public URL

These can be accessed in subsequent workflow steps:
```yaml
- name: Upload to Appetize.io
  id: appetize
  run: ./scripts/upload-to-appetize.sh path/to/app.apk

- name: Use the output
  run: echo "App URL: ${{ steps.appetize.outputs.app_url }}"
```

## Adding New Scripts

When adding new scripts to this directory:

1. **Make it executable**: `chmod +x scripts/your-script.sh`
2. **Add shebang**: Start with `#!/bin/bash` or appropriate interpreter
3. **Add documentation**: Update this README with usage instructions
4. **Error handling**: Use `set -e` and check for required inputs
5. **Environment variables**: Document required environment variables
6. **Test locally**: Test the script before committing

## Script Best Practices

- Use meaningful variable names
- Add comments for complex logic
- Validate inputs and provide helpful error messages
- Use `set -e` to exit on errors
- Clean up temporary files
- Output clear success/failure messages
- Document all environment variables and arguments

---

For more information on app distribution, see [DISTRIBUTION.md](../DISTRIBUTION.md).
