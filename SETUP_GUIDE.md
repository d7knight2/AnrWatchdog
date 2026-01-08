# Quick Setup Guide for Repository Maintainers

This guide provides a quick reference for setting up the Firebase App Distribution and Appetize.io integrations.

## Prerequisites

- Access to Firebase Console
- Access to Appetize.io account
- Repository admin access (to add secrets)

## Setup Steps

### 1. Firebase App Distribution

#### Step 1: Configure Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create or select a project
3. Add Android app with package name: `com.example.demoapp`
4. Download `google-services.json`
5. Replace `demoapp/google-services.json` with your downloaded file

#### Step 2: Create Service Account
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project
3. Navigate to IAM & Admin > Service Accounts
4. Create service account with "Firebase App Distribution Admin" role
5. Create and download JSON key

#### Step 3: Add GitHub Secret
1. Repository Settings > Secrets and variables > Actions
2. Add secret: `FIREBASE_SERVICE_CREDENTIALS`
3. Paste entire JSON key content as value

#### Step 4: Create Tester Group
1. Firebase Console > App Distribution
2. Create tester group named "testers"
3. Add tester email addresses

### 2. Appetize.io

#### Step 1: Get API Token
1. Sign up at [Appetize.io](https://appetize.io/)
2. Navigate to Account > API Token
3. Copy your API token

#### Step 2: Add GitHub Secret
1. Repository Settings > Secrets and variables > Actions
2. Add secret: `APPETIZE_API_TOKEN`
3. Paste API token as value

#### Step 3: (Optional) Add Public Key
If you want to update an existing Appetize.io app:
1. Get the public key from your Appetize.io dashboard
2. Add secret: `APPETIZE_PUBLIC_KEY`
3. Paste public key as value

## GitHub Secrets Summary

Required secrets for full functionality:

| Secret Name | Required | Purpose |
|-------------|----------|---------|
| `FIREBASE_SERVICE_CREDENTIALS` | For Firebase | Service account JSON for Firebase uploads |
| `APPETIZE_API_TOKEN` | For Appetize.io | API token for Appetize.io uploads |
| `APPETIZE_PUBLIC_KEY` | Optional | Update existing app instead of creating new |

## Testing the Setup

### Manual Workflow Trigger
1. Go to Actions > Nightly Build and Distribution
2. Click "Run workflow"
3. Select `main` branch
4. Click "Run workflow"
5. Monitor the run for any errors

### Check Workflow Status
- ✅ Green check = Successful
- ❌ Red X = Failed (check logs)
- ⚠️ Yellow warning = Partial success (some steps skipped)

### Verify Distributions

**Firebase:**
- Firebase Console > App Distribution
- Should see latest build listed
- Testers should receive notification

**Appetize.io:**
- Appetize.io dashboard
- Should see new/updated app
- Test the app URL in browser

**GitHub:**
- Actions > Nightly Build and Distribution > Latest run
- Download artifact under "Artifacts" section

## Troubleshooting Quick Reference

| Issue | Solution |
|-------|----------|
| Workflow runs but skips Firebase | Add `FIREBASE_SERVICE_CREDENTIALS` secret |
| Workflow runs but skips Appetize.io | Add `APPETIZE_API_TOKEN` secret |
| Build fails with "google-services.json not found" | Replace placeholder file with real one |
| Firebase upload fails with permission error | Verify service account has correct role |
| Appetize.io returns 401 | Check API token is valid |

## Schedule Configuration

Current schedule: **Daily at 2 AM UTC**

To change:
- Edit `.github/workflows/nightly-build.yml`
- Modify the cron expression under `schedule:`
- Use [crontab.guru](https://crontab.guru/) for help with cron syntax

Examples:
- `0 2 * * *` - Daily at 2 AM UTC (current)
- `0 0 * * 1` - Every Monday at midnight UTC
- `0 */6 * * *` - Every 6 hours

## Next Steps

1. ✅ Configure Firebase (follow steps above)
2. ✅ Configure Appetize.io (follow steps above)
3. ✅ Replace `google-services.json` with real file
4. ✅ Test manual workflow trigger
5. ✅ Invite testers to Firebase App Distribution
6. ✅ Share Appetize.io URL for browser testing

## Additional Documentation

For detailed information, see:
- [DISTRIBUTION.md](DISTRIBUTION.md) - Complete setup and usage guide
- [Firebase Documentation](https://firebase.google.com/docs/app-distribution)
- [Appetize.io API Docs](https://appetize.io/docs)

## Support

If you encounter issues:
1. Check workflow logs in GitHub Actions
2. Review [DISTRIBUTION.md](DISTRIBUTION.md) troubleshooting section
3. Verify all secrets are configured correctly
4. Check Firebase/Appetize.io dashboards for errors

---

**Important Security Notes:**
- Never commit real `google-services.json` with sensitive data to public repos
- Keep API tokens and service account keys secure
- Rotate credentials regularly (every 90 days recommended)
- Use GitHub Secrets for all sensitive data

---

Setup completed by: ________________  
Date: ________________
