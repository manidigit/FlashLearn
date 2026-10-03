import { DifficultyState, LearningState, Stage, VocabularyDifficulty } from '../types';

export interface TransitionResult {
  newStage: Stage;
  nextReviewAt: string | null;
  hasPathFailure: boolean;
  monthlyWrongCount: number;
}

/**
 * Calculates spaced repetition stage transition based on answer correctness.
 * Faithful port of com.flashlearn.domain.algorithm.calculateLearningTransition
 */
export function calculateLearningTransition(
  learningState: LearningState,
  isCorrect: Boolean,
  reviewedAt: Date = new Date()
): TransitionResult {
  const current = learningState.stage;
  const wrong = learningState.monthlyWrongCount;
  const failure = learningState.hasPathFailure;

  if (current === 'LEARNED') {
    return {
      newStage: 'LEARNED',
      nextReviewAt: null,
      hasPathFailure: failure,
      monthlyWrongCount: wrong,
    };
  }

  // Next calendar day start
  const nextDay = new Date(reviewedAt);
  nextDay.setDate(nextDay.getDate() + 1);
  nextDay.setHours(0, 0, 0, 0);

  if (isCorrect) {
    switch (current) {
      case 'DAILY': {
        const nextDate = new Date(reviewedAt);
        nextDate.setDate(nextDate.getDate() + 7);
        nextDate.setHours(0, 0, 0, 0);
        return {
          newStage: 'WEEKLY',
          nextReviewAt: nextDate.toISOString(),
          hasPathFailure: failure,
          monthlyWrongCount: wrong,
        };
      }
      case 'WEEKLY': {
        const nextDate = new Date(reviewedAt);
        nextDate.setDate(nextDate.getDate() + 30);
        nextDate.setHours(0, 0, 0, 0);
        return {
          newStage: 'MONTHLY',
          nextReviewAt: nextDate.toISOString(),
          hasPathFailure: failure,
          monthlyWrongCount: wrong,
        };
      }
      case 'MONTHLY':
        return {
          newStage: 'LEARNED',
          nextReviewAt: null,
          hasPathFailure: failure,
          monthlyWrongCount: wrong,
        };
      default:
        return {
          newStage: 'LEARNED',
          nextReviewAt: null,
          hasPathFailure: failure,
          monthlyWrongCount: wrong,
        };
    }
  } else {
    // Incorrect answer returns non-learned concepts to Daily (reviewed tomorrow)
    return {
      newStage: 'DAILY',
      nextReviewAt: nextDay.toISOString(),
      hasPathFailure: failure || current === 'MONTHLY',
      monthlyWrongCount: current === 'MONTHLY' ? wrong + 1 : wrong,
    };
  }
}

/**
 * Calculates vocabulary difficulty progression based on consecutive answers.
 * Faithful port of com.flashlearn.domain.algorithm.calculateDifficulty
 */
export function calculateDifficulty(
  state: DifficultyState,
  isCorrect: boolean,
  threshold: number = 3
): DifficultyState {
  const safeThreshold = Math.max(1, threshold);
  let level = state.current;
  let cc = state.consecutiveCorrect;
  let cw = state.consecutiveWrong;

  if (isCorrect) {
    cw = 0;
    const n = cc + 1;
    if (n >= safeThreshold) {
      level = makeEasier(level);
      cc = 0;
    } else {
      cc = n;
    }
  } else {
    cc = 0;
    const n = cw + 1;
    if (n >= safeThreshold) {
      level = makeHarder(level);
      cw = 0;
    } else {
      cw = n;
    }
  }

  const reached = state.hasReachedVeryHard || level === 'VERY_HARD';
  return {
    ...state,
    current: level,
    consecutiveCorrect: cc,
    consecutiveWrong: cw,
    hasReachedVeryHard: reached,
  };
}

function makeEasier(d: VocabularyDifficulty): VocabularyDifficulty {
  switch (d) {
    case 'VERY_HARD':
      return 'HARD';
    case 'HARD':
      return 'MEDIUM';
    case 'MEDIUM':
      return 'EASY';
    case 'EASY':
      return 'EASY';
  }
}

function makeHarder(d: VocabularyDifficulty): VocabularyDifficulty {
  switch (d) {
    case 'EASY':
      return 'MEDIUM';
    case 'MEDIUM':
      return 'HARD';
    case 'HARD':
      return 'VERY_HARD';
    case 'VERY_HARD':
      return 'VERY_HARD';
  }
}

/**
 * Checks if a concept has already been reviewed today on the current local calendar day.
 * As defined in README: "A concept reviewed once on the current local calendar day is excluded from subsequent review that day."
 */
export function isReviewedToday(lastReviewedAt: string | null): boolean {
  if (!lastReviewedAt) return false;
  const reviewed = new Date(lastReviewedAt);
  const now = new Date();
  return (
    reviewed.getFullYear() === now.getFullYear() &&
    reviewed.getMonth() === now.getMonth() &&
    reviewed.getDate() === now.getDate()
  );
}

/**
 * Checks if an item is currently due for review.
 */
export function isDueForReview(state: LearningState): boolean {
  if (state.stage === 'LEARNED') return false;
  if (isReviewedToday(state.lastReviewedAt)) return false;
  if (!state.nextReviewAt) return true;
  return new Date(state.nextReviewAt).getTime() <= Date.now();
}
