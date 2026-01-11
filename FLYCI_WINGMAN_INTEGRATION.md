# FlyCI Wingman Integration Guide

This document explains the FlyCI Wingman integration in the AnrWatchdog repository, including how it works and how to use the automation features.

## Overview

FlyCI Wingman has been integrated into all GitHub Actions CI workflows to provide intelligent failure analysis and automated fix suggestions. When a CI job fails, Wingman analyzes the failure and posts a comment on the pull request with suggested fixes in the form of unified diff patches.

## What's Been Integrated

### 1. API Connectivity Check

Before triggering FlyCI Wingman, all workflows now include a connectivity check to ensure reliable network access to `api.flyci.net`. This enhancement provides:

- **DNS Resolution Check**: Verifies that `api.flyci.net` resolves correctly using `nslookup`
- **HTTP Connectivity Check**: Tests connectivity via `curl` with HTTPS/HTTP fallback
- **Fail-Fast Behavior**: Stops execution immediately with clear error messages if connectivity issues are detected
- **Detailed Logging**: Provides comprehensive debugging information including DNS lookup results and API response status

The connectivity check is implemented as a reusable composite action located at `.github/actions/flyci-connectivity-check/action.yml` and is integrated before every FlyCI Wingman step:

```yaml
- name: Check FlyCI API Connectivity
  if: always()
  uses: ./.github/actions/flyci-connectivity-check

- name: FlyCI Wingman
  if: always()
  uses: fly-ci/wingman-action@v1
```

**Benefits:**
- Catches connectivity issues early in the workflow
- Provides actionable debug information when failures occur
- Reduces troubleshooting time by identifying network issues immediately
- Improves developer experience with clear, informative error messages

### 2. CI Workflows with FlyCI Wingman

The following workflows have been updated to include FlyCI Wingman:

- **android-ci.yml**: Added to `test`, `build`, and `instrumented-test` jobs
- **pr-validation.yml**: Added to `unit-tests`, `build`, and `ui-tests` jobs
- **nightly-build.yml**: Added to `build-and-distribute` job
- **appetize-upload.yml**: Added to `upload-to-appetize` job

Each workflow now includes a final step that runs on all job outcomes:

```yaml
- name: FlyCI Wingman
  if: always()
  uses: fly-ci/wingman-action@v1
```

This ensures Wingman can analyze and comment on the PR regardless of job status.

### Security Features

The workflows that handle pull requests have been configured with security best practices:

- **pull_request_target event**: Used instead of `pull_request` for elevated permissions
- **Base repository checkout**: Workflows check out the base repository code to avoid executing untrusted code from forks
- **Explicit permissions**: All workflows define required permissions (contents: write, pull-requests: write, issues: write)
- **Contributor validation**: PR workflows include validation steps (currently permissive but can be restricted)
- **Debug logging**: All workflows log context information for troubleshooting

### 3. Automated Fix Application

Two automation solutions have been provided to automatically apply Wingman's suggested fixes:

#### Solution 1: GitHub Actions Workflow (`.github/workflows/flyci-auto-apply.yml`)

A GitHub Actions workflow that:
- Monitors PR comments for FlyCI Wingman suggestions
- Extracts unified diff patches from comments
- Applies patches using `git apply`
- Commits and pushes changes back to the PR branch
- Posts status comments about success or failure
- Triggers workflow re-runs to verify fixes

**Activation**: Automatically runs when a comment is posted on a PR that:
- Contains "FlyCI Wingman" in the text
- Includes code blocks marked as ```diff` or ```patch`

**Permissions Required**:
- `contents: write` - To commit and push changes
- `pull-requests: write` - To post comments
- `actions: write` - To trigger workflow re-runs

#### Solution 2: Probot GitHub App (`probot-app/`)

A Node.js-based GitHub App built with Probot that:
- Listens for PR comments via webhooks
- Detects FlyCI Wingman suggestions
- Clones the PR branch
- Applies patches using simple-git
- Commits and pushes changes
- Posts status updates
- Triggers workflow re-runs

**Advantages**:
- More control over the application logic
- Can be hosted independently
- Easier to customize and extend
- Better error handling and logging

**Setup Required**:
- Create a GitHub App
- Deploy to a server (Heroku, Vercel, AWS, etc.)
- Configure webhooks and permissions

See [probot-app/README.md](probot-app/README.md) for detailed setup instructions.

## How It Works

### Workflow Execution

1. **Code Push**: Developer pushes code to a PR branch
2. **CI Runs**: GitHub Actions workflows execute tests and builds
3. **Failure Detection**: If a job fails, FlyCI Wingman is triggered
4. **Analysis**: Wingman analyzes logs, error messages, and code
5. **Suggestion**: Wingman posts a PR comment with suggested fixes

### Automatic Fix Application

6. **Detection**: The auto-apply system detects the Wingman comment
7. **Extraction**: Patches are extracted from the comment body
8. **Validation**: Patches are validated before application
9. **Application**: Patches are applied to the PR branch using `git apply`
10. **Commit**: Changes are committed with a descriptive message
11. **Push**: Changes are pushed back to the PR branch
12. **Re-run**: CI workflows are automatically re-triggered
13. **Feedback**: A status comment is posted on the PR

### Comment Format

Wingman suggestions should include patches in this format:

```markdown
FlyCI Wingman suggests the following fixes:

```diff
diff --git a/src/Example.java b/src/Example.java
index 1234567..abcdefg 100644
--- a/src/Example.java
+++ b/src/Example.java
@@ -10,7 +10,7 @@ public class Example {
     public void method() {
-        // Old implementation
+        // New implementation
     }
 }
```
```

Or using patch format:

```markdown
```patch
--- a/src/Example.java
+++ b/src/Example.java
@@ -10 +10 @@
-// Old implementation
+// New implementation
```
```

## Configuration

### GitHub Actions Workflow

The GitHub Actions workflow (`.github/workflows/flyci-auto-apply.yml`) requires no additional configuration. It uses the built-in `GITHUB_TOKEN` with the following permissions:

```yaml
permissions:
  contents: write
  pull-requests: write
  actions: write
```

### Probot GitHub App

The Probot app requires configuration via environment variables:

```env
APP_ID=your_app_id
WEBHOOK_SECRET=your_webhook_secret
PRIVATE_KEY_PATH=path/to/private-key.pem
```

See [probot-app/README.md](probot-app/README.md) for complete setup instructions.

## Usage Examples

### Example 1: Test Failure

1. Developer pushes code that breaks a test
2. PR validation workflow runs and fails
3. Wingman analyzes the failure and suggests a fix:

```diff
diff --git a/src/main/java/com/example/Calculator.java b/src/main/java/com/example/Calculator.java
--- a/src/main/java/com/example/Calculator.java
+++ b/src/main/java/com/example/Calculator.java
@@ -5,7 +5,7 @@
 public class Calculator {
     public int add(int a, int b) {
-        return a - b;  // Bug: should be addition
+        return a + b;  // Fixed: now correctly adds
     }
 }
```

4. Auto-apply system detects the suggestion
5. Patch is applied, committed, and pushed
6. Tests run again and pass

### Example 2: Build Failure

1. Developer introduces a compilation error
2. Build job fails
3. Wingman suggests a fix for the syntax error
4. Fix is automatically applied
5. Build succeeds

## Troubleshooting

### API Connectivity Issues

**Symptoms**:
- Workflow fails at "Check FlyCI API Connectivity" step
- Error message: "DNS resolution failed for api.flyci.net"
- Error message: "Unable to connect to api.flyci.net"

**Possible Causes**:
- FlyCI API service is down or experiencing issues
- Network connectivity problems in GitHub Actions runner
- DNS server configuration issues
- Firewall or network policies blocking access

**Solutions**:
1. **Check FlyCI Service Status**: Visit https://status.flyci.net (if available) to verify service health
2. **Review Workflow Logs**: Check the detailed connectivity check output for specific error messages
3. **DNS Troubleshooting**:
   - Check if DNS resolution is working for other domains
   - Verify DNS server is responding (shown in nslookup output)
4. **Network Troubleshooting**:
   - Review curl verbose output in the logs
   - Check if HTTP or HTTPS connections are being blocked
   - Verify no network policies are preventing outbound connections
5. **Retry the Workflow**: Temporary network issues may resolve themselves
6. **Contact Support**: If the issue persists, contact FlyCI support or repository administrators

**Debug Information Available**:
- DNS lookup results (server address, resolved IPs)
- HTTP response status codes
- Detailed curl connection information
- Timing information for connection attempts

### Patches Not Being Applied

**Possible Causes**:
- Comment doesn't contain "FlyCI Wingman" text
- Patch blocks are not properly formatted with ````diff` or ````patch`
- Patch conflicts with recent changes
- Repository protections prevent auto-commits

**Solutions**:
- Ensure comments include "FlyCI Wingman" identifier
- Check patch format matches unified diff standard
- Pull latest changes and regenerate suggestion
- Configure branch protection to allow bot commits

### Workflow Not Triggering

**Possible Causes**:
- Workflow file is not in `.github/workflows/` directory
- Insufficient permissions
- Event type is filtered out

**Solutions**:
- Verify workflow file location and name
- Check workflow permissions configuration
- Review workflow `on` triggers

### Probot App Not Responding

**Possible Causes**:
- App not installed on repository
- Webhook URL is incorrect
- Server is not running or accessible
- Authentication credentials are invalid

**Solutions**:
- Install app from GitHub App settings
- Verify webhook URL in app configuration
- Check server logs and status
- Validate APP_ID, WEBHOOK_SECRET, and PRIVATE_KEY

## Security Considerations

### GitHub Actions Workflow

- Uses built-in `GITHUB_TOKEN` which is automatically scoped to the repository
- Commits are made by `github-actions[bot]` user
- Limited to repository where workflow exists
- Subject to branch protection rules

### Pull Request Security

For workflows triggered by pull requests, additional security measures are in place:

- **pull_request_target event**: Provides elevated permissions while isolating untrusted code
- **Base repository checkout**: Workflows check out the base repository code (`github.base_ref`), not the PR code, preventing execution of malicious code from forks
- **Explicit permissions**: Workflows declare minimum required permissions (contents: write, pull-requests: write, issues: write)
- **Contributor validation**: Optional validation step that can be configured to restrict workflow execution to trusted users or organization members
- **Debug logging**: Context information is logged for security auditing and troubleshooting

### Probot App

- Uses GitHub App authentication with private key
- Requires secure storage of private key
- Operates with explicitly granted permissions
- Should be hosted on secure infrastructure
- Webhook secret prevents unauthorized requests

## Best Practices

1. **Review Auto-Applied Changes**: Even though changes are automatically applied, review them before merging
2. **Monitor Auto-Apply Activity**: Check workflow runs and bot comments regularly
3. **Keep Secrets Secure**: Never commit private keys or secrets to version control
4. **Test in Development**: Test auto-apply on non-production branches first
5. **Configure Branch Protection**: Ensure critical branches require manual review even with auto-applied fixes

## Benefits

- **Faster Feedback Loop**: Fixes are applied immediately after detection
- **Reduced Manual Work**: No need to manually copy and apply patches
- **Consistent Application**: Automated application reduces human error
- **CI Integration**: Automatically triggers re-runs to verify fixes
- **Transparent Process**: All changes are committed with clear messages and references

## Limitations

- **Simple Fixes Only**: Works best for straightforward, localized fixes
- **Conflict Handling**: May fail if patch conflicts with recent changes
- **Patch Format**: Requires standard unified diff format
- **Branch Protection**: May be blocked by strict protection rules
- **Complex Refactoring**: Not suitable for large-scale refactoring

## Future Enhancements

Potential improvements to consider:

- Support for multiple patch formats (context diff, git diff with binary)
- Intelligent conflict resolution
- Integration with code review tools
- Support for multi-file refactoring
- Machine learning to improve fix suggestions
- Custom approval workflows for auto-applied fixes

## Support

For issues or questions:
- Check GitHub Actions workflow logs
- Review Probot app logs (if using)
- Consult [FlyCI Wingman documentation](https://fly-ci.com/docs/wingman)
- Open an issue on the [AnrWatchdog repository](https://github.com/d7knight2/AnrWatchdog/issues)

## Related Documentation

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Probot Framework](https://probot.github.io/)
- [GitHub Apps Documentation](https://docs.github.com/en/developers/apps)
- [FlyCI Wingman](https://fly-ci.com/docs/wingman)
