import React, { useState } from 'react';
import { useApp } from './context/AppContext';
import { Navigation, NavTab } from './components/Navigation';
import { HomeScreen } from './screens/HomeScreen';
import { ReviewScreen } from './screens/ReviewScreen';
import { LibraryScreen } from './screens/LibraryScreen';
import { ProgressScreen } from './screens/ProgressScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { AddWordMethodScreen } from './screens/AddWordMethodScreen';
import { AddWordFormScreen } from './screens/AddWordFormScreen';
import { BulkImportScreen } from './screens/BulkImportScreen';
import { NeedsReviewScreen } from './screens/NeedsReviewScreen';
import { BackupScreen } from './screens/BackupScreen';
import { AboutScreen } from './screens/AboutScreen';
import { HelpScreen } from './screens/HelpScreen';
import { FullVocabularyItem, ReviewType } from './types';

export const AppContent: React.FC = () => {
  const { theme, isDark, db } = useApp();
  const [currentTab, setCurrentTab] = useState<NavTab>('home');
  const [activeReviewType, setActiveReviewType] = useState<ReviewType>('DAILY');
  const [editingItem, setEditingItem] = useState<FullVocabularyItem | null>(null);

  const handleStartReview = (type: ReviewType) => {
    setActiveReviewType(type);
    setCurrentTab('review');
  };

  const handleSelectAddMethod = (method: 'single' | 'bulk' | 'backup') => {
    if (method === 'single') {
      setEditingItem(null);
      setCurrentTab('add_word_form' as NavTab);
    } else if (method === 'bulk') {
      setCurrentTab('bulk_import' as NavTab);
    } else if (method === 'backup') {
      setCurrentTab('backup' as NavTab);
    }
  };

  const handleEditWord = (item: FullVocabularyItem) => {
    setEditingItem(item);
    setCurrentTab('add_word_form' as NavTab);
  };

  return (
    <div
      className="min-h-screen transition-colors duration-200"
      style={{
        backgroundColor: isDark ? theme.bgDark : theme.bgLight,
        color: isDark ? theme.textPrimaryDark : theme.textPrimaryLight,
      }}
    >
      <Navigation
        currentTab={currentTab}
        onSelectTab={(tab) => {
          if (tab === 'add_word') setEditingItem(null);
          setCurrentTab(tab);
        }}
        pendingReviewCount={db.reviewQueue.length}
      />

      <main className="w-full">
        {currentTab === 'home' && (
          <HomeScreen
            onStartReview={handleStartReview}
            onNavigate={(tab) => setCurrentTab(tab as NavTab)}
          />
        )}

        {currentTab === 'review' && (
          <ReviewScreen
            initialReviewType={activeReviewType}
            onClose={() => setCurrentTab('home')}
          />
        )}

        {currentTab === 'library' && (
          <LibraryScreen
            onAddWord={() => {
              setEditingItem(null);
              setCurrentTab('add_word_form' as NavTab);
            }}
            onEditWord={handleEditWord}
          />
        )}

        {currentTab === 'progress' && <ProgressScreen />}

        {currentTab === 'settings' && (
          <SettingsScreen onNavigate={(tab) => setCurrentTab(tab as NavTab)} />
        )}

        {currentTab === 'add_word' && (
          <AddWordMethodScreen onSelectMethod={handleSelectAddMethod} />
        )}

        {currentTab === ('add_word_form' as NavTab) && (
          <AddWordFormScreen
            initialItem={editingItem}
            onBack={() => setCurrentTab('library')}
            onSaved={() => setCurrentTab('library')}
          />
        )}

        {currentTab === ('bulk_import' as NavTab) && (
          <BulkImportScreen
            onBack={() => setCurrentTab('add_word')}
            onSuccess={() => setCurrentTab('library')}
          />
        )}

        {currentTab === 'needs_review' && (
          <NeedsReviewScreen onBack={() => setCurrentTab('home')} />
        )}

        {currentTab === 'backup' && <BackupScreen onBack={() => setCurrentTab('home')} />}

        {currentTab === 'about' && <AboutScreen onBack={() => setCurrentTab('home')} />}

        {currentTab === 'help' && <HelpScreen onBack={() => setCurrentTab('home')} />}
      </main>
    </div>
  );
};

export default function App() {
  return <AppContent />;
}
