## v6.05 — Review Setup UI Match + Release/Process Ledger Update
- Advanced application identity to version 6.05 / versionCode 105.
- Recorded the Review setup screen visual implementation as the current UI checkpoint, matching the supplied reference layout: RTL section order, compact selection cards, difficulty/test controls, word-count choices, filtered-word summary, and the primary `شروع مرور` action.
- Preserved existing ReviewViewModel behavior and review algorithms; this checkpoint is a UI/layout refinement rather than a learning-engine or scheduling rewrite.
- Kept theme-token based colors and global RTL/LTR handling; no hard-coded app-wide direction was introduced.
- Updated the runtime version gate and GitHub Actions release/upgrade gate from 6.04/104 to 6.05/105, with 6.04/104 as the previous-version gate.
- This checkpoint is not marked as fully verified until GitHub Actions Build/Unit and Instrumentation + Upgrade Gate jobs complete successfully.

## v6.04 — Review Setup UI redesign and RTL/LTR correction
- Reworked the Review setup screen itself: compact card proportions, refined primary action, and distinct semantic icon colors.
- Removed the Review screen's hard-coded RTL provider so the global app layout direction now controls the screen.
- Direction-sensitive back/navigation icons now follow RTL/LTR automatically.
- Application release identity advanced to version 6.04 / code 104.
- GitHub Actions release gate aligned with 6.04/104 and previous 6.03/103.

## v6.03 — Review UI modernization + CI release alignment
- Modernized Review screen iconography, compact controls, and layout-direction handling.
- Application release identity advanced to version 6.03 / code 103.
- GitHub Actions release gate aligned with 6.03/103 and previous 6.02/102.

## v6.01 — Progress Dashboard Activity Chart + Learning Progress Correction
- Corrected learning-progress scoring so a newly added word remains at 0% until it has a real review history.
- Added a visible Y-axis with review-count scale to the review-activity chart.
- Added activity-range filters for weekly, monthly, three-month, and all-history views.
- Monthly and three-month views use real review-history data rather than the seven-day slice.
- Updated the About screen to describe the current statistics/progress dashboard behavior.

## v6.01 — Project Source / Version / CI Reconciliation
- Advanced application identity to 6.01 / versionCode 101 as the post-v6.00 audit checkpoint.
- Recorded the existing Spanish Quiz TTS playback path in the release ledger.
- Corrected the ledger so v4.33–v4.39 are historical specification-stage identifiers, not the current application version.
- Recorded the v6.00 source snapshot and the separately packaged v4.33→Final certification archive as distinct artifacts; filenames do not define runtime version.
- Restored the CI release gate to the current application version.
- Restored an Android instrumentation-test job so Build/Unit success is not mistaken for full Android verification.
- No Room schema, learning algorithm, scheduling contract, parser/import contract, or Quiz answer semantics are intentionally changed by this bookkeeping checkpoint.

# FlashLearn Changelog

## v6.00 — Quiz Translation Display and Answer Feedback Timing
- Advanced application identity to 6.00 / versionCode 100.
- Quiz answer options now display all target-language translations belonging to each Concept, in translation-index order, joined with ` / `.
- Added regression coverage proving multi-translation correct and distractor options are shown as complete translation groups rather than only the first translation.
- After submitting a Quiz answer, the selected wrong option remains red and the correct option remains green/visible for 3 seconds.
- Quiz then advances automatically after the 3-second feedback interval; the manual `ادامه` button is removed.
- No Room schema, learning algorithm, scheduling contract, or import behavior changed.

## v5.99 — Quiz Distractor Rotation and Difficulty Bands
- Advanced application identity to 5.99 / versionCode 99.
- Prevented the same three distractors from repeating across sequential quiz cards when at least three fresh valid candidates exist.
- Replaced the overly broad MEDIUM fixed-score selection with deterministic rank bands: EASY lowest three, MEDIUM middle three, HARD highest three.
- Preserved Vocabulary Difficulty pool rules, final four-option uniqueness checks, and Flashcard fallback.
