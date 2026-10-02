# v6.70 — Achievements evaluation path Process Log

## Scope
Issue 8 — Achievements.

## Current repository baseline
- Repository: manidigit/FlashLearn
- Base commit: de47b94dd09e3c4a0cbb51fe9613139cd5d78df4
- Application checkpoint: v6.69 / 669

## Root cause
The domain already contained CheckAndUnlockAchievements, which computes the AchievementContext from active Concepts, review history, LearningState and DifficultyState and persists newly unlocked states. However, ProgressViewModel bypassed it and manually constructed a partial context, leaving practiced-word, active-word, VERY_HARD+Learned and monthly-correct-concept inputs at their defaults. As a result, several recorded achievement rules could not unlock through the Statistics/Progress path.

## Changes
- app/src/main/java/com/flashlearn/app/ui/progress/ProgressViewModel.kt: injected CheckAndUnlockAchievements; removed partial manual AchievementContext construction; refresh invokes the domain checker and then reads persisted states; Activity-range changes skip achievement evaluation.
- domain/src/test/java/com/flashlearn/domain/gamification/AchievementTest.kt: added a regression test with ten active Concepts and ten review-history records; verifies FIRST_TEN_WORDS unlocks and is persisted through the real checker contract.
- Version/release metadata advanced to 6.70 / 670 with 6.69 / 669 as the previous-version gate.

## Why this solution
The existing domain checker is the single source of truth for AchievementContext construction and unlock persistence. Reusing it prevents the UI layer from duplicating achievement business logic and ensures all seven recorded rules consume the same persisted data model.

## Tests
- Domain achievement threshold tests retained.
- Added check_and_unlock_achievements_uses_real_practice_context.
- Full GitHub Actions verification: GREEN — run 36988213115 (#2032), with Build + Unit Test, release/R8, Instrumentation + Upgrade Gate, and 6.69→6.70 upgrade path passing.

## CI / completion status
Issue 8 is complete and verified. Authoritative GitHub Actions run 36988213115 (#2032) is green.
