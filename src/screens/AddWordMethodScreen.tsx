import React, { useState } from 'react';
import {
  FileText,
  UploadCloud,
  Database,
  Copy,
  Plus,
  CheckCircle2,
  Trash2,
  AlertTriangle,
} from 'lucide-react';
import { useApp } from '../context/AppContext';

interface AddWordMethodScreenProps {
  onSelectMethod: (method: 'single' | 'bulk' | 'backup') => void;
}

export const AddWordMethodScreen: React.FC<AddWordMethodScreenProps> = ({ onSelectMethod }) => {
  const { theme, isDark, t, findDuplicates, cleanDuplicates } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const [dupInfo, setDupInfo] = useState<{
    groups: any[];
    extraCount: number;
  } | null>(null);
  const [cleanedMsg, setCleanedMsg] = useState<string | null>(null);

  const handleCheckDuplicates = () => {
    setCleanedMsg(null);
    const result = findDuplicates();
    setDupInfo(result);
  };

  const handleCleanDuplicates = () => {
    const count = cleanDuplicates();
    setDupInfo(null);
    setCleanedMsg(`${count} duplicate entries removed successfully.`);
  };

  return (
    <div className="max-w-xl mx-auto px-4 py-6 pb-24 space-y-4">
      <div>
        <h2 className="text-2xl font-black">{t.addWordMethodTitle}</h2>
        <p className="text-xs opacity-65 mt-0.5">
          Choose how you would like to expand your vocabulary
        </p>
      </div>

      <div className="space-y-3 pt-2">
        {/* Single Word Entry */}
        <div
          onClick={() => onSelectMethod('single')}
          className="group cursor-pointer p-5 rounded-2xl border shadow-xs transition-all hover:shadow-md hover:-translate-y-0.5 flex items-center gap-4"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div
            className="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform"
            style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
          >
            <Plus size={24} />
          </div>
          <div className="flex-1">
            <h3 className="font-extrabold text-base">{t.singleWordEntry}</h3>
            <p className="text-xs opacity-65 mt-0.5">{t.singleWordDesc}</p>
          </div>
        </div>

        {/* Bulk Import */}
        <div
          onClick={() => onSelectMethod('bulk')}
          className="group cursor-pointer p-5 rounded-2xl border shadow-xs transition-all hover:shadow-md hover:-translate-y-0.5 flex items-center gap-4"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div
            className="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform bg-blue-500/15 text-blue-500"
          >
            <UploadCloud size={24} />
          </div>
          <div className="flex-1">
            <h3 className="font-extrabold text-base">{t.bulkImportTitle}</h3>
            <p className="text-xs opacity-65 mt-0.5">{t.bulkImportDesc}</p>
          </div>
        </div>

        {/* Restore Backup */}
        <div
          onClick={() => onSelectMethod('backup')}
          className="group cursor-pointer p-5 rounded-2xl border shadow-xs transition-all hover:shadow-md hover:-translate-y-0.5 flex items-center gap-4"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div
            className="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform bg-purple-500/15 text-purple-500"
          >
            <Database size={24} />
          </div>
          <div className="flex-1">
            <h3 className="font-extrabold text-base">{t.restoreBackupTitle}</h3>
            <p className="text-xs opacity-65 mt-0.5">{t.restoreBackupDesc}</p>
          </div>
        </div>

        {/* Duplicate Finder & Cleaner Utility */}
        <div
          className="p-5 rounded-2xl border shadow-xs space-y-3"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl flex items-center justify-center bg-amber-500/15 text-amber-500 shrink-0">
              <Copy size={20} />
            </div>
            <div>
              <h3 className="font-extrabold text-sm">{t.findDuplicatesTitle}</h3>
              <p className="text-xs opacity-65">{t.findDuplicatesDesc}</p>
            </div>
          </div>

          <div className="flex items-center gap-2 pt-1">
            <button
              onClick={handleCheckDuplicates}
              className="px-4 py-2 rounded-xl text-xs font-bold border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
              style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            >
              Scan for Duplicates
            </button>

            {dupInfo && dupInfo.extraCount > 0 && (
              <button
                onClick={handleCleanDuplicates}
                className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold bg-rose-500 text-white shadow-xs hover:bg-rose-600 transition-colors"
              >
                <Trash2 size={14} />
                <span>{t.cleanDuplicatesBtn}</span>
              </button>
            )}
          </div>

          {dupInfo && (
            <div className="text-xs pt-1">
              {dupInfo.extraCount > 0 ? (
                <div className="p-3 rounded-xl bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20">
                  <p className="font-bold">
                    {t.duplicatesFound(dupInfo.groups.length, dupInfo.extraCount)}
                  </p>
                  <ul className="mt-1 list-disc list-inside space-y-0.5 opacity-90">
                    {dupInfo.groups.slice(0, 3).map((g, idx) => (
                      <li key={idx}>
                        "{g.key}" ({g.items.length} occurrences)
                      </li>
                    ))}
                    {dupInfo.groups.length > 3 && (
                      <li>...and {dupInfo.groups.length - 3} more groups</li>
                    )}
                  </ul>
                </div>
              ) : (
                <p className="text-emerald-500 font-semibold">{t.noDuplicatesFound}</p>
              )}
            </div>
          )}

          {cleanedMsg && (
            <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 text-xs font-bold border border-emerald-500/20">
              {cleanedMsg}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
