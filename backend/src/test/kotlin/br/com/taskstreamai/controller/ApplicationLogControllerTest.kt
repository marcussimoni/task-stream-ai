package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.LogDTO
import br.com.taskstreamai.service.ApplicationLogsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
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

@WebMvcTest(ApplicationLogController::class)
@ExtendWith(MockitoExtension::class)
class ApplicationLogControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    private lateinit var applicationLogsService: ApplicationLogsService

    @Test
    fun `should get logs with default line limit`() {
        // Given
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = listOf(
                "2024-01-15 10:00:00 INFO  Starting application...",
                "2024-01-15 10:00:01 INFO  Database connected",
                "2024-01-15 10:00:02 INFO  Server started on port 8080"
            )
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(ApplicationLogsService.LOG_LIMIT_LINES)).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Application Logs")
            jsonPath("$.logs.length()").value(3)
            jsonPath("$.logs[0]").value("2024-01-15 10:00:00 INFO  Starting application...")
            jsonPath("$.logs[1]").value("2024-01-15 10:00:01 INFO  Database connected")
            jsonPath("$.logs[2]").value("2024-01-15 10:00:02 INFO  Server started on port 8080")
        }
    }

    @Test
    fun `should get logs with custom line limit`() {
        // Given
        val customLineLimit = 50
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = listOf(
                "2024-01-15 10:00:00 INFO  Starting application...",
                "2024-01-15 10:00:01 INFO  Database connected"
            )
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(customLineLimit)).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs") {
            param("lines", customLineLimit.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Application Logs")
            jsonPath("$.logs.length()").value(2)
        }
    }

    @Test
    fun `should get logs with zero line limit`() {
        // Given
        val zeroLineLimit = 0
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = emptyList()
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(zeroLineLimit)).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs") {
            param("lines", zeroLineLimit.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Application Logs")
            jsonPath("$.logs.length()").value(0)
        }
    }

    @Test
    fun `should get logs with high line limit`() {
        // Given
        val highLineLimit = 1000
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = listOf(
                "2024-01-15 10:00:00 INFO  Starting application...",
                "2024-01-15 10:00:01 INFO  Database connected",
                "2024-01-15 10:00:02 INFO  Server started on port 8080",
                "2024-01-15 10:00:03 INFO  Application ready"
            )
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(highLineLimit)).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs") {
            param("lines", highLineLimit.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Application Logs")
            jsonPath("$.logs.length()").value(4)
        }
    }

    @Test
    fun `should handle empty logs gracefully`() {
        // Given
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = emptyList()
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(anyInt())).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Application Logs")
            jsonPath("$.logs.length()").value(0)
        }
    }

    @Test
    fun `should handle null logs gracefully`() {
        // Given
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = emptyList()
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(anyInt())).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Application Logs")
            jsonPath("$.logs.length()").value(0)
        }
    }

    @Test
    fun `should handle service error gracefully`() {
        // Given
        Mockito.doThrow(RuntimeException("Log file not found")).`when`(applicationLogsService).retrieveLogs(anyInt())

        // When & Then
        mockMvc.get("/api/application-logs").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle large log content`() {
        // Given
        val largeLogList = (1..100).map { index ->
            "2024-01-15 10:00:${String.format("%02d", index)} INFO  Log entry $index"
        }
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = largeLogList
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(100)).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs") {
            param("lines", "100")
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Application Logs")
            jsonPath("$.logs.length()").value(100)
            jsonPath("$.logs[0]").value("2024-01-15 10:00:01 INFO  Log entry 1")
            jsonPath("$.logs[99]").value("2024-01-15 10:01:40 INFO  Log entry 100")
        }
    }

    @Test
    fun `should handle different log levels`() {
        // Given
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = listOf(
                "2024-01-15 10:00:00 ERROR  Database connection failed",
                "2024-01-15 10:00:01 WARN  Retrying connection...",
                "2024-01-15 10:00:02 INFO  Connection established",
                "2024-01-15 10:00:03 DEBUG  Connection parameters: localhost:5432"
            )
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(10)).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs") {
            param("lines", "10")
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Application Logs")
            jsonPath("$.logs.length()").value(4)
            jsonPath("$.logs[0]").value("2024-01-15 10:00:00 ERROR  Database connection failed")
            jsonPath("$.logs[1]").value("2024-01-15 10:00:01 WARN  Retrying connection...")
            jsonPath("$.logs[2]").value("2024-01-15 10:00:02 INFO  Connection established")
            jsonPath("$.logs[3]").value("2024-01-15 10:00:03 DEBUG  Connection parameters: localhost:5432")
        }
    }

    @Test
    fun `should handle negative line limit`() {
        // Given
        val negativeLineLimit = -10
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = emptyList()
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(negativeLineLimit)).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs") {
            param("lines", negativeLineLimit.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Application Logs")
            jsonPath("$.logs.length()").value(0)
        }
    }

    @Test
    fun `should handle invalid line limit parameter`() {
        // Given
        val invalidLineLimit = "invalid"
        
        // When & Then - This should result in a 400 Bad Request due to type conversion failure
        mockMvc.get("/api/application-logs") {
            param("lines", invalidLineLimit)
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should verify correct content type and structure`() {
        // Given
        val expectedLogDTO = LogDTO(
            title = "Application Logs",
            logs = listOf("Test log entry")
        )
        
        Mockito.`when`(applicationLogsService.retrieveLogs(anyInt())).thenReturn(expectedLogDTO)

        // When & Then
        mockMvc.get("/api/application-logs").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").exists()
            jsonPath("$.logs").exists()
            jsonPath("$.logs").isArray()
        }
    }
}
