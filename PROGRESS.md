## v6.05 checkpoint — Review Setup UI Match + Release/Process Ledger
- Current application identity: versionName 6.05 / versionCode 105.
- Recorded the supplied reference-image implementation as the current Review setup UI checkpoint.
- Review setup now follows the requested visual structure: title/header, response mode, category filter, special review, scheduled review, vocabulary difficulty, quiz difficulty, word count, filter summary, and `شروع مرور`.
- Existing ReviewViewModel selection behavior and review algorithms are preserved; this checkpoint does not intentionally alter scheduling, learning-state transitions, database schema, import/backup contracts, or Quiz semantics.
- UI remains theme-token based and respects the app-wide RTL/LTR direction.
- Release process updated: CI current version is 6.05/105 and previous-version upgrade gate is 6.04/104.
- Verification status: pending authoritative GitHub Actions Build/Unit and Instrumentation + Upgrade Gate results.

## v6.04 checkpoint — Review setup UI
- Target screen confirmed as app/src/main/java/com/flashlearn/app/ui/review/ReviewScreen.kt.
- Review setup controls were compacted and visually refined.
- Review mode icons now use distinct semantic theme colors.
- Hard-coded RTL was removed; Review now inherits the app-wide RTL/LTR setting.
- Direction-sensitive navigation icons follow the active layout direction.
- Release identity: 6.04 / versionCode 104.

## v6.03 — Review UI modernization + CI alignment
- Review UI modernization checkpoint recorded.
- Release identity: 6.03 / 103.
- CI release gate synchronized with 6.03/103 and previous 6.02/102.
- GitHub Actions remains the build/test verification gate.

# FlashLearn — PROGRESS TRACKER

## Current checkpoint: v6.01
- Latest dashboard correction: review activity chart now has a visible review-count Y-axis and selectable weekly/monthly/three-month/all-history ranges.
- Learning progress remains 0% until the first real review of a concept; stage-based scoring applies only after review history exists.
- Runtime identity: versionName 6.01 / versionCode 101.
- This is a reconciliation/verification checkpoint, not a claim that every historical specification item is present in current source.
- Historical v4.33–v4.39 labels remain historical stage identifiers, not current app versions.
- Current source contains the v6.00 Quiz translation-display and 3-second feedback behavior.
- Current Review source also contains Spanish TTS playback for Quiz questions; this is now explicitly logged.
- GitHub Actions is authoritative for build/test status.
- Full verification requires Build/Unit, instrumentation, APK verification, and the previous-version upgrade gate.
- The uploaded archive named with v4.39 is tracked as a historical/documentation package; its filename is not used to infer application version.

### Reconciliation boundary
The v6.00 source backup and the separately uploaded v4.33→Final source/certification package were audited as separate artifacts. Where source trees differ, the project record must identify the difference rather than silently overwrite one snapshot with another.

## v5.99 — Quiz Distractor Rotation and Difficulty Bands
- Advanced application identity to 5.99 / versionCode 99.
- Added session-level distractor rotation so fresh valid wrong answers are preferred across sequential quiz cards.
- Changed Quiz Difficulty selection to deterministic EASY/MEDIUM/HARD category and entry-type tiers.
- Added regression coverage for sequential distractor rotation and difficulty-band separation.
- CI/runtime version gates advanced to 5.99/99 with 5.98/98 as the previous-version upgrade gate.

### Verification gate
- GitHub Actions remains the authoritative build/test gate.
- v5.99 is not marked fully verified until Build/Unit and Instrumentation jobs both complete successfully.

