# FlyCI Wingman Test: Intentional Compilation Error

## Purpose
This pull request deliberately introduces a compilation failure to test the FlyCI Wingman workflow's ability to detect and suggest fixes for build errors.

## Error Type: Type Mismatch (Incompatible Assignment)

### Location
- **File**: `src/main/kotlin/com/d7knight/anrwatchdog/rxjava/SlowRxExperimentEnabledRepository.kt`
- **Line**: ~10 (in SlowRxExperimentEnabledRepository object)

### Error Description
A `String` value is being assigned to a variable declared as `Int` type:
```kotlin
private val testErrorValue: Int = "This should be an Int, not a String"
```

### Expected Compiler Error
```
Type mismatch: inferred type is String but Int was expected
```

## Uniqueness
This error differs from existing test PRs:
- **PR #20**: Used undefined variable references, missing imports, and non-existent method calls
- **This PR**: Uses type mismatch / incompatible type assignment

## Testing Objective
- Verify FlyCI Wingman can detect type mismatch errors during CI builds
- Validate automated fix suggestions are provided
- Ensure proper error reporting in pull request comments

## Important Note
⚠️ **DO NOT MERGE THIS PR** - This is intentionally broken code for testing purposes only.

## Expected FlyCI Wingman Behavior
1. CI build should fail during Kotlin compilation
2. FlyCI Wingman should detect the compilation error
3. Wingman should provide a fix suggestion (e.g., remove the line or change the type)
4. Wingman should comment on this PR with analysis and recommendations
