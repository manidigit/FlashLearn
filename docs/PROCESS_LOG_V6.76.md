# FlashLearn v6.76 — Issue 10 Process Log

## Problem
Settings displayed all available themes as a horizontally scrollable collection of fixed-width cards. The requested UX was a dropdown/select control.

## Root cause
The presentation layer iterated over `themes` directly inside a horizontally scrollable Row. The underlying state and persistence path were not the problem:
`SettingsScreen → onThemeChange → AppViewModel.setTheme → SharedPreferences → FlashLearnTheme`.

## Solution
- Replaced the horizontal theme-card list with one full-width Material dropdown.
- The closed control displays the selected theme and whether it is built-in/imported.
- Menu items show compact color previews and a check mark for the active theme.
- Selection closes the menu and calls the existing `onThemeChange(spec.id)` callback.
- Kept import/export unchanged.
- Added localized accessibility labels in Persian and English.
- Added `SettingsThemeDropdownTest` to exercise opening and changing the selector.

## Compatibility
- No Room schema or migration change.
- No Theme JSON format change.
- No learning/review algorithm change.
- No change to AppViewModel persistence semantics.

## Release hygiene
Version 6.75/675 → 6.76/676 in app/build.gradle.kts, CI env, FinalReleaseAuditContractTest and RuntimeGatePreflightTest. CHANGELOG, VERSION_LEDGER, docs/VERSION_LEDGER.md, PROGRESS.md and this process log updated.

## Verification
GitHub Actions is authoritative. This checkpoint remains unverified until the v6.76 Build + Unit Test and Instrumentation + Upgrade Gate run is green.
