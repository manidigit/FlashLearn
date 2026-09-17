# FlashLearn — PROGRESS TRACKER

## v5.88 — Four-Part Functional Hardening + Global Theme Audit
- Completed the four-part hardening track: Library Refresh/Duplicate, Bulk Import, Restore Backup, and Global Theme Audit.
- Library Refresh and exact-duplicate cleanup are exposed in the active LibraryScreenV2 UI and remain connected to the existing ViewModel/use-case logic.
- Bulk Import duplicate detection, review classification, failure accounting, and theme-token usage were hardened without changing the established editor → parse → preview → import flow.
- Restore routing distinguishes legacy schema-1 VOCABULARY/FULL from typed schema-2 VOCABULARY/PROGRESS/FULL and preserves the authoritative FULL compatibility contract.
- Typed PROGRESS restore validates UUIDs, references, stages, review types, timestamps, and duplicate review attempts before mutation.
- Global theme audit confirmed FlashLearnTheme, FlashLearnThemeSpec, and LocalFlashLearnThemeTokens are the active theme foundation; active Library, Bulk Import, Settings, Progress, About, and Home surfaces use MaterialTheme/FlashLearn tokens rather than separate color palettes.
- BackupScreen was migrated from private hard-coded purple/green colors and RoundedCornerShape values to the shared FlashLearn theme tokens and MaterialTheme shapes, so custom themes and density/typography settings now apply consistently there too.
- Theme specifications retain light/dark primary, secondary, background, surface, card, outline, gradient, icon-style, elevation, corner, typography, and density controls.
- CI debug-signing continuity was hardened: the workflow no longer deletes and regenerates a different debug keystore on every run. It restores a stable cached key and also supports an optional `FL_DEBUG_KEYSTORE_B64` secret for an explicitly configured stable CI debug key.
- The instrumentation upgrade test now uses the same stable CI signing identity for the synthetic v5.87 install and the v5.88 `adb install -r`, preventing future CI APKs from silently changing signing identity between runs.
- Application identity remains `versionName = 5.88`, `versionCode = 88` for this completed four-part checkpoint.

## Current checkpoint: v5.88 — Four-Part Functional Hardening + Global Theme Audit
**Application identity:** `versionName = 5.88`, `versionCode = 88`

### Current implementation status
- Review sessions are capped at 30 eligible cards per session instead of opening an entire restored due queue (8k/100k cards) at once.
- Review queue selection is shuffled before taking the 30-card batch so cards do not follow the database/UUID ordering rhythm.
- Review queue joins concepts, learning states, difficulty states, and tags with bulk reads instead of per-card Room calls.
- Review language-pair validation loads candidate content in bulk and reuses it for the session.
- Quiz generation bulk-loads the quiz bank and caches it for the active process/session path.
- Quiz distractor selection expands beyond a small category when necessary, so a category with fewer than four distinct answers does not unnecessarily break a four-choice quiz.
- Missing DifficultyState still cannot silently switch an explicit Quiz session into Flashcards.
- Progress and Progress Summary bulk-load state tables and join in memory for large libraries.
- Library and Review content lookup use chunked bulk content queries designed to remain below SQLite bound-variable limits for large libraries.
- Legacy and typed backup restore paths are explicitly routed and validated before mutation.
- Library, Bulk Import, Backup, Progress, About, and Home UI surfaces use the shared FlashLearn theme foundation; Backup no longer owns a private palette.
- CI remains the authoritative build/test gate.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- The latest pushed run must complete Build/Unit and Instrumentation successfully before the checkpoint is considered fully verified.
- Release Gate is non-blocking when stable production signing secrets are absent; the debug APK/source artifacts remain the normal downloadable CI outputs.

---

## Historical checkpoints

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
