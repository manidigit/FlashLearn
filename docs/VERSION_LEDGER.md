# Post-v6.82 branch reconciliation — 2026-10-03

**Documentation-only record. Android remains v6.82 / versionCode 682.**

The `main` branch contains implementation commits after the v6.82 checkpoint. They are recorded here without promoting them to a new Android release.

- `67ccf452`: Vite/React/TypeScript/Tailwind web scaffold introduced; web package version is 6.82.0.
- `4458485e`: About localization, difficulty-threshold Settings state, Compose test adjustments, and repository ignore rules.
- `0506812e`: Home → Statistics/Progress navigation and localized Home/theme updates.
- `53dd9899`: current Modern Minimal ThemeDesign/property representation normalized in source.

**Governance:** do not change Android version identity until the post-v6.82 implementation is audited, scoped, and CI-verified. See `docs/POST_V6.82_DOCUMENTATION_RECONCILIATION.md`.

---

## v6.82 — Audit Phase 1 hardening

- B-10: replaced the HARD_MODE_MASTER quadratic cross-scan with learned/VERY_HARD concept-ID sets.
- A-3: removed bare Persian marker words from marker-prefix classification and made marker matching exact unless the marker explicitly ends with a colon.
- C-1: persisted enum decoding now falls back safely instead of using valueOf; malformed parser-metadata JSON arrays degrade to empty lists.
- C-2: Needs Review queue repository operations are cancellation-safe and expose a user-facing error state.
- Added focused regression tests for parser marker collisions, malformed persisted enums, and cancellable coroutine handling.
- No Room schema or migration change.
- Release identity: 6.82 / 682; previous-version gate: 6.81 / 681.
- GitHub Actions verification pending.

## v6.78 release alignment
- app/build.gradle.kts: versionName 6.78 / versionCode 678.
- CI current gate: 6.78/678; previous-version upgrade gate: 6.77/677.
- Quiz feedback timer is cancellable and session-scoped; stale feedback jobs are cancelled on session lifecycle changes.
- Quiz card transition clears old card/feedback together while loading the next question; UI feedback uses Crossfade and semantic theme tokens.
- No data schema, learning algorithm, review scheduling, backup format, or theme JSON changes.
- CI verification is pending until the authoritative v6.78 gates pass.

## v6.77 release alignment
- app/build.gradle.kts: versionName 6.77 / versionCode 677.
- CI current gate: 6.77/677; previous-version upgrade gate: 6.76/676.
- Added creator WhatsApp contact in About with localized labels and a direct WhatsApp URI.
- No data schema, learning algorithm, review scheduling, backup format, or theme JSON changes.

| **6.76** | **676** | 2026-10-02 | Issue 10: replace horizontal theme cards with accessible dropdown selector | CI pending |

## v6.76 release alignment
- app/build.gradle.kts: versionName 6.76 / versionCode 676.
- CI current gate: 6.76/676; previous-version upgrade gate: 6.75/675.
- Theme persistence remains in AppViewModel/shared preferences.
- Theme JSON format and Room schema are unchanged.
- Added SettingsThemeDropdownTest regression coverage.


| **6.75** | **675** | 2026-10-02 | Issue 5: faster Home startup — one shared load for the three ready counts, lightweight history reads, parallel loading, O(N²) fix in progress | CI pending |

## v6.75 release alignment
- app/build.gradle.kts: versionName 6.75 / versionCode 675.
- CI current gate: 6.75/675; previous-version upgrade gate: 6.74/674.
- No Room schema, migration or string resource change.

| **6.74** | **674** | 2026-10-02 | Issue 4: fix crash when opening Category selection (Library and Review) — %d resources received text arguments | CI pending |

## v6.74 release alignment
- app/build.gradle.kts: versionName 6.74 / versionCode 674.
- CI current gate: 6.74/674; previous-version upgrade gate: 6.73/673.
- Four string resources changed from %1$d to %1$s (fa + en); no Room schema or migration change.

| **6.73** | **673** | 2026-10-02 | Issue 7 Activity Chart: verified existing fix (chronological data + LTR chart + test), documentation only | CI pending |

## v6.73 release alignment
- app/build.gradle.kts: versionName 6.73 / versionCode 673.
- CI current gate: 6.73/673; previous-version upgrade gate: 6.72/672.
- Documentation-only; no production code, Room schema or migration change.

| **6.72** | **672** | 2026-10-02 | Fix unit-test compile error in StatisticsTest (suspend call outside coroutine); no production change | CI pending |

## v6.72 release alignment
- app/build.gradle.kts: versionName 6.72 / versionCode 672.
- CI current gate: 6.72/672; previous-version upgrade gate: 6.71/671.
- Test-only fix; no production code, Room schema or migration change.
- Verification pending until GitHub Actions gates are green.

## v6.21 — Modern Minimal + CI/instrumentation reconciliation
- Application version: 6.21
- Version code: 121
- Previous version gate: 6.20 / 120
- Build + Unit Test: PASS on GitHub Actions run 35996886108.
- Instrumentation + Upgrade Gate: failed on two stale test expectations; tests have now been corrected to consume the active Modern Minimal specification.
- Verification remains pending until a new GitHub Actions run is green.

| Application version | Version code | Meaning | Verification |
|---|---:|---|---|
| 6.21 | 121 | Modern Minimal runtime presentation + theme/CI reconciliation | Pending new GitHub CI |
| 6.20 | 120 | Theme geometry/fallback hardening | Prior checkpoint |
| 6.19 | 119 | Modern Minimal built-in theme added | Prior checkpoint |
| 6.18 | 118 | Library theme-token presentation audit | Prior checkpoint |
| 6.17 | 117 | Adaptive Stats/Review UI hardening | Prior checkpoint |
| 6.16 | 116 | Review Compose icon compatibility hotfix | Prior checkpoint |
| 6.15 | 115 | Review RTL/LTR regression correction | Prior checkpoint |
| 6.14 | 114 | Review setup visual redesign | Prior checkpoint |
| 6.13 | 113 | Root Theme + Design System centralization | Prior checkpoint |
| 6.12 | 112 | Complete RTL/LTR + Theme hardening | Prior checkpoint |
| 6.11 | 111 | CI release-artifact hardening | Prior checkpoint |
| 6.10 | 110 | Secondary-screen RTL/LTR navigation | Prior checkpoint |
| 6.09 | 109 | Global RTL/LTR direction-chain correction | Verified — run 1117 |

## Authoritative numbering sources
1. app/build.gradle.kts
2. RuntimeGatePreflightTest
3. .github/workflows/android-ci.yml
4. this ledger
5. CHANGELOG.md / PROGRESS.md
6. docs/FlashLearn_PROGRESS_TRACKER.md

All six must agree before a release checkpoint is considered correctly numbered.

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
