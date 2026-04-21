package br.com.taskstreamai.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DatabaseBackupSchedulerService(
    private val databaseBackupService: DatabaseBackupService
) {

    val logger = LoggerFactory.getLogger(DatabaseBackupSchedulerService::class.java)

    @Scheduled(cron = $$"${application.start-database-backup-scheduler}")
    fun databaseBackupScheduler() {
        logger.info("Starting database backup scheduler")
        databaseBackupService.backupDatabase()
        logger.info("Finished database backup scheduler")
    }

}