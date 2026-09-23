# Phase 1 Implementation — v5.61

Implemented in this source snapshot:

1. Version advanced to `5.61` / `versionCode 61`.
2. Release signing is no longer an implicit debug/default configuration. The release build reads the stable signing keystore and credentials from `FL_RELEASE_STORE_FILE`, `FL_RELEASE_STORE_PASSWORD`, `FL_RELEASE_KEY_ALIAS`, and `FL_RELEASE_KEY_PASSWORD`.
3. Added `DataVersionRepository` as a contract separate from Room schema versioning.
4. Added persistent Concept and Content data-version markers in the existing settings table.
5. Added `RefreshDataUseCase` with explicit version migration functions.
6. Content v1 migration repairs persisted canonical keys using the same canonicalization contract used by current data paths.
7. Application startup runs the refresh migration on `Dispatchers.IO` in a supervised coroutine.

Important release constraint: an upgrade can only be installed over an existing APK if the release is signed with the same signing key as that APK. The source can enforce the signing configuration, but it cannot reconstruct an unavailable historical keystore.
