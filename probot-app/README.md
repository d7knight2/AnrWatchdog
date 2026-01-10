# FlyCI Wingman Auto-Apply GitHub App

A Probot-based GitHub App that automatically detects FlyCI Wingman's PR comments, extracts unified diff patches, and applies them to pull requests.

## Features

- 🤖 **Automatic Detection**: Monitors PR comments for FlyCI Wingman suggestions
- 🔧 **Patch Extraction**: Extracts unified diff patches from comment bodies
- ✨ **Automatic Application**: Applies patches using `git apply`
- 💾 **Commit & Push**: Commits and pushes changes back to the PR branch
- 🔄 **CI Re-run**: Automatically triggers workflow re-runs to verify fixes
- 📝 **Status Comments**: Posts informative comments about success, failure, or issues
- 🔒 **Secure Authentication**: Uses GitHub App private key for secure operations

## How It Works

1. **Comment Detection**: The app listens for new comments on pull requests
2. **Wingman Identification**: Checks if the comment is from FlyCI Wingman and contains patches
3. **Patch Extraction**: Extracts code blocks marked as `diff` or `patch`
4. **Repository Cloning**: Clones the PR branch to a temporary directory
5. **Patch Application**: Applies the extracted patches using `git apply`
6. **Commit & Push**: If successful, commits and pushes the changes
7. **Feedback**: Posts a comment on the PR with the result
8. **CI Trigger**: Attempts to re-run failed workflows

## Prerequisites

- Node.js 18 or higher
- A GitHub account with permissions to create GitHub Apps
- A server or hosting platform to run the app (e.g., Heroku, Vercel, AWS)

## Setup Instructions

### Step 1: Create a GitHub App

1. Go to your GitHub account settings: https://github.com/settings/apps
2. Click "New GitHub App"
3. Fill in the following details:
   - **GitHub App name**: `FlyCI Wingman Auto-Apply` (or your preferred name)
   - **Homepage URL**: Your repository or app URL
   - **Webhook URL**: Your server URL + `/webhook` (e.g., `https://your-domain.com/webhook`)
   - **Webhook secret**: Generate a random secret string
   
4. Set the following **permissions**:
   - **Repository permissions**:
     - Contents: Read & Write
     - Issues: Read & Write
     - Pull requests: Read & Write
     - Actions: Read & Write
     - Metadata: Read-only
   
5. Subscribe to the following **events**:
   - Issue comments
   - Pull requests
   
6. Click "Create GitHub App"

### Step 2: Generate and Download Private Key

1. After creating the app, scroll down to "Private keys"
2. Click "Generate a private key"
3. Save the downloaded `.pem` file securely - you'll need it to authenticate the app

### Step 3: Install the App on Your Repository

1. On your GitHub App settings page, go to "Install App" in the left sidebar
2. Click "Install" next to your organization or account
3. Choose "Only select repositories" and select the `AnrWatchdog` repository
4. Click "Install"

### Step 4: Configure the Application

1. Clone this repository or navigate to the `probot-app` directory:
   ```bash
   cd probot-app
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Copy the `.env.example` file to `.env`:
   ```bash
   cp .env.example .env
   ```

4. Edit the `.env` file with your GitHub App credentials:
   ```env
   APP_ID=your_app_id_here
   WEBHOOK_SECRET=your_webhook_secret_here
   PRIVATE_KEY_PATH=path/to/your-private-key.pem
   ```
   
   You can find your `APP_ID` on the GitHub App settings page.

### Step 5: Run the Application

#### For Development (Local Testing)

For local testing, you'll need to use a webhook proxy like [Smee.io](https://smee.io):

1. Go to https://smee.io and click "Start a new channel"
2. Copy the webhook proxy URL
3. Update your GitHub App webhook URL to use the Smee.io URL
4. Add the Smee.io URL to your `.env` file:
   ```env
   WEBHOOK_PROXY_URL=https://smee.io/your-unique-channel
   ```

5. Start the app in development mode:
   ```bash
   npm run dev
   ```

The app will now receive webhooks via the Smee.io proxy.

#### For Production

1. Deploy the app to your preferred hosting platform (Heroku, Vercel, AWS, etc.)
2. Set environment variables on your hosting platform:
   - `APP_ID`
   - `WEBHOOK_SECRET`
   - `PRIVATE_KEY` (the full contents of the .pem file)
   
3. Update your GitHub App webhook URL to point to your production server
4. Start the app:
   ```bash
   npm start
   ```

## Deployment Options

### Heroku

1. Create a new Heroku app:
   ```bash
   heroku create your-app-name
   ```

2. Set environment variables:
   ```bash
   heroku config:set APP_ID=your_app_id
   heroku config:set WEBHOOK_SECRET=your_webhook_secret
   heroku config:set PRIVATE_KEY="$(cat path/to/private-key.pem)"
   ```

3. Deploy:
   ```bash
   git push heroku main
   ```

### Vercel

1. Install Vercel CLI:
   ```bash
   npm install -g vercel
   ```

2. Deploy:
   ```bash
   vercel
   ```

3. Set environment variables in the Vercel dashboard

### Docker

1. Build the Docker image:
   ```bash
   docker build -t flyci-wingman-app .
   ```

2. Run the container:
   ```bash
   docker run -e APP_ID=your_app_id \
              -e WEBHOOK_SECRET=your_webhook_secret \
              -e PRIVATE_KEY="$(cat private-key.pem)" \
              -p 3000:3000 \
              flyci-wingman-app
   ```

## Usage

Once the app is running and installed on your repository:

1. **Create a Pull Request** with code that needs fixes
2. **Wait for CI to run** and potentially fail
3. **FlyCI Wingman** will analyze the failure and post a comment with suggested fixes
4. **The app will automatically**:
   - Detect the Wingman comment
   - Extract the patch from the comment
   - Apply the patch to the PR branch
   - Commit and push the changes
   - Post a status comment
   - Trigger CI re-run

## Comment Format

The app expects FlyCI Wingman comments to include patches in the following format:

```markdown
FlyCI Wingman suggests the following fixes:

```diff
diff --git a/file.java b/file.java
index 1234567..abcdefg 100644
--- a/file.java
+++ b/file.java
@@ -10,7 +10,7 @@
-    old line
+    new line
```
```

Or using `patch` code blocks:

```markdown
```patch
--- a/file.java
+++ b/file.java
@@ -10 +10 @@
-old line
+new line
```
```

## Troubleshooting

### App not responding to comments

- Verify the webhook URL is correct in your GitHub App settings
- Check that the webhook secret matches your `.env` configuration
- Ensure the app is running and accessible
- Check the app logs for errors

### Patches failing to apply

- The PR branch may have diverged from when the suggestion was made
- There may be conflicts with other changes
- The patch format may not be standard unified diff format

### Authentication errors

- Verify your `APP_ID` is correct
- Ensure the private key file path is correct and accessible
- Check that the app is installed on the repository

### Webhook delivery issues

- Check webhook delivery status in GitHub App settings under "Advanced" → "Recent Deliveries"
- Verify your webhook secret is correct
- Ensure your server is publicly accessible (for production)

## Security Considerations

- **Private Key**: Never commit your private key to version control
- **Webhook Secret**: Keep your webhook secret secure
- **Environment Variables**: Use secure methods to store sensitive configuration
- **Permissions**: The app has write access to repository contents - review code before installing
- **Logs**: Be careful not to log sensitive information

## Development

### Project Structure

```
probot-app/
├── index.js           # Main application logic
├── package.json       # Dependencies and scripts
├── manifest.json      # GitHub App manifest
├── .env.example       # Example environment variables
└── README.md          # This file
```

### Key Dependencies

- **probot**: Framework for building GitHub Apps
- **simple-git**: Node.js interface for git commands

### Making Changes

1. Modify `index.js` for app logic changes
2. Test locally using Smee.io for webhook proxying
3. Use `npm run dev` for auto-reloading during development

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

MIT License - see the repository root for details

## Support

For issues or questions:
- Open an issue on the [GitHub repository](https://github.com/d7knight2/AnrWatchdog/issues)
- Check the [Probot documentation](https://probot.github.io/docs/)
- Review [GitHub Apps documentation](https://docs.github.com/en/developers/apps)

## Related

- [FlyCI Wingman Documentation](https://fly-ci.com/docs/wingman)
- [GitHub Actions Auto-Apply Workflow](../.github/workflows/flyci-auto-apply.yml)
- [Probot Framework](https://probot.github.io/)
