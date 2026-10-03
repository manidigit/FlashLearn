import React, { createContext, useContext, useEffect, useState } from 'react';
import { ThemeId, THEMES, ThemeSpec } from '../theme/themeConfig';
import { LanguageCode, TRANSLATIONS } from '../i18n/translations';
import {
  computeProgressSummary,
  DatabaseState,
  getFullVocabularyItems,
  loadDatabase,
  loadStreak,
  recordStreakActivity,
  saveDatabase,
} from '../storage/db';
import {
  Concept,
  Content,
  DifficultyState,
  FullVocabularyItem,
  LearningState,
  ProgressSummary,
  ReviewQueueItem,
  ReviewType,
  StreakInfo,
} from '../types';
import { calculateDifficulty, calculateLearningTransition } from '../domain/algorithms';

interface AppContextType {
  // Theme & Locale
  themeId: ThemeId;
  theme: ThemeSpec;
  isDark: boolean;
  language: LanguageCode;
  t: typeof TRANSLATIONS.en;
  setThemeId: (id: ThemeId) => void;
  setIsDark: (dark: boolean) => void;
  setLanguage: (lang: LanguageCode) => void;
  toggleDarkMode: () => void;
  difficultyThreshold: number;
  setDifficultyThreshold: (val: number) => void;

  // Database
  db: DatabaseState;
  vocabulary: FullVocabularyItem[];
  progressSummary: ProgressSummary;
  streak: StreakInfo;
  refreshDb: () => void;

  // Actions
  recordReviewResult: (conceptId: string, isCorrect: boolean, reviewType: ReviewType) => void;
  saveConceptWord: (params: {
    id?: string;
    spanish: string;
    persian: string;
    categoryId?: string;
    notes?: string;
    grammarNote?: string;
    exampleEs?: string;
    exampleFa?: string;
    entryType?: 'WORD' | 'PHRASE' | 'SENTENCE' | 'IDIOM';
  }) => string;
  deleteConcept: (conceptId: string) => void;
  toggleFavorite: (conceptId: string) => void;
  bulkImportEntries: (entries: { sourceText: string; translationText: string | null; notes: string | null; grammarNote: string | null }[]) => number;
  approveQueueItem: (item: ReviewQueueItem) => void;
  rejectQueueItem: (itemId: string) => void;
  resetToSampleData: () => void;
  clearDatabase: () => void;
  findDuplicates: () => { groups: { key: string; items: FullVocabularyItem[] }[]; extraCount: number };
  cleanDuplicates: () => number;
}

const AppContext = createContext<AppContextType | null>(null);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [themeId, setThemeIdState] = useState<ThemeId>(() => {
    return (localStorage.getItem('flashlearn_theme') as ThemeId) || 'grok';
  });

  const [isDark, setIsDarkState] = useState<boolean>(() => {
    const saved = localStorage.getItem('flashlearn_dark');
    return saved !== null ? saved === 'true' : true; // Default to dark luxury theme as in Grok design
  });

  const [language, setLanguageState] = useState<LanguageCode>(() => {
    return (localStorage.getItem('flashlearn_lang') as LanguageCode) || 'fa';
  });

  const [difficultyThreshold, setDifficultyThresholdState] = useState<number>(() => {
    const saved = localStorage.getItem('flashlearn_difficulty_threshold');
    const parsed = saved ? parseInt(saved, 10) : 3;
    return isNaN(parsed) || parsed < 1 || parsed > 20 ? 3 : parsed;
  });

  const setDifficultyThreshold = (val: number) => {
    const clamped = Math.max(1, Math.min(20, val));
    setDifficultyThresholdState(clamped);
    localStorage.setItem('flashlearn_difficulty_threshold', String(clamped));
  };

  const [db, setDb] = useState<DatabaseState>(() => loadDatabase());
  const [streak, setStreak] = useState<StreakInfo>(() => loadStreak());

  const theme = THEMES[themeId] || THEMES.grok;
  const t = TRANSLATIONS[language] || TRANSLATIONS.en;

  const setThemeId = (id: ThemeId) => {
    setThemeIdState(id);
    localStorage.setItem('flashlearn_theme', id);
  };

  const setIsDark = (dark: boolean) => {
    setIsDarkState(dark);
    localStorage.setItem('flashlearn_dark', String(dark));
  };

  const toggleDarkMode = () => setIsDark(!isDark);

  const setLanguage = (lang: LanguageCode) => {
    setLanguageState(lang);
    localStorage.setItem('flashlearn_lang', lang);
  };

  // Sync document root attributes
  useEffect(() => {
    document.documentElement.dir = language === 'fa' ? 'rtl' : 'ltr';
    document.documentElement.lang = language;
    if (isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [language, isDark]);

  const refreshDb = () => {
    const loaded = loadDatabase();
    setDb(loaded);
    setStreak(loadStreak());
  };

  const vocabulary = getFullVocabularyItems(db);
  const progressSummary = computeProgressSummary(db);

  // Review result handler
  const recordReviewResult = (conceptId: string, isCorrect: boolean, reviewType: ReviewType) => {
    const now = new Date();
    const updatedLearnings = db.learningStates.map((l) => {
      if (l.conceptId !== conceptId) return l;
      const transition = calculateLearningTransition(l, isCorrect, now);
      return {
        ...l,
        stage: transition.newStage,
        nextReviewAt: transition.nextReviewAt,
        hasPathFailure: transition.hasPathFailure,
        monthlyWrongCount: transition.monthlyWrongCount,
        totalCorrect: isCorrect ? l.totalCorrect + 1 : l.totalCorrect,
        totalWrong: isCorrect ? l.totalWrong : l.totalWrong + 1,
        lastReviewedAt: now.toISOString(),
      };
    });

    const updatedDiffs = db.difficultyStates.map((d) => {
      if (d.conceptId !== conceptId) return d;
      return calculateDifficulty(d, isCorrect, difficultyThreshold);
    });

    const newHistory = {
      id: `hist-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`,
      sessionId: 'sess-active',
      conceptId,
      reviewedAt: now.toISOString(),
      isCorrect,
      reviewType,
    };

    const newDb: DatabaseState = {
      ...db,
      learningStates: updatedLearnings,
      difficultyStates: updatedDiffs,
      reviewHistory: [newHistory, ...db.reviewHistory],
    };

    saveDatabase(newDb);
    setDb(newDb);

    const updatedStreak = recordStreakActivity();
    setStreak(updatedStreak);
  };

  // Add / Edit word
  const saveConceptWord = (params: {
    id?: string;
    spanish: string;
    persian: string;
    categoryId?: string;
    notes?: string;
    grammarNote?: string;
    exampleEs?: string;
    exampleFa?: string;
    entryType?: 'WORD' | 'PHRASE' | 'SENTENCE' | 'IDIOM';
  }): string => {
    const now = new Date().toISOString();
    let conceptId = params.id;

    let concepts = [...db.concepts];
    let contents = [...db.contents];
    let learnings = [...db.learningStates];
    let diffs = [...db.difficultyStates];

    if (conceptId) {
      // Edit existing
      concepts = concepts.map((c) =>
        c.id === conceptId
          ? {
              ...c,
              categoryId: params.categoryId || c.categoryId,
              entryType: (params.entryType as any) || c.entryType,
              updatedAt: now,
            }
          : c
      );

      contents = contents.map((cnt) => {
        if (cnt.conceptId !== conceptId) return cnt;
        if (cnt.languageCode === 'es') {
          return {
            ...cnt,
            text: params.spanish,
            canonicalKey: params.spanish.toLowerCase().trim(),
            notes: params.notes || cnt.notes,
            grammarNote: params.grammarNote || cnt.grammarNote,
            example: params.exampleEs !== undefined ? params.exampleEs : cnt.example,
          };
        }
        if (cnt.languageCode === 'fa') {
          return {
            ...cnt,
            text: params.persian,
            canonicalKey: params.persian.trim(),
            example: params.exampleFa !== undefined ? params.exampleFa : cnt.example,
          };
        }
        return cnt;
      });
    } else {
      // Create new
      conceptId = `concept-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`;
      const newConcept: Concept = {
        id: conceptId,
        entryType: (params.entryType as any) || 'WORD',
        categoryId: params.categoryId || 'cat-general',
        favorite: false,
        active: true,
        createdAt: now,
        updatedAt: now,
      };
      concepts.unshift(newConcept);

      const esContent: Content = {
        id: `cnt-es-${Date.now()}`,
        conceptId,
        languageCode: 'es',
        text: params.spanish,
        canonicalKey: params.spanish.toLowerCase().trim(),
        notes: params.notes || null,
        grammarNote: params.grammarNote || null,
        example: params.exampleEs || null,
        translationIndex: 0,
        possibleCorrection: null,
      };

      const faContent: Content = {
        id: `cnt-fa-${Date.now()}`,
        conceptId,
        languageCode: 'fa',
        text: params.persian,
        canonicalKey: params.persian.trim(),
        notes: null,
        grammarNote: null,
        example: params.exampleFa || null,
        translationIndex: 0,
        possibleCorrection: null,
      };

      contents.push(esContent, faContent);

      const newLearn: LearningState = {
        id: `learn-${Date.now()}`,
        conceptId,
        stage: 'DAILY',
        nextReviewAt: now, // immediately due for review!
        monthlyWrongCount: 0,
        hasPathFailure: false,
        totalCorrect: 0,
        totalWrong: 0,
        lastReviewedAt: null,
      };
      learnings.push(newLearn);

      const newDiff: DifficultyState = {
        id: `diff-${Date.now()}`,
        conceptId,
        current: 'MEDIUM',
        consecutiveCorrect: 0,
        consecutiveWrong: 0,
        hasReachedVeryHard: false,
      };
      diffs.push(newDiff);
    }

    const updatedDb: DatabaseState = {
      ...db,
      concepts,
      contents,
      learningStates: learnings,
      difficultyStates: diffs,
    };
    saveDatabase(updatedDb);
    setDb(updatedDb);
    return conceptId;
  };

  const deleteConcept = (conceptId: string) => {
    const updatedDb: DatabaseState = {
      ...db,
      concepts: db.concepts.filter((c) => c.id !== conceptId),
      contents: db.contents.filter((cnt) => cnt.conceptId !== conceptId),
      learningStates: db.learningStates.filter((l) => l.conceptId !== conceptId),
      difficultyStates: db.difficultyStates.filter((d) => d.conceptId !== conceptId),
      conceptTags: db.conceptTags.filter((ct) => ct.conceptId !== conceptId),
      relations: db.relations.filter((r) => r.sourceConceptId !== conceptId && r.targetConceptId !== conceptId),
    };
    saveDatabase(updatedDb);
    setDb(updatedDb);
  };

  const toggleFavorite = (conceptId: string) => {
    const updatedDb: DatabaseState = {
      ...db,
      concepts: db.concepts.map((c) => (c.id === conceptId ? { ...c, favorite: !c.favorite } : c)),
    };
    saveDatabase(updatedDb);
    setDb(updatedDb);
  };

  const bulkImportEntries = (entries: { sourceText: string; translationText: string | null; notes: string | null; grammarNote: string | null }[]) => {
    let imported = 0;
    entries.forEach((e) => {
      if (e.sourceText.trim() && e.translationText?.trim()) {
        saveConceptWord({
          spanish: e.sourceText.trim(),
          persian: e.translationText.trim(),
          notes: e.notes || undefined,
          grammarNote: e.grammarNote || undefined,
        });
        imported++;
      }
    });
    return imported;
  };

  const approveQueueItem = (item: ReviewQueueItem) => {
    if (item.sourceText && item.targetText) {
      saveConceptWord({
        spanish: item.sourceText,
        persian: item.targetText,
      });
    }
    const updatedDb = {
      ...db,
      reviewQueue: db.reviewQueue.filter((q) => q.id !== item.id),
    };
    saveDatabase(updatedDb);
    setDb(updatedDb);
  };

  const rejectQueueItem = (itemId: string) => {
    const updatedDb = {
      ...db,
      reviewQueue: db.reviewQueue.filter((q) => q.id !== itemId),
    };
    saveDatabase(updatedDb);
    setDb(updatedDb);
  };

  const resetToSampleData = () => {
    localStorage.removeItem('flashlearn_db_v2');
    const fresh = loadDatabase();
    setDb(fresh);
  };

  const clearDatabase = () => {
    const emptyDb: DatabaseState = {
      ...db,
      concepts: [],
      contents: [],
      learningStates: [],
      difficultyStates: [],
      conceptTags: [],
      relations: [],
      variants: [],
      reviewQueue: [],
      reviewHistory: [],
    };
    saveDatabase(emptyDb);
    setDb(emptyDb);
  };

  const findDuplicates = () => {
    const all = getFullVocabularyItems(db);
    const groupsMap = new Map<string, FullVocabularyItem[]>();
    all.forEach((item) => {
      const key = item.sourceContent.canonicalKey.toLowerCase().trim();
      const arr = groupsMap.get(key) || [];
      arr.push(item);
      groupsMap.set(key, arr);
    });

    const groups: { key: string; items: FullVocabularyItem[] }[] = [];
    let extraCount = 0;
    groupsMap.forEach((items, key) => {
      if (items.length > 1) {
        groups.push({ key, items });
        extraCount += items.length - 1;
      }
    });

    return { groups, extraCount };
  };

  const cleanDuplicates = () => {
    const { groups } = findDuplicates();
    let removed = 0;
    const idsToRemove = new Set<string>();

    groups.forEach((g) => {
      // Keep the first concept, remove the rest
      const [first, ...rest] = g.items;
      rest.forEach((r) => {
        idsToRemove.add(r.concept.id);
        removed++;
      });
    });

    if (removed > 0) {
      const updatedDb: DatabaseState = {
        ...db,
        concepts: db.concepts.filter((c) => !idsToRemove.has(c.id)),
        contents: db.contents.filter((cnt) => !idsToRemove.has(cnt.conceptId)),
        learningStates: db.learningStates.filter((l) => !idsToRemove.has(l.conceptId)),
        difficultyStates: db.difficultyStates.filter((d) => !idsToRemove.has(d.conceptId)),
      };
      saveDatabase(updatedDb);
      setDb(updatedDb);
    }
    return removed;
  };

  return (
    <AppContext.Provider
      value={{
        themeId,
        theme,
        isDark,
        language,
        t,
        setThemeId,
        setIsDark,
        setLanguage,
        toggleDarkMode,
        difficultyThreshold,
        setDifficultyThreshold,
        db,
        vocabulary,
        progressSummary,
        streak,
        refreshDb,
        recordReviewResult,
        saveConceptWord,
        deleteConcept,
        toggleFavorite,
        bulkImportEntries,
        approveQueueItem,
        rejectQueueItem,
        resetToSampleData,
        clearDatabase,
        findDuplicates,
        cleanDuplicates,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
