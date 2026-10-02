# FlashLearn v6.80 — Quiz transition prefetch fix

## Problem
After Quiz answer registration, the 3-second feedback period was followed by a visible loading/refresh state while the next question was generated.

## Root cause
The previous transition set `isLoading = true` and then synchronously awaited next-question generation on the ViewModel coroutine after the feedback timer.

## Fix
- Prefetch the next Quiz question in `Dispatchers.Default` while the current answer feedback is visible.
- Cache the prefetched `QuizCardUiState` by concept ID.
- Consume the cached question immediately after the 3-second feedback delay.
- Keep `isLoading = false` during the Quiz-to-Quiz transition.
- Preserve session-generation/session-ID guards and cancellation.
- Register distractor texts when a prefetched question is consumed.
- Added regression assertions for the prefetch contract and no-loading transition.

## Release identity
- Version: 6.80
- Version code: 680
- Previous gate: 6.79 / 679

## Verification
GitHub Actions is authoritative. The release is not considered CI-verified until the configured Build + Unit Test and Instrumentation + Upgrade Gate pass.
