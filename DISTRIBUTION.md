# App Distribution Setup Guide

This document provides detailed instructions for setting up and using Firebase App Distribution and Appetize.io integrations for the ANR Watchdog demo app.

## Table of Contents

1. [Firebase App Distribution](#firebase-app-distribution)
2. [Appetize.io Integration](#appetizeio-integration)
3. [Nightly Builds](#nightly-builds)
4. [Accessing Builds](#accessing-builds)
5. [Troubleshooting](#troubleshooting)

---

## Firebase App Distribution

Firebase App Distribution makes it easy to distribute pre-release versions of your app to trusted testers.

### Initial Setup

#### 1. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or select an existing project
3. Follow the setup wizard to create your project

#### 2. Register Your Android App

1. In the Firebase Console, click "Add app" and select Android
2. Enter the package name: `com.example.demoapp`
3. (Optional) Enter an app nickname: "ANR Watchdog Demo"
4. Download the `google-services.json` file

#### 3. Configure google-services.json

Replace the placeholder `google-services.json` file in the `demoapp/` directory with the one you downloaded from Firebase:

```bash
# Replace the placeholder file
cp /path/to/downloaded/google-services.json demoapp/google-services.json
```

**Important:** For security reasons, you may want to add `google-services.json` to `.gitignore` if it contains sensitive data. However, for open-source projects with public Firebase projects, it can be committed.

#### 4. Set Up Firebase App Distribution

1. In the Firebase Console, navigate to "Release & Monitor" > "App Distribution"
2. Click "Get started" if prompted
3. Create a tester group named "testers" (or modify the group name in `demoapp/build.gradle.kts`)

#### 5. Generate Service Account Credentials

To enable automated uploads from GitHub Actions:

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project
3. Navigate to "IAM & Admin" > "Service Accounts"
4. Click "Create Service Account"
5. Name it "GitHub Actions" with description "For CI/CD deployments"
6. Grant the role "Firebase App Distribution Admin"
7. Click "Create Key" and choose JSON format
8. Save the downloaded JSON file securely

#### 6. Configure GitHub Secrets

Add the Firebase service account credentials to your GitHub repository:

1. Go to your repository on GitHub
2. Navigate to Settings > Secrets and variables > Actions
3. Click "New repository secret"
4. Name: `FIREBASE_SERVICE_CREDENTIALS`
5. Value: Paste the entire contents of the service account JSON file
6. Click "Add secret"

### Configuration Files

The Firebase integration requires the following configuration:

**`demoapp/build.gradle.kts`:**
```kotlin
plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

configure<com.google.firebase.appdistribution.gradle.AppDistributionExtension> {
    releaseNotesFile = file("release-notes.txt").path
    groups = "testers"
    serviceCredentialsFile = System.getenv("FIREBASE_SERVICE_CREDENTIALS") ?: ""
}
```

**`build.gradle.kts`:** (root)
```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-appdistribution-gradle:4.0.1")
    }
}
```

### Manual Upload

To manually upload a build to Firebase App Distribution:

```bash
# Build the APK
./gradlew assembleDebug

# Upload to Firebase (requires FIREBASE_SERVICE_CREDENTIALS environment variable)
export FIREBASE_SERVICE_CREDENTIALS=/path/to/service-account.json
./gradlew appDistributionUploadDebug
```

---

## Appetize.io Integration

Appetize.io allows you to run mobile apps directly in the browser, making it easy to demo and test your app without requiring a physical device.

### Initial Setup

#### 1. Create an Appetize.io Account

1. Go to [Appetize.io](https://appetize.io/)
2. Sign up for an account (free tier available)
3. Navigate to your account settings

#### 2. Get Your API Token

1. In Appetize.io dashboard, go to "Account" > "API Token"
2. Copy your API token
3. Keep this token secure - it provides full access to your Appetize.io account

#### 3. Configure GitHub Secrets

Add your Appetize.io credentials to GitHub:

1. Go to your repository on GitHub
2. Navigate to Settings > Secrets and variables > Actions
3. Add two secrets:
   - Name: `APPETIZE_API_TOKEN`
     Value: Your Appetize.io API token
   - Name: `APPETIZE_PUBLIC_KEY` (Optional)
     Value: If updating an existing app, provide its public key

### Upload Script

The repository includes a script at `scripts/upload-to-appetize.sh` for uploading APKs:

```bash
#!/bin/bash
# Usage: ./scripts/upload-to-appetize.sh <path-to-apk> [app-public-key]

export APPETIZE_API_TOKEN="your_token_here"
./scripts/upload-to-appetize.sh demoapp/build/outputs/apk/debug/demoapp-debug.apk
```

The script will:
- Upload the APK to Appetize.io
- Create a new app (or update existing if public key provided)
- Output the app URL and public key
- Save details to `appetize-outputs/` directory

### Manual Upload

You can also manually upload via the Appetize.io web interface:

1. Go to [Appetize.io](https://appetize.io/)
2. Click "Upload" in the dashboard
3. Select your APK file
4. Configure device settings as needed
5. Click "Save"

---

## Nightly Builds

The repository is configured to automatically build and distribute the demo app every night at 2 AM UTC.

### Workflow Configuration

The nightly build workflow (`.github/workflows/nightly-build.yml`) performs the following steps:

1. **Build APK**: Compiles the debug APK with timestamp
2. **Update Release Notes**: Generates release notes with build date and commit
3. **Firebase Upload**: Uploads to Firebase App Distribution (if configured)
4. **Appetize.io Upload**: Uploads to Appetize.io (if configured)
5. **Artifact Storage**: Saves APK as GitHub Actions artifact (30-day retention)
6. **Build Summary**: Creates a summary with distribution links

### Triggering Manually

You can manually trigger a nightly build:

1. Go to your repository on GitHub
2. Navigate to Actions > Nightly Build and Distribution
3. Click "Run workflow"
4. Select the branch (usually `main`)
5. Click "Run workflow"

### Monitoring Builds

To check the status of nightly builds:

1. Go to the Actions tab in your GitHub repository
2. Look for "Nightly Build and Distribution" workflow runs
3. Click on a run to see detailed logs and download artifacts

---

## Accessing Builds

### Firebase App Distribution

**For Testers:**

1. Download the Firebase App Distribution app from Google Play
2. Wait for an email invitation (sent automatically after first upload)
3. Accept the invitation
4. New builds will appear automatically in the Firebase App Distribution app

**For Administrators:**

1. Go to Firebase Console > App Distribution
2. View all releases and their status
3. Invite additional testers or create new groups

### Appetize.io

**Public App URL:**
- After upload, the app is available at: `https://appetize.io/app/YOUR_PUBLIC_KEY`
- This URL can be shared for browser-based testing
- No installation required - runs directly in browser

**Embed in Documentation:**
```html
<iframe src="https://appetize.io/embed/YOUR_PUBLIC_KEY" 
        width="378" height="800" frameborder="0" scrolling="no"></iframe>
```

**Features:**
- Test on various Android versions
- Simulate different device types
- Network throttling
- Debug logging
- No physical device needed

### GitHub Actions Artifacts

1. Go to Actions > Nightly Build and Distribution
2. Click on a completed workflow run
3. Scroll to "Artifacts" section
4. Download the APK (available for 30 days)

---

## Troubleshooting

### Firebase App Distribution

**Issue: Build fails with "google-services.json not found"**
- Ensure `google-services.json` is present in the `demoapp/` directory
- Verify the file is valid JSON and contains your Firebase project info

**Issue: Upload fails with "Permission denied"**
- Verify `FIREBASE_SERVICE_CREDENTIALS` secret is set correctly
- Ensure the service account has "Firebase App Distribution Admin" role
- Check that the service account JSON is complete and valid

**Issue: Testers not receiving notifications**
- Verify testers are added to the correct group in Firebase Console
- Check tester email addresses are correct
- Ask testers to check spam folders

### Appetize.io

**Issue: Upload script fails with "401 Unauthorized"**
- Verify `APPETIZE_API_TOKEN` is set correctly
- Check that your Appetize.io account is active
- Regenerate API token if necessary

**Issue: App doesn't load in browser**
- Ensure APK is valid and not corrupted
- Check Appetize.io dashboard for error messages
- Verify minimum Android version compatibility

**Issue: Public key not found**
- If updating an existing app, verify `APPETIZE_PUBLIC_KEY` is correct
- Check Appetize.io dashboard for the correct public key
- Leave blank to create a new app instance

### General

**Issue: Workflow runs but skips distribution**
- Check that required secrets are configured
- Review workflow logs for specific error messages
- Ensure secrets are available to the workflow (check repository settings)

**Issue: Build fails during compilation**
- Check Gradle wrapper version compatibility
- Verify all dependencies are accessible
- Review build logs in Actions tab

---

## Security Considerations

1. **Never commit sensitive credentials** to the repository
2. **Use GitHub Secrets** for all API tokens and service accounts
3. **Limit access** to Firebase and Appetize.io to authorized team members
4. **Rotate credentials** regularly (every 90 days recommended)
5. **Monitor usage** in Firebase and Appetize.io dashboards
6. **Use tester groups** in Firebase to control access to builds

---

## Customization

### Change Distribution Schedule

Edit `.github/workflows/nightly-build.yml`:

```yaml
on:
  schedule:
    # Run every day at 2 AM UTC
    - cron: '0 2 * * *'  # Change this line
```

[Cron syntax reference](https://crontab.guru/)

### Modify Release Notes

Edit `demoapp/release-notes.txt` or modify the script in the workflow:

```yaml
- name: Update release notes with timestamp
  run: |
    echo "Your custom release notes" > demoapp/release-notes.txt
```

### Change Tester Group

Edit `demoapp/build.gradle.kts`:

```kotlin
configure<com.google.firebase.appdistribution.gradle.AppDistributionExtension> {
    groups = "your-group-name"  // Change this
}
```

---

## Additional Resources

- [Firebase App Distribution Documentation](https://firebase.google.com/docs/app-distribution)
- [Appetize.io API Documentation](https://appetize.io/docs)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Firebase Plugin](https://github.com/firebase/firebase-android-sdk)

---

## Support

For issues or questions:

1. Check this documentation first
2. Review workflow logs in GitHub Actions
3. Check Firebase/Appetize.io dashboards for errors
4. Open an issue in the GitHub repository
5. Contact the development team

---

Last updated: 2026-01-05
