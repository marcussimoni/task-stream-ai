package br.com.taskstreamai.service

import br.com.taskstreamai.dto.BackupCreatedDTO
import br.com.taskstreamai.dto.BackupFileDTO
import br.com.taskstreamai.dto.BackupRestoredDTO
import br.com.taskstreamai.repository.DatabaseBackupRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.log10
import kotlin.math.pow

@Service
class DatabaseBackupService(
    val databaseBackupRepository: DatabaseBackupRepository,
    @Value("\${database.backup.base-path}")
    private var basePath: String
) {

    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val logger = LoggerFactory.getLogger(DatabaseBackupService::class.java)

    fun backupDatabase(): BackupCreatedDTO {
        val filename = "dailytrack-backup-${LocalDate.now().format(formatter)}";
        val sqlFilename = databaseBackupRepository.createSqlDatabaseBackup(filename)
        val dbFilename = databaseBackupRepository.createDbDatabaseBackup(filename)
        return BackupCreatedDTO(
            dbFilename = dbFilename,
            sqlFilename = sqlFilename,
            timestamp = LocalDateTime.now(),
            status = "Backup Successfully Created"
        )

    }

    fun restoreDatabase(filename: String): BackupRestoredDTO {

        logger.info("Restoring database backup $filename")

        val sqlFilename = if (filename.contains(".sql")){
            filename.replace(".sql", "")
        } else {
            filename
        }

        val filename = databaseBackupRepository.restoreDatabaseBackup(sqlFilename)

        return BackupRestoredDTO(
            filename = filename,
            timestamp = LocalDateTime.now(),
            status = "Backup Successfully Restored"
        )
    }

    fun listDatabaseBackups(): List<BackupFileDTO> {
        val home = System.getProperty("user.home")
        val sqlFiles = File("$home/$basePath/sql")
        if (!sqlFiles.exists()) {
            logger.info("Database backups not found")
            return emptyList()
        }
        return sqlFiles
            .listFiles()
            .map {
                BackupFileDTO(
                    filename = it.name,
                    directory = it.absolutePath,
                    size = formatFileSize(it.length()),
                    createdAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it.lastModified()),
                        ZoneOffset.UTC
                    )
                )
            }.toList()
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

}