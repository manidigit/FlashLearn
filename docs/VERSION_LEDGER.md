## v6.17 — Adaptive Stats/Review UI hardening
- Application version: 6.17
- Version code: 117
- Previous version gate: 6.16 / 116
- Review Quiz option sizing is adaptive with a 72dp minimum; long labels may wrap without clipping.
- Progress/Retention presentation is compacted without changing calculation sources.
- Learning-stage visual bars are normalized to the largest actual stage count.
- No Review Engine, scheduler, quiz generation/evaluation, persistence, database schema, or statistics calculation change.
- Verification gate: GitHub Actions Build + Unit Test + Instrumentation/Upgrade Gate must pass.

## v6.16 — Review Compose icon compatibility hotfix
- Application version: 6.16
- Version code: 116
- Previous version gate: 6.15 / 115
- Root cause: unsupported `Icons.AutoMirrored.Outlined.ChevronLeft` under pinned Compose BOM 2024.02.00.
- Fix: compatible `Icons.AutoMirrored.Outlined.KeyboardArrowLeft`, retaining automatic RTL/LTR mirroring.
- Verification gate: GitHub Actions Build + Unit Test + Instrumentation/Upgrade Gate must pass.
- No learning algorithm, scheduling, database schema, parser/import, backup/restore, or ReviewViewModel semantics changed.

# FlashLearn Version Ledger

| Application version | Version code | Meaning | Verification |
|---|---:|---|---|
| **6.15** | **115** | Review RTL/LTR regression correction + release/process reconciliation | Pending GitHub CI |
| 6.14 | 114 | Review setup visual redesign to match the supplied reference, tokenized palette/dimensions, preserved review behavior | Prior checkpoint |
| 6.12 | 112 | Complete RTL/LTR + Theme root-cause hardening | Prior checkpoint |
| 6.11 | 111 | CI release-artifact and verification hardening | Verified by prior checkpoint |
| 6.10 | 110 | Secondary-screen RTL/LTR navigation consistency | Prior checkpoint |
| 6.09 | 109 | Global RTL/LTR direction-chain correction + CI release alignment | Verified — GitHub Actions run 1117 green |
| 5.99 | 99 | Quiz distractor rotation / difficulty bands | Historical |
| 6.00 | 100 | Quiz translation display + 3-second feedback | Product checkpoint |
| 6.04 | 104 | Review setup UI redesign + global RTL/LTR correction | Prior checkpoint |
| 6.03 | 103 | Review UI modernization + CI release alignment | Historical |
| 6.02 | 102 | Theme/Material 3 token audit | Historical |
| 6.01 | 101 | Education/Help + localization/accessibility/presentation audit + progress/statistics dashboard hardening | Historical |

## Historical stage identifiers
v4.33–v4.39 are specification-stage identifiers preserved for traceability. They are not current Android application versions.

## Current 6.15 process record
- v6.15 is the Review RTL/LTR regression-correction checkpoint; v6.14 remains the prior Review visual-design checkpoint and v6.13 remains the prior root theme/design-system checkpoint.
- v6.14 is the prior application checkpoint; v6.00 remains historical product baseline.
- The v4.33→later Word content is treated as the specification/process source; its incorrect embedded version labels are not used for current application numbering.
- The current 6.01 implementation records the applicable work from that specification: Education/Help, localization resources, Spanish-audio quiz contract documentation, and presentation/theme audit alignment.
- v6.15 source changes are limited to Review RTL/LTR regression correction and release/process bookkeeping; review engine, scheduling, persistence, database schema, import/restore contracts, and quiz semantics are preserved.
- v6.14 remains the prior Review setup visual-design checkpoint; v6.13 remains the prior Root Theme + Design System centralization checkpoint.
- Final source changes are grouped under the current semantic checkpoint rather than creating a separate application version for each document stage.
- GitHub Actions is the runtime verification gate; documentation alone is never a PASS. The v6.09 checkpoint is verified by run 1117: Build + Unit Test and Instrumentation + Upgrade Gate both succeeded.

## Authoritative numbering sources
1. app/build.gradle.kts
2. RuntimeGatePreflightTest
3. .github/workflows/android-ci.yml
4. this ledger
5. CHANGELOG.md / PROGRESS.md
6. docs/FlashLearn_PROGRESS_TRACKER.md

All six must agree before a release checkpoint is considered correctly numbered.
