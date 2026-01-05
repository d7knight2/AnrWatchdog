# Scripts Directory

This directory contains utility scripts for the ANR Watchdog project.

## Available Scripts

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
