# Process Log — v6.84 Quiz Freeze Fix

## Problem
After choosing the question count for the Quiz review, the loading screen never finished.

## Root cause
v6.83 `GenerateQuizQuestionUseCase.refreshBank()` built distractor candidates for every active concept against every other active concept per language, computing Levenshtein/token/bigram similarity and compiling regexes on each comparison (O(N^2) with heavy constants). The ViewModel calls refreshBank() before the first card is shown, so the UI waited on it.

## Fix (domain/usecase/QuizUseCases.kt only)
- refreshBank(): stores contents-by-concept, active concepts and difficulty states only.
- QuizBank.entriesFor(language): lazy per-language list, built once, thread-safe cache.
- QuizTextProfile: normalized text, tokens and bigrams computed once per entry.
- Regexes precompiled at file level.
- DistractorCandidate.lexicalSimilarity is lazy.
- Removed unused full scans (eligible concept sets) executed per question.
- Effective vocabulary difficulty uses the passed DifficultyState first, then the bank value, then MEDIUM.

## Unchanged
Selection tiers, Quiz Difficulty policy, canonical-key guard, freshness exclusion, ViewModel flow, Room schema, backup format.

## Tests
- Added `refreshBankStaysFastOnLargeVocabularyAndStillBuildsQuestions` (3000 words).
- Version contract tests and CI gate moved to 6.84 / 684.
- Gradle could not be run in the authoring environment; GitHub Actions is authoritative.
