package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.BackupCreatedDTO
import br.com.taskstreamai.dto.BackupFileDTO
import br.com.taskstreamai.dto.BackupRestoredDTO
import br.com.taskstreamai.service.DatabaseBackupService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.coroutines.RestrictsSuspension

@RestController
@RequestMapping("/api/backup-database")
class BackupDatabaseController(
    val backupService: DatabaseBackupService
) {

    @GetMapping
    fun backupDatabase(): BackupCreatedDTO = backupService.backupDatabase()

    @GetMapping("/restore")
    fun restoreDatabase(@RequestParam(name = "filename", required = true) filename: String): BackupRestoredDTO = backupService.restoreDatabase(filename)

    @GetMapping("/backup-files")
    fun getBackupFiles(): List<BackupFileDTO> = backupService.listDatabaseBackups()

}