# FlashLearn v6.78 — Quiz Feedback Stability / Color Transition

## Scope
Issue: Quiz answer feedback intermittently loses its green/red state during the 3-second feedback window or appears to hang/jump while advancing to the next question.

## Source contract
- Quiz feedback remains visible for exactly 3 seconds after a successful answer submission.
- The correct option uses the success theme token; an incorrect selected option uses the error theme token.
- Quiz has no manual "Next" action during the feedback window; the next card is advanced automatically.
- The timer is session-scoped and must not advance a stale session.

## Root cause
The review state transition had two independent lifecycle concerns: the delayed coroutine was not centrally cancellable, and the old feedback/card state could be cleared independently of the next card state. A stale delayed job could therefore survive session changes, while Compose could observe an intermediate state during card replacement.

## Implementation
1. ReviewViewModel owns a single feedbackJob: Job? for the active Quiz feedback timer.
2. Starting a new review, changing language pair, exiting review, resetting after completion, and advancing to the next card cancel the previous feedback job.
3. The delayed transition is guarded by both sessionGeneration and sessionId before advancing.
4. During Quiz card replacement, the old quizCard and answerFeedback are cleared together while isLoading is asserted, preventing a neutral-color frame from representing the previous answer.
5. ReviewScreen uses Crossfade for the answer-feedback visual state, while success/error colors remain derived from the shared theme tokens.
6. Quiz-bank refresh remains once per review session; Quiz question generation remains on Dispatchers.Default.

## Coding principles applied
- Single owner for cancellable UI lifecycle work.
- Explicit generation/session guards for asynchronous state transitions.
- No blocking work on the main thread for Quiz generation.
- UI colors remain semantic theme tokens rather than hard-coded colors.
- State transitions are explicit and observable; no hidden navigation side effects are introduced.
- Existing Review scheduling, persistence, database schema, backup/import contracts, and Quiz semantics are preserved.

## Documentation / release synchronization
The following release authorities are advanced together to v6.78 / 678 with v6.77 / 677 as the previous-version gate:
- app/build.gradle.kts
- RuntimeGatePreflightTest
- .github/workflows/android-ci.yml
- docs/VERSION_LEDGER.md
- VERSION_LEDGER.md
- CHANGELOG.md
- PROGRESS.md
- docs/FlashLearn_PROGRESS_TRACKER.md

## Verification rule
Documentation and source inspection are not evidence of a green build. GitHub Actions Build + Unit Test and Instrumentation + Upgrade Gate are the authoritative verification gates for v6.78.