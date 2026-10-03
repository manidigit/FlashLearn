export type Stage = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'LEARNED';
export type VocabularyDifficulty = 'EASY' | 'MEDIUM' | 'HARD' | 'VERY_HARD';
export type QuizDifficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type ReviewType = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'LEARNED' | 'RANDOM';
export type EntryType = 'WORD' | 'PHRASE' | 'SENTENCE' | 'IDIOM' | 'COLLOCATION' | 'STRUCTURE';
export type ImportMode = 'ADD_NEW' | 'SKIP_DUPLICATE' | 'MERGE' | 'UPDATE';
export type VocabularyRelationType =
  | 'USED_IN'
  | 'DERIVED_FROM'
  | 'INFLECTED_FORM'
  | 'SYNONYM'
  | 'ANTONYM'
  | 'CONTRAST'
  | 'EXAMPLE_OF'
  | 'RELATED_TO';
export type VocabularyVariantType = 'MASCULINE' | 'FEMININE' | 'ALTERNATIVE';
export type ReviewQueueStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Tag {
  id: string;
  name: string;
}

export interface LearningState {
  id: string;
  conceptId: string;
  stage: Stage;
  nextReviewAt: string | null; // ISO string
  monthlyWrongCount: number;
  hasPathFailure: boolean;
  totalCorrect: number;
  totalWrong: number;
  lastReviewedAt: string | null; // ISO string
}

export interface DifficultyState {
  id: string;
  conceptId: string;
  current: VocabularyDifficulty;
  consecutiveCorrect: number;
  consecutiveWrong: number;
  hasReachedVeryHard: boolean;
}

export interface Content {
  id: string;
  conceptId: string;
  languageCode: string; // 'es' or 'fa'
  text: string;
  canonicalKey: string;
  notes?: string | null;
  pronunciation?: string | null;
  example?: string | null;
  translationIndex?: number;
  grammarNote?: string | null;
  possibleCorrection?: string | null;
}

export interface Category {
  id: string;
  name: string;
}

export interface Concept {
  id: string;
  entryType: EntryType;
  categoryId?: string | null;
  favorite: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface VocabularyRelation {
  id: string;
  sourceConceptId: string;
  targetConceptId?: string | null;
  relationType: VocabularyRelationType;
  unresolvedText?: string | null;
}

export interface VocabularyVariant {
  id: string;
  conceptId: string;
  text: string;
  variantType: VocabularyVariantType;
}

export interface ReviewQueueItem {
  id: string;
  conceptId?: string | null;
  sourceText: string;
  targetText?: string | null;
  confidence: number;
  possibleCorrection?: string | null;
  status: ReviewQueueStatus;
  lineNumber?: number | null;
  warning?: string | null;
}

export interface Language {
  code: string;
  name: string;
  flag: string;
  active: boolean;
}

export interface LanguagePair {
  sourceLanguageCode: string;
  targetLanguageCode: string;
  active: boolean;
}

export interface ReviewSession {
  id: string;
  startedAt: string;
  endedAt?: string | null;
  reviewType: ReviewType;
  totalReviewed: number;
  totalCorrect: number;
  totalWrong: number;
}

export interface ReviewHistory {
  id: string;
  sessionId: string;
  conceptId: string;
  reviewedAt: string;
  isCorrect: boolean;
  reviewType: ReviewType;
}

export interface ProgressSummary {
  activeConceptCount: number;
  learnedConceptCount: number;
  dueConceptCount: number;
  dailyDueConceptCount: number;
  weeklyDueConceptCount: number;
  monthlyDueConceptCount: number;
  totalCorrect: number;
  totalWrong: number;
  accuracyPercent: number;
}

export interface StreakInfo {
  currentStreakDays: number;
  bestStreakDays: number;
  lastActiveDate: string | null; // YYYY-MM-DD
}

export interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  isUnlocked: boolean;
  progress: number; // 0 to 100
  unlockedAt?: string | null;
}

export interface FullVocabularyItem {
  concept: Concept;
  sourceContent: Content;
  targetContent: Content;
  category?: Category | null;
  learningState: LearningState;
  difficultyState: DifficultyState;
  tags: Tag[];
  relations: VocabularyRelation[];
  variants: VocabularyVariant[];
}

export interface FullBackupPayload {
  schemaVersion: number;
  exportedAt: string;
  backupType: 'FULL' | 'VOCABULARY' | 'PROGRESS';
  concepts: Concept[];
  contents: Content[];
  tags: Tag[];
  categories: Category[];
  relations: VocabularyRelation[];
  variants: VocabularyVariant[];
  languages: Language[];
  languagePairs: LanguagePair[];
  learningStates: LearningState[];
  difficultyStates: DifficultyState[];
  conceptTags: { conceptId: string; tagId: string }[];
  reviewSessions: ReviewSession[];
  reviewHistory: ReviewHistory[];
  settings?: Record<string, unknown>[];
  achievements?: Achievement[];
  parserMetadata?: unknown[];
}
