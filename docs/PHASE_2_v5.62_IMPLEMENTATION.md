# FlashLearn v5.62 — Phase 2: Backup / Restore + Transfer Audit

- Release identity advanced to 5.62/62.
- Existing FULL JSON backup/restore was re-audited against Descriptions v4.20.
- FULL backup already preserves the current persisted entities, UUIDs, schemaVersion, DifficultyState including hasReachedVeryHard, parser metadata and achievements.
- FULL restore remains transactional and non-destructive, with UUID-first Content merge and `(conceptId, languageCode)` fallback, canonicalKey recomputation, duplicate/reference validation and automatic pre-restore snapshot.
- The current Room schema does not persist separate Language or LanguagePair entities, so those are not fabricated into Backup/Restore; real language-pair persistence remains part of Phase 3.
- The current executable app exposes FULL JSON backup/restore. The v4.20 document also lists CSV/JSON/XLSX/SQLite transfer formats; these remain a tracked implementation gap and are not falsely marked complete.
- Backup encryption remains future scope.
