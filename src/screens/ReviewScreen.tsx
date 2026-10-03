import React, { useState, useEffect, useMemo } from 'react';
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
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { FullVocabularyItem, ReviewType } from '../types';
import { isDueForReview, isReviewedToday } from '../domain/algorithms';
import { SpeechButton } from '../components/SpeechButton';

interface ReviewScreenProps {
  initialReviewType?: ReviewType;
  onClose: () => void;
}

export const ReviewScreen: React.FC<ReviewScreenProps> = ({
  initialReviewType = 'DAILY',
  onClose,
}) => {
  const { theme, isDark, t, vocabulary, recordReviewResult } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const [reviewType, setReviewType] = useState<ReviewType>(initialReviewType);
  const [reviewMode, setReviewMode] = useState<'flashcard' | 'quiz'>('flashcard');
  const [isFlipped, setIsFlipped] = useState<boolean>(false);
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [reverseDirection, setReverseDirection] = useState<boolean>(false); // false: ES -> FA, true: FA -> ES

  // Quiz state
  const [selectedQuizAnswer, setSelectedQuizAnswer] = useState<string | null>(null);
  const [isQuizAnswered, setIsQuizAnswered] = useState<boolean>(false);

  // Session summary counters
  const [sessionCorrect, setSessionCorrect] = useState<number>(0);
  const [sessionWrong, setSessionWrong] = useState<number>(0);
  const [isCompleted, setIsCompleted] = useState<boolean>(false);

  // Build the review queue based on reviewType and eligibility
  const queue = useMemo(() => {
    let items = vocabulary.filter((v) => {
      // Exclude already reviewed today unless random review or no others available
      if (reviewType !== 'RANDOM' && isReviewedToday(v.learningState.lastReviewedAt)) {
        return false;
      }

      if (reviewType === 'RANDOM') {
        return v.learningState.stage !== 'LEARNED';
      }
      if (reviewType === 'LEARNED') {
        return v.learningState.stage === 'LEARNED';
      }
      return v.learningState.stage === reviewType && isDueForReview(v.learningState);
    });

    // Fallback: If no cards are strictly due right now, allow practicing words from that stage
    if (items.length === 0) {
      items = vocabulary.filter((v) => {
        if (reviewType === 'RANDOM') return true;
        return v.learningState.stage === reviewType;
      });
    }

    // If still empty, use all vocabulary
    if (items.length === 0) {
      items = vocabulary;
    }

    // Shuffle queue for varied practice
    return [...items].sort(() => Math.random() - 0.5);
  }, [vocabulary, reviewType]);

  const currentItem: FullVocabularyItem | undefined = queue[currentIndex];

  // Prepare 4 choices for Quiz mode
  const quizChoices = useMemo(() => {
    if (!currentItem) return [];
    const correctAnswer = reverseDirection
      ? currentItem.sourceContent.text
      : currentItem.targetContent.text;

    // Get other words as distractors
    const otherItems = vocabulary.filter((v) => v.concept.id !== currentItem.concept.id);
    const shuffledOthers = [...otherItems].sort(() => Math.random() - 0.5);
    const distractors: string[] = [];

    for (const item of shuffledOthers) {
      const text = reverseDirection ? item.sourceContent.text : item.targetContent.text;
      if (text !== correctAnswer && !distractors.includes(text)) {
        distractors.push(text);
      }
      if (distractors.length >= 3) break;
    }

    // Fallbacks if fewer than 3 distractors exist
    const fallbacks = reverseDirection
      ? ['amigo', 'tiempo', 'casa', 'gracias', 'viaje', 'comida']
      : ['سلام', 'خانه', 'ممنون', 'سفر', 'غذا', 'زمان'];
    for (const f of fallbacks) {
      if (distractors.length >= 3) break;
      if (f !== correctAnswer && !distractors.includes(f)) {
        distractors.push(f);
      }
    }

    const allChoices = [correctAnswer, ...distractors.slice(0, 3)];
    return allChoices.sort(() => Math.random() - 0.5);
  }, [currentItem, reverseDirection, vocabulary]);

  const advanceCard = () => {
    setIsFlipped(false);
    setSelectedQuizAnswer(null);
    setIsQuizAnswered(false);

    if (currentIndex + 1 >= queue.length) {
      setIsCompleted(true);
    } else {
      setCurrentIndex((prev) => prev + 1);
    }
  };

  const handleFlashcardAnswer = (isCorrect: boolean) => {
    if (!currentItem) return;
    recordReviewResult(currentItem.concept.id, isCorrect, reviewType);
    if (isCorrect) setSessionCorrect((p) => p + 1);
    else setSessionWrong((p) => p + 1);
    advanceCard();
  };

  const handleQuizSelect = (choice: string) => {
    if (isQuizAnswered || !currentItem) return;
    const correctAnswer = reverseDirection
      ? currentItem.sourceContent.text
      : currentItem.targetContent.text;
    const isCorrect = choice.trim().toLowerCase() === correctAnswer.trim().toLowerCase();

    setSelectedQuizAnswer(choice);
    setIsQuizAnswered(true);

    recordReviewResult(currentItem.concept.id, isCorrect, reviewType);
    if (isCorrect) setSessionCorrect((p) => p + 1);
    else setSessionWrong((p) => p + 1);
  };

  // ── Session Finished Screen ──
  if (isCompleted || queue.length === 0) {
    const totalDone = sessionCorrect + sessionWrong;
    const accuracy = totalDone > 0 ? Math.round((sessionCorrect / totalDone) * 100) : 100;

    return (
      <div className="max-w-md mx-auto px-4 py-12 text-center space-y-6">
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
            onClick={() => {
              setCurrentIndex(0);
              setIsCompleted(false);
              setSessionCorrect(0);
              setSessionWrong(0);
            }}
            className="w-full py-3.5 rounded-2xl font-bold border transition-all hover:bg-black/5 dark:hover:bg-white/5"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            Review Again
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

  const questionWord = reverseDirection
    ? currentItem.targetContent.text
    : currentItem.sourceContent.text;
  const answerWord = reverseDirection
    ? currentItem.sourceContent.text
    : currentItem.targetContent.text;

  return (
    <div className="max-w-2xl mx-auto px-4 py-4 pb-28 space-y-4">
      {/* ── Top Bar ── */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <button
            onClick={onClose}
            className="p-2 rounded-xl border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            title={t.closeReview}
          >
            <X size={18} />
          </button>
          <div>
            <span className="text-xs font-bold uppercase tracking-wider opacity-60">
              {reviewType} REVIEW
            </span>
            <p className="text-xs opacity-80">
              {t.remainingCards(queue.length - currentIndex, queue.length)}
            </p>
          </div>
        </div>

        {/* Mode Toggle & Direction Toggle */}
        <div className="flex items-center gap-1.5">
          <button
            onClick={() => setReverseDirection(!reverseDirection)}
            className="flex items-center gap-1 px-2.5 py-1.5 rounded-xl border text-xs font-medium hover:bg-black/5 dark:hover:bg-white/5"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            title="Switch Language Direction"
          >
            <ArrowLeftRight size={13} />
            <span>{reverseDirection ? 'FA → ES' : 'ES → FA'}</span>
          </button>

          <div
            className="flex rounded-xl p-0.5 border"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            <button
              onClick={() => {
                setReviewMode('flashcard');
                setIsFlipped(false);
              }}
              className={`px-2.5 py-1 text-xs font-bold rounded-lg transition-all ${
                reviewMode === 'flashcard' ? 'text-white shadow-xs' : 'opacity-60'
              }`}
              style={{ backgroundColor: reviewMode === 'flashcard' ? primaryColor : 'transparent' }}
            >
              {t.flashcardMode}
            </button>
            <button
              onClick={() => {
                setReviewMode('quiz');
                setSelectedQuizAnswer(null);
                setIsQuizAnswered(false);
              }}
              className={`px-2.5 py-1 text-xs font-bold rounded-lg transition-all ${
                reviewMode === 'quiz' ? 'text-white shadow-xs' : 'opacity-60'
              }`}
              style={{ backgroundColor: reviewMode === 'quiz' ? primaryColor : 'transparent' }}
            >
              {t.quizMode}
            </button>
          </div>
        </div>
      </div>

      {/* Progress line */}
      <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-1.5 overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-300"
          style={{
            width: `${((currentIndex + 1) / queue.length) * 100}%`,
            backgroundColor: primaryColor,
          }}
        />
      </div>

      {/* ── Review Content ── */}
      {reviewMode === 'flashcard' ? (
        /* ── FLASHCARD MODE ── */
        <div className="space-y-4">
          <div
            onClick={() => setIsFlipped(!isFlipped)}
            className="cursor-pointer min-h-[300px] sm:min-h-[340px] rounded-3xl p-6 sm:p-8 border shadow-md flex flex-col justify-between transition-all hover:shadow-lg relative overflow-hidden select-none"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            {/* Stage & Difficulty Badge */}
            <div className="flex items-center justify-between text-xs">
              <span
                className="px-2.5 py-1 rounded-full font-bold uppercase text-[10px] tracking-wider"
                style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
              >
                {currentItem.learningState.stage}
              </span>

              <div className="flex items-center gap-2">
                <span className="text-[11px] opacity-60 font-medium">
                  {currentItem.difficultyState.current}
                </span>
                {!reverseDirection && (
                  <SpeechButton text={currentItem.sourceContent.text} lang="es-ES" size={20} />
                )}
              </div>
            </div>

            {/* Word Center Display */}
            <div className="text-center py-6 space-y-3">
              <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
                {questionWord}
              </h2>
              {currentItem.sourceContent.pronunciation && !reverseDirection && (
                <p className="text-sm opacity-60 font-mono">
                  [{currentItem.sourceContent.pronunciation}]
                </p>
              )}

              {/* Revealed Answer & Details */}
              {isFlipped ? (
                <div className="pt-4 border-t space-y-3 animate-fadeIn" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
                  <p className="text-2xl sm:text-3xl font-extrabold text-emerald-500">
                    {answerWord}
                  </p>

                  {currentItem.sourceContent.example && (
                    <div
                      className="p-3 rounded-2xl text-xs sm:text-sm text-left max-w-lg mx-auto"
                      style={{ backgroundColor: isDark ? '#1d232c' : '#f1f5f9' }}
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-semibold opacity-90">{currentItem.sourceContent.example}</span>
                        <SpeechButton text={currentItem.sourceContent.example} lang="es-ES" size={15} />
                      </div>
                      {currentItem.targetContent.example && (
                        <p className="opacity-70 mt-1 text-right">{currentItem.targetContent.example}</p>
                      )}
                    </div>
                  )}

                  {(currentItem.sourceContent.notes || currentItem.sourceContent.grammarNote) && (
                    <div className="text-xs opacity-75 max-w-md mx-auto space-y-1">
                      {currentItem.sourceContent.notes && <p>💡 {currentItem.sourceContent.notes}</p>}
                      {currentItem.sourceContent.grammarNote && (
                        <p>📘 {currentItem.sourceContent.grammarNote}</p>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                <p className="text-xs opacity-50 pt-8 flex items-center justify-center gap-1.5">
                  <RotateCw size={13} />
                  <span>{t.flipToSeeAnswer}</span>
                </p>
              )}
            </div>

            {/* Bottom info */}
            <div className="text-center text-[11px] opacity-45">
              {currentItem.category ? currentItem.category.name : 'FlashLearn Vocabulary'}
            </div>
          </div>

          {/* Action Buttons */}
          <div className="grid grid-cols-3 gap-3">
            <button
              onClick={() => handleFlashcardAnswer(false)}
              className="py-3.5 px-3 rounded-2xl font-bold text-sm bg-rose-500/15 text-rose-500 hover:bg-rose-500 hover:text-white transition-all border border-rose-500/30 flex items-center justify-center gap-1.5 active:scale-95"
            >
              <RotateCw size={16} />
              <span>{t.againButton}</span>
            </button>

            <button
              onClick={advanceCard}
              className="py-3.5 px-3 rounded-2xl font-semibold text-sm border hover:bg-black/5 dark:hover:bg-white/5 transition-all flex items-center justify-center gap-1"
              style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            >
              <span>{t.skipButton}</span>
            </button>

            <button
              onClick={() => handleFlashcardAnswer(true)}
              className="py-3.5 px-3 rounded-2xl font-bold text-sm bg-emerald-500/15 text-emerald-500 hover:bg-emerald-500 hover:text-white transition-all border border-emerald-500/30 flex items-center justify-center gap-1.5 active:scale-95"
            >
              <Check size={18} />
              <span>{t.goodButton}</span>
            </button>
          </div>
        </div>
      ) : (
        /* ── 4-CHOICE QUIZ MODE ── */
        <div className="space-y-4">
          <div
            className="rounded-3xl p-6 sm:p-8 border shadow-md text-center space-y-3"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <span className="text-xs font-semibold opacity-65">
              {reverseDirection ? t.quizQuestionReverse : t.quizQuestion}
            </span>
            <div className="flex items-center justify-center gap-3">
              <h2 className="text-3xl sm:text-4xl font-black">{questionWord}</h2>
              {!reverseDirection && (
                <SpeechButton text={currentItem.sourceContent.text} lang="es-ES" size={24} />
              )}
            </div>
            {currentItem.sourceContent.example && (
              <p className="text-xs opacity-75 font-mono italic max-w-sm mx-auto">
                "{currentItem.sourceContent.example}"
              </p>
            )}
          </div>

          {/* 4 Choices */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {quizChoices.map((choice, i) => {
              const correctAnswer = reverseDirection
                ? currentItem.sourceContent.text
                : currentItem.targetContent.text;
              const isCorrectOption =
                choice.trim().toLowerCase() === correctAnswer.trim().toLowerCase();
              const isSelected = selectedQuizAnswer === choice;

              let btnStyle = {
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: isDark ? theme.borderDark : theme.borderLight,
                color: 'inherit',
              };

              if (isQuizAnswered) {
                if (isCorrectOption) {
                  btnStyle = {
                    backgroundColor: '#10B981',
                    borderColor: '#059669',
                    color: '#FFFFFF',
                  };
                } else if (isSelected && !isCorrectOption) {
                  btnStyle = {
                    backgroundColor: '#EF4444',
                    borderColor: '#DC2626',
                    color: '#FFFFFF',
                  };
                }
              }

              return (
                <button
                  key={i}
                  disabled={isQuizAnswered}
                  onClick={() => handleQuizSelect(choice)}
                  className="p-4 rounded-2xl border text-left sm:text-center font-bold text-base transition-all shadow-xs hover:border-slate-400 active:scale-98 disabled:cursor-default"
                  style={btnStyle}
                >
                  <span className="text-xs opacity-60 mr-2">{String.fromCharCode(65 + i)}.</span>
                  <span>{choice}</span>
                </button>
              );
            })}
          </div>

          {/* Quiz Feedback & Next Button */}
          {isQuizAnswered && (
            <div
              className="p-4 rounded-2xl border flex items-center justify-between animate-fadeIn"
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div>
                <p className="font-extrabold text-sm">
                  {selectedQuizAnswer?.trim().toLowerCase() ===
                  (reverseDirection
                    ? currentItem.sourceContent.text
                    : currentItem.targetContent.text
                  ).trim().toLowerCase()
                    ? t.quizCorrect
                    : t.quizIncorrect}
                </p>
                {currentItem.sourceContent.notes && (
                  <p className="text-xs opacity-70 mt-0.5">💡 {currentItem.sourceContent.notes}</p>
                )}
              </div>

              <button
                onClick={advanceCard}
                className="flex items-center gap-1.5 px-5 py-2.5 rounded-xl font-bold text-white shadow-sm hover:opacity-90 active:scale-95 transition-all text-xs"
                style={{ backgroundColor: primaryColor }}
              >
                <span>{t.nextCard}</span>
                <ChevronRight size={16} />
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
