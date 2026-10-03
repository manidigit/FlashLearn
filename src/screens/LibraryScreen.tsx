import React, { useState, useMemo } from 'react';
import {
  Search,
  Filter,
  Star,
  Plus,
  Volume2,
  Tag,
  ArrowUpDown,
  BookOpen,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { FullVocabularyItem, Stage, VocabularyDifficulty } from '../types';
import { LibraryDetailModal } from './LibraryDetailModal';
import { SpeechButton } from '../components/SpeechButton';

interface LibraryScreenProps {
  onAddWord: () => void;
  onEditWord: (item: FullVocabularyItem) => void;
}

export const LibraryScreen: React.FC<LibraryScreenProps> = ({ onAddWord, onEditWord }) => {
  const { theme, isDark, t, vocabulary, db, toggleFavorite } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedStage, setSelectedStage] = useState<Stage | 'ALL'>('ALL');
  const [selectedDifficulty, setSelectedDifficulty] = useState<VocabularyDifficulty | 'ALL'>('ALL');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [favoritesOnly, setFavoritesOnly] = useState<boolean>(false);
  const [activeDetailItem, setActiveDetailItem] = useState<FullVocabularyItem | null>(null);

  const filteredWords = useMemo(() => {
    return vocabulary.filter((v) => {
      // Search filter
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase().trim();
        const esMatch = v.sourceContent.text.toLowerCase().includes(q);
        const faMatch = v.targetContent.text.toLowerCase().includes(q);
        const noteMatch = (v.sourceContent.notes || '').toLowerCase().includes(q);
        if (!esMatch && !faMatch && !noteMatch) return false;
      }

      // Stage filter
      if (selectedStage !== 'ALL' && v.learningState.stage !== selectedStage) {
        return false;
      }

      // Difficulty filter
      if (selectedDifficulty !== 'ALL' && v.difficultyState.current !== selectedDifficulty) {
        return false;
      }

      // Category filter
      if (selectedCategory !== 'ALL' && v.concept.categoryId !== selectedCategory) {
        return false;
      }

      // Favorites filter
      if (favoritesOnly && !v.concept.favorite) {
        return false;
      }

      return true;
    });
  }, [vocabulary, searchQuery, selectedStage, selectedDifficulty, selectedCategory, favoritesOnly]);

  return (
    <div className="max-w-4xl mx-auto px-4 py-6 pb-28 space-y-5">
      {/* ── Header ── */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-black">{t.navLibrary}</h2>
          <p className="text-xs opacity-65">
            {filteredWords.length} of {vocabulary.length} words
          </p>
        </div>
        <button
          onClick={onAddWord}
          className="flex items-center gap-1.5 px-4 py-2 rounded-2xl text-xs font-bold text-white shadow-sm hover:opacity-90 active:scale-95 transition-all"
          style={{ backgroundColor: primaryColor }}
        >
          <Plus size={16} />
          <span>{t.addWord}</span>
        </button>
      </div>

      {/* ── Search Bar ── */}
      <div
        className="flex items-center gap-3 px-4 py-3 rounded-2xl border shadow-xs"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <Search size={18} className="opacity-50" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder={t.searchPlaceholder}
          className="flex-1 bg-transparent text-sm font-medium outline-hidden"
        />
        {searchQuery && (
          <button
            onClick={() => setSearchQuery('')}
            className="text-xs opacity-60 hover:opacity-100 font-bold"
          >
            Clear
          </button>
        )}
      </div>

      {/* ── Stage Filters ── */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none">
        {(['ALL', 'DAILY', 'WEEKLY', 'MONTHLY', 'LEARNED'] as const).map((stage) => {
          const isSelected = selectedStage === stage;
          return (
            <button
              key={stage}
              onClick={() => setSelectedStage(stage)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition-all border ${
                isSelected ? 'text-white shadow-xs' : 'opacity-70 hover:opacity-100'
              }`}
              style={{
                backgroundColor: isSelected ? primaryColor : isDark ? theme.cardDark : theme.cardLight,
                borderColor: isSelected ? primaryColor : isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              {stage === 'ALL'
                ? t.filterAll
                : stage === 'DAILY'
                ? t.stageDaily
                : stage === 'WEEKLY'
                ? t.stageWeekly
                : stage === 'MONTHLY'
                ? t.stageMonthly
                : t.stageLearned}
            </button>
          );
        })}
      </div>

      {/* ── Secondary Filters (Category, Difficulty, Favorites) ── */}
      <div className="flex flex-wrap items-center gap-2 text-xs">
        {/* Category selector */}
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="px-3 py-1.5 rounded-xl border font-medium bg-transparent outline-hidden"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <option value="ALL" className={isDark ? 'bg-slate-900' : 'bg-white'}>
            {t.categoryAll}
          </option>
          {db.categories.map((c) => (
            <option key={c.id} value={c.id} className={isDark ? 'bg-slate-900' : 'bg-white'}>
              {c.name}
            </option>
          ))}
        </select>

        {/* Difficulty selector */}
        <select
          value={selectedDifficulty}
          onChange={(e) => setSelectedDifficulty(e.target.value as any)}
          className="px-3 py-1.5 rounded-xl border font-medium bg-transparent outline-hidden"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <option value="ALL" className={isDark ? 'bg-slate-900' : 'bg-white'}>
            {t.difficultyAll}
          </option>
          <option value="EASY" className={isDark ? 'bg-slate-900' : 'bg-white'}>
            {t.diffEasy}
          </option>
          <option value="MEDIUM" className={isDark ? 'bg-slate-900' : 'bg-white'}>
            {t.diffMedium}
          </option>
          <option value="HARD" className={isDark ? 'bg-slate-900' : 'bg-white'}>
            {t.diffHard}
          </option>
          <option value="VERY_HARD" className={isDark ? 'bg-slate-900' : 'bg-white'}>
            {t.diffVeryHard}
          </option>
        </select>

        {/* Favorites button */}
        <button
          onClick={() => setFavoritesOnly(!favoritesOnly)}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl border font-bold transition-all ${
            favoritesOnly ? 'text-amber-500 border-amber-500 bg-amber-500/10' : 'opacity-70'
          }`}
          style={{ borderColor: favoritesOnly ? undefined : isDark ? theme.borderDark : theme.borderLight }}
        >
          <Star size={13} className={favoritesOnly ? 'fill-amber-500' : ''} />
          <span>Favorites</span>
        </button>
      </div>

      {/* ── Vocabulary Grid ── */}
      {filteredWords.length === 0 ? (
        <div
          className="p-12 text-center rounded-3xl border shadow-xs space-y-3"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <BookOpen size={36} className="mx-auto opacity-35" />
          <p className="text-sm font-medium opacity-70">{t.emptyLibrary}</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {filteredWords.map((item) => (
            <div
              key={item.concept.id}
              onClick={() => setActiveDetailItem(item)}
              className="group cursor-pointer rounded-2xl p-4 border shadow-xs transition-all hover:shadow-md hover:-translate-y-0.5 flex flex-col justify-between"
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div className="flex items-start justify-between gap-2">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-lg font-black group-hover:text-amber-500 transition-colors">
                      {item.sourceContent.text}
                    </h3>
                    <SpeechButton text={item.sourceContent.text} lang="es-ES" size={15} />
                  </div>
                  <p className="text-sm font-bold text-emerald-500 mt-0.5">
                    {item.targetContent.text}
                  </p>
                </div>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleFavorite(item.concept.id);
                  }}
                  className="p-1 text-amber-400 hover:scale-110 transition-transform"
                >
                  <Star
                    size={16}
                    className={item.concept.favorite ? 'fill-amber-400' : 'opacity-30'}
                  />
                </button>
              </div>

              {item.sourceContent.example && (
                <p className="text-xs opacity-60 italic mt-2 line-clamp-1">
                  "{item.sourceContent.example}"
                </p>
              )}

              <div className="flex items-center justify-between text-[10px] mt-3 pt-2 border-t opacity-70" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
                <span className="font-semibold uppercase tracking-wider">
                  {item.learningState.stage}
                </span>
                <span>{item.category?.name || 'General'}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ── Word Detail Modal ── */}
      {activeDetailItem && (
        <LibraryDetailModal
          item={activeDetailItem}
          onClose={() => setActiveDetailItem(null)}
          onEdit={(it) => {
            setActiveDetailItem(null);
            onEditWord(it);
          }}
        />
      )}
    </div>
  );
};
