# v5.13 — Achievements domain engine
- Activated the product achievement catalog defined in the project backlog.
- Added deterministic rules for seven achievements: FIRST_TEN_WORDS, SEVEN_DAY_STREAK, THIRTY_DAY_STREAK, MEMORY_BUILDER, VOCABULARY_BUILDER, HARD_MODE_MASTER, and LONG_TERM_MEMORY.
- Existing unlocked achievements remain unlocked and only newly unlocked IDs are emitted by evaluation.
- Added threshold and streak-boundary regression coverage.
- This checkpoint builds the domain engine first; persistence and UI wiring remain separate follow-up work.

# v5.12 — Release identity monotonicity
- Advanced the application release identity to v5.12.
- Increased Android versionCode from 1 to 12 so release upgrades have a monotonic package version code.
- Extended the runtime instrumentation gate to verify both versionName `5.12` and versionCode `12`.
- No learning algorithm, scheduling rule, database schema, or parser behavior changed in this checkpoint.
- GitHub Actions remains the authoritative full Android build/test verification step.
