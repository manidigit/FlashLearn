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
