# Contributing to ANR Watchdog

Thank you for your interest in contributing to ANR Watchdog! We welcome contributions from the community and appreciate your help in making this project better.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Submitting Pull Requests](#submitting-pull-requests)
- [Coding Guidelines](#coding-guidelines)
- [Testing Requirements](#testing-requirements)
- [Pull Request Process](#pull-request-process)
- [Branch Protection Rules](#branch-protection-rules)
- [Code of Conduct](#code-of-conduct)

## Getting Started

ANR Watchdog is an Android library designed to detect Application Not Responding (ANR) states at runtime. Before contributing, please:

1. Read the [README.md](README.md) to understand the project's purpose and features
2. Familiarize yourself with the existing codebase
3. Check the [open issues](https://github.com/d7knight2/AnrWatchdog/issues) to see what needs work
4. Review the [TESTING.md](TESTING.md) documentation for testing guidelines

## Development Setup

### Prerequisites

- **JDK**: Java Development Kit 17 (Temurin distribution recommended)
- **Android SDK**: Latest stable version
- **Gradle**: Version 8.2 or higher (wrapper included)
- **Kotlin**: Version 1.9.0 or higher
- **Git**: For version control

### Setting Up Your Development Environment

1. **Fork the repository** to your GitHub account

2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/AnrWatchdog.git
   cd AnrWatchdog
   ```

3. **Add the upstream remote** to keep your fork in sync:
   ```bash
   git remote add upstream https://github.com/d7knight2/AnrWatchdog.git
   ```

4. **Set up your IDE** (Android Studio recommended):
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to your cloned repository
   - Wait for Gradle sync to complete

5. **Enable coroutine debugging** (optional but recommended):
   Add to your local `gradle.properties`:
   ```properties
   kotlin.coroutines.debug=on
   ```

6. **Build the project**:
   ```bash
   ./gradlew build
   ```

7. **Run tests** to verify your setup:
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

## How to Contribute

### Reporting Bugs

If you find a bug in the project:

1. **Check existing issues** to ensure the bug hasn't been reported already
2. **Create a new issue** with the following information:
   - **Clear title**: Briefly describe the issue
   - **Description**: Provide detailed information about the bug
   - **Steps to reproduce**: List the exact steps to reproduce the issue
   - **Expected behavior**: What you expected to happen
   - **Actual behavior**: What actually happened
   - **Environment**: Android version, device/emulator details, library version
   - **Logs/Screenshots**: Include relevant error messages, stack traces, or screenshots
   - **Sample code**: If applicable, provide a minimal code example

### Suggesting Features

We welcome feature suggestions! To propose a new feature:

1. **Check existing issues** to see if someone else has suggested it
2. **Create a new issue** with the "enhancement" label:
   - **Clear title**: Briefly describe the feature
   - **Use case**: Explain why this feature would be useful
   - **Proposed solution**: Describe how you envision the feature working
   - **Alternatives**: Mention any alternative solutions you've considered
   - **Additional context**: Provide any relevant examples or references

### Submitting Pull Requests

We love pull requests! Here's how to submit one:

1. **Create a new branch** for your work:
   ```bash
   git checkout -b feature/your-feature-name
   ```
   Or for bug fixes:
   ```bash
   git checkout -b fix/your-bug-fix-name
   ```

2. **Make your changes**:
   - Write clean, readable code
   - Follow the existing code style
   - Add or update tests as needed
   - Update documentation if necessary

3. **Commit your changes**:
   ```bash
   git add .
   git commit -m "Add feature: brief description"
   ```
   Follow conventional commit messages:
   - `feat:` for new features
   - `fix:` for bug fixes
   - `docs:` for documentation changes
   - `test:` for test additions or modifications
   - `refactor:` for code refactoring
   - `chore:` for maintenance tasks

4. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

5. **Create a Pull Request**:
   - Go to the original repository on GitHub
   - Click "New Pull Request"
   - Select your branch
   - Fill in the PR template with:
     - Description of changes
     - Related issue numbers (if any)
     - Testing performed
     - Screenshots (for UI changes)

## Coding Guidelines

### Kotlin Style Guide

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused on a single responsibility
- Add KDoc comments for public APIs
- Use nullable types appropriately and safely

### Code Structure

- **ANRWatchdog Library**: Core functionality in `anrwatchdog/src/main/kotlin/`
- **Demo App**: Example usage in `demoapp/src/main/java/`
- **Tests**: Unit tests in `src/test/`, instrumented tests in `src/androidTest/`

### Best Practices

- **DRY (Don't Repeat Yourself)**: Avoid code duplication
- **SOLID Principles**: Follow object-oriented design principles
- **Error Handling**: Handle errors gracefully with meaningful messages
- **Resource Management**: Properly clean up resources (threads, coroutines, etc.)
- **Performance**: Be mindful of performance, especially on the main thread
- **Security**: Never commit sensitive information (keys, passwords, etc.)

## Testing Requirements

All contributions must include appropriate tests. See [TESTING.md](TESTING.md) for detailed testing guidelines.

### Test Coverage

- **Unit Tests**: Required for all new functionality
  - Test core logic and edge cases
  - Mock dependencies appropriately
  - Aim for high code coverage

- **Instrumented Tests**: Required for UI changes
  - Test user interactions
  - Verify UI element behavior
  - Test across different device configurations

### Running Tests Locally

Before submitting a PR, ensure all tests pass:

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator or device)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests ANRWatchdogTest

# Generate test coverage report
./gradlew jacocoTestReport
```

## Pull Request Process

1. **Ensure all tests pass**: Both unit and instrumented tests must succeed
2. **Update documentation**: Reflect changes in README.md or other docs
3. **Follow branch protection rules**: PRs to `main` and `develop` require:
   - All automated tests to pass
   - At least one approval from a maintainer
   - No merge conflicts
4. **Address review feedback**: Respond to comments and make requested changes
5. **Squash commits** (if requested): Keep the git history clean
6. **Wait for CI/CD**: GitHub Actions will run all validations automatically

### What Happens After Submission

1. **Automated Checks**: CI/CD pipelines run automatically:
   - Unit tests (JUnit)
   - UI tests (Espresso)
   - Build validation
   - Code quality checks

2. **Code Review**: A maintainer will review your PR:
   - Code quality and style
   - Test coverage
   - Documentation completeness
   - Compatibility and performance

3. **Feedback Loop**: Address any requested changes:
   - Make updates to your branch
   - Push changes (tests re-run automatically)
   - Respond to comments

4. **Merge**: Once approved and all checks pass:
   - PR will be merged by a maintainer
   - Your contribution becomes part of the project!

## Branch Protection Rules

The `main` and `develop` branches are protected with the following rules:

- **Required status checks**: All CI tests must pass
- **Required reviews**: At least one approval required
- **No force pushes**: History must be preserved
- **Branch must be up-to-date**: Rebase or merge latest changes before merging

See [BRANCH_PROTECTION_SETUP.md](BRANCH_PROTECTION_SETUP.md) for more details.

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of:

- Age, body size, disability, ethnicity, gender identity and expression
- Level of experience, education, socio-economic status
- Nationality, personal appearance, race, religion, or sexual identity and orientation

### Expected Behavior

- **Be respectful**: Treat all participants with respect and consideration
- **Be collaborative**: Work together and help each other
- **Be constructive**: Provide helpful feedback and accept it gracefully
- **Be inclusive**: Welcome newcomers and help them get started
- **Be patient**: Not everyone has the same level of experience

### Unacceptable Behavior

- **Harassment**: Any form of harassment or discriminatory behavior
- **Trolling**: Inflammatory or off-topic comments
- **Personal attacks**: Insulting or derogatory comments
- **Spam**: Unsolicited commercial content or advertisements
- **Privacy violations**: Publishing others' private information

### Enforcement

Violations of the code of conduct may result in:

1. Warning from maintainers
2. Temporary ban from the project
3. Permanent ban from the project

Report violations to the project maintainers via GitHub issues or direct message.

## Questions?

If you have questions about contributing:

- **Read the docs**: Check [README.md](README.md), [TESTING.md](TESTING.md), and other documentation
- **Search issues**: Your question might already be answered
- **Ask the community**: Open a discussion issue with the "question" label
- **Contact maintainers**: Reach out via GitHub issues for project-specific questions

## Thank You!

Your contributions make ANR Watchdog better for everyone. We appreciate your time and effort in helping improve this project!

---

**Happy Contributing!** 🚀
