# FlashLearn v6.77 — Issue 11 Process Log

## Problem
The creator contact requirement called for the creator WhatsApp number `+34 685 644 444` to be available from the application.

## Root cause
The About screen exposed the creator name and technical metadata, but no creator WhatsApp contact or action existed in the current source.

## Solution
- Added a localized creator WhatsApp label and number to the About screen.
- Added `APP_CREATOR_WHATSAPP_URL` to BuildConfig using the normalized `wa.me` endpoint.
- The contact is clickable and opens the existing Android external-intent path without adding a new dependency.
- Added a release-contract test covering the URI and both localized string catalogs.
- Advanced release identity to v6.77 / versionCode 677 and CI previous-version gate to v6.76 / 676.

## Compatibility
- No Room schema or migration change.
- No learning/review algorithm change.
- No review scheduling change.
- No import/export or backup format change.
- No theme JSON or persistence change.

## Verification
The first v6.77 CI run failed in `:app:testDebugUnitTest`, not in APK compilation. Root cause: the newly added release-contract test still asserted v6.76 and used repository-root paths even though the test executes with the app module as its working directory. Both assertions were corrected to v6.77/677 and module-relative paths. The corrected commit is now pushed and requires a fresh authoritative CI run.
