import React, { useState } from 'react';
import { ArrowLeft, Download, Upload, CheckCircle2, Database, ShieldCheck } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { exportFullBackup, importFullBackup } from '../storage/db';

interface BackupScreenProps {
  onBack: () => void;
}

export const BackupScreen: React.FC<BackupScreenProps> = ({ onBack }) => {
  const { theme, isDark, t, db, refreshDb } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const [message, setMessage] = useState<string | null>(null);

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
    setMessage('Backup downloaded successfully.');
  };

  const handleImportFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      const content = event.target?.result as string;
      const res = importFullBackup(content);
      setMessage(res.message);
      if (res.success) {
        refreshDb();
      }
    };
    reader.readAsText(file);
  };

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
          <h2 className="text-2xl font-black">{t.navBackup}</h2>
          <p className="text-xs opacity-65">Export and restore your vocabulary and learning history</p>
        </div>
      </div>

      {message && (
        <div className="p-4 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-500 text-xs font-bold flex items-center gap-2 animate-fadeIn">
          <CheckCircle2 size={16} />
          <span>{message}</span>
        </div>
      )}

      {/* Backup info card */}
      <div
        className="p-5 rounded-3xl border shadow-xs space-y-4"
        style={{
          backgroundColor: isDark ? theme.cardDark : theme.cardLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        <div className="flex items-center gap-3">
          <div
            className="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0"
            style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
          >
            <Database size={24} />
          </div>
          <div>
            <h3 className="font-extrabold text-base">Schema v2 Full Backup</h3>
            <p className="text-xs opacity-65">
              Includes concepts, translations, examples, spaced-repetition stages, and streaks
            </p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2 text-xs opacity-80 pt-2 border-t" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
          <div>
            <span className="opacity-60">Total Concepts:</span>
            <p className="font-bold text-sm mt-0.5">{db.concepts.length}</p>
          </div>
          <div>
            <span className="opacity-60">Categories:</span>
            <p className="font-bold text-sm mt-0.5">{db.categories.length}</p>
          </div>
        </div>
      </div>

      {/* Export / Import Actions */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <button
          onClick={handleExport}
          className="flex items-center justify-center gap-2 p-4 rounded-2xl font-bold text-xs text-white shadow-md hover:opacity-95 active:scale-95 transition-all"
          style={{ backgroundColor: primaryColor }}
        >
          <Download size={18} />
          <span>{t.exportBackupBtn}</span>
        </button>

        <label
          className="flex items-center justify-center gap-2 p-4 rounded-2xl border font-bold text-xs hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition-all shadow-xs"
          style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
        >
          <Upload size={18} />
          <span>{t.importBackupBtn}</span>
          <input type="file" accept=".json" onChange={handleImportFile} className="hidden" />
        </label>
      </div>
    </div>
  );
};
