package com.flashlearn.domain.repository

interface BackupRepository {
    suspend fun exportFull(): String
    suspend fun restoreFull(json: String): RestoreResult
}

data class RestoreResult(val imported: Int, val skipped: Int, val issues: List<String>)
