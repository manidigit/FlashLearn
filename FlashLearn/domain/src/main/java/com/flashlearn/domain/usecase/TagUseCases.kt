package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Tag
import com.flashlearn.domain.repository.FlashLearnDatabase
import com.flashlearn.domain.repository.TagRepository
import java.util.UUID
import javax.inject.Inject

class GetAllTagsUseCase @Inject constructor(private val repository: TagRepository) {
    suspend operator fun invoke(): List<Tag> = repository.getAll().sortedBy { it.name.lowercase() }
}

class CreateTagUseCase @Inject constructor(
    private val repository: TagRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(name: String): UUID = database.withTransaction {
        val normalized = name.trim()
        require(normalized.isNotEmpty()) { "Tag name cannot be blank" }
        require(repository.getAll().none { it.name.equals(normalized, ignoreCase = true) }) { "Tag already exists: $normalized" }
        repository.insert(Tag(UUID.randomUUID(), normalized))
    }
}

class UpdateTagUseCase @Inject constructor(
    private val repository: TagRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(id: UUID, name: String) = database.withTransaction {
        val normalized = name.trim()
        require(normalized.isNotEmpty()) { "Tag name cannot be blank" }
        require(repository.getAll().none { it.id != id && it.name.equals(normalized, ignoreCase = true) }) { "Tag already exists: $normalized" }
        require(repository.getAll().any { it.id == id }) { "Tag not found: $id" }
        repository.update(Tag(id, normalized))
    }
}

class DeleteTagUseCase @Inject constructor(
    private val repository: TagRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(id: UUID) = database.withTransaction {
        require(repository.getAll().any { it.id == id }) { "Tag not found: $id" }
        repository.delete(id)
    }
}
