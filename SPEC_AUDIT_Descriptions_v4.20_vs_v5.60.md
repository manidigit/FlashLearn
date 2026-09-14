# FlashLearn — Descriptions v4.20 vs latest source audit

This file records the implementation-gap audit against `Descriptions_v4.20.docx`. It is a checklist only; it does not override the frozen specification.

## Critical release/install gaps
- [ ] Stable release signing key / upgrade path must be established. New APK must install over prior installed APK without uninstall.
- [ ] CI release artifact must be signed consistently; debug APK alone is not sufficient as a release artifact.
- [ ] VersionCode must monotonically increase and package/applicationId must remain stable.
- [ ] Add an instrumentation/release gate that verifies upgrade/install compatibility where feasible.

## Data versioning / refresh
- [ ] Concept must carry persistent `dataVersion`.
- [ ] Content must carry persistent `dataVersion`.
- [ ] RefreshDataUseCase must compare record dataVersion with current app/data version and run ordered idempotent `migrateToVersionX()` migrations.
- [ ] Refresh must never modify LearningState, DifficultyState, ReviewHistory or other learning data.
- [ ] Schema migration and content/data migration must remain separate.

## Backup / restore
- [ ] Full snapshot must include UUIDs and schemaVersion.
- [ ] Restore order must be: Languages -> Categories -> Tags -> LanguagePairs -> Concepts -> Contents -> ConceptTags -> ReviewSessions -> ReviewHistory -> LearningStates -> DifficultyStates -> Settings -> Achievements.
- [ ] Restore must be one transaction with full rollback on failure.
- [ ] Automatic backup of current device state before every restore.
- [ ] Vocabulary backup and Progress backup must be independently representable.
- [ ] Full backup must combine both.
- [ ] UUID conflict detection + merge/update policy.
- [ ] DifficultyState and hasReachedVeryHard must be preserved/restored.
- [ ] Backup encryption is future scope, not silently promoted to V1.

## Vocabulary / parser
- [ ] Manual add + bulk paste parser.
- [ ] Preserve-first parser: only exact duplicates may be automatically removed.
- [ ] Parenthetical content moves to notes only when definitely a note; meaningful gender/translation data must stay.
- [ ] Separator priority and script-mismatch protections.
- [ ] Numbering/bullet detection including Persian/Arabic numerals.
- [ ] Search across source, translation, example, notes and tags with LIKE/JOIN + pagination.
- [ ] Content uniqueness: `(conceptId, languageCode)`.
- [ ] Concept matching on `(languageCode, canonicalKey)`.
- [ ] `canonicalKey = trim + lowercase + whitespace collapse`; preserve accents and punctuation.
- [ ] Restore/merge inside a Concept: UUID first, then `(conceptId, languageCode)`; update rather than insert when text changed.

## Review engine
- [ ] Due rule: `nextReviewAt <= now`.
- [ ] Normal stages only DAILY/WEEKLY/MONTHLY; LEARNED excluded unless explicitly selected.
- [ ] Daily wrong answer returns next day, not immediately; max once/day per direction.
- [ ] Random Review is due DAILY/WEEKLY/MONTHLY only and shuffled.
- [ ] LEARNED review is a separate explicit mode, shuffled, with no stage/difficulty/schedule mutation.
- [ ] Queue ordering: nextReviewAt ASC then Concept.id ASC.
- [ ] Combined filters: Stage, Difficulty, Category, Tag, Language Pair.
- [ ] Flashcard and Quiz share SubmitReviewAnswerUseCase.
- [ ] Accepted answer creates exactly one append-only ReviewHistory.
- [ ] Duplicate `(sessionId, reviewAttemptId)` rejected; response button disabled after submit.
- [ ] Incomplete session may remain endedAt=null; exact queue-position resume is not required V1.
- [ ] Quiz always returns 4 unique options or FlashcardFallback.
- [ ] Quiz distractor selection follows the frozen contract; Quiz Difficulty is independent from Vocabulary Difficulty.
- [ ] Correct answer: no delay; wrong answer: red/green feedback + 2-second pause.
- [ ] Help/hint and Show Note in four-choice mode; notes are behind flashcard.

## Learning / difficulty
- [ ] LearningState and DifficultyState remain independent.
- [ ] Missing required state is DATA_INTEGRITY_ERROR; never synthesized.
- [ ] Difficulty EASY/MEDIUM/HARD/VERY_HARD.
- [ ] Stage DAILY/WEEKLY/MONTHLY/LEARNED; NEW is removed.
- [ ] Threshold default 3 and not user-editable in V1.
- [ ] Any difficulty change resets both consecutive counters.
- [ ] First MONTHLY failure -> HARD; later MONTHLY failures -> VERY_HARD.
- [ ] monthlyWrongCount cumulative and never reset.
- [ ] hasPathFailure becomes true only after WEEKLY/MONTHLY failure and is not reset by normal review.
- [ ] hasReachedVeryHard is historical and never returns false except explicit reset/delete.

## Statistics / progress / streak / achievements
- [ ] Statistics aggregate accepted ReviewHistory only; empty history is zero-safe.
- [ ] Metrics include total reviews, correct, wrong, accuracy, reviewed concept count.
- [ ] Progress aggregates canonical LearningState by all four stages.
- [ ] Progress includes path-failure and hasReachedVeryHard counts.
- [ ] Progress percentage uses stage-based weighted score, not learned/total only.
- [ ] Streak counts local calendar days with >=1 accepted response; multiple reviews same day count as one.
- [ ] Achievement evaluation isolated from UI; no invented thresholds.
- [ ] UI remains compact while retaining required statistics information.

## UI / UX
- [ ] Light + Dark theme.
- [ ] Compact cards; avoid oversized/tall/wide layouts.
- [ ] No duplicate flags, welcome text, plus signs, repeated waiting-word text, or unnecessary two-line cards.
- [ ] No hardcoded UI numbers; values come from UiState.
- [ ] Vocabulary screen includes Refresh.
- [ ] Settings supports actual language pair selection, direction/swap, difficulty and quiz challenge as specified by current executable product decisions.
- [ ] Navigation must not duplicate the same destination in both hamburger and bottom navigation without a clear purpose.
- [ ] Home hamburger should contain unique utility actions, not copies of bottom-nav destinations.
- [ ] Back navigation must navigate back rather than unexpectedly exiting except at Home/root.
- [ ] About page must exist and work.

## Architecture / quality
- [ ] Clean Architecture + MVVM + Repository + Compose/Material3 + Room/SQLite + Flow/Coroutines + Hilt.
- [ ] ViewModels orchestrate only; business logic remains in UseCases/domain.
- [ ] UI never accesses DAO/database directly.
- [ ] Single Room instance.
- [ ] DB operations on IO/appropriate dispatcher.
- [ ] Pagination/Lazy loading for large lists; target ~100k records.
- [ ] Performance targets from specification must be validated where measurable.
- [ ] Required unit/instrumentation coverage: transitions, scheduler/queue, parser, import/export, refresh/migration, transactions, duplicates, edge cases.

## Explicit out-of-scope / must NOT be reintroduced as V1
- [ ] Notifications/reminders.
- [ ] NEW stage.
- [ ] Anki/SM-2 scheduler.
- [ ] AI/cloud translation dependency.
- [ ] User-editable threshold in V1.
- [ ] Multi-active language pairs in V1.

## Audit rule
Every checked item above must be verified against the actual latest source, tests, and runtime behavior. A UI label or placeholder is not counted as implemented functionality. Any new feature must update the master specification before being treated as a frozen contract.
