package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.*
import br.com.taskstreamai.service.TaskService
import tools.jackson.databind.json.JsonMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(TaskController::class)
@ExtendWith(MockitoExtension::class)
class TaskControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jsonMapper: JsonMapper = JsonMapper.builder().build()
) {

    @MockitoBean
    private lateinit var taskService: TaskService

    @Test
    fun `should create task successfully`() {
        // Given
        val requestDTO = TaskRequestDTO(
            name = "Complete project documentation",
            description = "Write comprehensive documentation for the new feature",
            currentValue = 0,
            startDate = LocalDate.of(2024, 1, 15),
            endDateInterval = 5,
            endDate = null,
            completed = false,
            customEndDateSelected = false,
            priority = br.com.taskstreamai.model.Priority.HIGH,
            tagId = 1L,
            link = null
        )
        
        val responseDTO = TaskDTO(
            id = 1L,
            name = "Complete project documentation",
            description = "Write comprehensive documentation for the new feature",
            currentValue = 0,
            startDate = LocalDate.of(2024, 1, 15),
            endDateInterval = 5,
            endDate = LocalDate.of(2024, 1, 20),
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
            priority = br.com.taskstreamai.model.Priority.HIGH,
            link = null,
            summary = null
        )
        
        Mockito.`when`(taskService.createTask(requestDTO)).thenReturn(responseDTO)

        // When & Then
        mockMvc.post("/api/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status().isCreated()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.id").value(1)
            jsonPath("$.name").value("Complete project documentation")
            jsonPath("$.description").value("Write comprehensive documentation for the new feature")
            jsonPath("$.priority").value("HIGH")
            jsonPath("$.tag.name").value("Work")
        }
    }

    @Test
    fun `should create multiple tasks successfully`() {
        // Given
        val requestDTOs = listOf(
            TaskRequestDTO(
                name = "Task 1",
                description = "First task",
                currentValue = 0,
                startDate = LocalDate.now(),
                endDateInterval = 1,
                endDate = null,
                completed = false,
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.MEDIUM,
                tagId = 1L,
                link = null
            ),
            TaskRequestDTO(
                name = "Task 2",
                description = "Second task",
                currentValue = 0,
                startDate = LocalDate.now(),
                endDateInterval = 2,
                endDate = null,
                completed = false,
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.LOW,
                tagId = 2L,
                link = null
            )
        )
        
        Mockito.doNothing().`when`(taskService).createTasks(requestDTOs)

        // When & Then
        mockMvc.post("/api/tasks/create-all") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(requestDTOs)
        }.andExpect {
            status().isOk()
        }
    }

    @Test
    fun `should get all tasks successfully`() {
        // Given
        val tasks = listOf(
            TaskDTO(
                id = 1L,
                name = "Task 1",
                description = "First task",
                currentValue = 25,
                startDate = LocalDate.of(2024, 1, 15),
                endDateInterval = 5,
                endDate = LocalDate.of(2024, 1, 20),
                completed = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
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
                summary = null
            ),
            TaskDTO(
                id = 2L,
                name = "Task 2",
                description = "Second task",
                currentValue = 50,
                startDate = LocalDate.of(2024, 1, 16),
                endDateInterval = 3,
                endDate = LocalDate.of(2024, 1, 19),
                completed = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                tag = TagDTO(
                    id = 2L,
                    name = "Personal",
                    description = "Personal tasks",
                    color = "#00FF00",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.HIGH,
                link = null,
                summary = null
            )
        )
        
        Mockito.doReturn(tasks).`when`(taskService).getAllTasks(TaskQueryParamsDTO(task = null))

        // When & Then
        mockMvc.get("/api/tasks").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].name").value("Task 1")
            jsonPath("$[1].name").value("Task 2")
        }
    }

    @Test
    fun `should get all tasks with tag filter`() {
        // Given
        val tagId = 1L
        val tasks = listOf(
            TaskDTO(
                id = 1L,
                name = "Work Task",
                description = "A work related task",
                currentValue = 30,
                startDate = LocalDate.now(),
                endDateInterval = 5,
                endDate = LocalDate.now().plusDays(5),
                completed = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                tag = TagDTO(
                    id = tagId,
                    name = "Work",
                    description = "Work tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.MEDIUM,
                link = null,
                summary = null
            )
        )
        
        Mockito.doReturn(tasks).`when`(taskService).getAllTasks(TaskQueryParamsDTO(task = null))

        // When & Then
        mockMvc.get("/api/tasks") {
            param("tag", tagId.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(1)
            jsonPath("$[0].tag.id").value(tagId)
        }
    }

    @Test
    fun `should get task by id successfully`() {
        // Given
        val taskId = 1L
        val task = TaskDTO(
            id = taskId,
            name = "Specific Task",
            description = "A specific task",
            currentValue = 75,
            startDate = LocalDate.now(),
            endDateInterval = 2,
            endDate = LocalDate.now().plusDays(2),
            completed = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            tag = TagDTO(
                id = 1L,
                name = "Work",
                description = "Work tasks",
                color = "#FF0000",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            customEndDateSelected = false,
            priority = br.com.taskstreamai.model.Priority.HIGH,
            link = null,
            summary = null
        )
        
        Mockito.`when`(taskService.getTaskById(taskId)).thenReturn(task)

        // When & Then
        mockMvc.get("/api/tasks/$taskId").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.id").value(taskId)
            jsonPath("$.name").value("Specific Task")
        }
    }

    @Test
    fun `should update task successfully`() {
        // Given
        val taskId = 1L
        val requestDTO = TaskRequestDTO(
            name = "Updated Task",
            description = "Updated description",
            currentValue = 80,
            startDate = LocalDate.now(),
            endDateInterval = 3,
            endDate = LocalDate.now().plusDays(3),
            completed = false,
            customEndDateSelected = true,
            priority = br.com.taskstreamai.model.Priority.CRITICAL,
            tagId = 2L,
            link = "https://example.com"
        )
        
        val updatedTask = TaskDTO(
            id = taskId,
            name = "Updated Task",
            description = "Updated description",
            currentValue = 80,
            startDate = LocalDate.now(),
            endDateInterval = 3,
            endDate = LocalDate.now().plusDays(3),
            completed = false,
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now(),
            tag = TagDTO(
                id = 2L,
                name = "Personal",
                description = "Personal tasks",
                color = "#00FF00",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            customEndDateSelected = true,
            priority = br.com.taskstreamai.model.Priority.CRITICAL,
            link = "https://example.com",
            summary = null
        )
        
        Mockito.`when`(taskService.updateTask(taskId, requestDTO)).thenReturn(updatedTask)

        // When & Then
        mockMvc.put("/api/tasks/$taskId") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.id").value(taskId)
            jsonPath("$.name").value("Updated Task")
            jsonPath("$.priority").value("CRITICAL")
            jsonPath("$.link").value("https://example.com")
        }
    }

    @Test
    fun `should delete task successfully`() {
        // Given
        val taskId = 1L
        Mockito.doNothing().`when`(taskService).deleteTask(taskId)

        // When & Then
        mockMvc.delete("/api/tasks/$taskId").andExpect {
            status().isNoContent()
        }
    }

    @Test
    fun `should get tasks by tag successfully`() {
        // Given
        val tagId = 1L
        val tasks = listOf(
            TaskDTO(
                id = 1L,
                name = "Tag Task 1",
                description = "First task for tag",
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
                    description = "Work tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.MEDIUM,
                link = null,
                summary = null
            ),
            TaskDTO(
                id = 2L,
                name = "Tag Task 2",
                description = "Second task for tag",
                currentValue = 50,
                startDate = LocalDate.now(),
                endDateInterval = 3,
                endDate = LocalDate.now().plusDays(3),
                completed = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                tag = TagDTO(
                    id = tagId,
                    name = "Work",
                    description = "Work tasks",
                    color = "#FF0000",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.HIGH,
                link = null,
                summary = null
            )
        )
        
        Mockito.`when`(taskService.getTasksByTag(tagId)).thenReturn(tasks)

        // When & Then
        mockMvc.post("/api/tasks/tag/$tagId").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].tag.id").value(tagId)
            jsonPath("$[1].tag.id").value(tagId)
        }
    }

    @Test
    fun `should complete task successfully`() {
        // Given
        val taskId = 1L
        val completedTask = TaskDTO(
            id = taskId,
            name = "Completed Task",
            description = "This task is now completed",
            currentValue = 100,
            startDate = LocalDate.now().minusDays(5),
            endDateInterval = 5,
            endDate = LocalDate.now(),
            completed = true,
            createdAt = LocalDateTime.now().minusDays(5),
            updatedAt = LocalDateTime.now(),
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
            summary = null
        )
        
        Mockito.`when`(taskService.completeTask(taskId)).thenReturn(completedTask)

        // When & Then
        mockMvc.patch("/api/tasks/$taskId/completed").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.id").value(taskId)
            jsonPath("$.completed").value(true)
            jsonPath("$.currentValue").value(100)
        }
    }

    @Test
    fun `should get tasks for month successfully`() {
        // Given
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val tasks = listOf(
            TaskDTO(
                id = 1L,
                name = "January Task 1",
                description = "First task of January",
                currentValue = 25,
                startDate = LocalDate.of(2024, 1, 15),
                endDateInterval = 5,
                endDate = LocalDate.of(2024, 1, 20),
                completed = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
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
                summary = null
            )
        )
        
        Mockito.doReturn(tasks).`when`(taskService).getTasksForMonthRange(startDate, endDate)

        // When & Then
        mockMvc.get("/api/tasks/month") {
            param("startDate", startDate.toString())
            param("endDate", endDate.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(1)
            jsonPath("$[0].name").value("January Task 1")
        }
    }

    @Test
    fun `should get link content successfully`() {
        // Given
        val url = "https://example.com/article"
        val linkContent = LinkContentDTO(
            title = "Example Article"
        )
        
        Mockito.`when`(taskService.loadLinkContent(url)).thenReturn(linkContent)

        // When & Then
        mockMvc.get("/api/tasks/link-content") {
            param("url", url)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.title").value("Example Article")
            jsonPath("$.url").value(url)
        }
    }

    @Test
    fun `should get grouped tasks by tags successfully`() {
        // Given
        val month = LocalDate.of(2024, 1, 1)
        val groupedTasks = listOf(
            taskGroupedByTagDTOBuilder()
        )
        
        Mockito.`when`(taskService.getTasksGroupedByTag(month)).thenReturn(groupedTasks)

        // When & Then
        mockMvc.get("/api/tasks/grouped-by-tags") {
            param("month", month.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(1)
            jsonPath("$[0].tag.name").value("Work")
            jsonPath("$[0].totalTasks").value(1)
            jsonPath("$[0].completedTasks").value(0)
        }
    }

    @Test
    fun `should return 400 when creating task with invalid data`() {
        // Given
        val invalidRequestDTO = TaskRequestDTO(
            name = "", // Empty name should be invalid
            description = "Invalid task",
            currentValue = 0,
            startDate = LocalDate.now(),
            endDateInterval = 1,
            endDate = null,
            completed = false,
            customEndDateSelected = false,
            priority = br.com.taskstreamai.model.Priority.MEDIUM,
            tagId = 1L,
            link = null
        )

        // When & Then
        mockMvc.post("/api/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(invalidRequestDTO)
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when updating task with invalid data`() {
        // Given
        val taskId = 1L
        val invalidRequestDTO = TaskRequestDTO(
            name = "", // Empty name should be invalid
            description = "Invalid task",
            currentValue = 0,
            startDate = LocalDate.now(),
            endDateInterval = 1,
            endDate = null,
            completed = false,
            customEndDateSelected = false,
            priority = br.com.taskstreamai.model.Priority.MEDIUM,
            tagId = 1L,
            link = null
        )

        // When & Then
        mockMvc.put("/api/tasks/$taskId") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(invalidRequestDTO)
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when getting month tasks without required parameters`() {
        // When & Then - Missing startDate
        mockMvc.get("/api/tasks/month") {
            param("endDate", "2024-01-31")
        }.andExpect {
            status().isBadRequest()
        }

        // When & Then - Missing endDate
        mockMvc.get("/api/tasks/month") {
            param("startDate", "2024-01-01")
        }.andExpect {
            status().isBadRequest()
        }

        // When & Then - Missing both parameters
        mockMvc.get("/api/tasks/month").andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when getting link content without url parameter`() {
        // When & Then
        mockMvc.get("/api/tasks/link-content").andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when getting grouped tasks without month parameter`() {
        // When & Then
        mockMvc.get("/api/tasks/grouped-by-tags").andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle service errors gracefully`() {
        // Given
        Mockito.doThrow(RuntimeException("Database error")).`when`(taskService).getAllTasks(TaskQueryParamsDTO(task = null))

        // When & Then
        mockMvc.get("/api/tasks").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle creation service errors gracefully`() {
        // Given
        val requestDTO = TaskRequestDTO(
            name = "Error Task",
            description = "This will cause an error",
            currentValue = 0,
            startDate = LocalDate.now(),
            endDateInterval = 1,
            endDate = null,
            completed = false,
            customEndDateSelected = false,
            priority = br.com.taskstreamai.model.Priority.MEDIUM,
            tagId = 1L,
            link = null
        )
        
        Mockito.doThrow(RuntimeException("Creation failed")).`when`(taskService).createTask(requestDTO)

        // When & Then
        mockMvc.post("/api/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should verify correct content type for all endpoints`() {
        // Given
        val task = TaskDTO(
            id = 1L,
            name = "Test Task",
            description = "Test description",
            currentValue = 50,
            startDate = LocalDate.now(),
            endDateInterval = 5,
            endDate = LocalDate.now().plusDays(5),
            completed = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
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
            summary = null
        )
        
        Mockito.doReturn(listOf(task)).`when`(taskService).getAllTasks(TaskQueryParamsDTO(task = null))
        Mockito.doReturn(task).`when`(taskService).getTaskById(1L)
        Mockito.doReturn(listOf(task)).`when`(taskService).getTasksByTag(1L)
        Mockito.doReturn(listOf(task)).`when`(taskService).getTasksForMonthRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31))
        Mockito.doReturn(LinkContentDTO("https://test.com")).`when`(taskService).loadLinkContent("https://test.com")
        Mockito.doReturn(listOf(TasksGroupedDTO(task.tag.name,0, listOf()))).`when`(taskService).getTasksGroupedByTag(LocalDate.of(2024, 1, 1))

        // When & Then - All endpoints should return JSON
        mockMvc.get("/api/tasks").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        mockMvc.get("/api/tasks/1").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        mockMvc.post("/api/tasks/tag/1").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        mockMvc.get("/api/tasks/month") {
            param("startDate", "2024-01-01")
            param("endDate", "2024-01-31")
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        mockMvc.get("/api/tasks/link-content") {
            param("url", "https://test.com")
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        mockMvc.get("/api/tasks/grouped-by-tags") {
            param("month", "2024-01-01")
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }
    }

    private fun taskGroupedByTagDTOBuilder(): TasksGroupedDTO {
        return TasksGroupedDTO(
            tag = "Work",
            tasks = listOf(
                TasksByTagDTO(
                    id = 1L,
                    description = "First work task",
                    currentValue = 25,
                    startDate = LocalDate.of(2024, 1, 15),
                    endDate = LocalDate.of(2024, 1, 20),
                    completed = false,
                    tag = "Work",
                    priority = br.com.taskstreamai.model.Priority.MEDIUM.name,
                    task = "Task 1",
                )
            ),
            total = 1
        )
    }
}
