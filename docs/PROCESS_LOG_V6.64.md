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
CI must pass both Build + Unit Test and Instrumentation + Upgrade Gate before this checkpoint is considered complete.
