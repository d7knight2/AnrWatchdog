#!/bin/bash

# PR Conflict Auto-Resolver Script
# This script detects open PRs with merge conflicts and attempts to resolve them
# by merging the base branch into the PR branch.

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Initialize summary file
SUMMARY_FILE="/tmp/conflict-resolution-summary.txt"
> "$SUMMARY_FILE"

echo "============================================"
echo "PR Conflict Auto-Resolver"
echo "============================================"
echo ""

# Check required environment variables
if [ -z "$GITHUB_TOKEN" ]; then
    echo -e "${RED}Error: GITHUB_TOKEN environment variable is not set${NC}"
    exit 1
fi

if [ -z "$GITHUB_REPOSITORY" ]; then
    echo -e "${RED}Error: GITHUB_REPOSITORY environment variable is not set${NC}"
    exit 1
fi

# Extract owner and repo from GITHUB_REPOSITORY
REPO_OWNER=$(echo "$GITHUB_REPOSITORY" | cut -d'/' -f1)
REPO_NAME=$(echo "$GITHUB_REPOSITORY" | cut -d'/' -f2)

echo "Repository: $GITHUB_REPOSITORY"
echo ""

# Function to log to both console and summary file
log_summary() {
    echo "$1" | tee -a "$SUMMARY_FILE"
}

# Fetch all open pull requests
echo "Fetching open pull requests..."
PRS_JSON=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$GITHUB_REPOSITORY/pulls?state=open&per_page=100")

# Check if the API call was successful
if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to fetch pull requests${NC}"
    log_summary "❌ **Failed to fetch pull requests**"
    exit 1
fi

# Count the number of PRs
PR_COUNT=$(echo "$PRS_JSON" | jq '. | length')

# Validate PR_COUNT is a valid positive integer
if [ -z "$PR_COUNT" ] || [ "$PR_COUNT" == "null" ] || ! [[ "$PR_COUNT" =~ ^[0-9]+$ ]]; then
    echo -e "${YELLOW}No pull requests found or error parsing response${NC}"
    log_summary "ℹ️ No open pull requests found"
    exit 0
fi

echo "Found $PR_COUNT open pull request(s)"
echo ""

log_summary "**Date:** $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
log_summary ""
log_summary "**Total Open PRs:** $PR_COUNT"
log_summary ""

# Track statistics
TOTAL_CHECKED=0
CONFLICTS_DETECTED=0
CONFLICTS_RESOLVED=0
CONFLICTS_FAILED=0

# Store the current branch to return to later
ORIGINAL_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")

# Process each pull request (only if there are PRs)
if [ "$PR_COUNT" -gt 0 ]; then
for i in $(seq 0 $((PR_COUNT - 1))); do
    PR_NUMBER=$(echo "$PRS_JSON" | jq -r ".[$i].number")
    PR_TITLE=$(echo "$PRS_JSON" | jq -r ".[$i].title")
    PR_HEAD_REF=$(echo "$PRS_JSON" | jq -r ".[$i].head.ref")
    PR_BASE_REF=$(echo "$PRS_JSON" | jq -r ".[$i].base.ref")
    PR_HEAD_SHA=$(echo "$PRS_JSON" | jq -r ".[$i].head.sha")
    PR_MERGEABLE=$(echo "$PRS_JSON" | jq -r ".[$i].mergeable")
    PR_HEAD_REPO=$(echo "$PRS_JSON" | jq -r ".[$i].head.repo.full_name")
    
    TOTAL_CHECKED=$((TOTAL_CHECKED + 1))
    
    echo "----------------------------------------"
    echo "PR #$PR_NUMBER: $PR_TITLE"
    echo "Head: $PR_HEAD_REF ($PR_HEAD_SHA)"
    echo "Base: $PR_BASE_REF"
    echo "Mergeable: $PR_MERGEABLE"
    
    # Skip if PR is from a fork (we can't push to forks)
    if [ "$PR_HEAD_REPO" != "$GITHUB_REPOSITORY" ]; then
        echo -e "${YELLOW}⚠️  Skipping PR #$PR_NUMBER - from fork: $PR_HEAD_REPO${NC}"
        log_summary "- **PR #$PR_NUMBER**: Skipped (from fork: $PR_HEAD_REPO)"
        echo ""
        continue
    fi
    
    # Check if PR has conflicts (mergeable is false or null)
    if [ "$PR_MERGEABLE" == "false" ]; then
        echo -e "${YELLOW}⚠️  PR #$PR_NUMBER has merge conflicts${NC}"
        CONFLICTS_DETECTED=$((CONFLICTS_DETECTED + 1))
        
        # Attempt to resolve conflicts
        echo "Attempting to resolve conflicts for PR #$PR_NUMBER..."
        
        # Fetch the latest changes
        git fetch origin "$PR_HEAD_REF" || {
            echo -e "${RED}❌ Failed to fetch branch $PR_HEAD_REF${NC}"
            log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Failed to fetch branch"
            CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
            echo ""
            continue
        }
        
        git fetch origin "$PR_BASE_REF" || {
            echo -e "${RED}❌ Failed to fetch base branch $PR_BASE_REF${NC}"
            log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Failed to fetch base branch"
            CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
            echo ""
            continue
        }
        
        # Checkout the PR branch
        git checkout -B "$PR_HEAD_REF" "origin/$PR_HEAD_REF" || {
            echo -e "${RED}❌ Failed to checkout branch $PR_HEAD_REF${NC}"
            log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Failed to checkout branch"
            CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
            echo ""
            continue
        }
        
        # Attempt to merge the base branch
        echo "Merging $PR_BASE_REF into $PR_HEAD_REF..."
        
        if git merge "origin/$PR_BASE_REF" -m "Auto-merge $PR_BASE_REF to resolve conflicts" --no-edit; then
            echo -e "${GREEN}✅ Merge successful!${NC}"
            
            # Push the changes
            echo "Pushing changes to $PR_HEAD_REF..."
            if git push origin "$PR_HEAD_REF"; then
                echo -e "${GREEN}✅ Successfully resolved conflicts for PR #$PR_NUMBER${NC}"
                log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ✅ Conflicts resolved automatically"
                CONFLICTS_RESOLVED=$((CONFLICTS_RESOLVED + 1))
                
                # Add a comment to the PR using jq to properly escape JSON
                COMMENT_TEXT="🤖 **Automatic Conflict Resolution**

The merge conflicts in this PR have been automatically resolved by merging the base branch (\`$PR_BASE_REF\`) into this branch.

Please review the changes and ensure everything is correct before merging."
                
                jq -n --arg body "$COMMENT_TEXT" '{body: $body}' | \
                curl -s -X POST \
                    -H "Authorization: token $GITHUB_TOKEN" \
                    -H "Accept: application/vnd.github.v3+json" \
                    -H "Content-Type: application/json" \
                    "https://api.github.com/repos/$GITHUB_REPOSITORY/issues/$PR_NUMBER/comments" \
                    -d @- > /dev/null
                
            else
                echo -e "${RED}❌ Failed to push changes to $PR_HEAD_REF${NC}"
                log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Merge succeeded but push failed"
                CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
                # Reset to the original remote state before the merge attempt
                git reset --hard "origin/$PR_HEAD_REF" 2>/dev/null || true
            fi
        else
            echo -e "${RED}❌ Automatic merge failed - manual intervention required${NC}"
            
            # Get conflict details
            CONFLICT_FILES=$(git diff --name-only --diff-filter=U 2>/dev/null || echo "Unable to determine")
            
            log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Automatic merge failed (conflicting files: $CONFLICT_FILES)"
            CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
            
            # Clean up the failed merge by resetting to HEAD
            git reset --hard HEAD 2>/dev/null || true
            
            # Add a comment to the PR about the failure using jq to properly escape JSON
            COMMENT_TEXT="🤖 **Automatic Conflict Resolution Failed**

Attempted to automatically resolve merge conflicts by merging the base branch (\`$PR_BASE_REF\`), but the merge failed due to conflicts that require manual resolution.

**Conflicting files:**
\`\`\`
$CONFLICT_FILES
\`\`\`

Please resolve these conflicts manually."
            
            jq -n --arg body "$COMMENT_TEXT" '{body: $body}' | \
            curl -s -X POST \
                -H "Authorization: token $GITHUB_TOKEN" \
                -H "Accept: application/vnd.github.v3+json" \
                -H "Content-Type: application/json" \
                "https://api.github.com/repos/$GITHUB_REPOSITORY/issues/$PR_NUMBER/comments" \
                -d @- > /dev/null
        fi
        
    else
        echo -e "${GREEN}✓ No conflicts detected${NC}"
        log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ✓ No conflicts"
    fi
    
    echo ""
done
fi

# Return to the original branch
if [ -n "$ORIGINAL_BRANCH" ] && [ "$ORIGINAL_BRANCH" != "HEAD" ]; then
    git checkout "$ORIGINAL_BRANCH" 2>/dev/null || true
fi

# Print summary
echo "============================================"
echo "Summary"
echo "============================================"
echo "Total PRs checked: $TOTAL_CHECKED"
echo "Conflicts detected: $CONFLICTS_DETECTED"
echo "Conflicts resolved: $CONFLICTS_RESOLVED"
echo "Conflicts failed: $CONFLICTS_FAILED"
echo "============================================"

log_summary ""
log_summary "---"
log_summary ""
log_summary "**Summary Statistics:**"
log_summary "- Total PRs Checked: $TOTAL_CHECKED"
log_summary "- Conflicts Detected: $CONFLICTS_DETECTED"
log_summary "- Conflicts Resolved: $CONFLICTS_RESOLVED"
log_summary "- Conflicts Failed: $CONFLICTS_FAILED"

# Exit with error if there were failures (but not if we just didn't find conflicts)
if [ $CONFLICTS_FAILED -gt 0 ]; then
    exit 1
fi

exit 0
