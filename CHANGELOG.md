# v5.12 — Release identity monotonicity
- Advanced the application release identity to v5.12.
- Increased Android versionCode from 1 to 12 so release upgrades have a monotonic package version code.
- Extended the runtime instrumentation gate to verify both versionName `5.12` and versionCode `12`.
- No learning algorithm, scheduling rule, database schema, or parser behavior changed in this checkpoint.
- GitHub Actions remains the authoritative full Android build/test verification step.

# v5.11 — Release identity and runtime gate hardening
- Advanced the application release identity to v5.11 after the v5.10 parser-metadata persistence checkpoint.
- Added an instrumentation regression gate that verifies the packaged Android application reports versionName `5.11` at runtime.
- Kept the existing package-name preflight assertion in the same gate.
- No learning algorithm, scheduling rule, database schema, or parser behavior changed in this checkpoint.
- GitHub Actions remains the authoritative full Android build/test verification step.

# v5.10 — Parser metadata persistence
- Persisted parser breakdown, relationship, variant, and confidence metadata alongside concepts.
- Added Room schema version 4 and migration 3→4.
- Added repository/DI wiring and full-backup restore support for parser metadata.
- Added focused mapping, domain, backup/restore, and schema-contract regression coverage.
