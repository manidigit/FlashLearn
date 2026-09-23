# FlashLearn Version Ledger

| Application version | Version code | Meaning | Verification |
|---|---:|---|---|
| **6.12** | **112** | Complete RTL/LTR + Theme root-cause hardening | Pending GitHub CI |
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

## Current 6.12 process record
- v6.12 is the current application checkpoint; v6.00 remains historical product baseline.
- The v4.33→later Word content is treated as the specification/process source; its incorrect embedded version labels are not used for current application numbering.
- The current 6.01 implementation records the applicable work from that specification: Education/Help, localization resources, Spanish-audio quiz contract documentation, and presentation/theme audit alignment.
- v6.12 source changes are the current RTL/LTR + Theme root-cause hardening checkpoint.
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
