import React, { useState } from 'react';
import {
  Palette,
  Moon,
  Sun,
  Languages,
  Database,
  RefreshCw,
  Trash2,
  Download,
  Upload,
  Info,
  HelpCircle,
  Check,
  Sliders,
  Minus,
  Plus,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { THEMES, ThemeId } from '../theme/themeConfig';
import { exportFullBackup, importFullBackup } from '../storage/db';

interface SettingsScreenProps {
  onNavigate: (tab: string) => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({ onNavigate }) => {
  const {
    themeId,
    setThemeId,
    isDark,
    toggleDarkMode,
    language,
    setLanguage,
    difficultyThreshold,
    setDifficultyThreshold,
    t,
    theme,
    db,
    refreshDb,
    resetToSampleData,
    clearDatabase,
  } = useApp();

  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;
  const [statusMsg, setStatusMsg] = useState<string | null>(null);

  const handleExport = () => {
    const backup = exportFullBackup(db);
    const jsonStr = JSON.stringify(backup, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `flashlearn-backup-${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
    setStatusMsg(language === 'fa' ? 'پشتیبان با موفقیت دانلود و ذخیره شد.' : 'Backup downloaded successfully.');
    setTimeout(() => setStatusMsg(null), 3000);
  };

  const handleImportFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      const content = event.target?.result as string;
      const res = importFullBackup(content);
      if (res.success) {
        refreshDb();
        setStatusMsg(res.message);
      } else {
        alert(res.message);
      }
      setTimeout(() => setStatusMsg(null), 3000);
    };
    reader.readAsText(file);
  };

  const handleResetSample = () => {
    if (window.confirm(t.resetConfirm)) {
      resetToSampleData();
      setStatusMsg(language === 'fa' ? 'واژگان با موفقیت به نمونه پیش‌فرض اسپانیایی - فارسی بازگردانی شد.' : 'Reset to default Spanish-Persian vocabulary successfully.');
      setTimeout(() => setStatusMsg(null), 3000);
    }
  };

  const handleClear = () => {
    if (window.confirm(t.clearDbConfirm)) {
      clearDatabase();
      setStatusMsg(language === 'fa' ? 'پایگاه داده واژگان پاکسازی شد.' : 'Database cleared.');
      setTimeout(() => setStatusMsg(null), 3000);
    }
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-6 pb-28 space-y-6">
      <div>
        <h2 className="text-2xl font-black">{t.navSettings}</h2>
        <p className="text-xs opacity-65">
          {language === 'fa'
            ? 'تنظیمات پوسته ظاهری، قوانین یادگیری و مدیریت پشتیبان'
            : 'Customize appearance, learning rules, and data'}
        </p>
      </div>

      {statusMsg && (
        <div className="p-3.5 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-500 text-xs font-bold flex items-center gap-2 animate-fadeIn">
          <Check size={16} />
          <span>{statusMsg}</span>
        </div>
      )}

      {/* ── Theme Selection ── */}
      <div
        className="p-5 rounded-3xl border shadow-xs space-y-3"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <div className="flex items-center gap-2">
          <Palette size={18} style={{ color: primaryColor }} />
          <h3 className="font-bold text-sm">{t.themeLabel}</h3>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
          {(Object.keys(THEMES) as ThemeId[]).map((tid) => {
            const item = THEMES[tid];
            const isSelected = themeId === tid;
            return (
              <div
                key={tid}
                onClick={() => setThemeId(tid)}
                className={`p-3.5 rounded-2xl border cursor-pointer transition-all flex items-center justify-between ${
                  isSelected ? 'border-amber-400/80 shadow-xs' : 'opacity-70 hover:opacity-100'
                }`}
                style={{
                  backgroundColor: isDark ? theme.surfaceDark : theme.surfaceLight,
                  borderColor: isSelected ? undefined : isDark ? theme.borderDark : theme.borderLight,
                }}
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span
                      className="w-3.5 h-3.5 rounded-full"
                      style={{ backgroundColor: item.primaryLight }}
                    />
                    <h4 className="font-bold text-xs">{item.name}</h4>
                  </div>
                  <p className="text-[11px] opacity-60 mt-0.5">{item.nameFa}</p>
                </div>
                {isSelected && <Check size={16} className="text-amber-500" />}
              </div>
            );
          })}
        </div>
      </div>

      {/* ── Appearance & Language ── */}
      <div
        className="p-5 rounded-3xl border shadow-xs space-y-4"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        {/* Dark Mode */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            {isDark ? <Moon size={18} /> : <Sun size={18} />}
            <span className="text-xs font-bold">{t.darkModeLabel}</span>
          </div>
          <button
            onClick={toggleDarkMode}
            className={`w-12 h-6 rounded-full transition-colors relative p-1 ${
              isDark ? 'bg-amber-500' : 'bg-slate-300'
            }`}
          >
            <div
              className={`w-4 h-4 rounded-full bg-white transition-transform ${
                isDark ? 'translate-x-6' : 'translate-x-0'
              }`}
            />
          </button>
        </div>

        {/* Language */}
        <div className="flex items-center justify-between pt-3 border-t" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
          <div className="flex items-center gap-2.5">
            <Languages size={18} />
            <span className="text-xs font-bold">{t.languageLabel}</span>
          </div>
          <div className="flex rounded-xl p-0.5 border" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
            <button
              onClick={() => setLanguage('en')}
              className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${
                language === 'en' ? 'text-white' : 'opacity-60'
              }`}
              style={{ backgroundColor: language === 'en' ? primaryColor : 'transparent' }}
            >
              English
            </button>
            <button
              onClick={() => setLanguage('fa')}
              className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${
                language === 'fa' ? 'text-white' : 'opacity-60'
              }`}
              style={{ backgroundColor: language === 'fa' ? primaryColor : 'transparent' }}
            >
              فارسی (RTL)
            </button>
          </div>
        </div>
      </div>

      {/* ── Difficulty Threshold (تعداد پاسخ برای تغییر سطح) ── */}
      <div
        className="p-5 rounded-3xl border shadow-xs space-y-3"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <div className="flex items-center gap-2">
          <Sliders size={18} style={{ color: primaryColor }} />
          <h3 className="font-bold text-sm">{t.settingsDifficultyThreshold}</h3>
        </div>
        <p className="text-xs opacity-65 leading-relaxed">
          {t.settingsDifficultySummary}
        </p>

        <div
          className="p-4 rounded-2xl border flex items-center justify-between"
          style={{
            backgroundColor: isDark ? theme.surfaceDark : theme.surfaceLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <button
            onClick={() => setDifficultyThreshold(difficultyThreshold - 1)}
            disabled={difficultyThreshold <= 1}
            className="w-11 h-11 rounded-2xl border flex items-center justify-center font-black text-lg hover:bg-black/5 dark:hover:bg-white/5 transition-all disabled:opacity-30 disabled:pointer-events-none active:scale-95 shadow-xs"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            aria-label={t.decrease}
          >
            <Minus size={18} />
          </button>

          <div className="text-center px-4">
            <span className="text-3xl font-black">{difficultyThreshold}</span>
            <p className="text-[11px] opacity-65 font-bold mt-0.5">{t.consecutiveAnswers}</p>
          </div>

          <button
            onClick={() => setDifficultyThreshold(difficultyThreshold + 1)}
            disabled={difficultyThreshold >= 20}
            className="w-11 h-11 rounded-2xl border flex items-center justify-center font-black text-lg hover:bg-black/5 dark:hover:bg-white/5 transition-all disabled:opacity-30 disabled:pointer-events-none active:scale-95 shadow-xs"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            aria-label={t.increase}
          >
            <Plus size={18} />
          </button>
        </div>
      </div>

      {/* ── Data Management ── */}
      <div
        className="p-5 rounded-3xl border shadow-xs space-y-3"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <div className="flex items-center gap-2">
          <Database size={18} style={{ color: primaryColor }} />
          <h3 className="font-bold text-sm">{t.dataManagement}</h3>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-1">
          <button
            onClick={handleExport}
            className="flex items-center justify-center gap-2 p-3 rounded-2xl border text-xs font-bold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            <Download size={15} />
            <span>{t.exportBackupBtn}</span>
          </button>

          <label
            className="flex items-center justify-center gap-2 p-3 rounded-2xl border text-xs font-bold hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            <Upload size={15} />
            <span>{t.importBackupBtn}</span>
            <input type="file" accept=".json" onChange={handleImportFile} className="hidden" />
          </label>

          <button
            onClick={handleResetSample}
            className="flex items-center justify-center gap-2 p-3 rounded-2xl border text-xs font-bold text-amber-500 hover:bg-amber-500/10 transition-colors"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            <RefreshCw size={15} />
            <span>{t.resetSeedBtn}</span>
          </button>

          <button
            onClick={handleClear}
            className="flex items-center justify-center gap-2 p-3 rounded-2xl border text-xs font-bold text-rose-500 hover:bg-rose-500/10 transition-colors"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            <Trash2 size={15} />
            <span>{t.clearDbBtn}</span>
          </button>
        </div>
      </div>

      {/* ── Help and About Links ── */}
      <div className="grid grid-cols-2 gap-3 pt-2">
        <button
          onClick={() => onNavigate('help')}
          className="flex items-center justify-center gap-2 p-3 rounded-2xl border text-xs font-bold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
        >
          <HelpCircle size={16} />
          <span>{t.navHelp}</span>
        </button>

        <button
          onClick={() => onNavigate('about')}
          className="flex items-center justify-center gap-2 p-3 rounded-2xl border text-xs font-bold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
        >
          <Info size={16} />
          <span>{t.navAbout}</span>
        </button>
      </div>
    </div>
  );
};
