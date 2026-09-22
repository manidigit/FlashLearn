# FlashLearn Version Ledger

| Application version | Version code | Meaning | Verification |
|---|---:|---|---|
| 5.99 | 99 | Quiz distractor rotation / difficulty bands | Historical |
| 6.00 | 100 | Quiz translation display + 3-second feedback | Product checkpoint |
| **6.01** | **101** | Education/Help + localization/accessibility/presentation audit implementation after the v6.00 checkpoint | Pending GitHub CI |

## Historical stage identifiers
v4.33–v4.39 are specification-stage identifiers preserved for traceability. They are not current Android application versions.

## Current 6.01 process record
- v6.00 remains the product baseline.
- The v4.33→later Word content is treated as the specification/process source; its incorrect embedded version labels are not used for current application numbering.
- The current 6.01 implementation records the applicable work from that specification: Education/Help, localization resources, Spanish-audio quiz contract documentation, and presentation/theme audit alignment.
- Backup branch was created before implementation: `backup/pre-word-content-implementation-2026-09-22`.
- Final source changes are grouped under the 6.01 checkpoint rather than creating a separate application version for each document stage.
- GitHub Actions is the runtime verification gate; documentation alone is never a PASS.

## Authoritative numbering sources
1. app/build.gradle.kts
2. RuntimeGatePreflightTest
3. .github/workflows/android-ci.yml
4. this ledger
5. CHANGELOG.md / PROGRESS.md
6. docs/FlashLearn_PROGRESS_TRACKER.md

All six must agree before a release checkpoint is considered correctly numbered.
