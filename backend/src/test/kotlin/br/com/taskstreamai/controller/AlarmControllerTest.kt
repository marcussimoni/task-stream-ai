package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.AlarmDTO
import br.com.taskstreamai.service.AlarmEmitterService
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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@WebMvcTest(AlarmController::class)
@ExtendWith(MockitoExtension::class)
class AlarmControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    private lateinit var alarmEmitterService: AlarmEmitterService

    @Test
    fun `should stream alarms with Last-Event-ID header`() {
        // Given
        val lastEventId = "event-123"
        val mockEmitter = SseEmitter()
        
        Mockito.`when`(alarmEmitterService.createEmitter(lastEventId)).thenReturn(mockEmitter)

        // When & Then
        mockMvc.get("/api/alarms/stream") {
            header("Last-Event-ID", lastEventId)
        }.andExpect {
            status().isOk()
            header().exists("Content-Type")
            header().string("Content-Type", "text/event-stream;charset=UTF-8")
        }
    }

    @Test
    fun `should stream alarms with lastEventId parameter`() {
        // Given
        val lastEventId = "event-456"
        val mockEmitter = SseEmitter()
        
        Mockito.`when`(alarmEmitterService.createEmitter(lastEventId)).thenReturn(mockEmitter)

        // When & Then
        mockMvc.get("/api/alarms/stream") {
            param("lastEventId", lastEventId)
        }.andExpect {
            status().isOk()
            header().exists("Content-Type")
            header().string("Content-Type", "text/event-stream;charset=UTF-8")
        }
    }

    @Test
    fun `should stream alarms without any event ID`() {
        // Given
        val mockEmitter = SseEmitter()
        
        Mockito.`when`(alarmEmitterService.createEmitter(null)).thenReturn(mockEmitter)

        // When & Then
        mockMvc.get("/api/alarms/stream").andExpect {
            status().isOk()
            header().exists("Content-Type")
            header().string("Content-Type", "text/event-stream;charset=UTF-8")
        }
    }

    @Test
    fun `should stream alarms with header taking precedence over parameter`() {
        // Given
        val headerEventId = "event-header"
        val paramEventId = "event-param"
        val mockEmitter = SseEmitter()

        Mockito.`when`(alarmEmitterService.createEmitter(headerEventId)).thenReturn(mockEmitter)

        // When & Then
        mockMvc.get("/api/alarms/stream") {
            header("Last-Event-ID", headerEventId)
            param("lastEventId", paramEventId)
        }.andExpect {
            status().isOk()
        }
    }

    @Test
    fun `should acknowledge alarm successfully`() {
        // Given
        val alarmId = "alarm-123"
        
        Mockito.`when`(alarmEmitterService.acknowledgeAlarm(alarmId)).thenReturn(true)

        // When & Then
        mockMvc.post("/api/alarms/$alarmId/acknowledge").andExpect {
            status().isOk()
        }
    }

    @Test
    fun `should return 404 when acknowledging non-existent alarm`() {
        // Given
        val nonExistentAlarmId = "non-existent-alarm"
        
        Mockito.`when`(alarmEmitterService.acknowledgeAlarm(nonExistentAlarmId)).thenReturn(false)

        // When & Then
        mockMvc.post("/api/alarms/$nonExistentAlarmId/acknowledge").andExpect {
            status().isNotFound()
        }
    }

    @Test
    fun `should get pending alarms successfully`() {
        // Given
        val pendingAlarms = listOf(
            AlarmDTO(
                id = "alarm-1",
                type = "PRE_REMINDER",
                scheduleId = 1L,
                tagName = "Work",
                tagColor = "#FF0000",
                taskName = "Complete project",
                scheduledTime = "2024-01-15T10:00:00",
                message = "Your Work session starts in 5 minutes (10:00 AM)"
            ),
            AlarmDTO(
                id = "alarm-2",
                type = "START_ALARM",
                scheduleId = 2L,
                tagName = "Exercise",
                tagColor = "#00FF00",
                taskName = "Morning run",
                scheduledTime = "2024-01-15T07:00:00",
                message = "START NOW: Exercise session at 7:00 AM"
            )
        )
        
        Mockito.`when`(alarmEmitterService.getPendingAlarms()).thenReturn(pendingAlarms)

        // When & Then
        mockMvc.get("/api/alarms/pending").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].id").value("alarm-1")
            jsonPath("$[0].type").value("PRE_REMINDER")
            jsonPath("$[0].tagName").value("Work")
            jsonPath("$[0].message").value("Your Work session starts in 5 minutes (10:00 AM)")
            jsonPath("$[1].id").value("alarm-2")
            jsonPath("$[1].type").value("START_ALARM")
            jsonPath("$[1].tagName").value("Exercise")
            jsonPath("$[1].message").value("START NOW: Exercise session at 7:00 AM")
        }
    }

    @Test
    fun `should return empty list when no pending alarms`() {
        // Given
        Mockito.`when`(alarmEmitterService.getPendingAlarms()).thenReturn(emptyList())

        // When & Then
        mockMvc.get("/api/alarms/pending").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(0)
        }
    }

    @Test
    fun `should handle null pending alarms gracefully`() {
        // Given
        Mockito.`when`(alarmEmitterService.getPendingAlarms()).thenReturn(null)

        // When & Then
        mockMvc.get("/api/alarms/pending").andExpect {
            status().isOk()
            content().string("")
        }
    }

    @Test
    fun `should acknowledge alarm with complex ID format`() {
        // Given
        val complexAlarmId = "alarm_123-abc_456"
        
        Mockito.`when`(alarmEmitterService.acknowledgeAlarm(complexAlarmId)).thenReturn(true)

        // When & Then
        mockMvc.post("/api/alarms/$complexAlarmId/acknowledge").andExpect {
            status().isOk()
        }
    }

    @Test
    fun `should handle streaming errors gracefully`() {
        // Given
        Mockito.`when`(alarmEmitterService.createEmitter(any())).thenThrow(RuntimeException("Connection error"))

        // When & Then
        mockMvc.get("/api/alarms/stream").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle acknowledgment service errors gracefully`() {
        // Given
        val alarmId = "alarm-error"
        
        Mockito.`when`(alarmEmitterService.acknowledgeAlarm(alarmId)).thenThrow(RuntimeException("Service unavailable"))

        // When & Then
        mockMvc.post("/api/alarms/$alarmId/acknowledge").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle pending alarms service errors gracefully`() {
        // Given
        Mockito.`when`(alarmEmitterService.getPendingAlarms()).thenThrow(RuntimeException("Database error"))

        // When & Then
        mockMvc.get("/api/alarms/pending").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should validate alarm ID format in acknowledgment`() {
        // Given
        val emptyAlarmId = ""
        
        Mockito.`when`(alarmEmitterService.acknowledgeAlarm(emptyAlarmId)).thenReturn(false)

        // When & Then
        mockMvc.post("/api/alarms/$emptyAlarmId/acknowledge").andExpect {
            status().isNotFound()
        }
    }

    @Test
    fun `should handle special characters in alarm ID`() {
        // Given
        val specialAlarmId = "alarm@#$%^&*()"
        
        Mockito.`when`(alarmEmitterService.acknowledgeAlarm(specialAlarmId)).thenReturn(true)

        // When & Then
        mockMvc.post("/api/alarms/$specialAlarmId/acknowledge").andExpect {
            status().isOk()
        }
    }

    @Test
    fun `should verify correct content type for streaming endpoint`() {
        // Given
        val mockEmitter = SseEmitter()
        Mockito.`when`(alarmEmitterService.createEmitter(null)).thenReturn(mockEmitter)

        // When & Then
        mockMvc.get("/api/alarms/stream").andExpect {
            status().isOk()
            header().string("Content-Type", "text/event-stream;charset=UTF-8")
            header().string("Cache-Control", "no-cache")
            header().string("Connection", "keep-alive")
        }
    }
}
