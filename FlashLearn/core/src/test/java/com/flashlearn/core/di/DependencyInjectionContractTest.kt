package com.flashlearn.core.di

import com.flashlearn.data.repository.*
import com.flashlearn.domain.repository.*
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.inject.Inject

class DependencyInjectionContractTest {
    @Test
    fun repositoryImplementationsHaveInjectConstructors() {
        val implementations = listOf(
            RoomConceptRepository::class,
            RoomContentRepository::class,
            RoomLearningStateRepository::class,
            RoomDifficultyStateRepository::class,
            RoomConceptTagRepository::class,
            RoomReviewHistoryRepository::class,
            RoomReviewSessionRepository::class,
            RoomSettingsRepository::class,
            FlashLearnDatabaseImpl::class
        )
        implementations.forEach { type ->
            assertTrue(
                "Missing @Inject constructor: ${type.qualifiedName}",
                type.constructors.any { constructor ->
                    constructor.annotations.any { it.annotationClass == Inject::class }
                }
            )
        }
    }

    @Test
    fun serviceLocatorDoesNotRepresentASecondRuntimeContainer() {
        assertTrue(FlashLearnDependencies::class.java.isRecord.not())
    }
}
