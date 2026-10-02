package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Tag
import com.flashlearn.domain.repository.FlashLearnDatabase
import com.flashlearn.domain.repository.TagRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.UUID

class TagUseCasesTest {
    private class Repo : TagRepository {
        val values = linkedMapOf<UUID, Tag>()
        override suspend fun getAll(): List<Tag> = values.values.toList()
        override suspend fun insert(tag: Tag): UUID { values[tag.id] = tag; return tag.id }
        override suspend fun update(tag: Tag) { values[tag.id] = tag }
        override suspend fun delete(id: UUID) { values.remove(id) }
    }
    private object Db : FlashLearnDatabase {
        override suspend fun <T> withTransaction(block: suspend () -> T): T = block()
    }

    @Test fun create_normalizesAndRejectsDuplicateNames() = runBlocking {
        val repo = Repo()
        CreateTagUseCase(repo, Db)("  Travel  ")
        assertThrows(IllegalArgumentException::class.java) { runBlocking { CreateTagUseCase(repo, Db)("travel") } }
        assertEquals(listOf("Travel"), repo.getAll().map { it.name })
    }

    @Test fun update_rejectsBlankAndDuplicateNames() = runBlocking {
        val repo = Repo(); val first = UUID.randomUUID(); val second = UUID.randomUUID()
        repo.insert(Tag(first, "Travel")); repo.insert(Tag(second, "Food"))
        assertThrows(IllegalArgumentException::class.java) { runBlocking { UpdateTagUseCase(repo, Db)(first, "Food") } }
        assertThrows(IllegalArgumentException::class.java) { runBlocking { UpdateTagUseCase(repo, Db)(first, "   ") } }
        UpdateTagUseCase(repo, Db)(first, " Trips ")
        assertEquals("Trips", repo.getAll().first { it.id == first }.name)
    }

    @Test fun delete_removesExistingTag() = runBlocking {
        val repo = Repo(); val id = CreateTagUseCase(repo, Db)("Travel")
        DeleteTagUseCase(repo, Db)(id)
        assertEquals(emptyList<Tag>(), repo.getAll())
    }
}
