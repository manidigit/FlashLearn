package com.flashlearn.domain.usecase

import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DataVersionRepository
import com.flashlearn.domain.repository.FlashLearnDatabase
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class DataMigrationResult(val conceptFrom:Int,val conceptTo:Int,val contentFrom:Int,val contentTo:Int,val changedContentRows:Int)
class RefreshDataUseCase @Inject constructor(private val versions:DataVersionRepository,private val conceptRepository:ConceptRepository,private val contentRepository:ContentRepository,private val database:FlashLearnDatabase){
 companion object{const val CURRENT_CONCEPT_DATA_VERSION=2;const val CURRENT_CONTENT_DATA_VERSION=3}
 suspend operator fun invoke()=database.withTransaction{var cv=versions.getConceptDataVersion();var tv=versions.getContentDataVersion();val cf=cv;val tf=tv;var changed=0;while(cv<CURRENT_CONCEPT_DATA_VERSION){cv=migrateConcept(cv);versions.setConceptDataVersion(cv)};while(tv<CURRENT_CONTENT_DATA_VERSION){val r=migrateContent(tv);tv=r.first;changed+=r.second;versions.setContentDataVersion(tv)};require(cv==CURRENT_CONCEPT_DATA_VERSION){"Unsupported concept data version: $cv"};require(tv==CURRENT_CONTENT_DATA_VERSION){"Unsupported content data version: $tv"};DataMigrationResult(cf,cv,tf,tv,changed)}
 private suspend fun migrateConcept(current:Int):Int=when(current){0->{val now=Instant.now();conceptRepository.getAllActive().forEach{require(it.id!=UUID(0,0));};2};1->2;else->error("No concept migration path from version $current")}
 private suspend fun migrateContent(current:Int):Pair<Int,Int>=when(current){0->{var c=0;contentRepository.getAll().forEach{val key=computeCanonicalKey(it.text);if(it.canonicalKey!=key){contentRepository.upsert(it.copy(canonicalKey=key));c++}};1 to c};1->{var c=0;contentRepository.getAll().forEach{val key=computeCanonicalKey(it.text);if(it.canonicalKey!=key){contentRepository.upsert(it.copy(canonicalKey=key));c++}};2 to c};2->{var c=0;val all=contentRepository.getAll();all.groupBy{it.conceptId to it.languageCode}.forEach{(_,rows)->var next=0;rows.sortedWith(compareBy({it.translationIndex},{it.id})).forEach{row->val parts=row.text.split(Regex("\\s*/\\s*|\\s*؛\\s*|\\s*;\\s*")).map(String::trim).filter(String::isNotBlank).distinctBy(::computeCanonicalKey);if(parts.size<=1){if(row.translationIndex!=next){contentRepository.upsert(row.copy(translationIndex=next));c++};next++}else{parts.forEachIndexed{idx,text->if(idx==0){val key=computeCanonicalKey(text);if(row.text!=text||row.canonicalKey!=key||row.translationIndex!=next){contentRepository.upsert(row.copy(text=text,canonicalKey=key,translationIndex=next));c++}}else{contentRepository.insertTranslation(row.copy(id=UUID.randomUUID(),text=text,canonicalKey=computeCanonicalKey(text),translationIndex=next))};next++}}}};3 to c};else->error("No content migration path from version $current")}
}
