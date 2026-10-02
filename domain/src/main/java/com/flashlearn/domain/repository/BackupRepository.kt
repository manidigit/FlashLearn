package com.flashlearn.domain.repository

interface BackupRepository {
    suspend fun exportFull(): String
    suspend fun restoreFull(json: String): RestoreResult
}

data class RestoreResult(val newCount: Int, val mergedCount: Int, val issues: List<String>)
