package com.flashlearn.core.di

import com.flashlearn.domain.repository.*

/**
 * Composition boundary for the application.
 * Android/Hilt bindings can delegate to these concrete implementations.
 */
data class FlashLearnDependencies(
    val conceptRepository: ConceptRepository,
    val contentRepository: ContentRepository,
    val learningStateRepository: LearningStateRepository,
    val difficultyStateRepository: DifficultyStateRepository,
    val conceptTagRepository: ConceptTagRepository,
    val reviewHistoryRepository: ReviewHistoryRepository,
    val settingsRepository: SettingsRepository,
    val database: FlashLearnDatabase
)
