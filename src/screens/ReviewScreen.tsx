import React, { useState, useMemo, useEffect } from 'react';
import {
  X,
  RotateCw,
  Check,
  Volume2,
  ChevronRight,
  Sparkles,
  Trophy,
  ArrowLeftRight,
  Layers,
  HelpCircle,
  Folder,
  Tag,
  Calendar,
  Clock,
  Shuffle,
  Star,
  Zap,
  Play,
  CheckCircle2,
  Sliders,
  Filter,
  ArrowLeft,
  ChevronLeft,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { FullVocabularyItem, ReviewType, VocabularyDifficulty, QuizDifficulty } from '../types';
import { isDueForReview, isReviewedToday } from '../domain/algorithms';
import { SpeechButton } from '../components/SpeechButton';

interface ReviewScreenProps {
  initialReviewType?: ReviewType;
  onClose: () => void;
}

interface PreparedQuizCard {
  item: FullVocabularyItem;
  promptText: string;
  correctAnswerText: string;
  options: string[];
}

export const ReviewScreen: React.FC<ReviewScreenProps> = ({
  initialReviewType = 'RANDOM',
  onClose,
}) => {
  const { theme, isDark, t, vocabulary, recordReviewResult, db } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  // ── Setup / Filter States (faithful to original ReviewSetup) ──
  const [isSelectingMode, setIsSelectingMode] = useState<boolean>(true);
  const [selectedMode, setSelectedMode] = useState<'QUIZ' | 'FLASHCARD'>('QUIZ');
  const [selectedReviewType, setSelectedReviewType] = useState<ReviewType>(initialReviewType);
  const [selectedDifficulties, setSelectedDifficulties] = useState<Set<VocabularyDifficulty>>(new Set());
  const [selectedCategoryIds, setSelectedCategoryIds] = useState<Set<string>>(new Set());
  const [selectedQuizDifficulty, setSelectedQuizDifficulty] = useState<QuizDifficulty>('MEDIUM');
  const [maximumReviewCards, setMaximumReviewCards] = useState<number>(20);
  const [showCategoryPicker, setShowCategoryPicker] = useState<boolean>(false);

  // ── Session Execution States ──
  const [sessionCards, setSessionCards] = useState<FullVocabularyItem[]>([]);
  const [preparedQuizCards, setPreparedQuizCards] = useState<PreparedQuizCard[]>([]);
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [isAnswered, setIsAnswered] = useState<boolean>(false);
  const [isFlipped, setIsFlipped] = useState<boolean>(false);
  const [showNote, setShowNote] = useState<boolean>(false);
  const [hintRevealed, setHintRevealed] = useState<boolean>(false);
  const [reverseDirection, setReverseDirection] = useState<boolean>(false); // false: ES -> FA, true: FA -> ES

  // Counters
  const [sessionCorrect, setSessionCorrect] = useState<number>(0);
  const [sessionWrong, setSessionWrong] = useState<number>(0);
  const [isFinished, setIsFinished] = useState<boolean>(false);

  const diffLabel = (diff?: string) => {
    switch (diff) {
      case 'EASY': return 'آسان';
      case 'MEDIUM': return 'متوسط';
      case 'HARD': return 'سخت';
      case 'VERY_HARD': return 'بسیار سخت';
      default: return diff || '';
    }
  };

  const stageLabel = (stage?: string) => {
    switch (stage) {
      case 'DAILY': return 'روزانه';
      case 'WEEKLY': return 'هفتگی';
      case 'MONTHLY': return 'ماهانه';
      case 'LEARNED': return 'یادگرفته‌شده';
      default: return stage || '';
    }
  };

  const reviewTypeLabel = (rt: ReviewType) => {
    switch (rt) {
      case 'DAILY': return 'روزانه';
      case 'WEEKLY': return 'هفتگی';
      case 'MONTHLY': return 'ماهانه';
      case 'RANDOM': return 'تصادفی';
      case 'LEARNED': return 'یادگرفته‌شده‌ها';
    }
  };

  // ── Calculate Available Review Count matching the Setup Filters ──
  const matchingCandidates = useMemo(() => {
    return vocabulary.filter((v) => {
      // 1. Category Filter
      if (selectedCategoryIds.size > 0 && (!v.concept.categoryId || !selectedCategoryIds.has(v.concept.categoryId))) {
        return false;
      }

      // 2. Difficulty Filter (multi-select)
      if (selectedDifficulties.size > 0 && !selectedDifficulties.has(v.difficultyState.current)) {
        return false;
      }

      // 3. Review Type / Schedule Filter
      if (selectedReviewType === 'RANDOM') {
        return v.learningState.stage !== 'LEARNED';
      }
      if (selectedReviewType === 'LEARNED') {
        return v.learningState.stage === 'LEARNED';
      }
      if (selectedReviewType === 'DAILY' || selectedReviewType === 'WEEKLY' || selectedReviewType === 'MONTHLY') {
        return v.learningState.stage === selectedReviewType;
      }

      return true;
    });
  }, [vocabulary, selectedCategoryIds, selectedDifficulties, selectedReviewType]);

  const availableReviewCount = matchingCandidates.length;

  const toggleDifficulty = (diff: VocabularyDifficulty) => {
    const next = new Set(selectedDifficulties);
    if (next.has(diff)) next.delete(diff);
    else next.add(diff);
    setSelectedDifficulties(next);
  };

  // ── Start New Session with Selected Filters ──
  const startSession = () => {
    let pool = [...matchingCandidates];
    if (pool.length === 0) {
      pool = [...vocabulary];
    }
    // Shuffle pool
    const shuffled = pool.sort(() => Math.random() - 0.5);
    const selected = shuffled.slice(0, maximumReviewCards);

    // If Quiz mode: pre-generate distinct options for each question ONCE so options NEVER glitch or reshuffle!
    const prepared: PreparedQuizCard[] = selected.map((item) => {
      const prompt = reverseDirection ? item.targetContent.text : item.sourceContent.text;
      const correct = reverseDirection ? item.sourceContent.text : item.targetContent.text;

      // Pick 3 distinct distractors from the remaining vocabulary
      const others = vocabulary.filter((v) => v.concept.id !== item.concept.id);
      const shuffledOthers = [...others].sort(() => Math.random() - 0.5);
      const distractors: string[] = [];

      for (const o of shuffledOthers) {
        const distText = reverseDirection ? o.sourceContent.text : o.targetContent.text;
        if (distText && distText !== correct && !distractors.includes(distText)) {
          distractors.push(distText);
        }
        if (distractors.length >= 3) break;
      }

      // Fallback distractors
      const fallbackList = reverseDirection
        ? ['casa', 'viaje', 'tiempo', 'amigo', 'comida', 'agua', 'aprender', 'hola']
        : ['خانه', 'سفر', 'زمان', 'دوست', 'غذا', 'آب', 'یاد گرفتن', 'سلام'];
      for (const fb of fallbackList) {
        if (distractors.length >= 3) break;
        if (fb !== correct && !distractors.includes(fb)) {
          distractors.push(fb);
        }
      }

      const allOpts = [correct, ...distractors.slice(0, 3)].sort(() => Math.random() - 0.5);

      return {
        item,
        promptText: prompt,
        correctAnswerText: correct,
        options: allOpts,
      };
    });

    setSessionCards(selected);
    setPreparedQuizCards(prepared);
    setCurrentIndex(0);
    setSelectedOption(null);
    setIsAnswered(false);
    setIsFlipped(false);
    setShowNote(false);
    setHintRevealed(false);
    setSessionCorrect(0);
    setSessionWrong(0);
    setIsFinished(false);
    setIsSelectingMode(false);
  };

  const handleSelectQuizOption = (opt: string) => {
    if (isAnswered) return;
    const currentQuiz = preparedQuizCards[currentIndex];
    if (!currentQuiz) return;

    setSelectedOption(opt);
    setIsAnswered(true);

    const isCorrect = opt.trim().toLowerCase() === currentQuiz.correctAnswerText.trim().toLowerCase();
    if (isCorrect) setSessionCorrect((p) => p + 1);
    else setSessionWrong((p) => p + 1);

    recordReviewResult(currentQuiz.item.concept.id, isCorrect, selectedReviewType);
  };

  const handleFlashcardAnswer = (isCorrect: boolean) => {
    const card = sessionCards[currentIndex];
    if (!card) return;

    if (isCorrect) setSessionCorrect((p) => p + 1);
    else setSessionWrong((p) => p + 1);

    recordReviewResult(card.concept.id, isCorrect, selectedReviewType);
    advanceCard();
  };

  const advanceCard = () => {
    setSelectedOption(null);
    setIsAnswered(false);
    setIsFlipped(false);
    setShowNote(false);
    setHintRevealed(false);

    if (currentIndex + 1 >= sessionCards.length) {
      setIsFinished(true);
    } else {
      setCurrentIndex((prev) => prev + 1);
    }
  };

  // ── CATEGORY PICKER MODAL (Full fidelity to CategorySelectionScreen) ──
  if (showCategoryPicker) {
    const allSelected = selectedCategoryIds.size === 0;
    return (
      <div className="max-w-2xl mx-auto px-4 py-6 pb-28 space-y-5 animate-fadeIn">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowCategoryPicker(false)}
              className="p-2 rounded-xl border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
              style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            >
              <ArrowLeft size={18} />
            </button>
            <div>
              <h2 className="text-2xl font-black">انتخاب دسته‌بندی‌ها</h2>
              <p className="text-xs opacity-65">
                {allSelected ? 'همه دسته‌بندی‌ها انتخاب شده‌اند' : `${selectedCategoryIds.size} دسته انتخاب شده`}
              </p>
            </div>
          </div>
          <button
            onClick={() => setSelectedCategoryIds(new Set())}
            className="text-xs font-bold text-amber-500 hover:underline"
          >
            انتخاب همه
          </button>
        </div>

        <div className="space-y-2.5">
          {/* All categories option */}
          <div
            onClick={() => setSelectedCategoryIds(new Set())}
            className={`p-4 rounded-2xl border cursor-pointer flex items-center justify-between transition-all ${
              allSelected ? 'border-amber-400 bg-amber-400/10' : 'opacity-70'
            }`}
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: allSelected ? undefined : isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <div className="flex items-center gap-3">
              <Folder size={18} className="text-amber-500" />
              <div>
                <h4 className="font-bold text-sm">همه دسته‌بندی‌ها</h4>
                <p className="text-xs opacity-60">{vocabulary.length} واژه</p>
              </div>
            </div>
            {allSelected && <Check size={18} className="text-amber-500" />}
          </div>

          {/* Individual categories */}
          {db.categories.map((cat) => {
            const isCatSelected = selectedCategoryIds.has(cat.id);
            const count = vocabulary.filter((v) => v.concept.categoryId === cat.id).length;
            return (
              <div
                key={cat.id}
                onClick={() => {
                  const next = new Set(selectedCategoryIds);
                  if (next.has(cat.id)) next.delete(cat.id);
                  else next.add(cat.id);
                  setSelectedCategoryIds(next);
                }}
                className={`p-4 rounded-2xl border cursor-pointer flex items-center justify-between transition-all ${
                  isCatSelected ? 'border-amber-400 bg-amber-400/10' : 'opacity-70'
                }`}
                style={{
                  backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                  borderColor: isCatSelected ? undefined : isDark ? theme.borderDark : theme.borderLight,
                }}
              >
                <div className="flex items-center gap-3">
                  <Tag size={18} style={{ color: primaryColor }} />
                  <div>
                    <h4 className="font-bold text-sm">{cat.name}</h4>
                    <p className="text-xs opacity-60">{count} واژه</p>
                  </div>
                </div>
                {isCatSelected && <Check size={18} className="text-amber-500" />}
              </div>
            );
          })}
        </div>

        <div className="flex items-center justify-end gap-3 pt-3">
          <button
            onClick={() => setShowCategoryPicker(false)}
            className="px-6 py-3 rounded-2xl text-xs font-bold text-white shadow-md transition-all hover:opacity-95"
            style={{ backgroundColor: primaryColor }}
          >
            تأیید و بازگشت
          </button>
        </div>
      </div>
    );
  }

  // ── 1. REVIEW SETUP / FILTER DASHBOARD (Faithful to ReviewSetup in ReviewScreen.kt) ──
  if (isSelectingMode) {
    const categorySummaryText =
      selectedCategoryIds.size === 0
        ? 'همه دسته‌بندی‌ها'
        : `${selectedCategoryIds.size} دسته‌بندی انتخاب‌شده`;

    const diffSummaryText =
      selectedDifficulties.size === 0
        ? 'همه سطوح دشواری'
        : Array.from(selectedDifficulties).join(', ');

    return (
      <div className="max-w-3xl mx-auto px-4 py-6 pb-28 space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={onClose}
              className="p-2 rounded-xl border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
              style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            >
              <ArrowLeft size={18} />
            </button>
            <div>
              <h2 className="text-2xl font-black">مرور واژگان</h2>
              <p className="text-xs opacity-65">فیلتر و تنظیم نحوه مرور واژگان قبل از شروع</p>
            </div>
          </div>
          <button
            onClick={() => setReverseDirection(!reverseDirection)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border text-xs font-bold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            <ArrowLeftRight size={13} />
            <span>{reverseDirection ? 'فارسی ← اسپانیایی' : 'اسپانیایی ← فارسی'}</span>
          </button>
        </div>

        {/* Section 1: Answer Mode */}
        <div className="space-y-2">
          <h3 className="text-xs font-extrabold uppercase tracking-wider opacity-85">
            ۱. حالت پاسخ
          </h3>
          <div className="grid grid-cols-2 gap-3">
            <div
              onClick={() => setSelectedMode('QUIZ')}
              className={`p-4 rounded-2xl border cursor-pointer transition-all flex items-center justify-between ${
                selectedMode === 'QUIZ' ? 'border-amber-400 bg-amber-400/10 shadow-xs' : 'opacity-70 hover:opacity-100'
              }`}
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: selectedMode === 'QUIZ' ? undefined : isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div>
                <h4 className="font-extrabold text-sm">کوئیز چهارگزینه‌ای</h4>
                <p className="text-[11px] opacity-60">تست و گزینش سریع با بازخورد آنی</p>
              </div>
              <Sparkles size={20} className={selectedMode === 'QUIZ' ? 'text-amber-500' : 'opacity-40'} />
            </div>

            <div
              onClick={() => setSelectedMode('FLASHCARD')}
              className={`p-4 rounded-2xl border cursor-pointer transition-all flex items-center justify-between ${
                selectedMode === 'FLASHCARD' ? 'border-amber-400 bg-amber-400/10 shadow-xs' : 'opacity-70 hover:opacity-100'
              }`}
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: selectedMode === 'FLASHCARD' ? undefined : isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div>
                <h4 className="font-extrabold text-sm">فلش‌کارت سنتی</h4>
                <p className="text-[11px] opacity-60">نمایش پاسخ و ارزیابی خودکار حافظه</p>
              </div>
              <Layers size={20} className={selectedMode === 'FLASHCARD' ? 'text-amber-500' : 'opacity-40'} />
            </div>
          </div>
        </div>

        {/* Section 2: Categories */}
        <div className="space-y-2">
          <h3 className="text-xs font-extrabold uppercase tracking-wider opacity-85">
            ۲. دسته‌بندی واژگان
          </h3>
          <div
            onClick={() => setShowCategoryPicker(true)}
            className="p-4 rounded-2xl border cursor-pointer shadow-xs transition-all hover:border-amber-400 flex items-center justify-between"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <div className="flex items-center gap-3">
              <Folder size={20} style={{ color: primaryColor }} />
              <div>
                <h4 className="font-extrabold text-sm">{categorySummaryText}</h4>
                <p className="text-[11px] opacity-60">برای فیلتر کردن دسته‌های خاص ضربه بزنید</p>
              </div>
            </div>
            <span className="text-xs font-bold text-amber-500 flex items-center gap-1">
              تغییر دسته <ChevronLeft size={16} />
            </span>
          </div>
        </div>

        {/* Section 3: Special Review */}
        <div className="space-y-2">
          <h3 className="text-xs font-extrabold uppercase tracking-wider opacity-85">
            ۳. مرور ویژه
          </h3>
          <div className="grid grid-cols-2 gap-3">
            <div
              onClick={() => setSelectedReviewType('RANDOM')}
              className={`p-3.5 rounded-2xl border cursor-pointer transition-all flex items-center justify-between ${
                selectedReviewType === 'RANDOM' ? 'border-amber-400 bg-amber-400/10' : 'opacity-70'
              }`}
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: selectedReviewType === 'RANDOM' ? undefined : isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div>
                <h4 className="font-bold text-xs">مرور تصادفی ترکیبی</h4>
                <p className="text-[10px] opacity-60">از میان تمام واژگان در جریان</p>
              </div>
              <Shuffle size={16} className={selectedReviewType === 'RANDOM' ? 'text-amber-500' : 'opacity-40'} />
            </div>

            <div
              onClick={() => setSelectedReviewType('LEARNED')}
              className={`p-3.5 rounded-2xl border cursor-pointer transition-all flex items-center justify-between ${
                selectedReviewType === 'LEARNED' ? 'border-amber-400 bg-amber-400/10' : 'opacity-70'
              }`}
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: selectedReviewType === 'LEARNED' ? undefined : isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div>
                <h4 className="font-bold text-xs">یادگرفته‌شده‌ها (تثبیت)</h4>
                <p className="text-[10px] opacity-60">واژگانی که به حافظه دائم سپرده شده‌اند</p>
              </div>
              <CheckCircle2 size={16} className={selectedReviewType === 'LEARNED' ? 'text-amber-500' : 'opacity-40'} />
            </div>
          </div>
        </div>

        {/* Section 4: Schedule */}
        <div className="space-y-2">
          <h3 className="text-xs font-extrabold uppercase tracking-wider opacity-85">
            ۴. زمان‌بندی تکرار فاصله‌دار
          </h3>
          <div className="grid grid-cols-3 gap-2.5">
            {(['DAILY', 'WEEKLY', 'MONTHLY'] as const).map((stage) => {
              const isSelected = selectedReviewType === stage;
              return (
                <div
                  key={stage}
                  onClick={() => setSelectedReviewType(stage)}
                  className={`p-3 rounded-2xl border text-center cursor-pointer transition-all ${
                    isSelected ? 'border-amber-400 bg-amber-400/10' : 'opacity-70'
                  }`}
                  style={{
                    backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                    borderColor: isSelected ? undefined : isDark ? theme.borderDark : theme.borderLight,
                  }}
                >
                  <h4 className="font-bold text-xs">
                    {stage === 'DAILY' ? 'روزانه' : stage === 'WEEKLY' ? 'هفتگی' : 'ماهانه'}
                  </h4>
                  <p className="text-[10px] opacity-60 mt-0.5">
                    {stage === 'DAILY' ? 'تکرار امروز' : stage === 'WEEKLY' ? 'تثبیت ۷ روزه' : 'حافظه ۳۰ روزه'}
                  </p>
                </div>
              );
            })}
          </div>
        </div>

        {/* Section 5: Word Difficulty */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <h3 className="text-xs font-extrabold uppercase tracking-wider opacity-85">
              ۵. سطح دشواری واژگان
            </h3>
            {selectedDifficulties.size > 0 && (
              <button
                onClick={() => setSelectedDifficulties(new Set())}
                className="text-[11px] font-bold text-amber-500 hover:underline"
              >
                انتخاب همه سطوح
              </button>
            )}
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
            {(['EASY', 'MEDIUM', 'HARD', 'VERY_HARD'] as const).map((diff) => {
              const isDiffSelected = selectedDifficulties.has(diff);
              return (
                <div
                  key={diff}
                  onClick={() => toggleDifficulty(diff)}
                  className={`p-3 rounded-2xl border text-center cursor-pointer transition-all ${
                    isDiffSelected ? 'border-amber-400 bg-amber-400/15 shadow-xs' : 'opacity-70'
                  }`}
                  style={{
                    backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                    borderColor: isDiffSelected ? undefined : isDark ? theme.borderDark : theme.borderLight,
                  }}
                >
                  <h4 className="font-bold text-xs">
                    {diff === 'EASY'
                      ? 'آسان'
                      : diff === 'MEDIUM'
                      ? 'متوسط'
                      : diff === 'HARD'
                      ? 'سخت'
                      : 'بسیار سخت'}
                  </h4>
                  <span className="text-[10px] opacity-65">
                    {vocabulary.filter((v) => v.difficultyState.current === diff).length} واژه
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Section 6: Quiz Level */}
        {selectedMode === 'QUIZ' && (
          <div className="space-y-2">
            <h3 className="text-xs font-extrabold uppercase tracking-wider opacity-85">
              ۶. سطح گزینه‌های آزمون
            </h3>
            <div className="grid grid-cols-3 gap-2.5">
              {(['EASY', 'MEDIUM', 'HARD'] as const).map((level) => {
                const isLvl = selectedQuizDifficulty === level;
                return (
                  <div
                    key={level}
                    onClick={() => setSelectedQuizDifficulty(level)}
                    className={`p-3 rounded-2xl border text-center cursor-pointer transition-all ${
                      isLvl ? 'border-amber-400 bg-amber-400/10' : 'opacity-70'
                    }`}
                    style={{
                      backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                      borderColor: isLvl ? undefined : isDark ? theme.borderDark : theme.borderLight,
                    }}
                  >
                    <h4 className="font-bold text-xs">
                      {level === 'EASY' ? 'مبتدی' : level === 'MEDIUM' ? 'متوسط' : 'حرفه‌ای'}
                    </h4>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Section 7: Card Count */}
        <div className="space-y-2">
          <h3 className="text-xs font-extrabold uppercase tracking-wider opacity-85">
            {selectedMode === 'QUIZ' ? '۷' : '۶'}. سقف تعداد واژگان مرور
          </h3>
          <div className="flex items-center gap-2">
            {[10, 20, 30, 50, 100].map((count) => {
              const isSelected = maximumReviewCards === count;
              return (
                <button
                  key={count}
                  onClick={() => setMaximumReviewCards(count)}
                  className={`flex-1 py-2 rounded-xl text-xs font-bold border transition-all ${
                    isSelected ? 'text-white shadow-xs' : 'opacity-70'
                  }`}
                  style={{
                    backgroundColor: isSelected ? primaryColor : isDark ? theme.cardDark : theme.cardLight,
                    borderColor: isSelected ? primaryColor : isDark ? theme.borderDark : theme.borderLight,
                  }}
                >
                  {count}
                </button>
              );
            })}
          </div>
        </div>

        {/* Filter Summary Banner & Start Button */}
        <div
          className="p-5 rounded-3xl border shadow-xs space-y-4"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div className="flex items-center justify-between text-xs">
            <span className="font-medium opacity-70">خلاصه فیلترهای انتخابی:</span>
            <span className="font-bold text-emerald-500">
              آماده برای مرور: {Math.min(maximumReviewCards, availableReviewCount)} از {availableReviewCount} واژه
            </span>
          </div>

          <button
            onClick={startSession}
            disabled={availableReviewCount === 0}
            className="w-full py-4 rounded-2xl font-black text-sm text-white shadow-lg flex items-center justify-center gap-2 hover:opacity-95 active:scale-98 transition-all disabled:opacity-50"
            style={{ backgroundColor: primaryColor }}
          >
            <Play size={18} fill="currentColor" />
            <span>شروع مرور ({Math.min(maximumReviewCards, availableReviewCount)} واژه)</span>
          </button>
        </div>
      </div>
    );
  }

  // ── 2. SESSION FINISHED SCREEN ──
  if (isFinished || sessionCards.length === 0) {
    const totalDone = sessionCorrect + sessionWrong;
    const accuracy = totalDone > 0 ? Math.round((sessionCorrect / totalDone) * 100) : 100;

    return (
      <div className="max-w-md mx-auto px-4 py-12 text-center space-y-6 animate-fadeIn">
        <div
          className="w-20 h-20 mx-auto rounded-3xl flex items-center justify-center shadow-lg"
          style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
        >
          <Trophy size={40} className="animate-bounce" />
        </div>

        <div>
          <h2 className="text-2xl font-black">{t.sessionFinishedTitle}</h2>
          <p className="text-sm opacity-70 mt-1">{t.sessionFinishedSubtitle}</p>
        </div>

        <div
          className="rounded-3xl p-5 border shadow-sm grid grid-cols-3 gap-2"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div className="p-3">
            <span className="text-xs opacity-65">{t.accuracy}</span>
            <p className="text-xl font-black mt-1 text-emerald-500">{accuracy}%</p>
          </div>
          <div className="p-3 border-x" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
            <span className="text-xs opacity-65">{t.correctCount}</span>
            <p className="text-xl font-black mt-1 text-blue-500">{sessionCorrect}</p>
          </div>
          <div className="p-3">
            <span className="text-xs opacity-65">{t.wrongCount}</span>
            <p className="text-xl font-black mt-1 text-rose-500">{sessionWrong}</p>
          </div>
        </div>

        <div className="pt-4 flex flex-col gap-3">
          <button
            onClick={() => setIsSelectingMode(true)}
            className="w-full py-3.5 rounded-2xl font-bold border transition-all hover:bg-black/5 dark:hover:bg-white/5"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            تغییر فیلترها و مرور دوباره
          </button>
          <button
            onClick={onClose}
            className="w-full py-3.5 rounded-2xl font-bold text-white shadow-md transition-all hover:opacity-95"
            style={{ backgroundColor: primaryColor }}
          >
            {t.doneButton}
          </button>
        </div>
      </div>
    );
  }

  // ── 3. ACTIVE REVIEW RUNNER ──
  const currentCard = sessionCards[currentIndex];
  const currentQuiz = preparedQuizCards[currentIndex];
  const total = sessionCards.length;

  return (
    <div className="max-w-2xl mx-auto px-4 py-4 pb-28 space-y-4 animate-fadeIn">
      {/* Top Session Bar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <button
            onClick={() => setIsSelectingMode(true)}
            className="p-2 rounded-xl border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            title="بازگشت به فیلترها"
          >
            <Sliders size={17} />
          </button>
          <div>
            <span className="text-xs font-bold uppercase tracking-wider opacity-60">
              {selectedMode === 'QUIZ' ? 'کوئیز چهارگزینه‌ای' : 'فلش‌کارت'} • {reviewTypeLabel(selectedReviewType)}
            </span>
            <p className="text-xs opacity-80">
              {currentIndex + 1} از {total} کارت
            </p>
          </div>
        </div>

        {/* Counters (Correct ✓ / Wrong ✕) */}
        <div className="flex items-center gap-2">
          <span className="px-2.5 py-1 rounded-xl text-xs font-bold bg-rose-500/15 text-rose-500">
            {sessionWrong} ✕
          </span>
          <span className="px-2.5 py-1 rounded-xl text-xs font-bold bg-emerald-500/15 text-emerald-500">
            {sessionCorrect} ✓
          </span>
        </div>
      </div>

      {/* Progress Track */}
      <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-1.5 overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-300"
          style={{
            width: `${((currentIndex + 1) / total) * 100}%`,
            backgroundColor: primaryColor,
          }}
        />
      </div>

      {/* ── QUIZ MODE (Stable options, NO jitter, NO scramble!) ── */}
      {selectedMode === 'QUIZ' && currentQuiz && (
        <div className="space-y-4">
          {/* Question Prompt Card */}
          <div
            className="rounded-3xl p-6 sm:p-8 border shadow-md text-center space-y-3"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <div className="flex items-center justify-between text-xs opacity-60">
              <span>{currentQuiz.item.category?.name || 'عمومی'}</span>
              <span>{diffLabel(currentQuiz.item.difficultyState.current)}</span>
            </div>

            <div className="flex items-center justify-center gap-3 pt-2">
              <h2 className="text-3xl sm:text-4xl font-black">{currentQuiz.promptText}</h2>
              {!reverseDirection && (
                <SpeechButton text={currentQuiz.item.sourceContent.text} lang="es-ES" size={24} />
              )}
            </div>

            {/* Hint & Note Toggles */}
            <div className="flex items-center justify-center gap-2 pt-2">
              {currentQuiz.item.sourceContent.notes && (
                <button
                  onClick={() => setShowNote(!showNote)}
                  className="px-3 py-1 rounded-xl text-xs font-bold border hover:bg-black/5 dark:hover:bg-white/5 transition-colors opacity-75"
                  style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
                >
                  {showNote ? 'پنهان کردن یادداشت' : '💡 نمایش یادداشت'}
                </button>
              )}
              {currentQuiz.item.sourceContent.example && (
                <button
                  onClick={() => setHintRevealed(!hintRevealed)}
                  className="px-3 py-1 rounded-xl text-xs font-bold border hover:bg-black/5 dark:hover:bg-white/5 transition-colors opacity-75"
                  style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
                >
                  {hintRevealed ? 'پنهان کردن راهنما' : '🔍 جمله نمونه (راهنما)'}
                </button>
              )}
            </div>

            {showNote && currentQuiz.item.sourceContent.notes && (
              <p className="text-xs opacity-80 p-2.5 rounded-xl bg-black/5 dark:bg-white/5 max-w-sm mx-auto animate-fadeIn">
                {currentQuiz.item.sourceContent.notes}
              </p>
            )}

            {hintRevealed && currentQuiz.item.sourceContent.example && (
              <p className="text-xs font-mono italic opacity-85 p-2.5 rounded-xl bg-amber-500/10 border border-amber-500/20 max-w-sm mx-auto animate-fadeIn">
                "{currentQuiz.item.sourceContent.example}"
              </p>
            )}
          </div>

          {/* 4 Stable Choices (fixed order for this card, NO scramble) */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {currentQuiz.options.map((option, idx) => {
              const isCorrectOption =
                option.trim().toLowerCase() === currentQuiz.correctAnswerText.trim().toLowerCase();
              const isSelectedOption = selectedOption === option;

              let btnClass = 'p-4 rounded-2xl border text-sm sm:text-base font-bold transition-all shadow-xs flex items-center justify-between text-right';
              let customStyle: React.CSSProperties = {
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: isDark ? theme.borderDark : theme.borderLight,
              };

              if (isAnswered) {
                if (isCorrectOption) {
                  customStyle = {
                    backgroundColor: '#10B981',
                    borderColor: '#059669',
                    color: '#FFFFFF',
                  };
                } else if (isSelectedOption && !isCorrectOption) {
                  customStyle = {
                    backgroundColor: '#EF4444',
                    borderColor: '#DC2626',
                    color: '#FFFFFF',
                  };
                }
              }

              return (
                <button
                  key={`${currentIndex}-${idx}`}
                  disabled={isAnswered}
                  onClick={() => handleSelectQuizOption(option)}
                  className={btnClass}
                  style={customStyle}
                >
                  <span className="flex-1 text-right">{option}</span>
                  {isAnswered && isCorrectOption && (
                    <span className="w-6 h-6 rounded-full bg-white/20 flex items-center justify-center text-white text-xs font-black shrink-0 mr-2">
                      ✓
                    </span>
                  )}
                  {isAnswered && isSelectedOption && !isCorrectOption && (
                    <span className="w-6 h-6 rounded-full bg-white/20 flex items-center justify-center text-white text-xs font-black shrink-0 mr-2">
                      ✕
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          {/* Feedback & Next Card Action */}
          {isAnswered && (
            <div
              className="p-4 rounded-2xl border flex items-center justify-between animate-fadeIn shadow-sm"
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div>
                <p className="font-extrabold text-sm">
                  {selectedOption?.trim().toLowerCase() ===
                  currentQuiz.correctAnswerText.trim().toLowerCase()
                    ? '✓ آفرین! پاسخ کاملاً درست است.'
                    : `✕ پاسخ نادرست. پاسخ صحیح: "${currentQuiz.correctAnswerText}"`}
                </p>
              </div>

              <button
                onClick={advanceCard}
                className="flex items-center gap-1.5 px-5 py-2.5 rounded-xl font-bold text-white shadow-md hover:opacity-90 active:scale-95 transition-all text-xs"
                style={{ backgroundColor: primaryColor }}
              >
                <span>کارت بعدی</span>
                <ChevronLeft size={16} />
              </button>
            </div>
          )}
        </div>
      )}

      {/* ── FLASHCARD MODE ── */}
      {selectedMode === 'FLASHCARD' && currentCard && (
        <div className="space-y-4">
          <div
            onClick={() => setIsFlipped(!isFlipped)}
            className="cursor-pointer min-h-[300px] sm:min-h-[340px] rounded-3xl p-6 sm:p-8 border shadow-md flex flex-col justify-between transition-all hover:shadow-lg relative overflow-hidden select-none"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            {/* Header info */}
            <div className="flex items-center justify-between text-xs">
              <span
                className="px-2.5 py-1 rounded-full font-bold uppercase text-[10px] tracking-wider"
                style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
              >
                {stageLabel(currentCard.learningState.stage)}
              </span>
              <div className="flex items-center gap-2">
                <span className="text-[11px] opacity-60 font-medium">
                  {diffLabel(currentCard.difficultyState.current)}
                </span>
                {!reverseDirection && (
                  <SpeechButton text={currentCard.sourceContent.text} lang="es-ES" size={20} />
                )}
              </div>
            </div>

            {/* Word Center */}
            <div className="text-center py-6 space-y-3">
              <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
                {reverseDirection ? currentCard.targetContent.text : currentCard.sourceContent.text}
              </h2>

              {currentCard.sourceContent.pronunciation && !reverseDirection && (
                <p className="text-sm opacity-60 font-mono">
                  [{currentCard.sourceContent.pronunciation}]
                </p>
              )}

              {/* Revealed Answer & Details */}
              {isFlipped ? (
                <div className="pt-4 border-t space-y-3 animate-fadeIn" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
                  <p className="text-2xl sm:text-3xl font-extrabold text-emerald-500">
                    {reverseDirection ? currentCard.sourceContent.text : currentCard.targetContent.text}
                  </p>

                  {currentCard.sourceContent.example && (
                    <div
                      className="p-3 rounded-2xl text-xs sm:text-sm text-left max-w-lg mx-auto"
                      style={{ backgroundColor: isDark ? '#1d232c' : '#f1f5f9' }}
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-semibold opacity-90">{currentCard.sourceContent.example}</span>
                        <SpeechButton text={currentCard.sourceContent.example} lang="es-ES" size={15} />
                      </div>
                      {currentCard.targetContent.example && (
                        <p className="opacity-70 mt-1 text-right">{currentCard.targetContent.example}</p>
                      )}
                    </div>
                  )}

                  {currentCard.sourceContent.notes && (
                    <p className="text-xs opacity-75 max-w-md mx-auto">💡 {currentCard.sourceContent.notes}</p>
                  )}
                </div>
              ) : (
                <p className="text-xs opacity-50 pt-8 flex items-center justify-center gap-1.5">
                  <RotateCw size={13} />
                  <span>{t.flipToSeeAnswer}</span>
                </p>
              )}
            </div>

            <div className="text-center text-[11px] opacity-45">
              {currentCard.category ? currentCard.category.name : 'عمومی'}
            </div>
          </div>

          {/* Action Buttons */}
          {isFlipped ? (
            <div className="grid grid-cols-2 gap-3">
              <button
                onClick={() => handleFlashcardAnswer(false)}
                className="py-3.5 px-3 rounded-2xl font-bold text-sm bg-rose-500/15 text-rose-500 hover:bg-rose-500 hover:text-white transition-all border border-rose-500/30 flex items-center justify-center gap-1.5 active:scale-95"
              >
                <span>✕ نادرست (بازگشت به روزانه)</span>
              </button>

              <button
                onClick={() => handleFlashcardAnswer(true)}
                className="py-3.5 px-3 rounded-2xl font-bold text-sm bg-emerald-500/15 text-emerald-500 hover:bg-emerald-500 hover:text-white transition-all border border-emerald-500/30 flex items-center justify-center gap-1.5 active:scale-95"
              >
                <span>✓ درست (ارتقای مرحله)</span>
              </button>
            </div>
          ) : (
            <button
              onClick={() => setIsFlipped(true)}
              className="w-full py-3.5 rounded-2xl font-bold text-sm text-white shadow-md hover:opacity-95 active:scale-98 transition-all"
              style={{ backgroundColor: primaryColor }}
            >
              نمایش پاسخ
            </button>
          )}
        </div>
      )}
    </div>
  );
};
