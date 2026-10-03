export type ThemeId = 'grok' | 'claud' | 'modern_minimal' | 'gtp';

export interface ThemeSpec {
  id: ThemeId;
  name: string;
  nameFa: string;
  description: string;
  primaryLight: string;
  primaryDark: string;
  secondaryLight: string;
  secondaryDark: string;
  bgLight: string;
  bgDark: string;
  surfaceLight: string;
  surfaceDark: string;
  cardLight: string;
  cardDark: string;
  borderLight: string;
  borderDark: string;
  textPrimaryLight: string;
  textPrimaryDark: string;
  textMutedLight: string;
  textMutedDark: string;
  borderRadius: {
    sm: string;
    md: string;
    lg: string;
  };
  accentAlpha: number;
}

export const THEMES: Record<ThemeId, ThemeSpec> = {
  grok: {
    id: 'grok',
    name: 'Grok Luxury',
    nameFa: 'گروک طلایی',
    description: 'Warm gold accents, deep obsidian dark mode, rounded luxury cards',
    primaryLight: '#C79B32',
    primaryDark: '#E0B44C',
    secondaryLight: '#9F7925',
    secondaryDark: '#F0C65A',
    bgLight: '#F7F2E8',
    bgDark: '#080D13',
    surfaceLight: '#FFFFF8',
    surfaceDark: '#111820',
    cardLight: '#FFFBF2',
    cardDark: '#121B24',
    borderLight: '#E8D8B6',
    borderDark: '#2C3744',
    textPrimaryLight: '#1C1608',
    textPrimaryDark: '#F7F0E3',
    textMutedLight: '#756A59',
    textMutedDark: '#B9B2A6',
    borderRadius: {
      sm: '14px',
      md: '20px',
      lg: '28px',
    },
    accentAlpha: 0.16,
  },
  claud: {
    id: 'claud',
    name: 'Claud Editorial',
    nameFa: 'کلاد سبز زیتونی',
    description: 'Sage emerald, crisp editorial outlines, and minimalist typography',
    primaryLight: '#2C5F4E',
    primaryDark: '#5DAA92',
    secondaryLight: '#4A7C6E',
    secondaryDark: '#7BC9B3',
    bgLight: '#FAF9F7',
    bgDark: '#181B1A',
    surfaceLight: '#FFFFFF',
    surfaceDark: '#222725',
    cardLight: '#FFFFFF',
    cardDark: '#222725',
    borderLight: '#DDD8D4',
    borderDark: '#3A423E',
    textPrimaryLight: '#1A1A1A',
    textPrimaryDark: '#F5F5F5',
    textMutedLight: '#6B7A76',
    textMutedDark: '#A8C4BB',
    borderRadius: {
      sm: '12px',
      md: '16px',
      lg: '24px',
    },
    accentAlpha: 0.1,
  },
  modern_minimal: {
    id: 'modern_minimal',
    name: 'Modern Minimal',
    nameFa: 'مدرن مینیمال',
    description: 'Clean royal blue, slate neutrals, balanced everyday study',
    primaryLight: '#2563EB',
    primaryDark: '#60A5FA',
    secondaryLight: '#475569',
    secondaryDark: '#94A3B8',
    bgLight: '#F8FAFC',
    bgDark: '#0B0F14',
    surfaceLight: '#FFFFFF',
    surfaceDark: '#141A22',
    cardLight: '#FFFFFF',
    cardDark: '#171D26',
    borderLight: '#D8E0EA',
    borderDark: '#283342',
    textPrimaryLight: '#0F172A',
    textPrimaryDark: '#F1F5F9',
    textMutedLight: '#64748B',
    textMutedDark: '#94A3B8',
    borderRadius: {
      sm: '10px',
      md: '14px',
      lg: '20px',
    },
    accentAlpha: 0.12,
  },
  gtp: {
    id: 'gtp',
    name: 'GTP Cyber',
    nameFa: 'جی‌تی‌پی بنفش و فیروزه‌ای',
    description: 'Electric violet, neon cyan accents, compact geometry, high contrast',
    primaryLight: '#7C3AED',
    primaryDark: '#A78BFA',
    secondaryLight: '#06B6D4',
    secondaryDark: '#22D3EE',
    bgLight: '#F6F3FF',
    bgDark: '#0A0713',
    surfaceLight: '#FFFFFF',
    surfaceDark: '#151022',
    cardLight: '#FCFAFF',
    cardDark: '#191328',
    borderLight: '#D8CFF0',
    borderDark: '#36284F',
    textPrimaryLight: '#181228',
    textPrimaryDark: '#F7F2FF',
    textMutedLight: '#6F6485',
    textMutedDark: '#C4B9D6',
    borderRadius: {
      sm: '6px',
      md: '12px',
      lg: '18px',
    },
    accentAlpha: 0.18,
  },
};
