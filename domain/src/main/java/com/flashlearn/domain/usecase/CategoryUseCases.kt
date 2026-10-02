package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Category
import com.flashlearn.domain.repository.CategoryRepository
import com.flashlearn.domain.repository.FlashLearnDatabase
import java.util.UUID
import javax.inject.Inject

class GetAllCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(): List<Category> = repository.getAll()
}

class GetOrCreateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(name: String): UUID = database.withTransaction {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Category name cannot be blank" }
        repository.findByName(trimmed)?.id
            ?: repository.insert(Category(UUID.randomUUID(), trimmed))
    }
}
