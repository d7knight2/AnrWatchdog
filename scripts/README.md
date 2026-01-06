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
- Detects all open pull requests in the repository
- Identifies PRs with merge conflicts
- Attempts automatic conflict resolution by merging the base branch
- Commits and pushes changes if merge is successful
- Adds comments to PRs about resolution status
- Logs detailed success/failure information
- Skips PRs from forks (cannot push to fork branches)

**Output:**
- Console: Detailed progress and results for each PR
- `/tmp/conflict-resolution-summary.txt`: Summary report for GitHub Actions
- GitHub PR comments: Notifications about resolution attempts

**GitHub Actions Integration:**
This script is automatically run hourly by the `.github/workflows/pr-conflict-resolver.yml` workflow.

**Example Output:**
```
============================================
PR Conflict Auto-Resolver
============================================

Repository: owner/repo
Found 3 open pull request(s)

----------------------------------------
PR #123: Feature implementation
Head: feature-branch (abc123)
Base: main
Mergeable: false
⚠️  PR #123 has merge conflicts
Attempting to resolve conflicts...
✅ Merge successful!
✅ Successfully resolved conflicts for PR #123
============================================
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
