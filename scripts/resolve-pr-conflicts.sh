#!/bin/bash

# PR Conflict Auto-Resolver Script
# This script detects open PRs with merge conflicts and attempts to resolve them
# by merging the base branch into the PR branch.

set -e

# Enable detailed logging
set -x

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Initialize summary file
SUMMARY_FILE="/tmp/conflict-resolution-summary.txt"
> "$SUMMARY_FILE"

# Track if any meaningful action was taken
MEANINGFUL_ACTION=false

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

# Function to log with timestamp
log_with_timestamp() {
    local level="$1"
    shift
    echo "[$(date -u '+%Y-%m-%d %H:%M:%S UTC')] [$level] $*"
}

# Fetch all open pull requests with retry logic
log_with_timestamp "INFO" "Fetching open pull requests..."
for attempt in 1 2 3; do
    PRS_JSON=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/$GITHUB_REPOSITORY/pulls?state=open&per_page=100")
    
    # Check if the API call was successful
    if [ $? -eq 0 ] && [ -n "$PRS_JSON" ]; then
        log_with_timestamp "INFO" "Successfully fetched pull requests"
        break
    elif [ $attempt -eq 3 ]; then
        log_with_timestamp "ERROR" "Failed to fetch pull requests after 3 attempts"
        log_summary "❌ **Failed to fetch pull requests after retries**"
        exit 1
    else
        log_with_timestamp "WARN" "Fetch attempt $attempt failed, retrying..."
        sleep 2
    fi
done

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
NULL_MERGEABLE_COUNT=0

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
    
    log_with_timestamp "INFO" "----------------------------------------"
    log_with_timestamp "INFO" "Processing PR #$PR_NUMBER: $PR_TITLE"
    log_with_timestamp "INFO" "Head: $PR_HEAD_REF ($PR_HEAD_SHA)"
    log_with_timestamp "INFO" "Base: $PR_BASE_REF"
    log_with_timestamp "INFO" "Mergeable status: $PR_MERGEABLE"
    
    # Skip if PR is from a fork (we can't push to forks)
    if [ "$PR_HEAD_REPO" != "$GITHUB_REPOSITORY" ]; then
        log_with_timestamp "WARN" "Skipping PR #$PR_NUMBER - from fork: $PR_HEAD_REPO"
        log_summary "- **PR #$PR_NUMBER**: Skipped (from fork: $PR_HEAD_REPO)"
        echo ""
        continue
    fi
    
    # Handle null mergeable status - GitHub hasn't computed it yet
    if [ "$PR_MERGEABLE" == "null" ]; then
        log_with_timestamp "INFO" "PR #$PR_NUMBER has null mergeable status - GitHub hasn't computed merge status yet"
        log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ℹ️ Merge status not yet computed (will check on next run)"
        NULL_MERGEABLE_COUNT=$((NULL_MERGEABLE_COUNT + 1))
        echo ""
        continue
    fi
    
    # Check if PR has conflicts (mergeable is false)
    if [ "$PR_MERGEABLE" == "false" ]; then
        log_with_timestamp "WARN" "PR #$PR_NUMBER has merge conflicts - attempting resolution"
        CONFLICTS_DETECTED=$((CONFLICTS_DETECTED + 1))
        MEANINGFUL_ACTION=true
        
        # Attempt to resolve conflicts
        log_with_timestamp "INFO" "Attempting to resolve conflicts for PR #$PR_NUMBER..."
        
        # Fetch the latest changes with retry logic
        for attempt in 1 2 3; do
            if git fetch origin "$PR_HEAD_REF"; then
                log_with_timestamp "INFO" "Successfully fetched branch $PR_HEAD_REF"
                break
            elif [ $attempt -eq 3 ]; then
                log_with_timestamp "ERROR" "Failed to fetch branch $PR_HEAD_REF after 3 attempts"
                log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Failed to fetch branch after retries"
                CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
                echo ""
                continue 2
            else
                log_with_timestamp "WARN" "Fetch attempt $attempt failed, retrying..."
                sleep 2
            fi
        done
        
        for attempt in 1 2 3; do
            if git fetch origin "$PR_BASE_REF"; then
                log_with_timestamp "INFO" "Successfully fetched base branch $PR_BASE_REF"
                break
            elif [ $attempt -eq 3 ]; then
                log_with_timestamp "ERROR" "Failed to fetch base branch $PR_BASE_REF after 3 attempts"
                log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Failed to fetch base branch after retries"
                CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
                echo ""
                continue 2
            else
                log_with_timestamp "WARN" "Fetch attempt $attempt failed, retrying..."
                sleep 2
            fi
        done
        
        # Checkout the PR branch
        if ! git checkout -B "$PR_HEAD_REF" "origin/$PR_HEAD_REF"; then
            log_with_timestamp "ERROR" "Failed to checkout branch $PR_HEAD_REF"
            log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Failed to checkout branch"
            CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
            echo ""
            continue
        fi
        
        # Attempt to merge the base branch
        log_with_timestamp "INFO" "Merging $PR_BASE_REF into $PR_HEAD_REF..."
        
        if git merge "origin/$PR_BASE_REF" -m "Auto-merge $PR_BASE_REF to resolve conflicts" --no-edit; then
            log_with_timestamp "SUCCESS" "Merge successful for PR #$PR_NUMBER"
            
            # Push the changes
            log_with_timestamp "INFO" "Pushing changes to $PR_HEAD_REF..."
            if git push origin "$PR_HEAD_REF"; then
                log_with_timestamp "SUCCESS" "Successfully resolved conflicts for PR #$PR_NUMBER"
                log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ✅ Conflicts resolved automatically"
                CONFLICTS_RESOLVED=$((CONFLICTS_RESOLVED + 1))
                
                # Add a comment to the PR using jq to properly escape JSON
                COMMENT_TEXT="🤖 **Automatic Conflict Resolution**

The merge conflicts in this PR have been automatically resolved by merging the base branch (\`$PR_BASE_REF\`) into this branch.

Please review the changes and ensure everything is correct before merging."
                
                # Add comment with retry logic and better error handling
                for attempt in 1 2 3; do
                    COMMENT_RESPONSE=$(jq -n --arg body "$COMMENT_TEXT" '{body: $body}' | \
                        curl -s -w "\n%{http_code}" -X POST \
                            -H "Authorization: token $GITHUB_TOKEN" \
                            -H "Accept: application/vnd.github.v3+json" \
                            -H "Content-Type: application/json" \
                            "https://api.github.com/repos/$GITHUB_REPOSITORY/issues/$PR_NUMBER/comments" \
                            -d @-)
                    HTTP_CODE=$(echo "$COMMENT_RESPONSE" | tail -n1)
                    if [ "$HTTP_CODE" -eq 201 ]; then
                        log_with_timestamp "INFO" "Successfully posted comment to PR #$PR_NUMBER"
                        break
                    elif [ $attempt -eq 3 ]; then
                        log_with_timestamp "WARN" "Failed to post comment to PR #$PR_NUMBER after 3 attempts (HTTP $HTTP_CODE)"
                    else
                        log_with_timestamp "WARN" "Comment post attempt $attempt failed (HTTP $HTTP_CODE), retrying..."
                        sleep 2
                    fi
                done
                
            else
                log_with_timestamp "ERROR" "Failed to push changes to $PR_HEAD_REF"
                log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Merge succeeded but push failed"
                CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
                # Reset to the original remote state before the merge attempt
                git reset --hard "origin/$PR_HEAD_REF" 2>/dev/null || true
            fi
        else
            log_with_timestamp "ERROR" "Automatic merge failed - manual intervention required"
            
            # Get conflict details with better error handling
            if CONFLICT_FILES=$(git diff --name-only --diff-filter=U 2>/dev/null) && [ -n "$CONFLICT_FILES" ]; then
                CONFLICT_COUNT=$(echo "$CONFLICT_FILES" | wc -l)
            else
                CONFLICT_FILES="Unable to determine conflicting files"
                CONFLICT_COUNT="unknown"
            fi
            
            log_with_timestamp "ERROR" "Conflicting files ($CONFLICT_COUNT): $CONFLICT_FILES"
            log_summary "- **PR #$PR_NUMBER** (${PR_TITLE}): ❌ Automatic merge failed ($CONFLICT_COUNT conflicting files)"
            CONFLICTS_FAILED=$((CONFLICTS_FAILED + 1))
            
            # Clean up the failed merge by resetting to HEAD
            git reset --hard HEAD 2>/dev/null || true
            
            # Add a comment to the PR about the failure using jq to properly escape JSON with retry logic
            COMMENT_TEXT="🤖 **Automatic Conflict Resolution Failed**

Attempted to automatically resolve merge conflicts by merging the base branch (\`$PR_BASE_REF\`), but the merge failed due to conflicts that require manual resolution.

**Conflicting files ($CONFLICT_COUNT):**
\`\`\`
$CONFLICT_FILES
\`\`\`

Please resolve these conflicts manually."
            
            # Add a comment to the PR about the failure with retry logic and better error handling
            for attempt in 1 2 3; do
                COMMENT_RESPONSE=$(jq -n --arg body "$COMMENT_TEXT" '{body: $body}' | \
                    curl -s -w "\n%{http_code}" -X POST \
                        -H "Authorization: token $GITHUB_TOKEN" \
                        -H "Accept: application/vnd.github.v3+json" \
                        -H "Content-Type: application/json" \
                        "https://api.github.com/repos/$GITHUB_REPOSITORY/issues/$PR_NUMBER/comments" \
                        -d @-)
                HTTP_CODE=$(echo "$COMMENT_RESPONSE" | tail -n1)
                if [ "$HTTP_CODE" -eq 201 ]; then
                    log_with_timestamp "INFO" "Successfully posted failure comment to PR #$PR_NUMBER"
                    break
                elif [ $attempt -eq 3 ]; then
                    log_with_timestamp "WARN" "Failed to post failure comment to PR #$PR_NUMBER after 3 attempts (HTTP $HTTP_CODE)"
                else
                    log_with_timestamp "WARN" "Comment post attempt $attempt failed (HTTP $HTTP_CODE), retrying..."
                    sleep 2
                fi
            done
        fi
        
    else
        log_with_timestamp "INFO" "No conflicts detected for PR #$PR_NUMBER"
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
log_with_timestamp "INFO" "============================================"
log_with_timestamp "INFO" "Summary"
log_with_timestamp "INFO" "============================================"
log_with_timestamp "INFO" "Total PRs checked: $TOTAL_CHECKED"
log_with_timestamp "INFO" "PRs with null mergeable status: $NULL_MERGEABLE_COUNT"
log_with_timestamp "INFO" "Conflicts detected: $CONFLICTS_DETECTED"
log_with_timestamp "INFO" "Conflicts resolved: $CONFLICTS_RESOLVED"
log_with_timestamp "INFO" "Conflicts failed: $CONFLICTS_FAILED"
log_with_timestamp "INFO" "Meaningful action taken: $MEANINGFUL_ACTION"
log_with_timestamp "INFO" "============================================"

log_summary ""
log_summary "---"
log_summary ""
log_summary "**Summary Statistics:**"
log_summary "- Total PRs Checked: $TOTAL_CHECKED"
log_summary "- PRs with null mergeable status: $NULL_MERGEABLE_COUNT"
log_summary "- Conflicts Detected: $CONFLICTS_DETECTED"
log_summary "- Conflicts Resolved: $CONFLICTS_RESOLVED"
log_summary "- Conflicts Failed: $CONFLICTS_FAILED"

# Determine exit status based on outcomes
if [ $CONFLICTS_FAILED -gt 0 ]; then
    log_with_timestamp "ERROR" "Exiting with failure status due to $CONFLICTS_FAILED failed conflict resolutions"
    exit 1
elif [ "$MEANINGFUL_ACTION" = false ]; then
    log_with_timestamp "INFO" "No meaningful action taken - no conflicts to resolve"
    # Exit with special code to indicate no action needed
    exit 0
else
    log_with_timestamp "SUCCESS" "Successfully resolved $CONFLICTS_RESOLVED conflicts"
    exit 0
fi
