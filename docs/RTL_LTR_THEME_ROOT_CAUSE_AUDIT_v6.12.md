# FlashLearn — RTL/LTR + Theme Root-Cause Audit v6.12

## Scope

Audited the current `main` source at the time of this checkpoint, not an older ZIP or historical version. The repository currently identifies the app as v6.12 / versionCode 112 after this change set.

## P0 — Global direction chain

| Stage | Source | Current value/behavior | RTL | LTR | Hard-coded | Override |
|---|---|---|---|---|---|---|
| Settings | `SettingsScreen.kt` | Emits `AppLayoutDirection.RTL/LTR` through `onLayoutDirectionChange` | selectable | selectable | No | No |
| Saved preference | `AppViewModel.kt` | `flashlearn_ui_settings/KEY_LAYOUT` | persisted | persisted | default is RTL | No |
| AppLayoutDirection | `AppUiState.kt` | enum `RTL/LTR` | explicit | explicit | enum only | No |
| Compose mapping | `AppUiState.kt` | `RTL -> LayoutDirection.Rtl`, `LTR -> LayoutDirection.Ltr` | correct | correct | centralized | No |
| Root provider | `MainActivity.kt` | `CompositionLocalProvider(LocalLayoutDirection provides appState.layoutDirection.toComposeLayoutDirection())` | dynamic | dynamic | No | single app root |
| Navigation | `MainActivity.kt` / `FlashLearnShell.kt` | screens rendered below root provider | inherits | inherits | No direction provider | No |
| Screens/components | audited target/shared sources | no remaining screen-local direction provider found in the audited current paths | inherits | inherits | No | no local direction provider |

### Home regression

Current `HomeScreen.kt` contains no `CompositionLocalProvider` and no hard-coded `LayoutDirection.Rtl/Ltr`. The previous fixed-RTL Home regression therefore remains removed.

## CompositionLocalProvider / LocalLayoutDirection

Current source inspection found the application direction provider at the root in `MainActivity.kt`. The theme's `CompositionLocalProvider` is limited to theme tokens and density in `FlashLearnTheme.kt`; it does not override layout direction.

The previous Review implementation used `LocalLayoutDirection.current` only to manually flip an ArrowBack glyph. That screen-local dependency has now been removed in favor of `Icons.AutoMirrored.Outlined.ArrowBack`.

## P1 — Shared/navigation root causes

The remaining class of defects was not the global state mapping. It was UI code treating physical edges as semantic leading edges:

- shared `ScreenHeader` used a literal `←`.
- several secondary-screen headers placed Back at `CenterEnd`, which puts it on the physical left in RTL.
- several Persian content blocks used `TextAlign.End` / `Alignment.End` as if they meant “right”; in RTL Compose End is the logical trailing side, not a fixed physical right edge.
- Review manually mirrored ArrowBack through `graphicsLayer` and `LocalLayoutDirection` instead of using the Compose AutoMirrored icon.

These are shared semantic-direction issues, not Review-engine issues.

## Fixes applied

### Add Word
- Back icon switched to AutoMirrored.
- Method screen back placement changed from logical End to logical Start.
- Method-card chevron changed from literal `‹` to AutoMirrored ChevronLeft.

### Bulk Import
- Existing AutoMirrored back icon retained.
- Semantic leading text and content columns changed from End to Start so the same source follows RTL/LTR.
- Multiline import text remains a normal Compose TextField; no forced LTR island was introduced.

### Backup Restore
- Back button moved from CenterEnd to CenterStart.
- Persian restore/result content changed from End alignment to logical Start.

### Library
- Main header back button moved from CenterEnd to CenterStart.
- Search/feedback text changed to logical Start.
- Category card text column changed to logical Start.
- Existing AutoMirrored navigation icons retained.

### Statistics / Progress
- Header literal arrow replaced with AutoMirrored ArrowBack.
- Existing chart drawing remains data/coordinate based rather than being blindly mirrored.
- Chart axis/data ordering was not rewritten because that would change the meaning of the data rather than solve layout direction.

### Review
- Manual `LocalLayoutDirection` + `graphicsLayer` arrow mirroring removed.
- Review setup and category navigation now use AutoMirrored ArrowBack.
- Existing Review logical `TextAlign.Start` corrections retained.
- Review engine, scheduling, quiz semantics, and persistence were not changed.

### Shared components / regression surfaces
- Shared `ScreenHeader` literal back arrow replaced with AutoMirrored ArrowBack.
- Settings back/row navigation glyphs changed to AutoMirrored icons.
- Help and Category Selection back navigation hardened.
- No screen-specific RTL `CompositionLocalProvider` workaround was introduced.

## Theme audit

`FlashLearnTheme.kt` remains the single Material theme entry point. It derives:

- `MaterialTheme.colorScheme`
- `MaterialTheme.typography`
- `MaterialTheme.shapes`
- `LocalFlashLearnThemeTokens`
- themed density

from the selected appearance, theme specification, and accent.

The target screens inspected use `MaterialTheme` and/or `LocalFlashLearnThemeTokens` rather than creating independent `MaterialTheme` scopes. No direction override was found inside the theme provider.

The hard-coded `Color(...)` values in the theme implementation are theme-definition values (accent/error/success/warning and built-in theme specifications), not screen-local colors. They were not mechanically replaced because doing so would change the theme contract rather than fix RTL/LTR.

## Logical vs physical audit

- `Alignment.Start/End`, `Arrangement.Start/End`, and `TextAlign.Start/End` were treated as logical values.
- Literal left/right alignment was not introduced as a replacement.
- Chart `Offset(x,y)` coordinates remain physical/data coordinates intentionally; the chart was not blindly mirrored.
- Directional navigation icons use AutoMirrored variants.
- Non-directional icons such as category, chart, settings, add, save, and refresh remain unchanged.

## Theme × direction regression

Added `DirectionThemeMatrixTest` under `androidTest`.

It exercises the root Compose contract in all four combinations:

1. RTL + Light
2. RTL + Dark
3. LTR + Light
4. LTR + Dark

The test verifies both the effective `LocalLayoutDirection` and the selected default theme background. It also renders mixed Persian/English/digit text (`کتاب Book ۱۰`) as a BiDi smoke probe.

The existing `AppLayoutDirectionTest` continues to assert:

- RTL → Compose RTL
- LTR → Compose LTR

## Version / CI

- `versionName`: 6.12
- `versionCode`: 112
- previous gate: 6.11 / 111
- runtime gate updated
- GitHub Actions version environment updated
- changelog/progress/version ledger updated

## Completion status

Source-level root causes identified and fixes applied.

**Not yet marked Complete:** authoritative v6.12 Build + Unit Test, Instrumentation/UI Test, Lint/static analysis, and GitHub Actions green status still need to be observed for the new commits. Source inspection is not being treated as CI or device verification.
