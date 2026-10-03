import React from 'react';
import { Volume2 } from 'lucide-react';

interface SpeechButtonProps {
  text: string;
  lang?: string; // 'es-ES' or 'fa-IR'
  className?: string;
  size?: number;
}

export const SpeechButton: React.FC<SpeechButtonProps> = ({
  text,
  lang = 'es-ES',
  className = '',
  size = 18,
}) => {
  const speak = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!('speechSynthesis' in window)) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = lang;
    utterance.rate = 0.9;
    window.speechSynthesis.speak(utterance);
  };

  return (
    <button
      type="button"
      onClick={speak}
      title="Listen pronunciation"
      className={`p-1.5 rounded-full hover:bg-black/10 dark:hover:bg-white/10 active:scale-95 transition-all text-current opacity-80 hover:opacity-100 ${className}`}
    >
      <Volume2 size={size} />
    </button>
  );
};
