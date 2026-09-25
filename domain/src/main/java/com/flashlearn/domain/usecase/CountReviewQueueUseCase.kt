package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ConceptTagRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.repository.LearningStateRepository
import com.flashlearn.domain.repository.ReviewHistoryRepository
import java.time.ZoneId
import javax.inject.Inject

class CountReviewQueueUseCase @Inject constructor(private val conceptRepository:ConceptRepository,private val learningStateRepository:LearningStateRepository,private val difficultyStateRepository:DifficultyStateRepository,private val conceptTagRepository:ConceptTagRepository,private val reviewHistoryRepository:ReviewHistoryRepository){
    suspend operator fun invoke(filters:ReviewSelectionFilters):Int{
        val states=when(filters.reviewType){ReviewType.RANDOM->learningStateRepository.getDueNonLearned(filters.now);ReviewType.DAILY->learningStateRepository.getAllByStage(Stage.DAILY).filter{it.nextReviewAt!=null&&it.nextReviewAt<=filters.now};ReviewType.WEEKLY->learningStateRepository.getAllByStage(Stage.WEEKLY).filter{it.nextReviewAt!=null&&it.nextReviewAt<=filters.now};ReviewType.MONTHLY->learningStateRepository.getAllByStage(Stage.MONTHLY).filter{it.nextReviewAt!=null&&it.nextReviewAt<=filters.now};ReviewType.LEARNED->learningStateRepository.getAllByStage(Stage.LEARNED)}
        val today=filters.now.atZone(ZoneId.systemDefault()).toLocalDate();val conceptsById=conceptRepository.getAllActive().associateBy{it.id};val difficultiesById=difficultyStateRepository.getAll().associateBy{it.conceptId};val tagsByConcept=conceptTagRepository.getAll().groupBy(ConceptTag::conceptId).mapValues{(_,tags)->tags.map(ConceptTag::tagId)}
        return states.asSequence().mapNotNull{learning->if(learning.lastReviewedAt?.atZone(ZoneId.systemDefault())?.toLocalDate()==today)return@mapNotNull null;val concept=conceptsById[learning.conceptId]?:return@mapNotNull null;val difficulty=difficultiesById[learning.conceptId]?:return@mapNotNull null;val tags=tagsByConcept[learning.conceptId].orEmpty();if(filters.difficulty!=null&&difficulty.current!=filters.difficulty)return@mapNotNull null;if(filters.categoryId!=null&&concept.categoryId!=filters.categoryId)return@mapNotNull null;if(filters.tagId!=null&&filters.tagId !in tags)return@mapNotNull null;concept.id}.distinct().count()
    }
}
