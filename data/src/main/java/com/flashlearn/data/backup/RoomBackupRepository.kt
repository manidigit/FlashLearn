package com.flashlearn.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.flashlearn.database.*
import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.BackupRepository
import com.flashlearn.domain.repository.RestoreResult
import com.flashlearn.domain.usecase.computeCanonicalKey
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBackupRepository @Inject constructor(
    private val db: RoomFlashLearnDatabase,
    @ApplicationContext private val context: Context
) : BackupRepository {
    companion object {
        private const val SCHEMA = 2
        private const val PRE_RESTORE = "flashlearn-pre-restore-backup.json"
        private val SECTIONS = listOf(
            "concepts", "contents", "learningStates", "difficultyStates", "tags", "conceptTags",
            "reviewSessions", "reviewHistory", "settings", "categories", "achievements", "parserMetadata",
            "relations", "variants", "reviewQueue", "languages", "languagePairs"
        )
        private val STAGES = setOf("DAILY", "WEEKLY", "MONTHLY", "LEARNED")
        private val REVIEW_TYPES = setOf("DAILY", "WEEKLY", "MONTHLY", "LEARNED")
    }

    override suspend fun exportFull(): String = db.withTransaction {
        JSONObject().apply {
            put("schemaVersion", SCHEMA)
            put("exportedAt", Instant.now().toString())
            put("backupType", "FULL")
            put("concepts", db.conceptDao().getAll().json { JSONObject().put("id", it.id).put("entryType", it.entryType).put("categoryId", it.categoryId).put("favorite", it.favorite).put("active", it.active).put("createdAt", it.createdAt).put("updatedAt", it.updatedAt) })
            put("contents", db.contentDao().getAll().json { JSONObject().put("id", it.id).put("conceptId", it.conceptId).put("languageCode", it.languageCode).put("text", it.text).put("canonicalKey", it.canonicalKey).put("notes", it.notes).put("pronunciation", it.pronunciation).put("example", it.example).put("translationIndex", it.translationIndex).put("grammarNote", it.grammarNote).put("possibleCorrection", it.possibleCorrection) })
            put("learningStates", db.learningStateDao().getAll().json { JSONObject().put("id", it.id).put("conceptId", it.conceptId).put("stage", it.stage).put("nextReviewAt", it.nextReviewAt).put("monthlyWrongCount", it.monthlyWrongCount).put("hasPathFailure", it.hasPathFailure).put("totalCorrect", it.totalCorrect).put("totalWrong", it.totalWrong).put("lastReviewedAt", it.lastReviewedAt) })
            put("difficultyStates", db.difficultyStateDao().getAll().json { JSONObject().put("id", it.id).put("conceptId", it.conceptId).put("current", it.current).put("consecutiveCorrect", it.consecutiveCorrect).put("consecutiveWrong", it.consecutiveWrong).put("hasReachedVeryHard", it.hasReachedVeryHard) })
            put("tags", db.tagDao().getAll().json { JSONObject().put("id", it.id).put("name", it.name) })
            put("conceptTags", db.conceptTagDao().getAll().json { JSONObject().put("conceptId", it.conceptId).put("tagId", it.tagId) })
            put("reviewSessions", db.reviewSessionDao().getAll().json { JSONObject().put("id", it.id).put("startedAt", it.startedAt).put("endedAt", it.endedAt).put("reviewType", it.reviewType) })
            put("reviewHistory", db.reviewHistoryDao().getAll().json { JSONObject().put("id", it.id).put("sessionId", it.sessionId).put("reviewAttemptId", it.reviewAttemptId).put("conceptId", it.conceptId).put("reviewedAt", it.reviewedAt).put("isCorrect", it.isCorrect).put("reviewType", it.reviewType) })
            put("settings", db.settingsDao().getAll().json { JSONObject().put("key", it.key).put("value", it.value).put("updatedAt", it.updatedAt) })
            put("categories", db.categoryDao().getAll().json { JSONObject().put("id", it.id).put("name", it.name) })
            put("achievements", db.achievementDao().getAll().json { JSONObject().put("achievementId", it.achievementId).put("unlocked", it.unlocked) })
            put("parserMetadata", db.parserMetadataDao().getAll().json { JSONObject().put("conceptId", it.conceptId).put("breakdownJson", it.breakdownJson).put("relationshipsJson", it.relationshipsJson).put("variantsJson", it.variantsJson).put("confidence", it.confidence) })
            put("relations", db.vocabularyRelationDao().getAll().json { JSONObject().put("id", it.id).put("sourceConceptId", it.sourceConceptId).put("targetConceptId", it.targetConceptId).put("relationType", it.relationType).put("unresolvedText", it.unresolvedText) })
            put("variants", db.vocabularyVariantDao().getAll().json { JSONObject().put("id", it.id).put("conceptId", it.conceptId).put("text", it.text).put("variantType", it.variantType) })
            put("reviewQueue", db.reviewQueueDao().getAll().json { JSONObject().put("id", it.id).put("conceptId", it.conceptId).put("sourceText", it.sourceText).put("targetText", it.targetText).put("confidence", it.confidence).put("possibleCorrection", it.possibleCorrection).put("status", it.status).put("lineNumber", it.lineNumber).put("warning", it.warning) })
            put("languages", db.languageDao().getAll().json { JSONObject().put("code", it.code).put("name", it.name).put("active", it.active) })
            put("languagePairs", db.languagePairDao().getAll().json { JSONObject().put("sourceLanguageCode", it.sourceLanguageCode).put("targetLanguageCode", it.targetLanguageCode).put("active", it.active) })
            put("conceptReferences", JSONArray(db.conceptDao().getAll().map { it.id.toString() }))
        }.toString()
    }

    override suspend fun restoreFull(json: String): RestoreResult {
        val root = runCatching { JSONObject(json) }.getOrElse { return RestoreResult(0, 0, listOf("INVALID_JSON")) }
        val schema = root.optInt("schemaVersion", -1)
        if (schema !in 1..SCHEMA || root.optString("backupType") != "FULL") {
            return RestoreResult(0, 0, listOf("UNSUPPORTED_BACKUP"))
        }
        val missing = SECTIONS.filterNot(root::has)
        if (missing.isNotEmpty() && schema >= 2) {
            return RestoreResult(0, 0, listOf("MISSING_SECTION:${missing.joinToString(",")}"))
        }

        val validationIssues = validateBeforeMutation(root)
        if (validationIssues.isNotEmpty()) return RestoreResult(0, 0, validationIssues)

        return try {
            File(context.filesDir, PRE_RESTORE).writeText(exportFull(), Charsets.UTF_8)
            db.withTransaction {
                var added = 0
                var merged = 0
                val issues = mutableListOf<String>()
                fun uuid(o: JSONObject, key: String) = UUID.fromString(o.getString(key))
                fun instant(o: JSONObject, key: String): Instant? = if (o.isNull(key)) null else Instant.parse(o.getString(key))
                fun text(o: JSONObject, key: String) = o.optString(key).takeIf { it.isNotBlank() }
                val concepts = root.arr("concepts").map { ConceptEntity(uuid(it,"id"), it.getString("entryType"), text(it,"categoryId")?.let(UUID::fromString), it.getBoolean("favorite"), it.getBoolean("active"), Instant.parse(it.getString("createdAt")), Instant.parse(it.getString("updatedAt"))) }
                val conceptIds = concepts.map { it.id }.toSet()
                val existingConceptIds = db.conceptDao().getAll().map { it.id }.toSet()
                concepts.forEach { if (it.id in existingConceptIds) { db.conceptDao().update(it); merged++ } else { db.conceptDao().insert(it); added++ } }

                root.arr("categories").forEach { val e=CategoryEntity(uuid(it,"id"),it.getString("name")); if(db.categoryDao().getById(e.id)==null){db.categoryDao().insert(e);added++}else{db.categoryDao().update(e);merged++} }
                root.arr("tags").forEach { val e=TagEntity(uuid(it,"id"),it.getString("name")); if(db.tagDao().getById(e.id)==null){db.tagDao().insert(e);added++}else{db.tagDao().update(e);merged++} }

                root.arr("contents").filter { uuid(it,"conceptId") in conceptIds }.forEach { o ->
                    val conceptId=uuid(o,"conceptId")
                    val lang=o.getString("languageCode")
                    val index=o.optInt("translationIndex",0)
                    val incomingId=uuid(o,"id")
                    val e0=ContentEntity(incomingId,conceptId,lang,o.getString("text"),computeCanonicalKey(o.getString("text")),text(o,"notes"),text(o,"pronunciation"),text(o,"example"),index,text(o,"grammarNote"),text(o,"possibleCorrection"))
                    val existingById=db.contentDao().getById(incomingId)
                    val existingByIdentity=db.contentDao().getAll().firstOrNull { it.conceptId==conceptId && it.languageCode==lang && it.translationIndex==index }
                    when {
                        existingById != null -> { db.contentDao().update(e0); merged++ }
                        existingByIdentity != null -> { db.contentDao().update(e0.copy(id=existingByIdentity.id)); merged++ }
                        else -> { db.contentDao().insert(e0); added++ }
                    }
                }
                root.arr("learningStates").filter { uuid(it,"conceptId") in conceptIds }.forEach { o -> val e=LearningStateEntity(uuid(o,"id"),uuid(o,"conceptId"),o.getString("stage"),instant(o,"nextReviewAt"),o.getInt("monthlyWrongCount"),o.getBoolean("hasPathFailure"),o.getInt("totalCorrect"),o.getInt("totalWrong"),instant(o,"lastReviewedAt")); val old=db.learningStateDao().getByConceptId(e.conceptId); if(old==null){db.learningStateDao().upsert(e);added++}else{db.learningStateDao().upsert(e.copy(id=old.id));merged++} }
                root.arr("difficultyStates").filter { uuid(it,"conceptId") in conceptIds }.forEach { o -> val e=DifficultyStateEntity(uuid(o,"id"),uuid(o,"conceptId"),o.getString("current"),o.getInt("consecutiveCorrect"),o.getInt("consecutiveWrong"),o.getBoolean("hasReachedVeryHard")); val old=db.difficultyStateDao().getByConceptId(e.conceptId); if(old==null){db.difficultyStateDao().upsert(e);added++}else{db.difficultyStateDao().upsert(e.copy(id=old.id));merged++} }
                root.arr("conceptTags").filter { uuid(it,"conceptId") in conceptIds && db.tagDao().getById(uuid(it,"tagId")) != null }.forEach { db.conceptTagDao().insert(ConceptTagEntity(uuid(it,"conceptId"),uuid(it,"tagId"))) }
                root.arr("parserMetadata").filter { uuid(it,"conceptId") in conceptIds }.forEach { o -> db.parserMetadataDao().upsert(ParserMetadataEntity(uuid(o,"conceptId"),o.getString("breakdownJson"),o.getString("relationshipsJson"),o.getString("variantsJson"),o.getDouble("confidence"))) }
                root.arr("variants").filter { uuid(it,"conceptId") in conceptIds }.forEach { o -> db.vocabularyVariantDao().insert(VocabularyVariantEntity(uuid(o,"id"),uuid(o,"conceptId"),o.getString("text"),o.getString("variantType"))) }
                root.arr("relations").filter { uuid(it,"sourceConceptId") in conceptIds && (it.isNull("targetConceptId") || uuid(it,"targetConceptId") in conceptIds) }.forEach { o -> db.vocabularyRelationDao().insert(VocabularyRelationEntity(uuid(o,"id"),uuid(o,"sourceConceptId"),if(o.isNull("targetConceptId"))null else uuid(o,"targetConceptId"),o.getString("relationType"),text(o,"unresolvedText"))) }
                root.arr("reviewQueue").filter { it.isNull("conceptId") || uuid(it,"conceptId") in conceptIds }.forEach { o -> db.reviewQueueDao().upsert(ReviewQueueEntity(uuid(o,"id"),if(o.isNull("conceptId"))null else uuid(o,"conceptId"),o.getString("sourceText"),text(o,"targetText"),o.getDouble("confidence"),text(o,"possibleCorrection"),o.getString("status"),if(o.isNull("lineNumber"))null else o.getInt("lineNumber"),text(o,"warning"))) }
                root.arr("reviewSessions").forEach { o -> val e=ReviewSessionEntity(uuid(o,"id"),Instant.parse(o.getString("startedAt")),instant(o,"endedAt"),o.getString("reviewType")); if(db.reviewSessionDao().getById(e.id)==null){db.reviewSessionDao().insert(e);added++}else{db.reviewSessionDao().update(e);merged++} }
                val sessionIds=db.reviewSessionDao().getAll().map{it.id}.toSet()
                root.arr("reviewHistory").filter { uuid(it,"conceptId") in conceptIds && uuid(it,"sessionId") in sessionIds }.forEach { o -> val e=ReviewHistoryEntity(uuid(o,"id"),uuid(o,"sessionId"),uuid(o,"reviewAttemptId"),uuid(o,"conceptId"),Instant.parse(o.getString("reviewedAt")),o.getBoolean("isCorrect"),o.getString("reviewType")); if(!db.reviewHistoryDao().existsByAttemptId(e.sessionId,e.reviewAttemptId)){db.reviewHistoryDao().insert(e);added++}else{merged++} }
                root.arr("settings").forEach { o -> db.settingsDao().put(SettingsEntity(o.getString("key"),o.getString("value"),Instant.parse(o.getString("updatedAt")))) }
                root.arr("achievements").forEach { o -> db.achievementDao().upsert(AchievementEntity(o.getString("achievementId"),o.getBoolean("unlocked"))) }
                root.arr("languages").forEach { o -> db.languageDao().upsert(LanguageEntity(o.getString("code"),o.getString("name"),o.getBoolean("active"))) }
                root.arr("languagePairs").forEach { o -> db.languagePairDao().upsert(LanguagePairEntity(o.getString("sourceLanguageCode"),o.getString("targetLanguageCode"),o.getBoolean("active"))) }
                val refs=root.optJSONArray("conceptReferences")?.let { a -> (0 until a.length()).mapNotNull { runCatching{UUID.fromString(a.getString(it))}.getOrNull() }.toSet() } ?: conceptIds
                if (!refs.all { it in conceptIds }) issues += "INVALID_CONCEPT_REFERENCE"
                RestoreResult(added, merged, issues)
            }
        } catch (e: Exception) {
            RestoreResult(0, 0, listOf(e.message ?: "RESTORE_FAILED"))
        }
    }

    private fun validateBeforeMutation(root: JSONObject): List<String> {
        val issues = mutableListOf<String>()
        val arrays = mapOf(
            "concepts" to "id", "contents" to "id", "learningStates" to "id", "difficultyStates" to "id",
            "tags" to "id", "reviewSessions" to "id", "reviewHistory" to "id", "categories" to "id",
            "variants" to "id", "relations" to "id", "reviewQueue" to "id"
        )
        arrays.forEach { (section, key) ->
            val seen = mutableSetOf<String>()
            root.optJSONArray(section)?.let { a ->
                for (i in 0 until a.length()) {
                    val o = a.optJSONObject(i) ?: run { issues += "INVALID_ENTRY:$section[$i]"; continue }
                    val value = o.optString(key, "")
                    if (value.isBlank() || runCatching { UUID.fromString(value) }.isFailure) issues += "INVALID_UUID:$section"
                    else if (!seen.add(value)) issues += "DUPLICATE_UUID:$section"
                }
            }
        }

        root.optJSONArray("learningStates")?.let { a -> for (i in 0 until a.length()) { val stage=a.getJSONObject(i).optString("stage"); if(stage !in STAGES) issues += "INVALID_VALUE:stage" } }
        root.optJSONArray("reviewSessions")?.let { a ->
            for (i in 0 until a.length()) {
                val o=a.getJSONObject(i); val start=runCatching{Instant.parse(o.getString("startedAt"))}.getOrNull(); val end=runCatching{if(o.isNull("endedAt"))null else Instant.parse(o.getString("endedAt"))}.getOrNull()
                if(start==null || (!o.isNull("endedAt") && end==null) || (start!=null && end!=null && end.isBefore(start))) issues += "INVALID_VALUE:reviewSession_time"
                if(o.optString("reviewType") !in REVIEW_TYPES) issues += "INVALID_VALUE:reviewSession_reviewType"
            }
        }
        root.optJSONArray("reviewHistory")?.let { a ->
            val attempts=mutableSetOf<String>()
            for (i in 0 until a.length()) {
                val o=a.getJSONObject(i); val session=o.optString("sessionId"); val attempt=o.optString("reviewAttemptId")
                if(!attempts.add("$session:$attempt")) issues += "DUPLICATE_ATTEMPT:reviewHistory"
                if(o.optString("reviewType") !in REVIEW_TYPES) issues += "INVALID_VALUE:history_reviewType"
                val reviewed=runCatching{Instant.parse(o.getString("reviewedAt"))}.getOrNull(); if(reviewed==null) issues += "INVALID_VALUE:reviewHistory_reviewedAt"
            }
            val sessions=root.optJSONArray("reviewSessions")
            if(sessions!=null) {
                val types=(0 until sessions.length()).associate { val s=sessions.getJSONObject(it); s.optString("id") to s.optString("reviewType") }
                for(i in 0 until a.length()) { val o=a.getJSONObject(i); val st=types[o.optString("sessionId")]; if(st!=null && st != o.optString("reviewType")) issues += "INVALID_VALUE:history_session_reviewType" }
            }
        }
        root.optJSONArray("contents")?.let { a ->
            for(i in 0 until a.length()) {
                val o=a.getJSONObject(i)
                if(!o.has("canonicalKey") || o.isNull("canonicalKey")) issues += "INVALID_VALUE:canonicalKey"
                if(o.optString("text").isBlank()) issues += "INVALID_VALUE:content_text"
                if(o.optString("languageCode").isBlank()) issues += "INVALID_VALUE:languageCode"
            }
        }
        root.optJSONArray("parserMetadata")?.let { a -> for(i in 0 until a.length()) { val c=a.getJSONObject(i).optDouble("confidence",Double.NaN); if(c.isNaN() || c !in 0.0..1.0) issues += "INVALID_VALUE:parserMetadata_confidence" } }
        root.optJSONArray("conceptReferences")?.let { a ->
            val concepts=root.optJSONArray("concepts")?.let { c -> (0 until c.length()).mapNotNull { c.optJSONObject(it)?.optString("id") }.toSet() } ?: emptySet()
            for(i in 0 until a.length()) { val ref=a.optString(i); if(runCatching{UUID.fromString(ref)}.isFailure || ref !in concepts) issues += "INVALID_CONCEPT_REFERENCE" }
        }
        return issues.distinct()
    }

    private fun JSONObject.arr(name: String): List<JSONObject> { val a=getJSONArray(name); return (0 until a.length()).map(a::getJSONObject) }
    private fun <T> List<T>.json(map:(T)->JSONObject)=JSONArray().also{forEach{v->it.put(map(v).normalize())}}
    private fun Any?.normalize(): Any? = when(this){is UUID->toString();is Instant->toString();else->this}
}
