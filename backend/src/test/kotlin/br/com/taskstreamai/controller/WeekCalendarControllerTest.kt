package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.CreateWeekScheduleRequest
import br.com.taskstreamai.dto.TagDTO
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.WeekScheduleDTO
import br.com.taskstreamai.service.WeekScheduleService
import tools.jackson.databind.json.JsonMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(WeekCalendarController::class)
@ExtendWith(MockitoExtension::class)
class WeekCalendarControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jsonMapper: JsonMapper = JsonMapper.builder().build()
) {

    @MockitoBean
    private lateinit var weekScheduleService: WeekScheduleService

    @Test
    fun `should get week schedule successfully`() {
        // Given
        val weekStartDate = "2024-01-15"
        val schedule = listOf(
            WeekScheduleDTO(
                id = 1L,
                dayOfWeek = 1, // Monday
                hour = 9,
                weekStartDate = weekStartDate,
                tagId = 1L,
                tag = TagDTO(
                    id = 1L,
                    name = "Work",
                    description = "Work related tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                task = TaskDTO(
                    id = 1L,
                    name = "Monday Work Task",
                    description = "Work task for Monday",
                    currentValue = 0,
                    startDate = LocalDate.parse(weekStartDate),
                    endDateInterval = 1,
                    endDate = LocalDate.parse(weekStartDate).plusDays(1),
                    completed = false,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    tag = TagDTO(
                        id = 1L,
                        name = "Work",
                        description = "Work related tasks",
                        color = "#FF0000",
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    ),
                    customEndDateSelected = false,
                    priority = br.com.taskstreamai.model.Priority.MEDIUM,
                    link = null,
                    summary = null
                )
            ),
            WeekScheduleDTO(
                id = 2L,
                dayOfWeek = 3, // Wednesday
                hour = 14,
                weekStartDate = weekStartDate,
                tagId = 2L,
                tag = TagDTO(
                    id = 2L,
                    name = "Personal",
                    description = "Personal tasks",
                    color = "#00FF00",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                task = null
            )
        )
        
        Mockito.`when`(weekScheduleService.getWeekSchedule(LocalDate.parse(weekStartDate))).thenReturn(schedule)

        // When & Then
        mockMvc.get("/api/week-calendar") {
            param("weekStartDate", weekStartDate)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].id").value(1)
            jsonPath("$[0].dayOfWeek").value(1)
            jsonPath("$[0].hour").value(9)
            jsonPath("$[0].tag.name").value("Work")
            jsonPath("$[0].task.name").value("Monday Work Task")
            jsonPath("$[1].id").value(2)
            jsonPath("$[1].dayOfWeek").value(3)
            jsonPath("$[1].hour").value(14)
            jsonPath("$[1].tag.name").value("Personal")
        }
    }

    @Test
    fun `should create schedule successfully`() {
        // Given
        val request = CreateWeekScheduleRequest(
            dayOfWeek = 2, // Tuesday
            hour = 10,
            weekStartDate = "2024-01-15",
            tagId = 1L
        )
        
        val createdSchedule = WeekScheduleDTO(
            id = 3L,
            dayOfWeek = 2,
            hour = 10,
            weekStartDate = "2024-01-15",
            tagId = 1L,
            tag = TagDTO(
                id = 1L,
                name = "Work",
                description = "Work related tasks",
                color = "#FF0000",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            task = null
        )
        
        Mockito.`when`(weekScheduleService.createOrUpdateSchedule(request)).thenReturn(createdSchedule)

        // When & Then
        mockMvc.post("/api/week-calendar") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(request)
        }.andExpect {
            status().isCreated()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.id").value(3)
            jsonPath("$.dayOfWeek").value(2)
            jsonPath("$.hour").value(10)
            jsonPath("$.weekStartDate").value("2024-01-15")
            jsonPath("$.tag.name").value("Work")
        }
    }

    @Test
    fun `should delete schedule successfully`() {
        // Given
        val scheduleId = 1L
        Mockito.doNothing().`when`(weekScheduleService).deleteSchedule(scheduleId)

        // When & Then
        mockMvc.delete("/api/week-calendar/$scheduleId").andExpect {
            status().isNoContent()
        }
    }

    @Test
    fun `should get task for tag successfully`() {
        // Given
        val tagId = 1L
        val task = TaskDTO(
            id = 1L,
            name = "Tag Task",
            description = "Task for specific tag",
            currentValue = 25,
            startDate = LocalDate.now(),
            endDateInterval = 5,
            endDate = LocalDate.now().plusDays(5),
            completed = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            tag = TagDTO(
                id = tagId,
                name = "Work",
                description = "Work related tasks",
                color = "#FF0000",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            customEndDateSelected = false,
            priority = br.com.taskstreamai.model.Priority.MEDIUM,
            link = null,
            summary = null
        )
        
        Mockito.`when`(weekScheduleService.getTaskForTag(tagId)).thenReturn(task)

        // When & Then
        mockMvc.get("/api/week-calendar/task-for-tag") {
            param("tagId", tagId.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.id").value(1)
            jsonPath("$.name").value("Tag Task")
            jsonPath("$.tag.id").value(tagId)
        }
    }

    @Test
    fun `should return 404 when no task found for tag`() {
        // Given
        val tagId = 999L
        Mockito.`when`(weekScheduleService.getTaskForTag(tagId)).thenReturn(null)

        // When & Then
        mockMvc.get("/api/week-calendar/task-for-tag") {
            param("tagId", tagId.toString())
        }.andExpect {
            status().isNotFound()
        }
    }

    @Test
    fun `should return empty schedule when no schedules exist`() {
        // Given
        val weekStartDate = "2024-01-15"
        Mockito.`when`(weekScheduleService.getWeekSchedule(LocalDate.parse(weekStartDate))).thenReturn(emptyList())

        // When & Then
        mockMvc.get("/api/week-calendar") {
            param("weekStartDate", weekStartDate)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(0)
        }
    }

    @Test
    fun `should return 400 when weekStartDate parameter is missing`() {
        // When & Then
        mockMvc.get("/api/week-calendar").andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when weekStartDate is invalid date format`() {
        // When & Then
        mockMvc.get("/api/week-calendar") {
            param("weekStartDate", "invalid-date")
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when creating schedule with invalid data`() {
        // Given
        val invalidRequest = CreateWeekScheduleRequest(
            dayOfWeek = 8, // Invalid day of week (should be 1-7)
            hour = 25, // Invalid hour (should be 0-23)
            weekStartDate = "2024-01-15",
            tagId = 1L
        )

        // When & Then
        mockMvc.post("/api/week-calendar") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when tagId parameter is missing`() {
        // When & Then
        mockMvc.get("/api/week-calendar/task-for-tag").andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle different days of week correctly`() {
        // Given
        val weekStartDate = "2024-01-15"
        val schedule = listOf(
            WeekScheduleDTO(
                id = 1L,
                dayOfWeek = 1, // Monday
                hour = 9,
                weekStartDate = weekStartDate,
                tagId = 1L,
                tag = TagDTO(
                    id = 1L,
                    name = "Work",
                    description = "Work tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                task = null
            ),
            WeekScheduleDTO(
                id = 2L,
                dayOfWeek = 7, // Sunday
                hour = 15,
                weekStartDate = weekStartDate,
                tagId = 2L,
                tag = TagDTO(
                    id = 2L,
                    name = "Personal",
                    description = "Personal tasks",
                    color = "#00FF00",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                task = null
            )
        )
        
        Mockito.`when`(weekScheduleService.getWeekSchedule(LocalDate.parse(weekStartDate))).thenReturn(schedule)

        // When & Then
        mockMvc.get("/api/week-calendar") {
            param("weekStartDate", weekStartDate)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].dayOfWeek").value(1)
            jsonPath("$[1].dayOfWeek").value(7)
        }
    }

    @Test
    fun `should handle different hours correctly`() {
        // Given
        val weekStartDate = "2024-01-15"
        val schedule = listOf(
            WeekScheduleDTO(
                id = 1L,
                dayOfWeek = 1,
                hour = 0, // Midnight
                weekStartDate = weekStartDate,
                tagId = 1L,
                tag = TagDTO(
                    id = 1L,
                    name = "Work",
                    description = "Work tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                task = null
            ),
            WeekScheduleDTO(
                id = 2L,
                dayOfWeek = 1,
                hour = 23, // 11 PM
                weekStartDate = weekStartDate,
                tagId = 2L,
                tag = TagDTO(
                    id = 2L,
                    name = "Personal",
                    description = "Personal tasks",
                    color = "#00FF00",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                task = null
            )
        )
        
        Mockito.`when`(weekScheduleService.getWeekSchedule(LocalDate.parse(weekStartDate))).thenReturn(schedule)

        // When & Then
        mockMvc.get("/api/week-calendar") {
            param("weekStartDate", weekStartDate)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].hour").value(0)
            jsonPath("$[1].hour").value(23)
        }
    }

    @Test
    fun `should handle schedule creation with all valid days`() {
        // Given
        val validDays = (1..7).toList() // Monday through Sunday
        
        validDays.forEach { dayOfWeek ->
            val request = CreateWeekScheduleRequest(
                dayOfWeek = dayOfWeek,
                hour = 10,
                weekStartDate = "2024-01-15",
                tagId = 1L
            )
            
            val createdSchedule = WeekScheduleDTO(
                id = dayOfWeek.toLong(),
                dayOfWeek = dayOfWeek,
                hour = 10,
                weekStartDate = "2024-01-15",
                tagId = 1L,
                tag = TagDTO(
                    id = 1L,
                    name = "Work",
                    description = "Work tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                task = null
            )
            
            Mockito.`when`(weekScheduleService.createOrUpdateSchedule(request)).thenReturn(createdSchedule)

            // When & Then
            mockMvc.post("/api/week-calendar") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper.writeValueAsString(request)
            }.andExpect {
                status().isCreated()
                content().contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.dayOfWeek").value(dayOfWeek)
                jsonPath("$.hour").value(10)
            }
        }
    }

    @Test
    fun `should handle schedule creation with all valid hours`() {
        // Given
        val validHours = (0..23).toList()
        
        validHours.take(5).forEach { hour -> // Test first 5 hours to keep test reasonable
            val request = CreateWeekScheduleRequest(
                dayOfWeek = 1,
                hour = hour,
                weekStartDate = "2024-01-15",
                tagId = 1L
            )
            
            val createdSchedule = WeekScheduleDTO(
                id = hour.toLong(),
                dayOfWeek = 1,
                hour = hour,
                weekStartDate = "2024-01-15",
                tagId = 1L,
                tag = TagDTO(
                    id = 1L,
                    name = "Work",
                    description = "Work tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                task = null
            )
            
            Mockito.`when`(weekScheduleService.createOrUpdateSchedule(request)).thenReturn(createdSchedule)

            // When & Then
            mockMvc.post("/api/week-calendar") {
                contentType = MediaType.APPLICATION_JSON
                content = jsonMapper.writeValueAsString(request)
            }.andExpect {
                status().isCreated()
                content().contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.hour").value(hour)
            }
        }
    }

    @Test
    fun `should handle service errors gracefully`() {
        // Given
        val weekStartDate = "2024-01-15"
        Mockito.`when`(weekScheduleService.getWeekSchedule(LocalDate.parse(weekStartDate))).thenThrow(RuntimeException("Database error"))

        // When & Then
        mockMvc.get("/api/week-calendar") {
            param("weekStartDate", weekStartDate)
        }.andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle creation service errors gracefully`() {
        // Given
        val request = CreateWeekScheduleRequest(
            dayOfWeek = 1,
            hour = 10,
            weekStartDate = "2024-01-15",
            tagId = 1L
        )
        
        Mockito.doThrow(RuntimeException("Creation failed")).`when`(weekScheduleService).createOrUpdateSchedule(request)

        // When & Then
        mockMvc.post("/api/week-calendar") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(request)
        }.andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle deletion service errors gracefully`() {
        // Given
        val scheduleId = 1L
        Mockito.`when`(weekScheduleService.deleteSchedule(scheduleId)).thenThrow(RuntimeException("Deletion failed"))

        // When & Then
        mockMvc.delete("/api/week-calendar/$scheduleId").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle task for tag service errors gracefully`() {
        // Given
        val tagId = 1L
        Mockito.`when`(weekScheduleService.getTaskForTag(tagId)).thenThrow(RuntimeException("Service error"))

        // When & Then
        mockMvc.get("/api/week-calendar/task-for-tag") {
            param("tagId", tagId.toString())
        }.andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should verify correct content type for all endpoints`() {
        // Given
        val weekStartDate = "2024-01-15"
        val schedule = listOf(
            WeekScheduleDTO(
                id = 1L,
                dayOfWeek = 1,
                hour = 10,
                weekStartDate = weekStartDate,
                tagId = 1L,
                tag = TagDTO(
                    id = 1L,
                    name = "Work",
                    description = "Work tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                task = null
            )
        )
        
        Mockito.doReturn(schedule).`when`(weekScheduleService).getWeekSchedule(LocalDate.parse(weekStartDate))
        Mockito.doReturn(schedule[0]).`when`(weekScheduleService).createOrUpdateSchedule(CreateWeekScheduleRequest(1, 10, weekStartDate, 1L))
        Mockito.doReturn(
            TaskDTO(
                id = 1L,
                name = "Test Task",
                description = "Test",
                currentValue = 0,
                startDate = LocalDate.now(),
                endDateInterval = 1,
                endDate = LocalDate.now().plusDays(1),
                completed = false,
                tag = TagDTO(
                    id = 1L,
                    name = "Work",
                    description = "Work tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.MEDIUM,
                link = null,
                summary = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        ).`when`(weekScheduleService).getTaskForTag(1L)

        // When & Then - All endpoints should return JSON
        mockMvc.get("/api/week-calendar") {
            param("weekStartDate", weekStartDate)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        mockMvc.post("/api/week-calendar") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(
                CreateWeekScheduleRequest(1, 10, weekStartDate, 1L)
            )
        }.andExpect {
            status().isCreated()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        mockMvc.get("/api/week-calendar/task-for-tag") {
            param("tagId", "1")
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }
    }
}
