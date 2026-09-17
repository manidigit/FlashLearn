# FlashLearn — PROGRESS TRACKER

## v5.89 — Supplied FULL Backup Restore Alignment
- Advanced the application identity to `versionName = 5.89`, `versionCode = 89`; v5.88 is retained as the previous checkpoint and is not overwritten.
- Aligned the active FULL restore implementation with the supplied schema-2 FULL backup shape and its real category → concept relationships.
- FULL restore now restores parent categories before concepts that reference those categories, preventing foreign-key failures when importing into an empty Room database.
- Kept the strict historical v5.74 partial schema-2 FULL compatibility rule exact, so arbitrary missing-section backups are not silently accepted as historical backups.
- Kept `RANDOM` review-session/history support and regression coverage.
- Added regression coverage for the supplied backup shape, including category/concept relationship ordering and RANDOM review data.
- CI version/upgrade verification is aligned to `5.88 → 5.89` and versionCode `88 → 89` using the stable debug signing identity.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- The latest v5.89 run must complete Build/Unit and Instrumentation successfully before v5.89 is considered fully verified.
- Release Gate remains non-blocking when stable production signing secrets are absent; debug APK/source artifacts remain the normal CI outputs.

## Previous checkpoint: v5.88 — Four-Part Functional Hardening + Global Theme Audit
- Completed the four-part hardening track: Library Refresh/Duplicate, Bulk Import, Restore Backup, and Global Theme Audit.
- Library Refresh and exact-duplicate cleanup are exposed in the active LibraryScreenV2 UI and remain connected to the existing ViewModel/use-case logic.
- Bulk Import duplicate detection, review classification, failure accounting, and theme-token usage were hardened without changing the established editor → parse → preview → import flow.
- Restore routing distinguishes legacy schema-1 VOCABULARY/FULL from typed schema-2 VOCABULARY/PROGRESS/FULL and preserves the authoritative FULL compatibility contract.
- Typed PROGRESS restore validates UUIDs, references, stages, review types, timestamps, and duplicate review attempts before mutation.
- Global theme audit confirmed FlashLearnTheme, FlashLearnThemeSpec, and LocalFlashLearnThemeTokens as the active theme foundation.
- BackupScreen was migrated from private hard-coded colors/shapes to shared FlashLearn theme tokens and MaterialTheme shapes.
- CI debug-signing continuity was hardened so the workflow can reuse a stable cached debug keystore or an explicitly configured stable key.
- The v5.87 → v5.88 instrumentation upgrade check used the same stable CI signing identity.
- Application identity was `versionName = 5.88`, `versionCode = 88` for that checkpoint.

## Historical checkpoints

v5.89 — Supplied FULL backup restore alignment; category-parent ordering; supplied-shape regression coverage; CI aligned to 5.88 → 5.89.
v5.88 — Four-part functional hardening + Global Theme Audit.
v5.87 — Previous-Version FULL Backup Compatibility Hardening.
v5.86 — Backup FULL export/restore fix.
v5.85 — Previous checkpoint before Backup FULL restore fix.
v5.74 — Review Help + About + Library category selection.
v5.73 — Large-library review/performance + Quiz UX hardening.
v5.72 — previous performance checkpoint before 30-card review batching and bulk Progress/Statistics joins.
v5.71 — Legacy restore + Quiz mode + update-path hardening.
v5.70 — legacy FULL restore compatibility and update verification path.
v5.69 — dedicated legacy vocabulary restore, launcher icon binding, and CI alignment.
v5.68 — Phase 5 full verification checkpoint.
v5.66 — final specification reconciliation.
v5.52 — Progress/Statistics + E2E audit.
v5.50 — UI/Navigation audit.
v5.48 — restore regression hardening.
v5.46 — UUID-based non-destructive restore merge contract.
v5.45 — Android CI compile hardening.
v5.42 — pre-restore automatic backup hardening.
v5.41 — restore validation hardening.

> Older tracker material is intentionally retained as history; percentages from older trackers are not treated as current status.