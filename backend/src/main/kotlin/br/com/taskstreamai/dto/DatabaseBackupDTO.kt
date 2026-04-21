package br.com.taskstreamai.dto

import java.time.LocalDateTime

data class BackupCreatedDTO(
    val dbFilename: String,
    val sqlFilename: String,
    val timestamp: LocalDateTime,
    val status: String
) {
}

data class BackupRestoredDTO(
    val filename: String,
    val timestamp: LocalDateTime,
    val status: String
)

data class BackupFileDTO(
    val filename: String,
    val directory: String,
    val size: String,
    val createdAt: LocalDateTime,
)