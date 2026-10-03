import React, { useState } from 'react';
import {
  X,
  Volume2,
  Star,
  Edit2,
  Trash2,
  Calendar,
  CheckCircle,
  AlertCircle,
  Tag,
  BookOpen,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { FullVocabularyItem } from '../types';
import { SpeechButton } from '../components/SpeechButton';

interface LibraryDetailModalProps {
  item: FullVocabularyItem;
  onClose: () => void;
  onEdit: (item: FullVocabularyItem) => void;
}

export const LibraryDetailModal: React.FC<LibraryDetailModalProps> = ({
  item,
  onClose,
  onEdit,
}) => {
  const { theme, isDark, t, toggleFavorite, deleteConcept } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const handleDelete = () => {
    if (window.confirm(t.deleteConfirm)) {
      deleteConcept(item.concept.id);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-fadeIn">
      <div
        className="w-full max-w-lg rounded-3xl border shadow-2xl p-6 sm:p-7 space-y-5 max-h-[90vh] overflow-y-auto"
        style={{
          backgroundColor: isDark ? theme.surfaceDark : theme.surfaceLight,
          borderColor: isDark ? theme.borderDark : theme.borderLight,
        }}
      >
        {/* Header Bar */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span
              className="px-2.5 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-wider"
              style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
            >
              {item.learningState.stage}
            </span>
            <span
              className="px-2.5 py-1 rounded-full text-[10px] font-bold border"
              style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            >
              {item.difficultyState.current}
            </span>
            {item.category && (
              <span className="text-xs opacity-60 flex items-center gap-1">
                <Tag size={12} />
                <span>{item.category.name}</span>
              </span>
            )}
          </div>

          <div className="flex items-center gap-1">
            <button
              onClick={() => toggleFavorite(item.concept.id)}
              className="p-2 rounded-xl hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
              title={t.favoriteToggle}
            >
              <Star
                size={18}
                className={item.concept.favorite ? 'text-amber-400 fill-amber-400' : 'opacity-40'}
              />
            </button>
            <button
              onClick={onClose}
              className="p-2 rounded-xl hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Word and Translation Hero */}
        <div className="space-y-2 border-b pb-4" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
          <div className="flex items-center gap-3">
            <h2 className="text-3xl font-black">{item.sourceContent.text}</h2>
            <SpeechButton text={item.sourceContent.text} lang="es-ES" size={22} />
          </div>
          {item.sourceContent.pronunciation && (
            <p className="text-xs font-mono opacity-60">[{item.sourceContent.pronunciation}]</p>
          )}
          <p className="text-2xl font-bold text-emerald-500 pt-1">{item.targetContent.text}</p>
        </div>

        {/* Examples Section */}
        {item.sourceContent.example && (
          <div
            className="p-4 rounded-2xl border space-y-1"
            style={{
              backgroundColor: isDark ? theme.cardDark : theme.cardLight,
              borderColor: isDark ? theme.borderDark : theme.borderLight,
            }}
          >
            <span className="text-[11px] font-bold opacity-60 uppercase tracking-wider">
              {t.exampleSentence}
            </span>
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold">{item.sourceContent.example}</p>
              <SpeechButton text={item.sourceContent.example} lang="es-ES" size={15} />
            </div>
            {item.targetContent.example && (
              <p className="text-xs opacity-75 mt-1 text-right">{item.targetContent.example}</p>
            )}
          </div>
        )}

        {/* Notes & Grammar */}
        {(item.sourceContent.notes || item.sourceContent.grammarNote) && (
          <div className="space-y-2 text-xs">
            {item.sourceContent.notes && (
              <div
                className="p-3 rounded-xl border"
                style={{
                  backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                  borderColor: isDark ? theme.borderDark : theme.borderLight,
                }}
              >
                <span className="font-bold opacity-75">{t.notes}: </span>
                <span>{item.sourceContent.notes}</span>
              </div>
            )}
            {item.sourceContent.grammarNote && (
              <div
                className="p-3 rounded-xl border"
                style={{
                  backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                  borderColor: isDark ? theme.borderDark : theme.borderLight,
                }}
              >
                <span className="font-bold opacity-75">{t.grammarNotes}: </span>
                <span>{item.sourceContent.grammarNote}</span>
              </div>
            )}
          </div>
        )}

        {/* Learning & Review Metrics */}
        <div
          className="grid grid-cols-2 gap-2 p-3 rounded-2xl border text-xs"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <div>
            <span className="opacity-60">Correct / Wrong:</span>
            <p className="font-bold mt-0.5">
              <span className="text-emerald-500">{item.learningState.totalCorrect}</span> /{' '}
              <span className="text-rose-500">{item.learningState.totalWrong}</span>
            </p>
          </div>
          <div>
            <span className="opacity-60">Next Review:</span>
            <p className="font-bold mt-0.5">
              {item.learningState.nextReviewAt
                ? new Date(item.learningState.nextReviewAt).toLocaleDateString()
                : 'Learned (Retained)'}
            </p>
          </div>
        </div>

        {/* Bottom Modal Actions */}
        <div className="flex items-center justify-between pt-2">
          <button
            onClick={handleDelete}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-bold text-rose-500 hover:bg-rose-500/10 transition-colors"
          >
            <Trash2 size={15} />
            <span>Delete</span>
          </button>

          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                onEdit(item);
                onClose();
              }}
              className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
              style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
            >
              <Edit2 size={15} />
              <span>Edit Word</span>
            </button>
            <button
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-bold text-white shadow-xs hover:opacity-90"
              style={{ backgroundColor: primaryColor }}
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
