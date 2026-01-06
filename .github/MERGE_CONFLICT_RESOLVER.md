# Merge Conflict Resolver Workflow

## Overview

The Merge Conflict Resolver is a GitHub Actions workflow that automatically attempts to resolve merge conflicts in open pull requests every night at midnight UTC. This workflow helps maintain a clean PR pipeline by proactively addressing merge conflicts before they become blocking issues.

## Features

- **Scheduled Execution**: Runs automatically every night at midnight UTC
- **Manual Trigger**: Can be manually triggered via GitHub Actions UI
- **Conflict Detection**: Identifies all open pull requests with merge conflicts
- **Automatic Resolution**: Attempts to automatically merge the base branch into conflicting PRs
- **Detailed Logging**: Provides comprehensive logs for both successful and failed resolutions
- **PR Comments**: Automatically comments on PRs with resolution status
- **Summary Reports**: Generates a detailed summary of all resolution attempts
- **Artifact Storage**: Saves resolution logs as artifacts for 30 days

## Workflow Triggers

### Scheduled Run
The workflow runs automatically every night at midnight UTC:
```yaml
schedule:
  - cron: '0 0 * * *'
```

### Manual Trigger
You can manually trigger the workflow at any time:
1. Go to the "Actions" tab in your repository
2. Select "Nightly Merge Conflict Resolver" from the workflows list
3. Click "Run workflow"
4. Select the branch and click "Run workflow"

## How It Works

1. **Discovery Phase**
   - Checks out the repository with full history
   - Fetches all open pull requests
   - Identifies PRs with `CONFLICTING` merge status

2. **Resolution Phase** (for each conflicting PR)
   - Fetches the PR's head branch and base branch
   - Checks out the head branch
   - Attempts to merge the base branch using Git's automatic merge
   - If successful:
     - Commits the merge with a clear message
     - Pushes the changes to the PR branch
     - Comments on the PR with success details
   - If failed:
     - Identifies conflicting files
     - Logs the merge output
     - Comments on the PR with manual resolution instructions

3. **Reporting Phase**
   - Generates a comprehensive summary with statistics
   - Uploads logs as workflow artifacts
   - Creates a GitHub Actions job summary

## Permissions Required

The workflow requires the following permissions:
- `contents: write` - To push resolved changes
- `pull-requests: write` - To comment on PRs
- `issues: write` - To comment on PRs (alternate method)

## Output and Logs

### Job Summary
After each run, a summary is available in the GitHub Actions UI showing:
- Date and time of execution
- List of processed PRs with resolution status
- Statistics (successful, failed, total)
- Links to detailed logs

### PR Comments
The workflow automatically adds comments to each processed PR:

**Success Comment:**
```
🤖 Automated Merge Conflict Resolution

The nightly merge conflict resolver has successfully resolved conflicts 
in this pull request by merging the latest changes from `main`.

Resolution Date: 2024-01-06 00:00:00 UTC
Workflow Run: [link]

Please review the changes to ensure the automatic resolution is correct.
```

**Failure Comment:**
```
🤖 Automated Merge Conflict Resolution - Manual Action Required

The nightly merge conflict resolver attempted to resolve conflicts in 
this pull request but was unable to do so automatically.

Date: 2024-01-06 00:00:00 UTC
Base Branch: `main`
Status: ⚠️  Manual resolution required

Conflicting Files:
[list of files]

Next Steps:
1. Pull the latest changes from `main`
2. Manually resolve the conflicts in the files listed above
3. Commit and push your changes

Workflow Run: [link]
```

### Artifacts
Resolution logs are stored as workflow artifacts for 30 days and include:
- Complete resolution summary in markdown format
- Merge output logs
- Statistics and timestamps

## Best Practices

1. **Review Automatic Resolutions**: Even when conflicts are resolved automatically, always review the changes to ensure they're correct.

2. **Manual Intervention**: When the workflow cannot resolve conflicts automatically, follow the instructions in the PR comment to manually resolve them.

3. **Regular Monitoring**: Check the workflow runs regularly to ensure it's functioning correctly and address any persistent conflicts.

4. **Branch Protection**: Consider configuring branch protection rules that require up-to-date branches before merging to minimize conflicts.

## Limitations

- The workflow can only resolve conflicts that Git can automatically merge
- Complex conflicts involving overlapping changes will require manual resolution
- The workflow processes up to 100 open PRs per run (configurable)
- Resolution attempts may fail for PRs from forks if proper permissions aren't set

## Troubleshooting

### Workflow Doesn't Run
- Verify the workflow file is in `.github/workflows/`
- Check that Actions are enabled for the repository
- Ensure the cron schedule is correct

### Resolution Fails
- Check the workflow logs for specific error messages
- Verify that the workflow has write permissions
- Ensure the PR branch is not protected or requires different permissions

### PR Not Detected
- Confirm the PR is open and has a `CONFLICTING` mergeable status
- Verify the PR is visible to GitHub Actions
- Check that the PR limit (100) hasn't been exceeded

## Configuration

You can customize the workflow by editing `.github/workflows/merge-conflict-resolver.yml`:

- **Schedule**: Modify the cron expression to change execution time
- **PR Limit**: Change `--limit 100` to process more or fewer PRs
- **Artifact Retention**: Modify `retention-days: 30` to change how long logs are kept
- **Merge Strategy**: Add custom merge options to the `git merge` command

## Example Output

```
## Merge Conflict Resolution Summary

**Date:** 2024-01-06 00:00:00 UTC
**Workflow Run:** [link]

## PR #42: Add new feature
**Author:** @developer
**Branch:** `feature-branch` → `main`
**Status:** ✅ Success - Conflicts resolved automatically

## PR #43: Update documentation
**Author:** @contributor
**Branch:** `docs-update` → `main`
**Status:** ⚠️  Failed - Manual resolution required
**Conflicting Files:** `README.md, docs/guide.md`

---

## Summary Statistics

- ✅ **Successfully Resolved:** 1
- ❌ **Failed to Resolve:** 1
- **Total Processed:** 2
```

## Contributing

If you have suggestions for improving this workflow, please open an issue or submit a pull request.

## License

This workflow is part of the ANR Watchdog project and is licensed under the Apache License 2.0.
