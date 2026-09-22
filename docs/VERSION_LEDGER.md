# FlashLearn Version Ledger

| Application version | Version code | Meaning | Verification |
|---|---:|---|---|
| 5.99 | 99 | Quiz distractor rotation / difficulty bands | Historical |
| 6.00 | 100 | Quiz translation display + 3-second feedback | Product checkpoint |
| **6.01** | **101** | Project source/version/CI reconciliation | Build + Unit + Instrumentation + upgrade gate |

## Historical stage identifiers
v4.33–v4.39 are specification-stage identifiers preserved for traceability. They are not current Android application versions.

## Authoritative numbering sources
1. app/build.gradle.kts
2. RuntimeGatePreflightTest
3. .github/workflows/android-ci.yml
4. this ledger
5. CHANGELOG.md / PROGRESS.md

All five must agree before a release checkpoint is considered correctly numbered.
