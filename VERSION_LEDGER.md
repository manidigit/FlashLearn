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
