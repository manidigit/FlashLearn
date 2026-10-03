import {
  Category,
  Concept,
  Content,
  DifficultyState,
  FullBackupPayload,
  FullVocabularyItem,
  Language,
  LanguagePair,
  LearningState,
  ProgressSummary,
  ReviewHistory,
  ReviewQueueItem,
  ReviewSession,
  StreakInfo,
  Tag,
  VocabularyRelation,
  VocabularyVariant,
} from '../types';
import { INITIAL_ACHIEVEMENTS } from '../domain/achievements';
import { isDueForReview } from '../domain/algorithms';

const STORAGE_KEY = 'flashlearn_db_v2';
const STREAK_KEY = 'flashlearn_streak_v2';

export interface DatabaseState {
  concepts: Concept[];
  contents: Content[];
  categories: Category[];
  tags: Tag[];
  conceptTags: { conceptId: string; tagId: string }[];
  learningStates: LearningState[];
  difficultyStates: DifficultyState[];
  relations: VocabularyRelation[];
  variants: VocabularyVariant[];
  reviewQueue: ReviewQueueItem[];
  reviewSessions: ReviewSession[];
  reviewHistory: ReviewHistory[];
  languages: Language[];
  languagePairs: LanguagePair[];
}

const DEFAULT_CATEGORIES: Category[] = [
  { id: 'cat-general', name: 'واژگان عمومی' },
  { id: 'cat-greetings', name: 'احوال‌پرسی و روزمره' },
  { id: 'cat-travel', name: 'سفر و گردشگری' },
  { id: 'cat-food', name: 'غذا و نوشیدنی' },
  { id: 'cat-study', name: 'آموزش و یادگیری' },
];

const DEFAULT_LANGUAGES: Language[] = [
  { code: 'es', name: 'اسپانیایی (Español)', flag: '🇪🇸', active: true },
  { code: 'fa', name: 'فارسی (Persian)', flag: '🇮🇷', active: true },
  { code: 'en', name: 'انگلیسی (English)', flag: '🇬🇧', active: false },
];

const DEFAULT_PAIRS: LanguagePair[] = [
  { sourceLanguageCode: 'es', targetLanguageCode: 'fa', active: true },
];

interface SeedWord {
  es: string;
  fa: string;
  catId: string;
  exampleEs: string;
  exampleFa: string;
  notes?: string;
  grammarNote?: string;
  stage: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'LEARNED';
  difficulty: 'EASY' | 'MEDIUM' | 'HARD' | 'VERY_HARD';
  pronunciation?: string;
}

const SEED_WORDS: SeedWord[] = [
  {
    es: 'hola',
    fa: 'سلام',
    catId: 'cat-greetings',
    exampleEs: '¡Hola! ¿Cómo estás hoy?',
    exampleFa: 'سلام! امروز چطوری؟',
    notes: 'رایج‌ترین سلام در زبان اسپانیایی',
    grammarNote: 'حرف h در زبان اسپانیایی خوانده نمی‌شود.',
    stage: 'DAILY',
    difficulty: 'EASY',
    pronunciation: 'ola',
  },
  {
    es: 'gracias',
    fa: 'ممنون / متشکرم',
    catId: 'cat-greetings',
    exampleEs: 'Muchas gracias por tu ayuda.',
    exampleFa: 'خیلی ممنون از کمکت.',
    notes: 'عبارت muchas gracias به معنی خیلی ممنون است',
    stage: 'DAILY',
    difficulty: 'EASY',
    pronunciation: 'grasjas',
  },
  {
    es: 'casa',
    fa: 'خانه',
    catId: 'cat-general',
    exampleEs: 'Mi casa es acogedora y luminosa.',
    exampleFa: 'خانه من دنج و پرنور است.',
    notes: 'اسم مؤنث: la casa',
    stage: 'DAILY',
    difficulty: 'EASY',
    pronunciation: 'kasa',
  },
  {
    es: 'por favor',
    fa: 'لطفاً',
    catId: 'cat-greetings',
    exampleEs: 'Un café solo, por favor.',
    exampleFa: 'یک قهوه تلخ لطفاً.',
    stage: 'DAILY',
    difficulty: 'EASY',
  },
  {
    es: 'aprender',
    fa: 'یاد گرفتن / آموختن',
    catId: 'cat-study',
    exampleEs: 'Quiero aprender español todos los días.',
    exampleFa: 'می‌خواهم هر روز اسپانیایی یاد بگیرم.',
    grammarNote: 'فعل باقاعده با پسوند -er',
    stage: 'WEEKLY',
    difficulty: 'MEDIUM',
  },
  {
    es: 'amigo',
    fa: 'دوست',
    catId: 'cat-general',
    exampleEs: 'Carlos es un buen amigo mío.',
    exampleFa: 'کارلوس یک دوست خوب من است.',
    notes: 'مؤنث آن amiga است',
    stage: 'WEEKLY',
    difficulty: 'EASY',
  },
  {
    es: 'viaje',
    fa: 'سفر',
    catId: 'cat-travel',
    exampleEs: '¡Buen viaje a Barcelona!',
    exampleFa: 'سفرت به بارسلونا به‌خیر!',
    notes: 'اسم مذکر: el viaje',
    stage: 'WEEKLY',
    difficulty: 'MEDIUM',
  },
  {
    es: 'comida',
    fa: 'غذا / ناهار',
    catId: 'cat-food',
    exampleEs: 'La comida tradicional española es deliciosa.',
    exampleFa: 'غذای سنتی اسپانیایی لذیذ است.',
    stage: 'MONTHLY',
    difficulty: 'EASY',
  },
  {
    es: 'agua',
    fa: 'آب',
    catId: 'cat-food',
    exampleEs: 'Bebo un vaso de agua fresca.',
    exampleFa: 'یک لیوان آب خنک می‌نوشم.',
    grammarNote: 'حرف تعریف مفرد آن el agua است به دلیل شروع با آوای a تکیه‌دار.',
    stage: 'MONTHLY',
    difficulty: 'HARD',
  },
  {
    es: 'tiempo',
    fa: 'زمان / آب‌وهوا',
    catId: 'cat-general',
    exampleEs: '¿Qué tiempo hace hoy en Madrid?',
    exampleFa: 'امروز هوای مادرید چطور است؟',
    notes: 'هم به معنی وقت و هم به معنی آب‌وهوا به کار می‌رود',
    stage: 'LEARNED',
    difficulty: 'MEDIUM',
  },
  {
    es: 'desarrollar',
    fa: 'توسعه دادن / پرورش دادن',
    catId: 'cat-study',
    exampleEs: 'Es importante desarrollar nuevas habilidades.',
    exampleFa: 'پرورش دادن مهارت‌های جدید مهم است.',
    grammarNote: 'فعل باقاعده با پسوند -ar',
    stage: 'DAILY',
    difficulty: 'HARD',
  },
  {
    es: 'maravilloso',
    fa: 'فوق‌العاده / شگفت‌انگیز',
    catId: 'cat-general',
    exampleEs: 'Tuvimos una experiencia maravillosa.',
    exampleFa: 'تجربه فوق‌العاده‌ای داشتیم.',
    notes: 'مؤنث: maravillosa',
    stage: 'DAILY',
    difficulty: 'MEDIUM',
  },
];

function generateSeedState(): DatabaseState {
  const concepts: Concept[] = [];
  const contents: Content[] = [];
  const learningStates: LearningState[] = [];
  const difficultyStates: DifficultyState[] = [];

  const now = new Date();
  const past = new Date(now.getTime() - 24 * 3600 * 1000);

  SEED_WORDS.forEach((w, index) => {
    const conceptId = `concept-${index + 1}`;
    concepts.push({
      id: conceptId,
      entryType: 'WORD',
      categoryId: w.catId,
      favorite: index === 0 || index === 2,
      active: true,
      createdAt: past.toISOString(),
      updatedAt: past.toISOString(),
    });

    contents.push({
      id: `content-es-${index + 1}`,
      conceptId,
      languageCode: 'es',
      text: w.es,
      canonicalKey: w.es.toLowerCase().trim(),
      notes: w.notes || null,
      pronunciation: w.pronunciation || null,
      example: w.exampleEs,
      translationIndex: 0,
      grammarNote: w.grammarNote || null,
      possibleCorrection: null,
    });

    contents.push({
      id: `content-fa-${index + 1}`,
      conceptId,
      languageCode: 'fa',
      text: w.fa,
      canonicalKey: w.fa.trim(),
      notes: null,
      pronunciation: null,
      example: w.exampleFa,
      translationIndex: 0,
      grammarNote: null,
      possibleCorrection: null,
    });

    // Make DAILY words due immediately, WEEKLY words some due, etc.
    const isDue = w.stage === 'DAILY' || index % 3 === 0;
    learningStates.push({
      id: `learn-${index + 1}`,
      conceptId,
      stage: w.stage,
      nextReviewAt: isDue ? past.toISOString() : new Date(now.getTime() + 5 * 86400000).toISOString(),
      monthlyWrongCount: 0,
      hasPathFailure: false,
      totalCorrect: w.stage === 'LEARNED' ? 3 : index % 2 === 0 ? 1 : 0,
      totalWrong: 0,
      lastReviewedAt: w.stage === 'LEARNED' ? past.toISOString() : null,
    });

    difficultyStates.push({
      id: `diff-${index + 1}`,
      conceptId,
      current: w.difficulty,
      consecutiveCorrect: 0,
      consecutiveWrong: 0,
      hasReachedVeryHard: w.difficulty === 'VERY_HARD',
    });
  });

  return {
    concepts,
    contents,
    categories: DEFAULT_CATEGORIES,
    tags: [
      { id: 'tag-essential', name: 'ضروری' },
      { id: 'tag-verbs', name: 'افعال' },
      { id: 'tag-daily', name: 'روزمره' },
    ],
    conceptTags: [
      { conceptId: 'concept-1', tagId: 'tag-essential' },
      { conceptId: 'concept-2', tagId: 'tag-essential' },
      { conceptId: 'concept-5', tagId: 'tag-verbs' },
    ],
    learningStates,
    difficultyStates,
    relations: [],
    variants: [],
    reviewQueue: [],
    reviewSessions: [
      {
        id: 'sess-sample-1',
        startedAt: past.toISOString(),
        endedAt: past.toISOString(),
        reviewType: 'DAILY',
        totalReviewed: 5,
        totalCorrect: 4,
        totalWrong: 1,
      },
    ],
    reviewHistory: [],
    languages: DEFAULT_LANGUAGES,
    languagePairs: DEFAULT_PAIRS,
  };
}

export function loadDatabase(): DatabaseState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as DatabaseState;
      if (parsed && Array.isArray(parsed.concepts) && parsed.concepts.length > 0) {
        return parsed;
      }
    }
  } catch (e) {
    console.error('Failed to load database from localStorage, initializing fresh:', e);
  }
  const initial = generateSeedState();
  saveDatabase(initial);
  return initial;
}

export function saveDatabase(state: DatabaseState): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (e) {
    console.error('Failed to save database to localStorage:', e);
  }
}

export function loadStreak(): StreakInfo {
  try {
    const raw = localStorage.getItem(STREAK_KEY);
    if (raw) {
      return JSON.parse(raw);
    }
  } catch (e) {
    console.error('Failed to load streak:', e);
  }
  return {
    currentStreakDays: 3,
    bestStreakDays: 5,
    lastActiveDate: new Date().toISOString().slice(0, 10),
  };
}

export function recordStreakActivity(): StreakInfo {
  const current = loadStreak();
  const today = new Date().toISOString().slice(0, 10);
  if (current.lastActiveDate === today) {
    return current;
  }

  let streak = current.currentStreakDays;
  if (current.lastActiveDate) {
    const last = new Date(current.lastActiveDate);
    const diffDays = Math.round((new Date(today).getTime() - last.getTime()) / (1000 * 3600 * 24));
    if (diffDays === 1) {
      streak += 1;
    } else if (diffDays > 1) {
      streak = 1;
    }
  } else {
    streak = 1;
  }

  const updated: StreakInfo = {
    currentStreakDays: streak,
    bestStreakDays: Math.max(streak, current.bestStreakDays),
    lastActiveDate: today,
  };
  localStorage.setItem(STREAK_KEY, JSON.stringify(updated));
  return updated;
}

export function getFullVocabularyItems(state: DatabaseState): FullVocabularyItem[] {
  const categoryMap = new Map(state.categories.map((c) => [c.id, c]));
  const tagMap = new Map(state.tags.map((t) => [t.id, t]));
  const conceptTagsMap = new Map<string, string[]>();
  state.conceptTags.forEach((ct) => {
    const arr = conceptTagsMap.get(ct.conceptId) || [];
    arr.push(ct.tagId);
    conceptTagsMap.set(ct.conceptId, arr);
  });

  const learnMap = new Map(state.learningStates.map((l) => [l.conceptId, l]));
  const diffMap = new Map(state.difficultyStates.map((d) => [d.conceptId, d]));

  const contentMap = new Map<string, { es?: Content; fa?: Content }>();
  state.contents.forEach((c) => {
    const existing = contentMap.get(c.conceptId) || {};
    if (c.languageCode === 'es') existing.es = c;
    else if (c.languageCode === 'fa') existing.fa = c;
    contentMap.set(c.conceptId, existing);
  });

  return state.concepts
    .filter((c) => c.active)
    .map((concept) => {
      const contents = contentMap.get(concept.id) || {};
      const sourceContent = contents.es || {
        id: `content-es-${concept.id}`,
        conceptId: concept.id,
        languageCode: 'es',
        text: '---',
        canonicalKey: '---',
      };
      const targetContent = contents.fa || {
        id: `content-fa-${concept.id}`,
        conceptId: concept.id,
        languageCode: 'fa',
        text: '---',
        canonicalKey: '---',
      };

      const learningState: LearningState = learnMap.get(concept.id) || {
        id: `learn-${concept.id}`,
        conceptId: concept.id,
        stage: 'DAILY',
        nextReviewAt: new Date().toISOString(),
        monthlyWrongCount: 0,
        hasPathFailure: false,
        totalCorrect: 0,
        totalWrong: 0,
        lastReviewedAt: null,
      };

      const difficultyState: DifficultyState = diffMap.get(concept.id) || {
        id: `diff-${concept.id}`,
        conceptId: concept.id,
        current: 'MEDIUM',
        consecutiveCorrect: 0,
        consecutiveWrong: 0,
        hasReachedVeryHard: false,
      };

      const tagIds = conceptTagsMap.get(concept.id) || [];
      const tags = tagIds.map((id) => tagMap.get(id)).filter(Boolean) as Tag[];
      const category = concept.categoryId ? categoryMap.get(concept.categoryId) || null : null;
      const relations = state.relations.filter((r) => r.sourceConceptId === concept.id);
      const variants = state.variants.filter((v) => v.conceptId === concept.id);

      return {
        concept,
        sourceContent,
        targetContent,
        category,
        learningState,
        difficultyState,
        tags,
        relations,
        variants,
      };
    });
}

export function computeProgressSummary(state: DatabaseState): ProgressSummary {
  const activeConcepts = state.concepts.filter((c) => c.active);
  const activeIds = new Set(activeConcepts.map((c) => c.id));
  const activeLearnings = state.learningStates.filter((l) => activeIds.has(l.conceptId));

  let learnedCount = 0;
  let dueCount = 0;
  let dailyDue = 0;
  let weeklyDue = 0;
  let monthlyDue = 0;
  let totalCorrect = 0;
  let totalWrong = 0;

  activeLearnings.forEach((l) => {
    totalCorrect += l.totalCorrect;
    totalWrong += l.totalWrong;

    if (l.stage === 'LEARNED') {
      learnedCount++;
    } else if (isDueForReview(l)) {
      dueCount++;
      if (l.stage === 'DAILY') dailyDue++;
      else if (l.stage === 'WEEKLY') weeklyDue++;
      else if (l.stage === 'MONTHLY') monthlyDue++;
    }
  });

  const totalReviews = totalCorrect + totalWrong;
  const accuracyPercent = totalReviews > 0 ? Math.round((totalCorrect / totalReviews) * 100) : 0;

  return {
    activeConceptCount: activeConcepts.length,
    learnedConceptCount: learnedCount,
    dueConceptCount: dueCount,
    dailyDueConceptCount: dailyDue,
    weeklyDueConceptCount: weeklyDue,
    monthlyDueConceptCount: monthlyDue,
    totalCorrect,
    totalWrong,
    accuracyPercent,
  };
}

export function exportFullBackup(state: DatabaseState): FullBackupPayload {
  return {
    schemaVersion: 2,
    exportedAt: new Date().toISOString(),
    backupType: 'FULL',
    concepts: state.concepts,
    contents: state.contents,
    tags: state.tags,
    categories: state.categories,
    relations: state.relations,
    variants: state.variants,
    languages: state.languages,
    languagePairs: state.languagePairs,
    learningStates: state.learningStates,
    difficultyStates: state.difficultyStates,
    conceptTags: state.conceptTags,
    reviewSessions: state.reviewSessions,
    reviewHistory: state.reviewHistory,
    achievements: INITIAL_ACHIEVEMENTS,
  };
}

export function importFullBackup(rawJson: string): { success: boolean; message: string; count: number } {
  try {
    const data = JSON.parse(rawJson);
    if (!data.concepts || !Array.isArray(data.concepts) || !data.contents || !Array.isArray(data.contents)) {
      return { success: false, message: 'Invalid backup format: missing concepts or contents', count: 0 };
    }

    const currentState = loadDatabase();
    const newConcepts = data.concepts as Concept[];
    const newContents = data.contents as Content[];
    const newCategories = (data.categories as Category[]) || currentState.categories;
    const newLearnings = (data.learningStates as LearningState[]) || [];
    const newDiffs = (data.difficultyStates as DifficultyState[]) || [];

    const mergedState: DatabaseState = {
      ...currentState,
      concepts: newConcepts,
      contents: newContents,
      categories: newCategories,
      learningStates: newLearnings.length > 0 ? newLearnings : currentState.learningStates,
      difficultyStates: newDiffs.length > 0 ? newDiffs : currentState.difficultyStates,
      tags: (data.tags as Tag[]) || currentState.tags,
      relations: (data.relations as VocabularyRelation[]) || [],
      variants: (data.variants as VocabularyVariant[]) || [],
      reviewSessions: (data.reviewSessions as ReviewSession[]) || currentState.reviewSessions,
      reviewHistory: (data.reviewHistory as ReviewHistory[]) || currentState.reviewHistory,
    };

    saveDatabase(mergedState);
    return { success: true, message: `Successfully restored ${newConcepts.length} concepts.`, count: newConcepts.length };
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : 'Unknown error';
    return { success: false, message: `Failed to restore: ${errorMessage}`, count: 0 };
  }
}
