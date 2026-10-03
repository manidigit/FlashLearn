import React, { useState } from 'react';
import {
  Flame,
  Award,
  Trophy,
  CheckCircle,
  Clock,
  Layers,
  Sparkles,
  TrendingUp,
  Target,
  BookOpen,
  Zap,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { INITIAL_ACHIEVEMENTS } from '../domain/achievements';

export const ProgressScreen: React.FC = () => {
  const { theme, isDark, language, t, progressSummary, streak, vocabulary, db } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const [timeFilter, setTimeFilter] = useState<'7d' | '30d' | '90d' | 'all'>('30d');

  const totalWords = vocabulary.length;
  const stageCounts = {
    DAILY: vocabulary.filter((v) => v.learningState.stage === 'DAILY').length,
    WEEKLY: vocabulary.filter((v) => v.learningState.stage === 'WEEKLY').length,
    MONTHLY: vocabulary.filter((v) => v.learningState.stage === 'MONTHLY').length,
    LEARNED: vocabulary.filter((v) => v.learningState.stage === 'LEARNED').length,
  };

  const diffCounts = {
    EASY: vocabulary.filter((v) => v.difficultyState.current === 'EASY').length,
    MEDIUM: vocabulary.filter((v) => v.difficultyState.current === 'MEDIUM').length,
    HARD: vocabulary.filter((v) => v.difficultyState.current === 'HARD').length,
    VERY_HARD: vocabulary.filter((v) => v.difficultyState.current === 'VERY_HARD').length,
  };

  const ACHIEVEMENT_FA: Record<string, { title: string; desc: string }> = {
    first_word: { title: 'گام نخست', desc: 'افزودن اولین واژه به کتابخانه فلش‌لرن' },
    streak_3: { title: 'استمرار مقدماتی', desc: 'حفظ زنجیره ۳ روزه مطالعه متوالی' },
    streak_7: { title: 'استاد استمرار', desc: 'مرور واژگان برای ۷ روز متوالی بدون وقفه' },
    words_25: { title: 'گردآورنده واژگان', desc: 'رسیدن به ۲۵ واژه در کتابخانه' },
    learned_5: { title: 'تثبیت آغازین', desc: 'رساندن ۵ واژه به مرحله یادگرفته‌شده دائمی' },
    learned_20: { title: 'دانش‌پژوه زبان', desc: 'تسلط بر ۲۰ واژه در حافظه دائمی' },
    quiz_ace: { title: 'قهرمان آزمون', desc: 'پاسخ صحیح به ۵ سوال آزمون بدون خطا' },
    difficulty_crusher: { title: 'فاتح واژگان دشوار', desc: 'تسلط بر واژگان سطح سخت یا بسیار سخت' },
  };

  // Compute live achievement states based on current stats
  const achievements = INITIAL_ACHIEVEMENTS.map((ach) => {
    let unlocked = false;
    let prog = 0;
    if (ach.id === 'first_word') {
      unlocked = totalWords >= 1;
      prog = Math.min(100, totalWords * 100);
    } else if (ach.id === 'streak_3') {
      unlocked = streak.bestStreakDays >= 3;
      prog = Math.min(100, Math.round((streak.bestStreakDays / 3) * 100));
    } else if (ach.id === 'streak_7') {
      unlocked = streak.bestStreakDays >= 7;
      prog = Math.min(100, Math.round((streak.bestStreakDays / 7) * 100));
    } else if (ach.id === 'words_25') {
      unlocked = totalWords >= 25;
      prog = Math.min(100, Math.round((totalWords / 25) * 100));
    } else if (ach.id === 'learned_5') {
      unlocked = stageCounts.LEARNED >= 5;
      prog = Math.min(100, Math.round((stageCounts.LEARNED / 5) * 100));
    } else if (ach.id === 'learned_20') {
      unlocked = stageCounts.LEARNED >= 20;
      prog = Math.min(100, Math.round((stageCounts.LEARNED / 20) * 100));
    } else if (ach.id === 'quiz_ace') {
      unlocked = progressSummary.totalCorrect >= 5;
      prog = Math.min(100, Math.round((progressSummary.totalCorrect / 5) * 100));
    } else if (ach.id === 'difficulty_crusher') {
      unlocked = diffCounts.HARD > 0 || diffCounts.VERY_HARD > 0;
      prog = unlocked ? 100 : 0;
    }
    return { ...ach, isUnlocked: unlocked, progress: prog };
  });

  return (
    <div className="max-w-4xl mx-auto px-4 py-6 pb-28 space-y-6">
      <div>
        <h2 className="text-2xl font-black">{t.navProgress}</h2>
        <p className="text-xs opacity-65">
          {language === 'fa'
            ? 'پیگیری روند ماندگاری، استمرار روزانه و مدال‌های یادگیری'
            : 'Track your retention, consistency, and achievements'}
        </p>
      </div>

      {/* ── Top Streak & Accuracy Grid ── */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        {/* Streak card */}
        <div
          className="p-5 rounded-3xl border shadow-xs flex items-center gap-4"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div
            className="w-12 h-12 rounded-2xl flex items-center justify-center shadow-inner"
            style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
          >
            <Flame size={26} />
          </div>
          <div>
            <span className="text-2xl font-black">
              {streak.currentStreakDays} {language === 'fa' ? 'روز پیاپی' : 'days'}
            </span>
            <p className="text-xs opacity-60">
              {language === 'fa'
                ? `بهترین رکورد: ${streak.bestStreakDays} روز`
                : `Best: ${streak.bestStreakDays} days`}
            </p>
          </div>
        </div>

        {/* Accuracy card */}
        <div
          className="p-5 rounded-3xl border shadow-xs flex items-center gap-4"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div className="w-12 h-12 rounded-2xl flex items-center justify-center bg-emerald-500/20 text-emerald-500 shadow-inner">
            <Target size={26} />
          </div>
          <div>
            <span className="text-2xl font-black text-emerald-500">
              {progressSummary.accuracyPercent}%
            </span>
            <p className="text-xs opacity-60">
              {language === 'fa' ? 'دقت کلی آزمون‌ها' : 'Overall Accuracy'}
            </p>
          </div>
        </div>

        {/* Total Reviews */}
        <div
          className="p-5 rounded-3xl border shadow-xs flex items-center gap-4"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div className="w-12 h-12 rounded-2xl flex items-center justify-center bg-blue-500/20 text-blue-500 shadow-inner">
            <CheckCircle size={26} />
          </div>
          <div>
            <span className="text-2xl font-black">
              {progressSummary.totalCorrect + progressSummary.totalWrong}
            </span>
            <p className="text-xs opacity-60">
              {progressSummary.totalCorrect} {language === 'fa' ? 'درست' : 'correct'} • {progressSummary.totalWrong} {language === 'fa' ? 'نادرست' : 'wrong'}
            </p>
          </div>
        </div>
      </div>

      {/* ── Spaced Repetition Distribution ── */}
      <div
        className="p-5 rounded-3xl border shadow-xs space-y-4"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <div className="flex items-center justify-between">
          <h3 className="text-base font-bold">
            {language === 'fa' ? 'توزیع مراحل تکرار فاصله‌دار' : 'Learning Stages Distribution'}
          </h3>
          <span className="text-xs opacity-60">
            {totalWords} {language === 'fa' ? 'کل واژگان' : 'total vocabulary words'}
          </span>
        </div>

        <div className="space-y-3 text-xs font-semibold">
          {/* Daily */}
          <div>
            <div className="flex justify-between mb-1">
              <span>{t.stageDaily} {language === 'fa' ? '(امروز)' : '(Today)'}</span>
              <span>
                {stageCounts.DAILY} {language === 'fa' ? 'واژه' : 'words'} ({totalWords > 0 ? Math.round((stageCounts.DAILY / totalWords) * 100) : 0}%)
              </span>
            </div>
            <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-2 overflow-hidden">
              <div
                className="bg-amber-500 h-full rounded-full"
                style={{ width: `${totalWords > 0 ? (stageCounts.DAILY / totalWords) * 100 : 0}%` }}
              />
            </div>
          </div>

          {/* Weekly */}
          <div>
            <div className="flex justify-between mb-1">
              <span>{t.stageWeekly} {language === 'fa' ? '(تثبیت ۷ روزه)' : '(7-Day Retention)'}</span>
              <span>
                {stageCounts.WEEKLY} {language === 'fa' ? 'واژه' : 'words'} ({totalWords > 0 ? Math.round((stageCounts.WEEKLY / totalWords) * 100) : 0}%)
              </span>
            </div>
            <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-2 overflow-hidden">
              <div
                className="bg-blue-500 h-full rounded-full"
                style={{ width: `${totalWords > 0 ? (stageCounts.WEEKLY / totalWords) * 100 : 0}%` }}
              />
            </div>
          </div>

          {/* Monthly */}
          <div>
            <div className="flex justify-between mb-1">
              <span>{t.stageMonthly} {language === 'fa' ? '(حافظه ۳۰ روزه)' : '(30-Day Long-term)'}</span>
              <span>
                {stageCounts.MONTHLY} {language === 'fa' ? 'واژه' : 'words'} ({totalWords > 0 ? Math.round((stageCounts.MONTHLY / totalWords) * 100) : 0}%)
              </span>
            </div>
            <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-2 overflow-hidden">
              <div
                className="bg-purple-500 h-full rounded-full"
                style={{ width: `${totalWords > 0 ? (stageCounts.MONTHLY / totalWords) * 100 : 0}%` }}
              />
            </div>
          </div>

          {/* Learned */}
          <div>
            <div className="flex justify-between mb-1">
              <span>{t.stageLearned} {language === 'fa' ? '(دائمی)' : '(Mastered)'}</span>
              <span>
                {stageCounts.LEARNED} {language === 'fa' ? 'واژه' : 'words'} ({totalWords > 0 ? Math.round((stageCounts.LEARNED / totalWords) * 100) : 0}%)
              </span>
            </div>
            <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-2 overflow-hidden">
              <div
                className="bg-emerald-500 h-full rounded-full"
                style={{ width: `${totalWords > 0 ? (stageCounts.LEARNED / totalWords) * 100 : 0}%` }}
              />
            </div>
          </div>
        </div>
      </div>

      {/* ── Vocabulary Difficulty Spectrum ── */}
      <div
        className="p-5 rounded-3xl border shadow-xs space-y-4"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <h3 className="text-base font-bold">
          {language === 'fa' ? 'طیف سطح دشواری واژگان' : 'Difficulty Spectrum'}
        </h3>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-center">
          <div className="p-3 rounded-2xl bg-emerald-500/10 border border-emerald-500/20">
            <span className="text-xs font-bold text-emerald-500">{t.diffEasy}</span>
            <p className="text-xl font-black mt-1">{diffCounts.EASY}</p>
          </div>
          <div className="p-3 rounded-2xl bg-blue-500/10 border border-blue-500/20">
            <span className="text-xs font-bold text-blue-500">{t.diffMedium}</span>
            <p className="text-xl font-black mt-1">{diffCounts.MEDIUM}</p>
          </div>
          <div className="p-3 rounded-2xl bg-amber-500/10 border border-amber-500/20">
            <span className="text-xs font-bold text-amber-500">{t.diffHard}</span>
            <p className="text-xl font-black mt-1">{diffCounts.HARD}</p>
          </div>
          <div className="p-3 rounded-2xl bg-rose-500/10 border border-rose-500/20">
            <span className="text-xs font-bold text-rose-500">{t.diffVeryHard}</span>
            <p className="text-xl font-black mt-1">{diffCounts.VERY_HARD}</p>
          </div>
        </div>
      </div>

      {/* ── Gamification Achievements Gallery ── */}
      <div className="space-y-3">
        <div className="flex items-center gap-2">
          <Trophy size={18} style={{ color: primaryColor }} />
          <h3 className="text-base font-bold">{t.achievementsTitle}</h3>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {achievements.map((ach) => (
            <div
              key={ach.id}
              className={`p-4 rounded-2xl border shadow-xs flex items-center gap-3.5 transition-all ${
                ach.isUnlocked ? 'border-amber-400/40' : 'opacity-60'
              }`}
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: ach.isUnlocked ? undefined : isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div
                className={`w-11 h-11 rounded-2xl flex items-center justify-center shrink-0 ${
                  ach.isUnlocked
                    ? 'bg-amber-400/20 text-amber-500'
                    : 'bg-slate-200 dark:bg-slate-800 text-slate-500'
                }`}
              >
                <Award size={22} />
              </div>
              <div className="flex-1">
                <div className="flex items-center justify-between">
                  <h4 className="font-black text-sm">
                    {language === 'fa' && ACHIEVEMENT_FA[ach.id]?.title
                      ? ACHIEVEMENT_FA[ach.id].title
                      : ach.title}
                  </h4>
                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      ach.isUnlocked
                        ? 'bg-amber-500/15 text-amber-500'
                        : 'bg-slate-200 dark:bg-slate-800'
                    }`}
                  >
                    {ach.isUnlocked ? t.unlockedBadge : `${ach.progress}%`}
                  </span>
                </div>
                <p className="text-xs opacity-65 mt-0.5">
                  {language === 'fa' && ACHIEVEMENT_FA[ach.id]?.desc
                    ? ACHIEVEMENT_FA[ach.id].desc
                    : ach.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
