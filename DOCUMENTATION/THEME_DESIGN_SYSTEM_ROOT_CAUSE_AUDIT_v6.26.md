# FlashLearn Theme / Design System Root Cause Audit — v6.26

Repository: manidigit/FlashLearn
Branch: refactor/theme-design-system-v6.26
Audit date: 2026-09-25

## Executive conclusion

The previous Theme was not actually a complete Design System boundary. It already had a reasonably broad token provider, but several visual decisions bypassed it.

## Root cause (source-proven)

1. Theme data was broader than Theme consumption. FlashLearnThemeTokens contains semantic colors, spacing, dimensions, corners, elevations and an iconStyle, but individual screens still instantiate Material icons directly and still create local shapes, dp values and layout values.
2. Icon architecture was an enum, not an icon facade. The theme exposed IconStyle.FILLED/OUTLINED, but most screens selected concrete Icons.Outlined.* values themselves. Therefore changing iconStyle could only affect components that explicitly consulted the enum.
3. Layout was tokenized only as numeric values. The token model can provide spacing/heights, but it does not and should not own structural choices such as Row vs Column, navigation placement, number of columns, or content ordering.
4. FlashLearnScreen is a surface wrapper, not a complete screen design-system boundary. It supplies background through MaterialTheme, but it does not automatically inject padding, typography, icon selection, navigation, or component variants.
5. Shared components are only partially centralized. FlashLearnBackButton, buttons and cards use shared theme values, while many screens still instantiate their own Cards, Buttons, TextFields and Icons.
6. Root-level UI still contained hardcoded design values. MainActivity had review-session padding values outside the token layer.
7. The existing static audit was too narrow. tools/theme_design_system_audit.py checked a small set of regressions and seven target files; it did not globally classify hardcoded colors, dimensions, icons and components across all active screens.

## Facts vs hypotheses

### Facts proven from source
- FlashLearnTheme creates MaterialTheme colorScheme, typography, shapes, LocalFlashLearnThemeTokens, and a themed LocalDensity.
- FlashLearnThemeTokens contains semantic colors, spacing, component sizes, corners, elevations, review-specific tokens and IconStyle.
- FlashLearnBackButton previously chose a concrete Material icon from Icons.AutoMirrored.Filled/Outlined based on iconStyle.
- FlashLearnShell previously received concrete outlined/filled ImageVector instances from the call site.
- AppRoutes.all() declares 14 active routes.
- The source tree also contains legacy/reference UI files, including LibraryScreen.kt, while MainActivity routes the active library destination to LibraryScreenV2.

### Hypotheses / needs confirmation
- Whether every decorative/content icon should vary by theme. This is intentionally not assumed; only semantic/navigation icons are candidates for the shared icon facade.
- Whether a future design variant should alter structural layout. This should be decided per component/variant, not inferred from color theme changes.

## Classification rule

| Item | Classification | Reason |
|---|---|---|
| Semantic color role | A — Theme Token | Visual language |
| Spacing / control size | A — Theme Token | Design-system scale |
| Shape / radius | A — Theme Token | Visual language |
| Border / elevation / alpha | A or B | Theme or component visual role |
| Semantic action/navigation icon family | A — Theme Token | Same action, different visual family |
| Component-specific style | B — Component Token | Component contract |
| Screen-specific spacing/size | C — Screen/Layout Token | Local composition when genuinely unique |
| Row vs Column / content order | D — Structural decision | Not automatically a Theme responsibility |
| Domain/data-driven geometry | D — Fixed | Not visual theme data |
| Brand asset that must not vary | D — Intentionally fixed | Avoid meaningless abstraction |

## Active screen inventory

AppRoutes.all() defines 14 active destinations:
1. Home
2. Review
3. Needs Review
4. Progress
5. Settings
6. Help
7. About
8. Add Word
9. Add Word Form
10. Bulk Import
11. Backup
12. Library
13. Library Detail
14. Category Selection

## Architecture correction implemented in this branch

### Semantic icon facade

A new FlashLearnIconSet is provided by the Theme and exposed through FlashLearnThemeTokens.icons.

It currently centralizes semantic icons where family switching is meaningful: Back, Add, Close, Search, Refresh, Settings, Home, Review, Library and Progress.

The active theme iconStyle selects Filled or Outlined variants where both are available. Decorative/category-specific icons are not forced through this abstraction.

### Root-level leakage fixed

The review-session chrome in MainActivity no longer owns raw 18.dp, 6.dp and 2.dp spacing; it consumes existing theme tokens.

### Shared shell fixed

Bottom navigation now consumes semantic themed icons rather than passing separate concrete icon families from the shell call site.

## Remaining refactor scope

The source still contains screen-local visual decisions that must be migrated before the project can truthfully claim zero unnecessary hardcoded design values. Examples include direct Icons.Outlined.* in screen implementations, raw dp values in screen-level modifiers, local RoundedCornerShape calls in older/legacy screen code, and direct Material component construction where a shared FlashLearn component could own the visual contract.

These are not marked as solved by this audit.

## Verification status

- Repository/source audit: completed
- Root cause identified from source: completed
- Semantic icon architecture: implemented on branch
- Global screen-by-screen refactor: in progress
- Local Gradle build from this environment: unavailable because this runtime cannot clone the GitHub repository directly.
- GitHub Actions verification: must be checked against the branch after refactor commits are pushed.

## Final acceptance criteria

1. All 14 active destinations use the shared design-system boundary.
2. Semantic action/navigation icons no longer depend on concrete icon families at screen call sites.
3. Remaining raw UI values are classified and justified.
4. Accessibility behavior is preserved.
5. Navigation/back behavior is unchanged.
6. The global static audit passes.
7. GitHub Actions build/tests/lint pass.
8. Final branch/commit state is explicitly recorded.