import React, { useState } from 'react';
import {
  ArrowLeft,
  UploadCloud,
  AlertTriangle,
  CheckCircle,
  FileText,
  Check,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { parseVocabulary, ParseResult } from '../domain/parser';
import { ImportMode } from '../types';

interface BulkImportScreenProps {
  onBack: () => void;
  onSuccess: () => void;
}

export const BulkImportScreen: React.FC<BulkImportScreenProps> = ({ onBack, onSuccess }) => {
  const { theme, isDark, t, bulkImportEntries } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const [rawText, setRawText] = useState<string>(
`casa = خانه
note: اسم مؤنث
Mi casa es pequeña. = خانه من کوچک است.

---

perro = سگ
note: حیوان باوفا

---

libro = کتاب
note: اسم مذکر

---

ciudad = شهر
note: اسم مؤنث`
  );

  const [parseResult, setParseResult] = useState<ParseResult | null>(null);
  const [importMode, setImportMode] = useState<ImportMode>('ADD_NEW');
  const [importedMessage, setImportedMessage] = useState<string | null>(null);

  const handleParse = () => {
    if (!rawText.trim()) return;
    const res = parseVocabulary(rawText);
    setParseResult(res);
  };

  const handleImport = () => {
    if (!parseResult || parseResult.entries.length === 0) return;
    const count = bulkImportEntries(parseResult.entries);
    setImportedMessage(t.importSuccessMsg(count));
    setTimeout(() => {
      onSuccess();
    }, 1200);
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-6 pb-28 space-y-5">
      <div className="flex items-center gap-3">
        <button
          onClick={onBack}
          className="p-2 rounded-xl border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-2xl font-black">{t.bulkImportTitle}</h2>
          <p className="text-xs opacity-65">
            Smart multi-line Spanish-Persian vocabulary parser
          </p>
        </div>
      </div>

      {importedMessage && (
        <div className="p-4 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-500 text-sm font-bold flex items-center gap-2 animate-fadeIn">
          <CheckCircle size={18} />
          <span>{importedMessage}</span>
        </div>
      )}

      {/* Input Text Area */}
      <div className="space-y-2">
        <label className="text-xs font-bold opacity-80">
          Paste vocabulary text (supports <code className="font-mono text-amber-500">word = ترجمه</code>, notes, examples, and separators):
        </label>
        <textarea
          rows={9}
          value={rawText}
          onChange={(e) => {
            setRawText(e.target.value);
            setParseResult(null);
          }}
          placeholder={t.bulkInputPlaceholder}
          className="w-full p-4 rounded-2xl border font-mono text-xs sm:text-sm outline-hidden transition-all focus:ring-2 focus:ring-amber-500/40"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        />
      </div>

      {/* Parse Action Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2 text-xs">
          <span className="font-bold opacity-70">{t.importModeLabel}</span>
          <select
            value={importMode}
            onChange={(e) => setImportMode(e.target.value as ImportMode)}
            className="px-2.5 py-1.5 rounded-xl border font-semibold bg-transparent outline-hidden"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <option value="ADD_NEW" className={isDark ? 'bg-slate-900' : 'bg-white'}>
              Add New
            </option>
            <option value="SKIP_DUPLICATE" className={isDark ? 'bg-slate-900' : 'bg-white'}>
              Skip Duplicates
            </option>
            <option value="MERGE" className={isDark ? 'bg-slate-900' : 'bg-white'}>
              Merge Content
            </option>
          </select>
        </div>

        <button
          onClick={handleParse}
          className="px-5 py-2.5 rounded-xl text-xs font-bold text-white shadow-sm hover:opacity-90 active:scale-95 transition-all"
          style={{ backgroundColor: primaryColor }}
        >
          {t.parseTextBtn}
        </button>
      </div>

      {/* Parser Results Preview */}
      {parseResult && (
        <div
          className="rounded-3xl p-5 border shadow-sm space-y-4 animate-fadeIn"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div className="flex items-center justify-between border-b pb-3" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
            <div className="flex items-center gap-3">
              <span className="text-xs font-bold text-emerald-500 flex items-center gap-1">
                <Check size={14} />
                {t.parsedEntriesCount(parseResult.entries.length)}
              </span>
              {parseResult.warnings.length > 0 && (
                <span className="text-xs font-bold text-amber-500 flex items-center gap-1">
                  <AlertTriangle size={14} />
                  {t.parsedWarningsCount(parseResult.warnings.length)}
                </span>
              )}
            </div>

            <button
              onClick={handleImport}
              disabled={parseResult.entries.length === 0}
              className="px-5 py-2 rounded-xl text-xs font-bold text-white shadow-md hover:opacity-95 active:scale-95 transition-all disabled:opacity-50"
              style={{ backgroundColor: primaryColor }}
            >
              {t.importNowBtn}
            </button>
          </div>

          {/* Warnings List */}
          {parseResult.warnings.length > 0 && (
            <div className="p-3 rounded-2xl bg-amber-500/10 border border-amber-500/20 space-y-1 text-xs">
              <span className="font-bold text-amber-500">Parser Warnings:</span>
              <ul className="list-disc list-inside opacity-80 space-y-0.5">
                {parseResult.warnings.map((w, idx) => (
                  <li key={idx}>
                    Line {w.lineNumber}: {w.message} ({w.rawText})
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Parsed Entries List */}
          <div className="space-y-2 max-h-60 overflow-y-auto pr-1">
            {parseResult.entries.map((entry, idx) => (
              <div
                key={idx}
                className="p-3 rounded-xl border flex items-center justify-between text-xs"
                style={{
                  backgroundColor: isDark ? theme.surfaceDark : theme.surfaceLight,
                  borderColor: isDark ? theme.borderDark : theme.borderLight,
                }}
              >
                <div>
                  <span className="font-extrabold text-sm">{entry.sourceText}</span>
                  {entry.notes && <span className="opacity-60 ml-2">({entry.notes})</span>}
                </div>
                <span className="font-bold text-emerald-500 text-sm">{entry.translationText || '---'}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
