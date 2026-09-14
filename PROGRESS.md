# FlashLearn — PROGRESS TRACKER

## Current checkpoint: v5.73 — Large-library review/performance hardening
**Application identity:** `versionName = 5.73`, `versionCode = 73`

### Current implementation status
- Review sessions are capped at 30 eligible cards per session instead of opening an entire restored due queue (8k/100k cards) at once.
- Review queue selection is shuffled before taking the 30-card batch so cards do not follow the database/UUID ordering rhythm.
- Review queue joins concepts, learning states, difficulty states, and tags with bulk reads instead of per-card Room calls.
- Review language-pair validation loads candidate content in bulk and reuses it for the session.
- Quiz generation now bulk-loads the quiz bank and caches it for the active process/session path instead of reading the full contents table and difficulty rows for every question.
- Quiz distractor selection expands beyond a small category when necessary, so a category with fewer than four distinct answers does not unnecessarily break a four-choice quiz.
- Missing DifficultyState still cannot silently switch an explicit Quiz session into Flashcards.
- Progress and Progress Summary no longer query LearningState/DifficultyState once per concept; they bulk-load state tables and join in memory for large libraries.
- Library and Review content lookup already use chunked bulk content queries designed to remain below SQLite bound-variable limits for large libraries.
- Vocabulary and legacy FULL restore remain on `Dispatchers.IO` with batch Room writes; post-restore Progress/Statistics calculations now avoid the previous N+1 state queries.
- CI is aligned to v5.73 and keeps the deterministic CI debug signing key for in-place update smoke testing.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- The latest v5.73 run must complete Build/Unit and Instrumentation successfully before the performance fixes are marked fully verified.
- Release Gate is non-blocking when stable production signing secrets are absent; the debug APK/source artifacts remain the normal downloadable CI outputs.

---

## Historical checkpoints

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
