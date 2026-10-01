# FlashLearn v6.68 — Process Log

## Scope
Complete current backlog item A1: duplicate concept merge must not lose the survivor's LearningState or DifficultyState.

## Baseline verification
- Repository default branch: main.
- Actual source baseline at task start: v6.67 release candidate, not v6.65.
- v6.67 verification was pending in VERSION_LEDGER.md.
- Current CI workflow requires Build + Unit Test and Instrumentation + Upgrade Gate.
- The repository audit backlog identifies A1 as a Priority 0 data-integrity blocker.

## Root cause
RemoveExactDuplicateConceptsUseCase only merged LearningState when the survivor already had a LearningState, and only merged DifficultyState when the survivor already had a DifficultyState. If the survivor had no state, the duplicate's progress remained attached to the soon-to-be-soft-deleted concept.

The use case also captured the survivor state once before iterating duplicates. The corrected implementation refreshes the in-memory survivor state after each successful merge so subsequent duplicates operate on the latest survivor state.

## Implementation
- Added explicit LearningState merge behavior: transfer missing survivor state, preserve survivor concept identity, keep the more advanced stage when both exist, add total correct/wrong counters, and retain the latest review timestamp.
- Added explicit DifficultyState merge behavior: transfer missing survivor state, otherwise keep the more advanced difficulty, and remap the resulting state to the survivor concept.
- Updated the duplicate merge loop to keep the merged survivor state for subsequent duplicate records.
- Removed obsolete Pronunciation/Example constructor references left behind by the v6.67 schema cleanup so the current Content model and Room mapper agree.

## Regression test
Added duplicateLearningAndDifficulty_areTransferredWhenSurvivorHasNoState covering LearningState transfer, DifficultyState transfer, survivor concept remapping, and absence of state for the soft-deleted duplicate.

## Database / compatibility
No new Room schema version was introduced by A1. The branch retains the v8 schema and its 7→8 migration. The obsolete v6.67 field references were compile-only inconsistencies; no legacy fields are reintroduced.

## Version alignment
- VersionName: 6.68
- VersionCode: 668
- Previous-version gate: 6.67 / 667

## Issue 5 — Home startup performance hardening
- Added Room aggregate queries for active progress stage/difficulty counts and due counts without changing schema structure or version.
- Moved missing LearningState/DifficultyState creation into `EnsureStatesUseCase`; Home summary is now read-only.
- Batched progress aggregation and reused the single Home history read for progress percentage and streak calculation.
- Home refresh is cancellable; redundant navigation-triggered refreshes were removed.
- Added regression coverage for state repair isolation.

## Verification
- Local Android/Gradle execution was not available because the environment cannot resolve GitHub/dependency hosts.
- GitHub Actions is the authoritative build/test verification.
- Final status: GREEN — GitHub Actions run 36898411410 passed Build + Unit Test, Instrumentation + Upgrade Gate, and the 6.67→6.68 upgrade path.