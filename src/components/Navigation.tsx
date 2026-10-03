import React from 'react';
import {
  Home,
  BookOpen,
  Library,
  BarChart3,
  Settings,
  Plus,
  AlertCircle,
  Moon,
  Sun,
  Languages,
} from 'lucide-react';
import { useApp } from '../context/AppContext';

export type NavTab =
  | 'home'
  | 'review'
  | 'library'
  | 'progress'
  | 'settings'
  | 'add_word'
  | 'needs_review'
  | 'about'
  | 'help'
  | 'backup'
  | 'category_selection';

interface NavigationProps {
  currentTab: NavTab;
  onSelectTab: (tab: NavTab) => void;
  pendingReviewCount?: number;
}

export const Navigation: React.FC<NavigationProps> = ({
  currentTab,
  onSelectTab,
  pendingReviewCount = 0,
}) => {
  const { theme, isDark, toggleDarkMode, language, setLanguage, t } = useApp();

  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;
  const isRtl = language === 'fa';

  return (
    <>
      {/* Top Header Bar */}
      <header
        className="sticky top-0 z-40 w-full backdrop-blur-md border-b transition-colors px-4 py-3 flex items-center justify-between"
        style={{
          backgroundColor: isDark ? `${theme.surfaceDark}CC` : `${theme.surfaceLight}CC`,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <div className="flex items-center gap-2.5 cursor-pointer" onClick={() => onSelectTab('home')}>
          <div
            className="w-9 h-9 rounded-xl flex items-center justify-center font-bold text-white shadow-sm"
            style={{ backgroundColor: primaryColor }}
          >
            FL
          </div>
          <div>
            <h1 className="font-extrabold text-base tracking-tight leading-tight">{t.appName}</h1>
            <p className="text-[11px] opacity-60 leading-none">v6.82 • ES 🇪🇸 ⇄ FA 🇮🇷</p>
          </div>
        </div>

        {/* Right Actions */}
        <div className="flex items-center gap-1.5 sm:gap-2">
          {pendingReviewCount > 0 && (
            <button
              onClick={() => onSelectTab('needs_review')}
              className="relative p-2 rounded-xl text-amber-500 hover:bg-amber-500/10 transition-colors"
              title={t.navNeedsReview}
            >
              <AlertCircle size={19} />
              <span className="absolute top-1 right-1 w-2.5 h-2.5 bg-amber-500 rounded-full animate-pulse" />
            </button>
          )}

          {/* Language Switch */}
          <button
            onClick={() => setLanguage(language === 'en' ? 'fa' : 'en')}
            className="flex items-center gap-1 px-2.5 py-1.5 rounded-xl text-xs font-semibold border transition-all hover:bg-black/5 dark:hover:bg-white/5"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            title="Switch Language (English / Persian)"
          >
            <Languages size={15} />
            <span>{language === 'en' ? 'FA' : 'EN'}</span>
          </button>

          {/* Dark / Light Mode */}
          <button
            onClick={toggleDarkMode}
            className="p-2 rounded-xl border transition-all hover:bg-black/5 dark:hover:bg-white/5"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            title={t.darkModeLabel}
          >
            {isDark ? <Sun size={17} className="text-amber-400" /> : <Moon size={17} />}
          </button>

          {/* Quick Add Word Button */}
          <button
            onClick={() => onSelectTab('add_word')}
            className="hidden sm:flex items-center gap-1 px-3 py-1.5 rounded-xl text-xs font-bold text-white shadow-sm transition-all hover:opacity-90 active:scale-95"
            style={{ backgroundColor: primaryColor }}
          >
            <Plus size={16} />
            <span>{t.addWord}</span>
          </button>
        </div>
      </header>

      {/* Bottom Navigation Bar for Mobile and Desktop Viewport */}
      <nav
        className="fixed bottom-0 left-0 right-0 z-40 border-t backdrop-blur-lg px-2 sm:px-6 py-2 flex items-center justify-around transition-colors"
        style={{
          backgroundColor: isDark ? `${theme.surfaceDark}F2` : `${theme.surfaceLight}F2`,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <button
          onClick={() => onSelectTab('home')}
          className={`flex flex-col items-center py-1 px-3 rounded-2xl transition-all ${
            currentTab === 'home' ? 'font-bold' : 'opacity-60 hover:opacity-100'
          }`}
          style={{ color: currentTab === 'home' ? primaryColor : undefined }}
        >
          <Home size={20} />
          <span className="text-[11px] mt-1">{t.navHome}</span>
        </button>

        <button
          onClick={() => onSelectTab('review')}
          className={`flex flex-col items-center py-1 px-3 rounded-2xl transition-all ${
            currentTab === 'review' ? 'font-bold' : 'opacity-60 hover:opacity-100'
          }`}
          style={{ color: currentTab === 'review' ? primaryColor : undefined }}
        >
          <BookOpen size={20} />
          <span className="text-[11px] mt-1">{t.navReview}</span>
        </button>

        {/* Center Floating Plus CTA on Mobile */}
        <button
          onClick={() => onSelectTab('add_word')}
          className="flex sm:hidden flex-col items-center -mt-5 p-3 rounded-full text-white shadow-lg transition-transform active:scale-90"
          style={{ backgroundColor: primaryColor }}
          title={t.addWord}
        >
          <Plus size={22} />
        </button>

        <button
          onClick={() => onSelectTab('library')}
          className={`flex flex-col items-center py-1 px-3 rounded-2xl transition-all ${
            currentTab === 'library' ? 'font-bold' : 'opacity-60 hover:opacity-100'
          }`}
          style={{ color: currentTab === 'library' ? primaryColor : undefined }}
        >
          <Library size={20} />
          <span className="text-[11px] mt-1">{t.navLibrary}</span>
        </button>

        <button
          onClick={() => onSelectTab('progress')}
          className={`flex flex-col items-center py-1 px-3 rounded-2xl transition-all ${
            currentTab === 'progress' ? 'font-bold' : 'opacity-60 hover:opacity-100'
          }`}
          style={{ color: currentTab === 'progress' ? primaryColor : undefined }}
        >
          <BarChart3 size={20} />
          <span className="text-[11px] mt-1">{t.navProgress}</span>
        </button>

        <button
          onClick={() => onSelectTab('settings')}
          className={`flex flex-col items-center py-1 px-3 rounded-2xl transition-all ${
            currentTab === 'settings' ? 'font-bold' : 'opacity-60 hover:opacity-100'
          }`}
          style={{ color: currentTab === 'settings' ? primaryColor : undefined }}
        >
          <Settings size={20} />
          <span className="text-[11px] mt-1">{t.navSettings}</span>
        </button>
      </nav>
    </>
  );
};
