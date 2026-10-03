import React from 'react';
import { ArrowLeft, Github, MessageCircle, Info, Code, ShieldCheck, Heart } from 'lucide-react';
import { useApp } from '../context/AppContext';

interface AboutScreenProps {
  onBack: () => void;
}

export const AboutScreen: React.FC<AboutScreenProps> = ({ onBack }) => {
  const { theme, isDark, t } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  return (
    <div className="max-w-xl mx-auto px-4 py-6 pb-28 space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={onBack}
          className="p-2 rounded-xl border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-2xl font-black">{t.navAbout}</h2>
          <p className="text-xs opacity-65">App overview, version history & credits</p>
        </div>
      </div>

      {/* Main card */}
      <div
        className="p-6 rounded-3xl border shadow-xs text-center space-y-4"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <div
          className="w-16 h-16 mx-auto rounded-3xl flex items-center justify-center font-black text-2xl text-white shadow-md"
          style={{ backgroundColor: primaryColor }}
        >
          FL
        </div>
        <div>
          <h3 className="text-xl font-black">{t.appName}</h3>
          <p className="text-xs font-semibold opacity-70 mt-1">{t.aboutVersion}</p>
        </div>
        <p className="text-xs sm:text-sm opacity-80 max-w-md mx-auto leading-relaxed">
          FlashLearn is an offline vocabulary learning app for Spanish and Persian built around an authoritative spaced-repetition engine, adaptive theme system, multi-line parser, and 4-choice quiz generation.
        </p>
      </div>

      {/* Technical details list */}
      <div
        className="p-5 rounded-3xl border shadow-xs space-y-3 text-xs"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <h4 className="font-bold text-sm opacity-85">Technical Details</h4>
        <div className="space-y-2 divide-y" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
          <div className="flex justify-between py-1.5">
            <span className="opacity-60">{t.aboutAuthor}</span>
            <span className="font-bold">{t.aboutAuthorName}</span>
          </div>
          <div className="flex justify-between py-1.5">
            <span className="opacity-60">Version Code</span>
            <span className="font-mono font-bold">682</span>
          </div>
          <div className="flex justify-between py-1.5">
            <span className="opacity-60">Engine</span>
            <span className="font-bold">Daily → Weekly → Monthly → Learned</span>
          </div>
          <div className="flex justify-between py-1.5">
            <span className="opacity-60">Database</span>
            <span className="font-bold">Offline-First Schema v2 Compatible</span>
          </div>
          <div className="flex justify-between py-1.5">
            <span className="opacity-60">Visual Tokens</span>
            <span className="font-bold">ThemeDesign v3.3-ADAPTIVE</span>
          </div>
        </div>
      </div>

      {/* External links */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <a
          href="https://github.com/manidigit/FlashLearn"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center justify-center gap-2 p-3.5 rounded-2xl border text-xs font-bold hover:bg-black/5 dark:hover:bg-white/5 transition-all shadow-xs"
          style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
        >
          <Github size={16} />
          <span>GitHub Repository</span>
        </a>

        <a
          href="https://wa.me/34685644444"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center justify-center gap-2 p-3.5 rounded-2xl border text-xs font-bold hover:bg-black/5 dark:hover:bg-white/5 transition-all shadow-xs text-emerald-500"
          style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
        >
          <MessageCircle size={16} />
          <span>{t.aboutWhatsApp}</span>
        </a>
      </div>

      <div className="text-center text-xs opacity-50 pt-2 flex items-center justify-center gap-1">
        <span>FlashLearn © 2026 • Crafted with</span>
        <Heart size={12} className="text-rose-500 fill-rose-500" />
      </div>
    </div>
  );
};
