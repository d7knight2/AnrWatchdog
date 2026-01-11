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
    
    app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    app.log.info('📨 Received issue_comment.created event');
    app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    app.log.info(`Repository: ${repository.owner.login}/${repository.name}`);
    app.log.info(`Issue/PR: #${issue.number}`);
    app.log.info(`Comment ID: ${comment.id}`);
    app.log.info(`Comment Author: ${comment.user.login}`);
    app.log.info(`Comment Length: ${comment.body.length} characters`);

    // Only process comments on pull requests
    if (!issue.pull_request) {
      app.log.debug('❌ Comment is not on a pull request, ignoring');
      return;
    }
    
    app.log.info('✅ Comment is on a pull request');

    // Check if the comment is from FlyCI Wingman and contains patches
    if (!isFlyciWingmanComment(comment.body)) {
      app.log.debug('❌ Comment is not from FlyCI Wingman or does not contain patches');
      app.log.debug(`Comment includes "FlyCI Wingman": ${comment.body.includes('FlyCI Wingman')}`);
      app.log.debug(`Comment includes diff blocks: ${comment.body.includes('```diff')}`);
      app.log.debug(`Comment includes patch blocks: ${comment.body.includes('```patch')}`);
      return;
    }
    
    app.log.info('✅ Comment identified as FlyCI Wingman suggestion');

    app.log.info(`🔄 Processing FlyCI Wingman comment on PR #${issue.number}`);
    app.log.info(`Comment URL: ${comment.html_url}`);

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

      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.info('📋 Pull Request Details');
      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.info(`PR #${issue.number}: ${prData.title}`);
      app.log.info(`Branch: ${headBranch}`);
      app.log.info(`Head SHA: ${headSha}`);
      app.log.info(`Repository: ${repoFullName}`);
      app.log.info(`PR State: ${prData.state}`);
      app.log.info(`Mergeable: ${prData.mergeable}`);
      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

      // Extract patches from comment
      app.log.info('🔍 Extracting patches from comment...');
      const extractStartTime = Date.now();
      const patches = extractPatches(comment.body);
      const extractEndTime = Date.now();
      const extractDuration = extractEndTime - extractStartTime;
      
      if (patches.length === 0) {
        app.log.warn('⚠️  No patches found in comment');
        app.log.warn(`Extraction took ${extractDuration}ms`);
        app.log.warn('Comment body preview (first 500 chars):');
        app.log.warn(comment.body.substring(0, 500));
        await postNoPatchesComment(context, issue.number, comment.html_url);
        return;
      }

      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.info('📦 Patch Extraction Results');
      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.info(`✅ Extracted ${patches.length} patch(es) from comment`);
      app.log.info(`Extraction time: ${extractDuration}ms`);
      
      // Log details about each patch
      patches.forEach((patch, index) => {
        const patchLines = patch.split('\n').length;
        const patchSize = patch.length;
        app.log.info(`Patch ${index + 1}:`);
        app.log.info(`  • Lines: ${patchLines}`);
        app.log.info(`  • Size: ${patchSize} bytes`);
        app.log.info(`  • Preview (first 200 chars): ${patch.substring(0, 200).replace(/\n/g, '\\n')}`);
      });
      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

      // Create a temporary directory for git operations
      const tempDir = await fs.mkdtemp(path.join(os.tmpdir(), 'flyci-wingman-'));
      app.log.info(`📁 Created temporary directory: ${tempDir}`);
      
      try {
        // Clone the repository and apply patches
        app.log.info('🚀 Starting clone and patch application process...');
        const cloneStartTime = Date.now();
        
        const result = await cloneAndApplyPatches(
          context,
          tempDir,
          repository,
          headBranch,
          patches
        );
        
        const cloneEndTime = Date.now();
        const cloneDuration = cloneEndTime - cloneStartTime;
        app.log.info(`Clone and apply process completed in ${cloneDuration}ms`);

        if (result.success) {
          app.log.info('✅ Patches applied successfully');
          
          // Push changes
          app.log.info('📤 Pushing changes to remote...');
          const pushStartTime = Date.now();
          await pushChanges(context, tempDir, headBranch, comment.html_url);
          const pushEndTime = Date.now();
          const pushDuration = pushEndTime - pushStartTime;
          app.log.info(`✅ Push completed in ${pushDuration}ms`);
          
          // Post success comment
          app.log.info('💬 Posting success comment...');
          await postSuccessComment(context, issue.number, comment.html_url);
          
          // Trigger workflow re-run if needed
          app.log.info('🔄 Checking for failed workflows to re-run...');
          await triggerWorkflowRerun(context, repository, headSha);
          
          app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
          app.log.info('🎉 Successfully completed all operations');
          app.log.info(`Total time: ${Date.now() - extractStartTime}ms`);
          app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        } else {
          app.log.error('❌ Failed to apply patches');
          app.log.error(`Error: ${result.error}`);
          
          // Post failure comment
          app.log.info('💬 Posting failure comment...');
          await postFailureComment(context, issue.number, comment.html_url, result.error);
        }
      } finally {
        // Clean up temporary directory
        app.log.info(`🧹 Cleaning up temporary directory: ${tempDir}`);
        await fs.rm(tempDir, { recursive: true, force: true });
        app.log.info('✅ Cleanup completed');
      }
    } catch (error) {
      app.log.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.error('❌ Error processing Wingman comment');
      app.log.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.error(`Error type: ${error.name}`);
      app.log.error(`Error message: ${error.message}`);
      app.log.error(`Stack trace:`, error.stack);
      app.log.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      
      // Post error comment
      try {
        await context.octokit.issues.createComment({
          owner: repository.owner.login,
          repo: repository.name,
          issue_number: issue.number,
          body: `❌ **Error applying FlyCI Wingman fixes**
          
An unexpected error occurred while trying to apply the suggested fixes:
\`\`\`
${error.message}
\`\`\`

Please try applying the changes manually or contact support if the issue persists.

Original comment: ${comment.html_url}`,
        });
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
    app.log.debug('Starting patch extraction...');
    const patches = [];
    const regex = /```(?:diff|patch)\n([\s\S]*?)```/g;
    let match;
    let matchCount = 0;

    while ((match = regex.exec(body)) !== null) {
      matchCount++;
      const patch = match[1].trim();
      if (patch) {
        app.log.debug(`Found valid patch block ${matchCount} (${patch.length} bytes)`);
        patches.push(patch);
      } else {
        app.log.debug(`Found empty patch block ${matchCount}, skipping`);
      }
    }

    app.log.debug(`Patch extraction completed. Found ${patches.length} valid patches from ${matchCount} code blocks`);
    return patches;
  }

  /**
   * Clone repository and apply patches
   */
  async function cloneAndApplyPatches(context, tempDir, repository, branch, patches) {
    try {
      const git = simpleGit();
      
      app.log.info('🔐 Getting installation token for authentication...');
      // Get installation token for authentication
      const installation = await context.octokit.apps.getRepoInstallation({
        owner: repository.owner.login,
        repo: repository.name,
      });

      const { data: { token } } = await context.octokit.apps.createInstallationAccessToken({
        installation_id: installation.data.id,
      });
      app.log.info(`✅ Installation token acquired for installation ID: ${installation.data.id}`);

      // Clone the repository
      const cloneUrl = `https://x-access-token:${token}@github.com/${repository.owner.login}/${repository.name}.git`;
      
      app.log.info(`📥 Cloning repository to ${tempDir}...`);
      app.log.info(`Branch: ${branch}`);
      const cloneStartTime = Date.now();
      
      await git.clone(cloneUrl, tempDir, ['--branch', branch, '--single-branch']);
      
      const cloneEndTime = Date.now();
      app.log.info(`✅ Repository cloned successfully in ${cloneEndTime - cloneStartTime}ms`);

      // Configure git
      app.log.info('⚙️  Configuring git user...');
      const repoGit = simpleGit(tempDir);
      await repoGit.addConfig('user.name', 'flyci-wingman-bot[bot]');
      await repoGit.addConfig('user.email', 'flyci-wingman-bot[bot]@users.noreply.github.com');
      app.log.info('✅ Git user configured');

      // Apply patches
      app.log.info(`🔧 Applying ${patches.length} patch(es)...`);
      
      for (let i = 0; i < patches.length; i++) {
        const patchFile = path.join(tempDir, `patch-${i}.patch`);
        await fs.writeFile(patchFile, patches[i]);

        app.log.info(`━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`);
        app.log.info(`Applying patch ${i + 1}/${patches.length}`);
        app.log.info(`Patch file: ${patchFile}`);
        app.log.info(`Patch size: ${patches[i].length} bytes`);
        
        const patchStartTime = Date.now();
        
        try {
          // Check if patch can be applied
          app.log.info('Validating patch...');
          await repoGit.raw(['apply', '--check', patchFile]);
          app.log.info('✅ Patch validation passed');
          
          // Apply the patch
          app.log.info('Applying patch...');
          await repoGit.raw(['apply', patchFile]);
          
          const patchEndTime = Date.now();
          app.log.info(`✅ Patch ${i + 1} applied successfully in ${patchEndTime - patchStartTime}ms`);
          
          // Log what files were changed
          const status = await repoGit.status();
          if (status.files.length > 0) {
            app.log.info(`Files affected by patch ${i + 1}:`);
            status.files.forEach(file => {
              app.log.info(`  • ${file.path} [${file.working_dir}]`);
            });
          }
        } catch (error) {
          const patchEndTime = Date.now();
          app.log.error(`❌ Failed to apply patch ${i + 1} after ${patchEndTime - patchStartTime}ms`);
          app.log.error(`Error type: ${error.name}`);
          app.log.error(`Error message: ${error.message}`);
          app.log.error('Patch content:');
          app.log.error(patches[i].substring(0, 500)); // Log first 500 chars of failed patch
          
          return {
            success: false,
            error: `Failed to apply patch ${i + 1}: ${error.message}`,
          };
        }
      }

      // Check if there are changes
      app.log.info('🔍 Checking for changes after applying all patches...');
      const status = await repoGit.status();
      
      if (status.files.length === 0) {
        app.log.warn('⚠️  No changes detected after applying patches');
        app.log.warn('This may indicate patches were already applied or are empty');
        return { success: false, error: 'No changes after applying patches' };
      }

      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.info('📊 Final Change Summary');
      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.info(`Total files changed: ${status.files.length}`);
      status.files.forEach(file => {
        app.log.info(`  • ${file.path} [${file.working_dir}]`);
      });
      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

      return { success: true };
    } catch (error) {
      app.log.error('❌ Error in cloneAndApplyPatches function');
      app.log.error(`Error type: ${error.name}`);
      app.log.error(`Error message: ${error.message}`);
      app.log.error(`Stack trace:`, error.stack);
      return { success: false, error: error.message };
    }
  }

  /**
   * Push changes to the PR branch
   */
  async function pushChanges(context, tempDir, branch, commentUrl) {
    const git = simpleGit(tempDir);

    app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    app.log.info('Preparing to push changes...');
    
    // Stage all changes
    app.log.info('📦 Staging all changes...');
    await git.add('-A');
    app.log.info('✅ Changes staged');

    // Commit changes
    app.log.info('💾 Creating commit...');
    const commitMessage = `Apply FlyCI Wingman suggested fixes\n\nAutomatically applied fixes suggested by FlyCI Wingman.\nOriginal comment: ${commentUrl}`;
    await git.commit(commitMessage);
    app.log.info('✅ Commit created');
    
    // Get commit details
    const log = await git.log(['-1']);
    if (log && log.latest) {
      app.log.info('Commit details:');
      app.log.info(`  • SHA: ${log.latest.hash}`);
      app.log.info(`  • Author: ${log.latest.author_name}`);
      app.log.info(`  • Message: ${log.latest.message.substring(0, 100)}...`);
    }

    // Push to remote
    app.log.info(`📤 Pushing changes to branch: ${branch}`);
    await git.push('origin', branch);
    
    app.log.info('✅ Changes pushed successfully');
    app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  }

  /**
   * Trigger workflow re-run for failed workflows
   */
  async function triggerWorkflowRerun(context, repository, headSha) {
    try {
      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.info('Checking for failed workflows to re-run...');
      
      // Get workflow runs for this commit
      const { data: workflowRuns } = await context.octokit.actions.listWorkflowRunsForRepo({
        owner: repository.owner.login,
        repo: repository.name,
        head_sha: headSha,
        status: 'completed',
      });

      app.log.info(`Found ${workflowRuns.workflow_runs.length} completed workflow runs for SHA ${headSha}`);

      // Find failed workflow runs
      const failedRuns = workflowRuns.workflow_runs.filter(
        (run) => run.conclusion === 'failure' && run.head_sha === headSha
      );

      if (failedRuns.length > 0) {
        app.log.info(`━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`);
        app.log.info(`📋 Found ${failedRuns.length} failed workflow run(s):`);
        failedRuns.forEach((run, index) => {
          app.log.info(`  ${index + 1}. ${run.name} (ID: ${run.id})`);
          app.log.info(`     • Status: ${run.status}`);
          app.log.info(`     • Conclusion: ${run.conclusion}`);
          app.log.info(`     • Started: ${run.created_at}`);
        });

        // Re-run the most recent failed workflow
        const runToRerun = failedRuns[0];
        app.log.info(`━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`);
        app.log.info(`🔄 Re-running workflow: ${runToRerun.name} (ID: ${runToRerun.id})`);

        await context.octokit.actions.reRunWorkflow({
          owner: repository.owner.login,
          repo: repository.name,
          run_id: runToRerun.id,
        });

        app.log.info('✅ Workflow re-run triggered successfully');
      } else {
        app.log.info('ℹ️  No failed workflows found to re-run');
      }
      app.log.info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    } catch (error) {
      app.log.warn('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      app.log.warn('⚠️  Could not trigger workflow re-run');
      app.log.warn(`Error: ${error.message}`);
      app.log.warn('Workflow will run automatically on the next push');
      app.log.warn('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    }
  }

  /**
   * Post success comment on PR
   */
  async function postSuccessComment(context, issueNumber, commentUrl) {
    const { repository } = context.payload;

    await context.octokit.issues.createComment({
      owner: repository.owner.login,
      repo: repository.name,
      issue_number: issueNumber,
      body: `✅ **FlyCI Wingman fixes applied successfully!**

The suggested fixes have been automatically applied and committed to this PR.

🔄 CI checks will run automatically to verify the fixes.

Original suggestion: ${commentUrl}`,
    });
  }

  /**
   * Post failure comment on PR
   */
  async function postFailureComment(context, issueNumber, commentUrl, error) {
    const { repository } = context.payload;

    await context.octokit.issues.createComment({
      owner: repository.owner.login,
      repo: repository.name,
      issue_number: issueNumber,
      body: `⚠️ **Failed to apply FlyCI Wingman fixes**

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
3. Committing and pushing to trigger CI again`,
    });
  }

  /**
   * Post no patches found comment on PR
   */
  async function postNoPatchesComment(context, issueNumber, commentUrl) {
    const { repository } = context.payload;

    await context.octokit.issues.createComment({
      owner: repository.owner.login,
      repo: repository.name,
      issue_number: issueNumber,
      body: `ℹ️ **No patches found to apply**

The comment was recognized as a FlyCI Wingman suggestion, but no valid diff/patch blocks were found.

Please ensure the suggestion includes code blocks formatted as:
\`\`\`diff
... patch content ...
\`\`\`

or

\`\`\`patch
... patch content ...
\`\`\`

Original comment: ${commentUrl}`,
    });
  }
};
