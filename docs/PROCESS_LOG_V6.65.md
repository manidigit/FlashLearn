# FlashLearn v6.65 — Process Log

## Scope
Clarify the two utility actions on the Add Word method screen.

## Root cause
The action labelled "Find duplicates" actually calls `libraryViewModel.removeExactDuplicates()`, which performs a removal operation. "Refresh" calls `libraryViewModel.refresh()` and refreshes the library state/counts.

## Change
- Renamed "Find duplicates" to "Remove duplicates" / "حذف واژه‌های تکراری".
- Renamed "Refresh" to "Refresh word count" / "به‌روزرسانی تعداد واژه‌ها".
- No behavior was changed; labels now describe the existing actions accurately.

## Release
- Version: 6.65
- versionCode: 665
- Previous-version upgrade gate: 6.64 / 664

## Verification
Pending GitHub Actions. This checkpoint is complete only when Build + Unit Test and Instrumentation + Upgrade Gate are green.
