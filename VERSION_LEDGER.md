# v6.84 — Quiz preparation freeze fix
- **Android release identity:** 6.84 / 684
- **Previous-version gate:** 6.83 / 683
- Root cause: v6.83 `refreshBank()` precomputed distractor candidates and lexical similarity for every active concept against every other concept (N x N, with regex compilation per comparison). On large vocabularies this froze the Quiz loading screen after choosing the question count.
- `refreshBank()` is now O(N): it only stores raw contents, concepts and difficulty states.
- Distractor entries are indexed lazily once per language; normalized text, tokens and bigrams are computed once per entry; regexes are precompiled.
- Lexical similarity is computed lazily, only for candidates that reach ranking.
- Removed unused per-question full scans of all contents.
- Quiz Difficulty, Vocabulary Difficulty, canonical-key duplicate guard, freshness and filter behavior unchanged.
- No Room schema/migration, backup format, or learning-stage rule change.
- CI verification: pending.

# v6.83 — Quiz batch preparation
- **Android release identity:** 6.83 / 683
- **Previous-version gate:** 6.82 / 682
- Quiz review now prepares the selected question set once at session start.
- Quiz distractor candidates are indexed by concept and target language when the review bank is refreshed, avoiding a full active-concept scan for every question.
- The selected question count remains bounded by the existing review filters and available eligible vocabulary.
- Invalid quiz questions are filtered during preparation; no out-of-filter fallback vocabulary is introduced.
- No Room schema/migration, backup format, or learning-stage rule change.
- CI verification: pending.

# Post-v6.82 branch reconciliation — 2026-10-03

**Important:** This is a documentation reconciliation record, **not a new Android release**. The executable Android identity remains **6.82 / 682**.

| Branch state | Android release identity | Documentation status |
|---|---:|---|
| v6.82 checkpoint | 6.82 / 682 | Release record exists |
| Post-v6.82 commits on `main` | 6.82 / 682 | Documented below; not promoted to a new Android version |
| Web scaffold | package version 6.82.0 | Branch artifact; release status not yet defined |

### Changes after the v6.82 checkpoint

- **67ccf452** — introduced a Vite/React/TypeScript/Tailwind web scaffold and core web/domain files.
- **4458485e** — expanded About localization, added/updated difficulty-threshold Settings state, adjusted Compose tests, and ignored web/build artifacts.
- **0506812e** — changed Home navigation to expose Statistics/Progress and updated Home localized presentation; updated Modern Minimal theme values.
- **53dd9899** — current Modern Minimal ThemeDesign/property representation was reformatted/normalized. The resulting source must be treated as the current theme state, not assumed to be formatting-only.

### Version-governance decision for this reconciliation

No `versionName`, `versionCode`, Android CI gate, or release artifact identity is changed here. A future release number should be assigned only after the post-v6.82 implementation state is audited and the Android/Web scope is explicitly decided.

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

## v6.81 — Quiz prefetch build fix

- Root cause: `ConceptRepository.get()` is suspend and was called while constructing the prefetch job, outside a coroutine.
- Fix: resolve the next concept inside the `Dispatchers.Default` prefetch coroutine, with session-generation/session guards before and after the lookup.
- Release identity: 6.81 / 681; previous gate: 6.80 / 680.
- No Room schema, learning algorithm, review scheduling, backup/export format, or theme contract change.
- CI verification pending.

## v6.79 — Backup export format removal

- CSV and SQLite were removed as executable backup/data-export formats.
- Backup UI exposes JSON and XLSX only.
- Internal Room/SQLite database remains unchanged.
- Added BackupExportFormatContractTest.
- Release identity: 6.79 / 679; previous gate: 6.78 / 678.
- CI verification is pending.

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

| **6.71** | **671** | 2026-10-02 | Statistics DB aggregation, bounded activity window, persistent activity snapshot, timestamp-only streak input | CI pending |

## v6.71 release alignment
- app/build.gradle.kts: versionName 6.71 / versionCode 671.
- CI current gate: 6.71/671; previous-version upgrade gate: 6.70/670.
- No Room schema or migration change.
- Statistics no longer loads the full ReviewHistory dataset for aggregation.
- Activity range changes reuse the stored 90-day activity snapshot.
- CI was not awaited by task instruction.

| **6.70** | **670** | 2026-10-02 | Fix Achievement evaluation path; use real persisted context; add integration regression test | GREEN — GitHub Actions run 36988213115 |

## v6.70 release alignment
- app/build.gradle.kts: versionName 6.70 / versionCode 670.
- CI: current gate 6.70/670; previous-version upgrade gate is 6.69/669.
- Progress/Statistics invokes the domain achievement checker; Activity-range changes do not re-evaluate achievements.
- No Room schema or migration change.
- Verification: GREEN — run 36988213115, including Build + Unit Test, release/R8, Instrumentation + Upgrade Gate, and 6.69→6.70 upgrade path.

| **6.69** | **669** | 2026-10-02 | Refresh: strip stray symbols (e.g. `*`) from the edges of Spanish words; content data version 3→4 | Pending CI |

## v6.69 release alignment
- app/build.gradle.kts: versionName 6.69 / versionCode 669.
- CI: current gate 6.69/669; previous-version upgrade gate is 6.68/668.
- Refresh content data version: 3 → 4 (CURRENT_CONTENT_DATA_VERSION).
- No Room schema version or Room migration changed (schema stays at version 8).
- Verification remains pending until all authoritative GitHub Actions release gates are green.

| **6.68** | **668** | 2026-10-01 | Statistics vocabulary-count alignment; active reviewed-word scope; reconciliation regression tests | Pending CI |

## v6.68 release alignment
- app/build.gradle.kts: versionName 6.68 / versionCode 668.
- CI: current gate 6.68/668; previous-version upgrade gate is 6.67/667.
- Total Words is defined from active Concepts.
- Basic statistics reconcile active totals as practiced + unpracticed + learned.
- Reviewed-word statistics include only active Concepts; review-event totals remain historical.
- No Room schema version or migration changed in this checkpoint.
- Verification remains pending until all authoritative GitHub Actions release gates are green.

| **6.67** | **667** | 2026-09-30 | Remove obsolete Pronunciation/Example fields; DB migration 7→8; backup compatibility | Pending CI |

## v6.67 release alignment
- app/build.gradle.kts: versionName 6.67 / versionCode 667.
- CI: current gate 6.67/667; previous-version upgrade gate is 6.66/666.
- Room schema: version 8 with migration 7→8 removing pronunciation and example from contents.
- Vocabulary Edit and Add Word now share the supported field contract.
- Legacy backup payloads may still contain the removed keys; restore ignores them rather than persisting them.
- Verification remains pending until both authoritative GitHub Actions jobs are green.

| **6.66** | **666** | 2026-09-30 | Non-destructive Add Word duplicate finding; reactive Library count refresh | GREEN — GitHub Actions run 36679020997 |

## v6.66 release alignment
- app/build.gradle.kts: versionName 6.66 / versionCode 666.
- CI: current gate 6.66/666; previous-version upgrade gate is 6.65/665.
- Find duplicates is read-only and reports exact duplicate groups without mutating data.
- Add Word observes Library state so Refresh updates the visible total count.
- Verification: GREEN — GitHub Actions run 36679020997.

| **6.65** | **665** | 2026-09-29 | Clarify Add Word duplicate-removal and refresh actions | Pending CI |

| **6.64** | **664** | 2026-09-29 | Home ready-review cards: remove duplicated ready count and align release gate | Pending CI |

## v6.64 release alignment
- app/build.gradle.kts: versionName 6.64 / versionCode 664.
- CI: current gate 6.64/664; previous-version upgrade gate is 6.63/663.
- Home ready-review cards display the ready count once; the secondary label displays only the total stage count.
- Verification remains pending until both authoritative GitHub Actions jobs are green.

| **6.63** | **663** | 2026-09-28 | Remove Spark theme | Pending CI |

| **6.62** | **662** | 2026-09-28 | ThemeDesign single-source ownership, adaptive breakpoints, Home/Add Word localization, accent isolation | Pending CI |

## v6.62 release alignment
- app/build.gradle.kts: versionName 6.62 / versionCode 662.
- CI: current gate 6.62/662; previous-version upgrade gate 6.61/661.
- Runtime visual parameters are sourced from ThemeDesign; legacy ThemeSpec visual fields remain compatibility-only.
- Adaptive breakpoints are ThemeDesign metrics and Home statistics reflow below the theme-owned medium breakpoint.
- User-facing Home/Add Word strings are resource-backed in Persian and English.
- Material dynamic color remains intentionally disabled so the selected FlashLearn theme owns the visual system.
- Verification is pending the authoritative GitHub Actions run.

| **6.61** | **661** | 2026-09-28 | Review hints use category/translation initial; bulk-import category selection; combined multi-type backup restore | GREEN — GitHub Actions run 36406917581 |

## v6.61 release alignment
- app/build.gradle.kts: versionName 6.61 / versionCode 661.
- CI: current gate 6.61/661; previous-version upgrade gate 6.60/660.
- Review hint priority: category name, otherwise first non-whitespace character of the translation.
- Bulk import applies an explicitly selected category to newly created concepts and fills only missing category on existing concepts.
- Backup bundle schema v3 can contain Vocabulary and/or Progress, while Full remains standalone; restore accepts legacy v1, typed v2 and bundle v3.
- Verification remains pending until both authoritative GitHub Actions jobs are green.

| **6.60** | **660** | 2026-09-28 | Grok visual refinement: dashboard geometry, full-width navigation pill, complete ThemeDesign persistence | GREEN — GitHub Actions run 36400464908 |

## v6.60 release alignment
- app/build.gradle.kts: versionName 6.60 / versionCode 660.
- CI: current gate 6.60/660; previous-version upgrade gate 6.59/659.
- Grok home geometry is theme-owned through ThemeDesign metrics.
- Grok selected navigation uses a full-width pill with theme-owned icon profile.
- Custom theme JSON now persists the complete ThemeDesign contract.
- Verification: GREEN — GitHub Actions run 36400464908.

| **6.59** | **659** | 2026-09-28 | Startup crash fix: initialize NeedsReview StateFlow before refresh; startup smoke-test hardening | GREEN — GitHub Actions run 36393654803 |

## v6.59 release alignment
- app/build.gradle.kts: versionName 6.59 / versionCode 659.
- CI: current gate 6.59/659; previous-version upgrade gate 6.58/658.
- `NeedsReviewViewModel` now initializes its backing StateFlow before its startup refresh call.
- The regression was confirmed by the Android emulator smoke test as a NullPointerException at the previous initialization order.
- Verification: GREEN — GitHub Actions run 36393654803 passed Build + Unit Test, Instrumentation + Upgrade Gate, and the 6.58→6.59 upgrade path.

| **6.58** | **658** | 2026-09-28 | Room 5→6 contents-index migration hardening; startup schema validation fix | Pending CI |

## v6.58 release alignment
- app/build.gradle.kts: versionName 6.58 / versionCode 658.
- CI: current gate 6.58/658; previous-version upgrade gate 6.57/657.
- Migration 5→6 now removes the temporary index_contents_new_languageCode_canonicalKey and recreates index_contents_languageCode_canonicalKey after renaming contents_new to contents.
- Migration instrumentation explicitly asserts the canonical index name.
- Verification remains pending until both authoritative GitHub Actions jobs are green.

| **6.57** | **657** | 2026-09-28 | Manual Needs-Review approval opens editable Add Word flow; Concept creation + queue approval are atomic | Pending CI |

## v6.57 release alignment
- app/build.gradle.kts: versionName 6.57 / versionCode 657.
- CI: current gate 6.57/657; previous-version upgrade gate 6.56/656.
- Manual review approval no longer changes a queue item to APPROVED directly from the list.
- The queue item remains PENDING until the edited Add Word form is successfully saved.
- Concept creation and queue status update share one database transaction.
- Verification is pending the authoritative GitHub Actions run.

| **6.56** | **656** | 2026-09-28 | Comprehensive audit hardening: review flow, language-pair routing, backup/export integrity, one-time starter data, periodic soft-delete cleanup | Pending CI |

## v6.56 release alignment
- app/build.gradle.kts: versionName 6.56 / versionCode 656.
- CI: current gate 6.56/656; previous-version upgrade gate 6.55/655.
- Learning and Difficulty remain independent systems. Weekly/Monthly correct-review dates are aligned to local calendar-day start; legacy monthly/path fields remain compatibility data only.
- Home ready counts use the active language pair and the same review eligibility rule as the review queue; denominators remain stage totals.
- Translation editing is reconciled by target-language translation index, including deletion of removed meanings.
- Starter data is guarded by a persisted one-time flag.
- Deleted inactive concepts are periodically purged after 90 days.
- Verification is pending the authoritative GitHub Actions run.

| **6.54** | **654** | 2026-09-28 | Room database optimization: 6→7 safe migration, query indexes, atomic GZIP JSON export | Pending CI |

## v6.54 release alignment
- app/build.gradle.kts: versionName 6.54 / versionCode 654.
- Database: Room schema version 7 with non-destructive migration from version 6.
- Added indexes for active/category, active/favorite, review-history lookup, session chronology, tag names, active languages and active language pairs.
- JSON data export is now GZIP-compressed and written atomically through a temporary file before replacement.
- Existing backup schema/field names remain compatible; no destructive migration is enabled.
- Verification is pending the authoritative GitHub Actions run.

# FlashLearn Version Ledger

| Version | Code | Date | Checkpoint | Verification |
|---|---:|---|---|---|
| **6.53** | **653** | 2026-09-27 | Independent Learning + Difficulty algorithms; threshold-only Difficulty | Pending CI |
| 6.52 | 652 | 2026-09-27 | Unified Review eligibility + same-day lock + Home due-count alignment | Prior checkpoint |

## v6.53 release alignment
- app/build.gradle.kts: versionName 6.53 / versionCode 653.
- CI: current gate 6.53/653; previous-version upgrade gate 6.52/652.
- Learning: Daily → Weekly → Monthly → Learned; correct advances, wrong returns to Daily.
- Difficulty: Easy → Medium → Hard → Very Hard; threshold-only consecutive answer logic, default threshold 3.
- No Weekly/Monthly-specific Difficulty escalation.
- Verification is pending the authoritative GitHub Actions run.

| Version | Code | Date | Checkpoint | Verification |
|---|---:|---|---|---|
| 6.51 | 651 | 2026-09-27 | Grok visual reconstruction + full ThemeDesign persistence | Pending CI |
| 6.50 | 650 | 2026-09-26 | GTP fifth built-in theme runtime correction | GREEN — GitHub Actions run 1429 |

## v6.51 release alignment
- app/build.gradle.kts: versionName 6.51 / versionCode 651.
- CI: current gate 6.51/651; previous-version upgrade gate 6.50/650.
- Theme system: format version 3 with complete runtime ThemeDesign ownership.
- Grok owns its full visual profile: palette, metrics, borders, elevation, typography, navigation, buttons, statistics, review, library and ornaments.
- Verification is pending the authoritative GitHub Actions run.


| Version | Code | Date | Checkpoint | Verification |
|---|---:|---|---|---|
| 6.50 | 650 | 2026-09-26 | GTP fifth built-in theme runtime correction | GREEN — GitHub Actions run 1429 |
| 6.49 | 649 | 2026-09-25 | Previous theme-system checkpoint; metadata claimed 5 themes but runtime registry contained 4 | Historical |

## v6.50 release alignment
- app/build.gradle.kts: versionName 6.50 / versionCode 650.
- CI: current gate 6.50/650; previous-version upgrade gate 6.49/649.
- Theme system: format version 3.
- Built-in catalog: GROK, CLAUD, MODERN_MINIMAL, SPARK, GTP.
- GTP owns palette, spacingScale, densityScale, typographyScale, corner geometry, elevationScale and filled icon style.
- Runtime picker path: FlashLearnThemeSpec.BUILT_IN → AppViewModel.availableThemes() → SettingsScreen.
- Verification completed: both CI jobs passed on GitHub Actions run 1429, including the previous-version upgrade gate.
