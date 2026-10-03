import { EntryType } from '../types';

export type DetectedLanguage = 'SPANISH' | 'PERSIAN' | 'MIXED' | 'UNKNOWN';
export type ParsedLineType =
  | 'ENTRY_HEADER'
  | 'TRANSLATION'
  | 'BREAKDOWN'
  | 'NOTE'
  | 'GRAMMAR_NOTE'
  | 'DERIVATIVE'
  | 'RELATION'
  | 'VARIANT'
  | 'COMMENT'
  | 'NUMBER'
  | 'SEPARATOR'
  | 'UNKNOWN';

export type ParseWarningType =
  | 'INCOMPLETE_ENTRY'
  | 'ORPHAN_LINE'
  | 'ORPHAN_SOURCE'
  | 'ORPHAN_TRANSLATION'
  | 'UNKNOWN_FORMAT';

export interface ParseWarning {
  warningType: ParseWarningType;
  lineNumber: number;
  rawText: string;
  message: string;
  confidence: number;
}

export interface ParseLogEntry {
  lineNumber: number;
  rawText: string;
  lineType: ParsedLineType;
  action: string;
}

export interface BreakdownPart {
  text: string;
  sourcePart?: string | null;
  translationPart?: string | null;
  orderIndex: number;
}

export interface ParsedRelationship {
  label: string;
  text: string;
}

export interface ParsedVariant {
  text: string;
}

export interface ParsedEntry {
  sourceText: string;
  translationText: string | null;
  notes: string | null;
  grammarNote: string | null;
  language: DetectedLanguage;
  entryType: EntryType;
  rawLines: string[];
  breakdown: BreakdownPart[];
  relationships: ParsedRelationship[];
  variants: ParsedVariant[];
  confidence: number;
  evidence: string[];
}

export interface ParseResult {
  entries: ParsedEntry[];
  warnings: ParseWarning[];
  importLog: ParseLogEntry[];
}

export function isLikelyPersian(text: string): boolean {
  // Arabic / Persian Unicode blocks: \u0600-\u06FF, \u0750-\u077F, \uFB50-\uFDFF, \uFE70-\uFEFF
  const persianRegex = /[\u0600-\u06FF\uFB50-\uFDFF\uFE70-\uFEFF]/;
  return persianRegex.test(text);
}

export function isLikelySpanish(text: string): boolean {
  // Contains Latin letters, especially Spanish letters: ñ, á, é, í, ó, ú, ü, ¿, ¡
  const latinCount = (text.match(/[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ]/g) || []).length;
  const nonSpaceCount = text.replace(/\s+/g, '').length;
  return nonSpaceCount > 0 && latinCount / nonSpaceCount > 0.4;
}

export function detectLanguage(text: string): DetectedLanguage {
  const hasEs = isLikelySpanish(text);
  const hasFa = isLikelyPersian(text);
  if (hasEs && hasFa) return 'MIXED';
  if (hasEs) return 'SPANISH';
  if (hasFa) return 'PERSIAN';
  return 'UNKNOWN';
}

function splitPair(line: string): [string, string] | null {
  // Matches separator symbols between source and target: =, :, -, —
  const separators = [' = ', ' - ', ' — ', ' : ', '= ', ': '];
  for (const sep of separators) {
    if (line.includes(sep)) {
      const parts = line.split(sep);
      if (parts.length >= 2 && parts[0].trim().length > 0 && parts[1].trim().length > 0) {
        return [parts[0].trim(), parts.slice(1).join(sep).trim()];
      }
    }
  }
  return null;
}

function stripNumbering(line: string): string {
  return line.replace(/^\s*\d+[\.\)\-:]\s*/, '').trim();
}

function classifyEntryKind(source: string): EntryType {
  const words = source.trim().split(/\s+/).length;
  if (source.includes('?') || source.includes('!') || (words > 5 && source.includes('.'))) {
    return 'SENTENCE';
  }
  if (words === 1) return 'WORD';
  if (words <= 3) return 'PHRASE';
  return 'SENTENCE';
}

export function parseVocabulary(raw: string): ParseResult {
  const lines = raw.split(/\r?\n/);
  const entries: ParsedEntry[] = [];
  const warnings: ParseWarning[] = [];
  const importLog: ParseLogEntry[] = [];

  let currentEntry: {
    source: string;
    translation: string | null;
    notes: string | null;
    grammarNote: string | null;
    breakdown: BreakdownPart[];
    relationships: ParsedRelationship[];
    variants: ParsedVariant[];
    rawLines: string[];
    confidence: number;
    evidence: string[];
    lineNumber: number;
  } | null = null;

  function flushCurrent() {
    if (currentEntry) {
      if (!currentEntry.translation || currentEntry.translation.trim().length === 0) {
        warnings.push({
          warningType: 'ORPHAN_SOURCE',
          lineNumber: currentEntry.lineNumber,
          rawText: currentEntry.rawLines[0] || '',
          message: 'Source without translation found.',
          confidence: currentEntry.confidence,
        });
      }
      entries.push({
        sourceText: currentEntry.source,
        translationText: currentEntry.translation,
        notes: currentEntry.notes,
        grammarNote: currentEntry.grammarNote,
        language: detectLanguage(currentEntry.source),
        entryType: classifyEntryKind(currentEntry.source),
        rawLines: currentEntry.rawLines,
        breakdown: currentEntry.breakdown,
        relationships: currentEntry.relationships,
        variants: currentEntry.variants,
        confidence: currentEntry.confidence,
        evidence: currentEntry.evidence,
      });
      currentEntry = null;
    }
  }

  lines.forEach((rawLine, index) => {
    const lineNumber = index + 1;
    const trimmed = rawLine.trim();
    if (!trimmed) return;

    if (trimmed.startsWith('#') || trimmed.startsWith('//')) {
      importLog.push({ lineNumber, rawText: trimmed, lineType: 'COMMENT', action: 'ignored' });
      return;
    }

    if (/^[-=_*]{3,}$/.test(trimmed)) {
      flushCurrent();
      importLog.push({ lineNumber, rawText: trimmed, lineType: 'SEPARATOR', action: 'flushed' });
      return;
    }

    // Check if line is a metadata line
    const lower = trimmed.toLowerCase();
    if (lower.startsWith('note:') || lower.startsWith('یادداشت:')) {
      const noteBody = trimmed.replace(/^[^:]+:\s*/, '').trim();
      if (currentEntry) {
        currentEntry.notes = currentEntry.notes ? `${currentEntry.notes}; ${noteBody}` : noteBody;
        currentEntry.rawLines.push(trimmed);
        importLog.push({ lineNumber, rawText: trimmed, lineType: 'NOTE', action: 'attached_note' });
      } else {
        warnings.push({
          warningType: 'ORPHAN_LINE',
          lineNumber,
          rawText: trimmed,
          message: 'Note found without preceding word entry.',
          confidence: 0.9,
        });
      }
      return;
    }

    if (lower.startsWith('grammar:') || lower.startsWith('g:') || lower.startsWith('گرامر:')) {
      const gBody = trimmed.replace(/^[^:]+:\s*/, '').trim();
      if (currentEntry) {
        currentEntry.grammarNote = currentEntry.grammarNote ? `${currentEntry.grammarNote}; ${gBody}` : gBody;
        currentEntry.rawLines.push(trimmed);
        importLog.push({ lineNumber, rawText: trimmed, lineType: 'GRAMMAR_NOTE', action: 'attached_grammar' });
      }
      return;
    }

    if (lower.startsWith('synonym:') || lower.startsWith('antonym:') || lower.startsWith('مترادف:') || lower.startsWith('متضاد:')) {
      const isSyn = lower.startsWith('syn') || lower.startsWith('مترادف');
      const val = trimmed.replace(/^[^:]+:\s*/, '').trim();
      if (currentEntry) {
        currentEntry.relationships.push({
          label: isSyn ? 'SYNONYM' : 'ANTONYM',
          text: val,
        });
        currentEntry.rawLines.push(trimmed);
        importLog.push({ lineNumber, rawText: trimmed, lineType: 'RELATION', action: 'attached_relation' });
      }
      return;
    }

    const cleanLine = stripNumbering(trimmed);
    const pair = splitPair(cleanLine);

    if (pair) {
      flushCurrent();
      const [src, trans] = pair;
      currentEntry = {
        source: src,
        translation: trans,
        notes: null,
        grammarNote: null,
        breakdown: [],
        relationships: [],
        variants: [],
        rawLines: [trimmed],
        confidence: 1.0,
        evidence: ['inline_pair_marker'],
        lineNumber,
      };
      importLog.push({ lineNumber, rawText: trimmed, lineType: 'ENTRY_HEADER', action: 'created_pair' });
      return;
    }

    // Single language line: check if Spanish or Persian
    if (isLikelySpanish(cleanLine) && !isLikelyPersian(cleanLine)) {
      if (currentEntry && !currentEntry.translation) {
        // Multi-line Spanish source continuation
        currentEntry.source = `${currentEntry.source} ${cleanLine}`;
        currentEntry.rawLines.push(trimmed);
        importLog.push({ lineNumber, rawText: trimmed, lineType: 'ENTRY_HEADER', action: 'extended_source' });
      } else {
        flushCurrent();
        currentEntry = {
          source: cleanLine,
          translation: null,
          notes: null,
          grammarNote: null,
          breakdown: [],
          relationships: [],
          variants: [],
          rawLines: [trimmed],
          confidence: 0.85,
          evidence: ['spanish_header'],
          lineNumber,
        };
        importLog.push({ lineNumber, rawText: trimmed, lineType: 'ENTRY_HEADER', action: 'started_spanish_source' });
      }
      return;
    }

    if (isLikelyPersian(cleanLine)) {
      if (currentEntry) {
        if (!currentEntry.translation) {
          currentEntry.translation = cleanLine;
          currentEntry.rawLines.push(trimmed);
          currentEntry.confidence = Math.max(currentEntry.confidence, 0.95);
          importLog.push({ lineNumber, rawText: trimmed, lineType: 'TRANSLATION', action: 'attached_translation' });
        } else {
          currentEntry.translation = `${currentEntry.translation}، ${cleanLine}`;
          currentEntry.rawLines.push(trimmed);
          importLog.push({ lineNumber, rawText: trimmed, lineType: 'TRANSLATION', action: 'extended_translation' });
        }
      } else {
        warnings.push({
          warningType: 'ORPHAN_TRANSLATION',
          lineNumber,
          rawText: trimmed,
          message: 'Persian translation found without preceding source entry.',
          confidence: 0.6,
        });
      }
      return;
    }

    // Default unknown line
    if (currentEntry) {
      currentEntry.rawLines.push(trimmed);
      if (!currentEntry.notes) currentEntry.notes = trimmed;
      else currentEntry.notes += ` | ${trimmed}`;
    }
  });

  flushCurrent();

  return { entries, warnings, importLog };
}
