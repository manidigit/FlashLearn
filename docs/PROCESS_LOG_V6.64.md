# FlashLearn v6.64 — Process Log

## Scope
Single requested fix: Home ready-review cards were visually duplicating the ready count.

## Root cause
`ReviewReadyCard` rendered `readyCount` as the large number and then passed the same `readyCount` again into `home_ready_of_total`, whose resource text also contained the ready count.

## Change
- Kept the large ready count as the sole numeric ready value.
- Changed the secondary resource to show only the stage total: `آماده از %1$d کلمه`.
- Updated the English resource equivalently.
- No review-queue calculation or algorithm was changed.

## Release bookkeeping
- Version: 6.64
- versionCode: 664
- Previous-version upgrade gate: 6.63 / 663

## Verification
## CI verification history
- Run 36607134237: FAILED — existing FinalReleaseAuditContractTest still asserted 6.63.
- Run 36607763210: FAILED — release contract test still contained the old 6.63 version-name assertion.
- Run 36608192960: FAILED — runtime instrumentation gate still asserted 6.63/663.
- Run 36610449658: GREEN — Build + Unit Test and Instrumentation + Upgrade Gate both passed, including the previous-version upgrade path 6.63 → 6.64.

Checkpoint status: GREEN.
