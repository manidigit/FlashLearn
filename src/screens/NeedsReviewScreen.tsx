import React, { useState } from 'react';
import { AlertCircle, Check, X, Edit2, ArrowLeft } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { ReviewQueueItem } from '../types';

interface NeedsReviewScreenProps {
  onBack: () => void;
  onEditItem?: (item: ReviewQueueItem) => void;
}

export const NeedsReviewScreen: React.FC<NeedsReviewScreenProps> = ({ onBack, onEditItem }) => {
  const { theme, isDark, t, db, approveQueueItem, rejectQueueItem } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const queue = db.reviewQueue;

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
          <h2 className="text-2xl font-black">{t.needsReviewTitle}</h2>
          <p className="text-xs opacity-65">{t.needsReviewDesc}</p>
        </div>
      </div>

      {queue.length === 0 ? (
        <div
          className="p-12 text-center rounded-3xl border shadow-xs space-y-2"
          style={{
            backgroundColor: isDark ? theme.cardDark : theme.cardLight,
            borderColor: isDark ? theme.borderDark : theme.borderLight,
          }}
        >
          <Check size={36} className="mx-auto text-emerald-500" />
          <h3 className="font-bold text-base">Queue is Empty</h3>
          <p className="text-xs opacity-65">{t.emptyQueue}</p>
        </div>
      ) : (
        <div className="space-y-3">
          {queue.map((item) => (
            <div
              key={item.id}
              className="p-4 rounded-2xl border shadow-xs space-y-3"
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-black text-base">{item.sourceText}</h3>
                  <p className="font-bold text-sm text-emerald-500 mt-0.5">
                    {item.targetText || 'No translation provided'}
                  </p>
                </div>
                <span className="text-xs font-mono font-bold px-2 py-0.5 rounded-md bg-amber-500/10 text-amber-500">
                  Confidence: {Math.round(item.confidence * 100)}%
                </span>
              </div>

              {item.warning && (
                <p className="text-xs text-amber-500 font-medium">⚠️ {item.warning}</p>
              )}

              <div className="flex items-center justify-end gap-2 pt-2 border-t" style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}>
                <button
                  onClick={() => rejectQueueItem(item.id)}
                  className="px-3 py-1.5 rounded-xl text-xs font-bold text-rose-500 hover:bg-rose-500/10 transition-colors"
                >
                  {t.rejectBtn}
                </button>
                <button
                  onClick={() => approveQueueItem(item)}
                  className="px-4 py-1.5 rounded-xl text-xs font-bold text-white shadow-xs"
                  style={{ backgroundColor: primaryColor }}
                >
                  {t.approveBtn}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
