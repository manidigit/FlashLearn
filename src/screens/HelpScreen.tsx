import React from 'react';
import { ArrowLeft, BookOpen, Clock, HelpCircle, Layers, Volume2, Sparkles } from 'lucide-react';
import { useApp } from '../context/AppContext';

interface HelpScreenProps {
  onBack: () => void;
}

export const HelpScreen: React.FC<HelpScreenProps> = ({ onBack }) => {
  const { theme, isDark, t } = useApp();
  const primaryColor = isDark ? theme.primaryDark : theme.primaryLight;

  const topics = [
    {
      icon: BookOpen,
      title: 'Getting Started',
      desc: 'Choose a review goal from Home. Daily cards represent immediate retention tasks, Weekly cards represent 7-day reinforcement, and Monthly cards cement long-term vocabulary.',
    },
    {
      icon: Clock,
      title: 'Spaced Repetition Engine',
      desc: 'Words advance through four stages: Daily → Weekly (after 7 days) → Monthly (after 30 days) → Learned. If you get a card wrong, non-learned concepts return to Daily to ensure reliable memory retention.',
    },
    {
      icon: Layers,
      title: 'Vocabulary Difficulty',
      desc: 'Difficulty (Easy → Medium → Hard → Very Hard) operates independently of learning stage. When you answer 3 consecutive times correctly or wrongly, the difficulty dynamically adjusts.',
    },
    {
      icon: Volume2,
      title: 'Pronunciation Audio',
      desc: 'Tap the audio speaker icon on any Spanish word or sentence to hear native Spanish pronunciation powered by browser speech synthesis.',
    },
    {
      icon: Sparkles,
      title: 'Adding Words & Bulk Import',
      desc: 'Add words individually via the single word form, or import dozens of entries at once using the Bulk Import tool with smart format detection (e.g. "casa = خانه").',
    },
  ];

  return (
    <div className="max-w-xl mx-auto px-4 py-6 pb-28 space-y-5">
      <div className="flex items-center gap-3">
        <button
          onClick={onBack}
          className="p-2 rounded-xl border hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          style={{ borderColor: isDark ? theme.borderDark : theme.borderLight }}
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h2 className="text-2xl font-black">{t.helpTitle}</h2>
          <p className="text-xs opacity-65">How to learn efficiently with FlashLearn</p>
        </div>
      </div>

      <div className="space-y-3">
        {topics.map((item, idx) => {
          const Icon = item.icon;
          return (
            <div
              key={idx}
              className="p-5 rounded-2xl border shadow-xs space-y-2"
              style={{
                backgroundColor: isDark ? theme.cardDark : theme.cardLight,
                borderColor: isDark ? theme.borderDark : theme.borderLight,
              }}
            >
              <div className="flex items-center gap-3">
                <div
                  className="w-9 h-9 rounded-xl flex items-center justify-center shrink-0"
                  style={{ backgroundColor: `${primaryColor}20`, color: primaryColor }}
                >
                  <Icon size={18} />
                </div>
                <h3 className="font-extrabold text-sm">{item.title}</h3>
              </div>
              <p className="text-xs opacity-75 leading-relaxed">{item.desc}</p>
            </div>
          );
        })}
      </div>
    </div>
  );
};
