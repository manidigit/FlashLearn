import React from 'react';
import {
  Flame,
  Calendar,
  Sparkles,
  Plus,
  BookOpen,
  ArrowRight,
  CheckCircle2,
  Clock,
  Layers,
  HelpCircle,
  Info,
  Database,
  Shuffle,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { ReviewType } from '../types';

interface HomeScreenProps {
  onStartReview: (type: ReviewType) => void;
  onNavigate: (tab: string) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({ onStartReview, onNavigate }) => {
  const { theme, isDark, language, t, progressSummary, streak, db, vocabulary } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const totalWords = vocabulary.length;
  const learnedWords = vocabulary.filter((v) => v.learningState.stage === 'LEARNED').length;
  const practicedWords = vocabulary.filter(
    (v) => v.learningState.totalCorrect > 0 || v.learningState.totalWrong > 0
  ).length;
  const unpracticedWords = Math.max(0, totalWords - practicedWords);

  // Counts ready vs total for each stage
  const dailyTotal = vocabulary.filter((v) => v.learningState.stage === 'DAILY').length;
  const weeklyTotal = vocabulary.filter((v) => v.learningState.stage === 'WEEKLY').length;
  const monthlyTotal = vocabulary.filter((v) => v.learningState.stage === 'MONTHLY').length;

  const dailyReady = progressSummary.dailyDueConceptCount;
  const weeklyReady = progressSummary.weeklyDueConceptCount;
  const monthlyReady = progressSummary.monthlyDueConceptCount;
  const totalReady = progressSummary.dueConceptCount;

  const progressPercent = totalWords > 0 ? Math.round((learnedWords / totalWords) * 100) : 0;

  return (
    <div className="max-w-4xl mx-auto px-4 py-6 pb-24 space-y-6">
      {/* ── Top greeting & Language Pair ── */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl sm:text-3xl font-black tracking-tight">{t.greeting}</h2>
          <p className="text-xs sm:text-sm opacity-70 mt-0.5">{t.tagline}</p>
        </div>
        <div
          className="flex items-center gap-2 px-3 py-1.5 rounded-2xl border text-sm font-semibold shadow-xs"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <span className="text-lg">🇪🇸</span>
          <span className="text-xs opacity-50">⇄</span>
          <span className="text-lg">🇮🇷</span>
        </div>
      </div>

      {/* ── Streak Hero Banner ── */}
      <div
        className="rounded-3xl p-5 border flex items-center gap-4 transition-all shadow-sm"
        style={{
          backgroundColor: isDark ? `${theme.primaryDark}15` : `${theme.primaryLight}12`,
          borderColor: isDark ? `${theme.primaryDark}40` : `${theme.primaryLight}30`,
        }}
      >
        <div
          className="w-14 h-14 rounded-2xl flex items-center justify-center shadow-inner"
          style={{ backgroundColor: isDark ? `${theme.primaryDark}30` : `${theme.primaryLight}25` }}
        >
          <Flame size={32} style={{ color: primaryColor }} className="animate-pulse" />
        </div>
        <div className="flex-1">
          <div className="flex items-baseline gap-2">
            <span className="text-2xl sm:text-3xl font-extrabold" style={{ color: primaryColor }}>
              {streak.currentStreakDays}
            </span>
            <span className="text-sm font-bold opacity-80">{t.streakHeroLabel}</span>
          </div>
          <p className="text-xs opacity-70 mt-0.5">
            {t.learningStreak} • {language === 'fa' ? 'عادت روزانه مطالعه را حفظ کنید' : 'Keep up your daily habit'}
          </p>
        </div>
        <button
          onClick={() => onStartReview('DAILY')}
          className="hidden sm:flex items-center gap-1.5 px-4 py-2 rounded-2xl text-xs font-bold text-white shadow-sm hover:opacity-90 active:scale-95 transition-all"
          style={{ backgroundColor: primaryColor }}
        >
          <span>{t.daily}</span>
          <ArrowRight size={14} />
        </button>
      </div>

      {/* ── Progress Bar ── */}
      <div
        className="rounded-3xl p-5 border shadow-xs space-y-3"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sparkles size={18} style={{ color: primaryColor }} />
            <span className="text-sm font-bold">{t.learningProgress}</span>
          </div>
          <span className="text-base font-extrabold" style={{ color: primaryColor }}>
            {progressPercent}%
          </span>
        </div>
        <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-3 overflow-hidden">
          <div
            className="h-full rounded-full transition-all duration-500 ease-out"
            style={{
              width: `${Math.min(100, Math.max(0, progressPercent))}%`,
              backgroundColor: primaryColor,
            }}
          />
        </div>
        <div className="flex justify-between text-[11px] opacity-65">
          <span>
            {learnedWords} {t.learnedWords}
          </span>
          <span>
            {totalWords} {t.totalWords}
          </span>
        </div>
      </div>

      {/* ── Statistics Summary Tiles ── */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-bold opacity-80">{t.statsSummary}</h3>
          <button
            onClick={() => onNavigate('progress')}
            className="text-xs font-semibold hover:underline"
            style={{ color: primaryColor }}
          >
            {language === 'fa' ? 'مشاهده همه آمار ←' : 'View all stats →'}
          </button>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div
            className="p-4 rounded-2xl border shadow-xs flex flex-col justify-between"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <span className="text-xs font-medium opacity-65">{t.totalWords}</span>
            <span className="text-2xl font-black mt-2">{totalWords}</span>
          </div>
          <div
            className="p-4 rounded-2xl border shadow-xs flex flex-col justify-between"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <span className="text-xs font-medium opacity-65">{t.practicedWords}</span>
            <span className="text-2xl font-black mt-2 text-blue-500">{practicedWords}</span>
          </div>
          <div
            className="p-4 rounded-2xl border shadow-xs flex flex-col justify-between"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <span className="text-xs font-medium opacity-65">{t.unpracticedWords}</span>
            <span className="text-2xl font-black mt-2 text-amber-500">{unpracticedWords}</span>
          </div>
          <div
            className="p-4 rounded-2xl border shadow-xs flex flex-col justify-between"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <span className="text-xs font-medium opacity-65">{t.learnedWords}</span>
            <span className="text-2xl font-black mt-2 text-emerald-500">{learnedWords}</span>
          </div>
        </div>
      </div>

      {/* ── Ready Reviews Section ── */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <h3 className="text-base font-bold">{t.readyReviews}</h3>
            {totalReady > 0 && (
              <span
                className="px-2 py-0.5 rounded-full text-xs font-extrabold text-white"
                style={{ backgroundColor: primaryColor }}
              >
                {t.wordsCount(totalReady)}
              </span>
            )}
          </div>
          <button
            onClick={() => onStartReview('RANDOM')}
            className="flex items-center gap-1 text-xs font-bold px-3 py-1.5 rounded-xl border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            <Shuffle size={14} />
            <span>{t.random}</span>
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {/* Daily Card */}
          <div
            onClick={() => onStartReview('DAILY')}
            className="group cursor-pointer rounded-2xl p-4 border shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between min-h-[110px]"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <div className="flex items-start justify-between">
              <div>
                <span className="text-base font-bold">{t.daily}</span>
                <p className="text-xs opacity-60 mt-0.5">{t.dailySubtitle}</p>
              </div>
              <div
                className="w-8 h-8 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform"
                style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
              >
                <Clock size={16} />
              </div>
            </div>
            <div className="flex items-baseline justify-between mt-4">
              <span className="text-2xl font-black" style={{ color: primaryColor }}>
                {dailyReady}
              </span>
              <span className="text-xs opacity-60">{t.readyOfTotal(dailyTotal)}</span>
            </div>
          </div>

          {/* Weekly Card */}
          <div
            onClick={() => onStartReview('WEEKLY')}
            className="group cursor-pointer rounded-2xl p-4 border shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between min-h-[110px]"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <div className="flex items-start justify-between">
              <div>
                <span className="text-base font-bold">{t.weekly}</span>
                <p className="text-xs opacity-60 mt-0.5">{t.weeklySubtitle}</p>
              </div>
              <div
                className="w-8 h-8 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform"
                style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
              >
                <Calendar size={16} />
              </div>
            </div>
            <div className="flex items-baseline justify-between mt-4">
              <span className="text-2xl font-black" style={{ color: primaryColor }}>
                {weeklyReady}
              </span>
              <span className="text-xs opacity-60">{t.readyOfTotal(weeklyTotal)}</span>
            </div>
          </div>

          {/* Monthly Card */}
          <div
            onClick={() => onStartReview('MONTHLY')}
            className="group cursor-pointer rounded-2xl p-4 border shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md flex flex-col justify-between min-h-[110px]"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <div className="flex items-start justify-between">
              <div>
                <span className="text-base font-bold">{t.monthly}</span>
                <p className="text-xs opacity-60 mt-0.5">{t.monthlySubtitle}</p>
              </div>
              <div
                className="w-8 h-8 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform"
                style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
              >
                <Layers size={16} />
              </div>
            </div>
            <div className="flex items-baseline justify-between mt-4">
              <span className="text-2xl font-black" style={{ color: primaryColor }}>
                {monthlyReady}
              </span>
              <span className="text-xs opacity-60">{t.readyOfTotal(monthlyTotal)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* ── Primary Action Buttons ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
        <button
          onClick={() => onStartReview(dailyReady > 0 ? 'DAILY' : weeklyReady > 0 ? 'WEEKLY' : 'RANDOM')}
          className="flex items-center justify-center gap-2 py-3.5 px-4 rounded-2xl font-bold text-white shadow-md hover:opacity-95 active:scale-98 transition-all"
          style={{ backgroundColor: primaryColor }}
        >
          <BookOpen size={18} />
          <span>
            {language === 'fa'
              ? totalReady > 0
                ? `شروع مرور (${totalReady} واژه آماده)`
                : 'شروع مرور تمرینی'
              : totalReady > 0
              ? `Start Review (${totalReady} due)`
              : 'Start Practice Review'}
          </span>
        </button>

        <button
          onClick={() => onNavigate('add_word')}
          className="flex items-center justify-center gap-2 py-3.5 px-4 rounded-2xl font-bold border transition-all hover:bg-black/5 dark:hover:bg-white/5 active:scale-98 shadow-xs"
          style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
        >
          <Plus size={18} />
          <span>{t.addWord}</span>
        </button>
      </div>

      {/* ── Secondary Quick Utilities ── */}
      <div className="flex items-center justify-center gap-4 pt-4 text-xs opacity-75">
        <button onClick={() => onNavigate('backup')} className="flex items-center gap-1 hover:underline">
          <Database size={14} />
          <span>{t.navBackup}</span>
        </button>
        <span>•</span>
        <button onClick={() => onNavigate('help')} className="flex items-center gap-1 hover:underline">
          <HelpCircle size={14} />
          <span>{t.navHelp}</span>
        </button>
        <span>•</span>
        <button onClick={() => onNavigate('about')} className="flex items-center gap-1 hover:underline">
          <Info size={14} />
          <span>{t.navAbout}</span>
        </button>
      </div>
    </div>
  );
};
