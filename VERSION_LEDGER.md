# FlashLearn Version Ledger

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
