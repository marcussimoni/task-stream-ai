package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.BackupCreatedDTO
import br.com.taskstreamai.dto.BackupFileDTO
import br.com.taskstreamai.dto.BackupRestoredDTO
import br.com.taskstreamai.service.DatabaseBackupService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(BackupDatabaseController::class)
@ExtendWith(MockitoExtension::class)
class BackupDatabaseControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    private lateinit var backupService: DatabaseBackupService

    @Test
    fun `should backup database successfully`() {
        // Given
        val timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0)
        val expectedBackupCreatedDTO = BackupCreatedDTO(
            dbFilename = "task_stream_ai_20240115_103000.db",
            sqlFilename = "task_stream_ai_20240115_103000.sql",
            timestamp = timestamp,
            status = "SUCCESS"
        )
        
        Mockito.`when`(backupService.backupDatabase()).thenReturn(expectedBackupCreatedDTO)

        // When & Then
        mockMvc.get("/api/backup-database").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.dbFilename").value("task_stream_ai_20240115_103000.db")
            jsonPath("$.sqlFilename").value("task_stream_ai_20240115_103000.sql")
            jsonPath("$.timestamp").value("2024-01-15T10:30:00")
            jsonPath("$.status").value("SUCCESS")
        }
    }

    @Test
    fun `should restore database successfully`() {
        // Given
        val filename = "task_stream_ai_20240115_103000.sql"
        val timestamp = LocalDateTime.of(2024, 1, 15, 10, 45, 0)
        val expectedBackupRestoredDTO = BackupRestoredDTO(
            filename = filename,
            timestamp = timestamp,
            status = "RESTORED"
        )
        
        Mockito.`when`(backupService.restoreDatabase(filename)).thenReturn(expectedBackupRestoredDTO)

        // When & Then
        mockMvc.get("/api/backup-database/restore") {
            param("filename", filename)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.filename").value(filename)
            jsonPath("$.timestamp").value("2024-01-15T10:45:00")
            jsonPath("$.status").value("RESTORED")
        }
    }

    @Test
    fun `should get backup files list successfully`() {
        // Given
        val timestamp1 = LocalDateTime.of(2024, 1, 15, 10, 30, 0)
        val timestamp2 = LocalDateTime.of(2024, 1, 14, 15, 20, 0)
        val expectedBackupFiles = listOf(
            BackupFileDTO(
                filename = "task_stream_ai_20240115_103000.db",
                directory = "/backups",
                size = "2.5 MB",
                createdAt = timestamp1
            ),
            BackupFileDTO(
                filename = "task_stream_ai_20240114_152000.db",
                directory = "/backups",
                size = "2.3 MB",
                createdAt = timestamp2
            )
        )
        
        Mockito.`when`(backupService.listDatabaseBackups()).thenReturn(expectedBackupFiles)

        // When & Then
        mockMvc.get("/api/backup-database/backup-files").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].filename").value("task_stream_ai_20240115_103000.db")
            jsonPath("$[0].directory").value("/backups")
            jsonPath("$[0].size").value("2.5 MB")
            jsonPath("$[0].createdAt").value("2024-01-15T10:30:00")
            jsonPath("$[1].filename").value("task_stream_ai_20240114_152000.db")
            jsonPath("$[1].directory").value("/backups")
            jsonPath("$[1].size").value("2.3 MB")
            jsonPath("$[1].createdAt").value("2024-01-14T15:20:00")
        }
    }

    @Test
    fun `should return empty backup files list`() {
        // Given
        Mockito.`when`(backupService.listDatabaseBackups()).thenReturn(emptyList())

        // When & Then
        mockMvc.get("/api/backup-database/backup-files").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(0)
        }
    }

    @Test
    fun `should handle backup failure`() {
        // Given
        val timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0)
        val expectedBackupCreatedDTO = BackupCreatedDTO(
            dbFilename = "",
            sqlFilename = "",
            timestamp = timestamp,
            status = "FAILED"
        )
        
        Mockito.`when`(backupService.backupDatabase()).thenReturn(expectedBackupCreatedDTO)

        // When & Then
        mockMvc.get("/api/backup-database").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.status").value("FAILED")
            jsonPath("$.dbFilename").value("")
            jsonPath("$.sqlFilename").value("")
        }
    }

    @Test
    fun `should handle restore failure`() {
        // Given
        val filename = "nonexistent_backup.sql"
        val timestamp = LocalDateTime.of(2024, 1, 15, 10, 45, 0)
        val expectedBackupRestoredDTO = BackupRestoredDTO(
            filename = filename,
            timestamp = timestamp,
            status = "FAILED"
        )
        
        Mockito.`when`(backupService.restoreDatabase(filename)).thenReturn(expectedBackupRestoredDTO)

        // When & Then
        mockMvc.get("/api/backup-database/restore") {
            param("filename", filename)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.filename").value(filename)
            jsonPath("$.status").value("FAILED")
        }
    }

    @Test
    fun `should return 400 when filename parameter is missing for restore`() {
        // When & Then
        mockMvc.get("/api/backup-database/restore").andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle empty filename for restore`() {
        // Given
        val emptyFilename = ""
        val timestamp = LocalDateTime.of(2024, 1, 15, 10, 45, 0)
        val expectedBackupRestoredDTO = BackupRestoredDTO(
            filename = emptyFilename,
            timestamp = timestamp,
            status = "FAILED"
        )
        
        Mockito.`when`(backupService.restoreDatabase(emptyFilename)).thenReturn(expectedBackupRestoredDTO)

        // When & Then
        mockMvc.get("/api/backup-database/restore") {
            param("filename", emptyFilename)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.filename").value("")
            jsonPath("$.status").value("FAILED")
        }
    }

    @Test
    fun `should handle special characters in filename`() {
        // Given
        val specialFilename = "backup_2024-01-15_10:30:00.sql"
        val timestamp = LocalDateTime.of(2024, 1, 15, 10, 45, 0)
        val expectedBackupRestoredDTO = BackupRestoredDTO(
            filename = specialFilename,
            timestamp = timestamp,
            status = "RESTORED"
        )
        
        Mockito.`when`(backupService.restoreDatabase(specialFilename)).thenReturn(expectedBackupRestoredDTO)

        // When & Then
        mockMvc.get("/api/backup-database/restore") {
            param("filename", specialFilename)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.filename").value(specialFilename)
            jsonPath("$.status").value("RESTORED")
        }
    }

    @Test
    fun `should handle backup service errors gracefully`() {
        // Given
        Mockito.`when`(backupService.backupDatabase()).thenThrow(RuntimeException("Database connection failed"))

        // When & Then
        mockMvc.get("/api/backup-database").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle restore service errors gracefully`() {
        // Given
        val filename = "test_backup.sql"
        Mockito.`when`(backupService.restoreDatabase(filename)).thenThrow(RuntimeException("File not found"))

        // When & Then
        mockMvc.get("/api/backup-database/restore") {
            param("filename", filename)
        }.andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle list backup files service errors gracefully`() {
        // Given
        Mockito.`when`(backupService.listDatabaseBackups()).thenThrow(RuntimeException("Directory access denied"))

        // When & Then
        mockMvc.get("/api/backup-database/backup-files").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle large backup files list`() {
        // Given
        val largeBackupFilesList = (1..50).map { index ->
            BackupFileDTO(
                filename = "backup_$index.db",
                directory = "/backups",
                size = "${index}.${index % 10} MB",
                createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0).plusMinutes(index.toLong())
            )
        }
        
        Mockito.`when`(backupService.listDatabaseBackups()).thenReturn(largeBackupFilesList)

        // When & Then
        mockMvc.get("/api/backup-database/backup-files").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(50)
            jsonPath("$[0].filename").value("backup_1.db")
            jsonPath("$[49].filename").value("backup_50.db")
        }
    }

    @Test
    fun `should verify correct content type for all endpoints`() {
        // Given
        val timestamp = LocalDateTime.now()
        Mockito.`when`(backupService.backupDatabase()).thenReturn(
            BackupCreatedDTO("test.db", "test.sql", timestamp, "SUCCESS")
        )
        Mockito.`when`(backupService.restoreDatabase("test.sql")).thenReturn(
            BackupRestoredDTO("test.sql", timestamp, "SUCCESS")
        )
        Mockito.`when`(backupService.listDatabaseBackups()).thenReturn(
            listOf(BackupFileDTO("test.db", "/backups", "1.0 MB", timestamp))
        )

        // When & Then - Backup endpoint
        mockMvc.get("/api/backup-database").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        // When & Then - Restore endpoint
        mockMvc.get("/api/backup-database/restore") {
            param("filename", "test.sql")
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        // When & Then - List files endpoint
        mockMvc.get("/api/backup-database/backup-files").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }
    }
}
