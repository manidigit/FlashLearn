import React, { useState } from 'react';
import { ArrowLeft, Save, Plus, Tag } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { EntryType, FullVocabularyItem } from '../types';

interface AddWordFormScreenProps {
  initialItem?: FullVocabularyItem | null;
  onBack: () => void;
  onSaved: () => void;
}

export const AddWordFormScreen: React.FC<AddWordFormScreenProps> = ({
  initialItem,
  onBack,
  onSaved,
}) => {
  const { theme, isDark, t, db, saveConceptWord } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const [spanish, setSpanish] = useState<string>(initialItem?.sourceContent.text || '');
  const [persian, setPersian] = useState<string>(initialItem?.targetContent.text || '');
  const [categoryId, setCategoryId] = useState<string>(
    initialItem?.concept.categoryId || db.categories[0]?.id || 'cat-general'
  );
  const [entryType, setEntryType] = useState<EntryType>(initialItem?.concept.entryType || 'WORD');
  const [exampleEs, setExampleEs] = useState<string>(initialItem?.sourceContent.example || '');
  const [exampleFa, setExampleFa] = useState<string>(initialItem?.targetContent.example || '');
  const [notes, setNotes] = useState<string>(initialItem?.sourceContent.notes || '');
  const [grammarNote, setGrammarNote] = useState<string>(initialItem?.sourceContent.grammarNote || '');

  const [newCatName, setNewCatName] = useState<string>('');
  const [showNewCatInput, setShowNewCatInput] = useState<boolean>(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!spanish.trim() || !persian.trim()) {
      setErrorMsg('Both Spanish word and Persian translation are required.');
      return;
    }

    saveConceptWord({
      id: initialItem?.concept.id,
      spanish: spanish.trim(),
      persian: persian.trim(),
      categoryId: categoryId || undefined,
      entryType: entryType as any,
      exampleEs: exampleEs.trim() || undefined,
      exampleFa: exampleFa.trim() || undefined,
      notes: notes.trim() || undefined,
      grammarNote: grammarNote.trim() || undefined,
    });

    onSaved();
  };

  const handleCreateCategory = () => {
    if (!newCatName.trim()) return;
    const catId = `cat-${Date.now()}`;
    db.categories.push({ id: catId, name: newCatName.trim() });
    setCategoryId(catId);
    setNewCatName('');
    setShowNewCatInput(false);
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
          <h2 className="text-2xl font-black">
            {initialItem ? t.editWordTitle : t.addWord}
          </h2>
          <p className="text-xs opacity-65">Enter vocabulary and contextual details</p>
        </div>
      </div>

      {errorMsg && (
        <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-500 text-xs font-bold">
          {errorMsg}
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-4 text-sm font-medium">
        {/* Spanish text */}
        <div className="space-y-1.5">
          <label className="text-xs font-bold opacity-80">{t.sourceWord} *</label>
          <input
            type="text"
            required
            value={spanish}
            onChange={(e) => setSpanish(e.target.value)}
            placeholder="e.g. casa, viaje, aprender..."
            className="w-full px-4 py-3 rounded-2xl border outline-hidden transition-all focus:ring-2 focus:ring-amber-500/40"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          />
        </div>

        {/* Persian translation */}
        <div className="space-y-1.5">
          <label className="text-xs font-bold opacity-80">{t.targetTranslation} *</label>
          <input
            type="text"
            required
            dir="rtl"
            value={persian}
            onChange={(e) => setPersian(e.target.value)}
            placeholder="مثال: خانه، سفر، یاد گرفتن..."
            className="w-full px-4 py-3 rounded-2xl border outline-hidden transition-all focus:ring-2 focus:ring-amber-500/40"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          />
        </div>

        {/* Category & Entry Type */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="space-y-1.5">
            <div className="flex items-center justify-between">
              <label className="text-xs font-bold opacity-80">{t.categoryName}</label>
              <button
                type="button"
                onClick={() => setShowNewCatInput(!showNewCatInput)}
                className="text-[11px] font-bold text-amber-500 hover:underline"
              >
                + New Category
              </button>
            </div>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="w-full px-3 py-3 rounded-2xl border outline-hidden"
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              {db.categories.map((c) => (
                <option key={c.id} value={c.id} className={isDark ? 'bg-slate-900' : 'bg-white'}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold opacity-80">{t.entryType}</label>
            <select
              value={entryType}
              onChange={(e) => setEntryType(e.target.value as any)}
              className="w-full px-3 py-3 rounded-2xl border outline-hidden"
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <option value="WORD" className={isDark ? 'bg-slate-900' : 'bg-white'}>
                Word (واژه)
              </option>
              <option value="PHRASE" className={isDark ? 'bg-slate-900' : 'bg-white'}>
                Phrase (عبارت)
              </option>
              <option value="SENTENCE" className={isDark ? 'bg-slate-900' : 'bg-white'}>
                Sentence (جمله)
              </option>
              <option value="IDIOM" className={isDark ? 'bg-slate-900' : 'bg-white'}>
                Idiom (اصطلاح)
              </option>
            </select>
          </div>
        </div>

        {/* Add new category inline if opened */}
        {showNewCatInput && (
          <div
            className="flex items-center gap-2 p-3 rounded-2xl border animate-fadeIn"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <input
              type="text"
              value={newCatName}
              onChange={(e) => setNewCatName(e.target.value)}
              placeholder="New category title..."
              className="flex-1 bg-transparent text-xs font-semibold outline-hidden"
            />
            <button
              type="button"
              onClick={handleCreateCategory}
              className="px-3 py-1.5 rounded-xl text-xs font-bold text-white shadow-xs"
              style={{ backgroundColor: primaryColor }}
            >
              Add
            </button>
          </div>
        )}

        {/* Spanish Example */}
        <div className="space-y-1.5">
          <label className="text-xs font-bold opacity-80">{t.exampleSentence} (Spanish)</label>
          <input
            type="text"
            value={exampleEs}
            onChange={(e) => setExampleEs(e.target.value)}
            placeholder="e.g. Mi casa es acogedora."
            className="w-full px-4 py-2.5 rounded-2xl border outline-hidden"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          />
        </div>

        {/* Persian Example */}
        <div className="space-y-1.5">
          <label className="text-xs font-bold opacity-80">{t.exampleSentence} (Persian)</label>
          <input
            type="text"
            dir="rtl"
            value={exampleFa}
            onChange={(e) => setExampleFa(e.target.value)}
            placeholder="ترجمه جمله مثال..."
            className="w-full px-4 py-2.5 rounded-2xl border outline-hidden"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          />
        </div>

        {/* Notes */}
        <div className="space-y-1.5">
          <label className="text-xs font-bold opacity-80">{t.notes}</label>
          <input
            type="text"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="e.g. اسم مؤنث، کلمه پرکاربرد..."
            className="w-full px-4 py-2.5 rounded-2xl border outline-hidden"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          />
        </div>

        {/* Grammar Note */}
        <div className="space-y-1.5">
          <label className="text-xs font-bold opacity-80">{t.grammarNotes}</label>
          <input
            type="text"
            value={grammarNote}
            onChange={(e) => setGrammarNote(e.target.value)}
            placeholder="e.g. با حرف تعریف la، فعل باقاعده..."
            className="w-full px-4 py-2.5 rounded-2xl border outline-hidden"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          />
        </div>

        <div className="pt-4 flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={onBack}
            className="px-5 py-3 rounded-2xl font-bold text-xs border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
          >
            {t.cancel}
          </button>
          <button
            type="submit"
            className="flex items-center gap-1.5 px-6 py-3 rounded-2xl font-bold text-xs text-white shadow-md hover:opacity-95 active:scale-95 transition-all"
            style={{ backgroundColor: primaryColor }}
          >
            <Save size={16} />
            <span>{t.saveChanges}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
