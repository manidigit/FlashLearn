# FlashLearn v6.79 — Backup export format removal process log

## Scope
- Removed CSV as a backup/data-export format from the executable code path.
- Removed SQLite as a backup/data-export format from the executable code path.
- Removed both options from the Backup UI.
- Kept Room/SQLite as the internal application database; this change does not alter database storage or Room migrations.

## Source contract
- ExportFormat now exposes only JSON and XLSX.
- DataExportRepositoryImpl implements only JSON and XLSX export.
- BackupScreen renders only JSON and XLSX export actions.
- Added BackupExportFormatContractTest to prevent CSV/SQLite export formats from returning.

## Compatibility boundary
Existing JSON backup/restore contracts remain unchanged. This release removes CSV/SQLite export capabilities; it does not remove the application's internal SQLite database engine.

## Release identity
- Application version: 6.79
- Version code: 679
- Previous-version gate: 6.78 / 678

## Verification rule
GitHub Actions remains authoritative. The release is not considered verified until Build + Unit Test and Instrumentation + Upgrade Gate pass for v6.79.
