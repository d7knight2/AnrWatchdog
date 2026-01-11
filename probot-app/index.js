/**
 * FlyCI Wingman Auto-Apply GitHub App
 * 
 * This Probot app automatically detects FlyCI Wingman's PR comments,
 * extracts unified diff patches from the comments, applies them using git,
 * and commits/pushes the changes back to the PR branch.
 * 
 * @param {import('probot').Probot} app
 */

const { simpleGit } = require('simple-git');
const fs = require('fs').promises;
const path = require('path');
const os = require('os');

module.exports = (app) => {
  app.log.info('FlyCI Wingman Auto-Apply app loaded!');

  // Listen for new comments on issues (which includes PR comments)
  app.on('issue_comment.created', async (context) => {
    const { comment, issue, repository } = context.payload;

    // Only process comments on pull requests
    if (!issue.pull_request) {
      app.log.debug('Comment is not on a pull request, ignoring');
      return;
    }

    // Check if the comment is from FlyCI Wingman and contains patches
    if (!isFlyciWingmanComment(comment.body)) {
      app.log.debug('Comment is not from FlyCI Wingman or does not contain patches');
      return;
    }

    app.log.info(`Processing FlyCI Wingman comment on PR #${issue.number}`);

    try {
      // Get PR details
      const pr = await context.octokit.pulls.get({
        owner: repository.owner.login,
        repo: repository.name,
        pull_number: issue.number,
      });

      const prData = pr.data;
      const headBranch = prData.head.ref;
      const headSha = prData.head.sha;
      const repoFullName = prData.head.repo.full_name;

      app.log.info(`PR #${issue.number}: branch=${headBranch}, sha=${headSha}`);

      // Extract patches from comment
      const patches = extractPatches(comment.body);
      
      if (patches.length === 0) {
        app.log.warn('No patches found in comment');
        await postNoPatchesComment(context, issue.number, comment.html_url);
        return;
      }

      app.log.info(`Extracted ${patches.length} patch(es) from comment`);

      // Create a temporary directory for git operations
      const tempDir = await fs.mkdtemp(path.join(os.tmpdir(), 'flyci-wingman-'));
      
      try {
        // Clone the repository and apply patches
        const result = await cloneAndApplyPatches(
          context,
          tempDir,
          repository,
          headBranch,
          patches
        );

        if (result.success) {
          // Push changes
          await pushChanges(context, tempDir, headBranch, comment.html_url);
          
          // Post success comment
          await postSuccessComment(context, issue.number, comment.html_url);
          
          // Trigger workflow re-run if needed
          await triggerWorkflowRerun(context, repository, headSha);
        } else {
          // Post failure comment
          await postFailureComment(context, issue.number, comment.html_url, result.error);
        }
      } finally {
        // Clean up temporary directory
        await fs.rm(tempDir, { recursive: true, force: true });
      }
    } catch (error) {
      app.log.error('Error processing Wingman comment:', error);
      
      // Post error comment
      try {
        const errorCommentBody = `❌ **Error applying FlyCI Wingman fixes**

An unexpected error occurred while trying to apply the suggested fixes:
\`\`\`
${error.message}
\`\`\`

Please try applying the changes manually or contact support if the issue persists.

Original comment: ${comment.html_url}`;

        app.log.debug(`Attempting to post error comment on PR #${issue.number}. Comment preview: ${errorCommentBody.substring(0, 100)}...`);
        
        await context.octokit.issues.createComment({
          owner: repository.owner.login,
          repo: repository.name,
          issue_number: issue.number,
          body: errorCommentBody,
        });
        
        app.log.info(`Successfully posted error comment on PR #${issue.number}`);
      } catch (commentError) {
        app.log.error('Failed to post error comment:', commentError);
      }
    }
  });

  /**
   * Check if a comment is from FlyCI Wingman and contains patches
   */
  function isFlyciWingmanComment(body) {
    return (
      body.includes('FlyCI Wingman') &&
      (body.includes('```diff') || body.includes('```patch'))
    );
  }

  /**
   * Extract patch blocks from comment body
   */
  function extractPatches(body) {
    const patches = [];
    const regex = /```(?:diff|patch)\n([\s\S]*?)```/g;
    let match;

    while ((match = regex.exec(body)) !== null) {
      const patch = match[1].trim();
      if (patch) {
        patches.push(patch);
      }
    }

    return patches;
  }

  /**
   * Clone repository and apply patches
   */
  async function cloneAndApplyPatches(context, tempDir, repository, branch, patches) {
    try {
      const git = simpleGit();
      
      // Get installation token for authentication
      const installation = await context.octokit.apps.getRepoInstallation({
        owner: repository.owner.login,
        repo: repository.name,
      });

      const { data: { token } } = await context.octokit.apps.createInstallationAccessToken({
        installation_id: installation.data.id,
      });

      // Clone the repository
      const cloneUrl = `https://x-access-token:${token}@github.com/${repository.owner.login}/${repository.name}.git`;
      
      app.log.info(`Cloning repository to ${tempDir}`);
      await git.clone(cloneUrl, tempDir, ['--branch', branch, '--single-branch']);

      // Configure git
      const repoGit = simpleGit(tempDir);
      await repoGit.addConfig('user.name', 'flyci-wingman-bot[bot]');
      await repoGit.addConfig('user.email', 'flyci-wingman-bot[bot]@users.noreply.github.com');

      // Apply patches
      for (let i = 0; i < patches.length; i++) {
        const patchFile = path.join(tempDir, `patch-${i}.patch`);
        await fs.writeFile(patchFile, patches[i]);

        app.log.info(`Applying patch ${i + 1}/${patches.length}`);
        
        try {
          // Check if patch can be applied
          await repoGit.raw(['apply', '--check', patchFile]);
          
          // Apply the patch
          await repoGit.raw(['apply', patchFile]);
        } catch (error) {
          app.log.error(`Failed to apply patch ${i + 1}:`, error);
          return {
            success: false,
            error: `Failed to apply patch ${i + 1}: ${error.message}`,
          };
        }
      }

      // Check if there are changes
      const status = await repoGit.status();
      if (status.files.length === 0) {
        app.log.info('No changes after applying patches');
        return { success: false, error: 'No changes after applying patches' };
      }

      return { success: true };
    } catch (error) {
      app.log.error('Error in cloneAndApplyPatches:', error);
      return { success: false, error: error.message };
    }
  }

  /**
   * Push changes to the PR branch
   */
  async function pushChanges(context, tempDir, branch, commentUrl) {
    const git = simpleGit(tempDir);

    // Stage all changes
    await git.add('-A');

    // Commit changes
    await git.commit(
      `Apply FlyCI Wingman suggested fixes\n\nAutomatically applied fixes suggested by FlyCI Wingman.\nOriginal comment: ${commentUrl}`
    );

    // Push to remote
    app.log.info(`Pushing changes to branch ${branch}`);
    await git.push('origin', branch);
    
    app.log.info('Changes pushed successfully');
  }

  /**
   * Trigger workflow re-run for failed workflows
   */
  async function triggerWorkflowRerun(context, repository, headSha) {
    try {
      // Get workflow runs for this commit
      const { data: workflowRuns } = await context.octokit.actions.listWorkflowRunsForRepo({
        owner: repository.owner.login,
        repo: repository.name,
        head_sha: headSha,
        status: 'completed',
      });

      // Find failed workflow runs
      const failedRuns = workflowRuns.workflow_runs.filter(
        (run) => run.conclusion === 'failure' && run.head_sha === headSha
      );

      if (failedRuns.length > 0) {
        app.log.info(`Found ${failedRuns.length} failed workflow runs`);

        // Re-run the most recent failed workflow
        const runToRerun = failedRuns[0];
        app.log.info(`Re-running workflow: ${runToRerun.name} (ID: ${runToRerun.id})`);

        await context.octokit.actions.reRunWorkflow({
          owner: repository.owner.login,
          repo: repository.name,
          run_id: runToRerun.id,
        });

        app.log.info('Workflow re-run triggered successfully');
      } else {
        app.log.info('No failed workflows found to re-run');
      }
    } catch (error) {
      app.log.warn('Could not trigger workflow re-run:', error.message);
      app.log.info('Workflow will run automatically on the next push');
    }
  }

  /**
   * Post success comment on PR
   */
  async function postSuccessComment(context, issueNumber, commentUrl) {
    const { repository } = context.payload;

    const commentBody = `✅ **FlyCI Wingman fixes applied successfully!**

The suggested fixes have been automatically applied and committed to this PR.

🔄 CI checks will run automatically to verify the fixes.

Original suggestion: ${commentUrl}`;

    app.log.debug(`Attempting to post success comment on PR #${issueNumber}. Comment preview: ${commentBody.substring(0, 100)}...`);

    try {
      await context.octokit.issues.createComment({
        owner: repository.owner.login,
        repo: repository.name,
        issue_number: issueNumber,
        body: commentBody,
      });
      app.log.info(`Successfully posted success comment on PR #${issueNumber}`);
    } catch (error) {
      app.log.error(`Failed to post success comment on PR #${issueNumber}:`, error);
      throw error;
    }
  }

  /**
   * Post failure comment on PR
   */
  async function postFailureComment(context, issueNumber, commentUrl, error) {
    const { repository } = context.payload;

    const commentBody = `⚠️ **Failed to apply FlyCI Wingman fixes**

The suggested patch could not be applied automatically. This might be due to:
- Conflicts with recent changes in the PR
- The patch format not being recognized
- Files being modified since the suggestion was made

Error: \`${error}\`

Please review the suggestion and apply the changes manually:
${commentUrl}

You can also try:
1. Pulling the latest changes from the PR branch
2. Manually applying the suggested changes
3. Committing and pushing to trigger CI again`;

    app.log.debug(`Attempting to post failure comment on PR #${issueNumber}. Comment preview: ${commentBody.substring(0, 100)}...`);

    try {
      await context.octokit.issues.createComment({
        owner: repository.owner.login,
        repo: repository.name,
        issue_number: issueNumber,
        body: commentBody,
      });
      app.log.info(`Successfully posted failure comment on PR #${issueNumber}`);
    } catch (commentError) {
      app.log.error(`Failed to post failure comment on PR #${issueNumber}:`, commentError);
      throw commentError;
    }
  }

  /**
   * Post no patches found comment on PR
   */
  async function postNoPatchesComment(context, issueNumber, commentUrl) {
    const { repository } = context.payload;

    const commentBody = `ℹ️ **No patches found to apply**

The comment was recognized as a FlyCI Wingman suggestion, but no valid diff/patch blocks were found.

Please ensure the suggestion includes code blocks formatted as:
\`\`\`diff
... patch content ...
\`\`\`

or

\`\`\`patch
... patch content ...
\`\`\`

Original comment: ${commentUrl}`;

    app.log.debug(`Attempting to post no-patches comment on PR #${issueNumber}. Comment preview: ${commentBody.substring(0, 100)}...`);

    try {
      await context.octokit.issues.createComment({
        owner: repository.owner.login,
        repo: repository.name,
        issue_number: issueNumber,
        body: commentBody,
      });
      app.log.info(`Successfully posted no-patches comment on PR #${issueNumber}`);
    } catch (commentError) {
      app.log.error(`Failed to post no-patches comment on PR #${issueNumber}:`, commentError);
      throw commentError;
    }
  }
};
