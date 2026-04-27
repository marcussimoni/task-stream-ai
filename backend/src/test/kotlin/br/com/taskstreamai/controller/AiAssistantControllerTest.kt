package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.AutomatedTaskDTO
import br.com.taskstreamai.dto.TaskRequestDTO
import br.com.taskstreamai.service.AiAssistantService
import tools.jackson.databind.json.JsonMapper
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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AiAssistantController::class)
@ExtendWith(MockitoExtension::class)
class AiAssistantControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jsonMapper: JsonMapper = JsonMapper.builder().build()
) {

    @MockitoBean
    private lateinit var aiAssistantService: AiAssistantService

    @Test
    fun `should plan automated task creation successfully`() {
        // Given
        val automatedTaskDTO = AutomatedTaskDTO(
            input = "Create a task for daily exercise routine"
        )
        
        val expectedTaskRequests = listOf(
            TaskRequestDTO(
                name = "Morning Exercise",
                description = "Complete 30 minutes of cardio exercise",
                currentValue = 0,
                startDate = java.time.LocalDate.now(),
                endDateInterval = 4,
                endDate = null,
                completed = false,
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.MEDIUM,
                tagId = 1L,
                link = null
            ),
            TaskRequestDTO(
                name = "Evening Stretch",
                description = "Complete 15 minutes of stretching exercises",
                currentValue = 0,
                startDate = java.time.LocalDate.now(),
                endDateInterval = 4,
                endDate = null,
                completed = false,
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.LOW,
                tagId = 1L,
                link = null
            )
        )
        
        Mockito.doReturn(expectedTaskRequests).`when`(aiAssistantService).planAutomatedTaskCreation(automatedTaskDTO)

        // When & Then
        mockMvc.post("/api/ai-assistant/plan-automated-creation") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(automatedTaskDTO)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].name").value("Morning Exercise")
            jsonPath("$[0].description").value("Complete 30 minutes of cardio exercise")
            jsonPath("$[1].name").value("Evening Stretch")
            jsonPath("$[1].description").value("Complete 15 minutes of stretching exercises")
        }
    }

    @Test
    fun `should return null when AI service returns null`() {
        // Given
        val automatedTaskDTO = AutomatedTaskDTO(
            input = "Invalid input that cannot be processed"
        )
        
        Mockito.doReturn(null).`when`(aiAssistantService).planAutomatedTaskCreation(automatedTaskDTO)

        // When & Then
        mockMvc.post("/api/ai-assistant/plan-automated-creation") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(automatedTaskDTO)
        }.andExpect {
            status().isOk()
            content().string("")
        }
    }

    @Test
    fun `should return empty list when AI service returns empty list`() {
        // Given
        val automatedTaskDTO = AutomatedTaskDTO(
            input = "No tasks needed"
        )
        
        Mockito.doReturn(emptyList<TaskRequestDTO>()).`when`(aiAssistantService).planAutomatedTaskCreation(automatedTaskDTO)

        // When & Then
        mockMvc.post("/api/ai-assistant/plan-automated-creation") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(automatedTaskDTO)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(0)
        }
    }

    @Test
    fun `should handle single task creation successfully`() {
        // Given
        val automatedTaskDTO = AutomatedTaskDTO(
            input = "Create a task for weekly grocery shopping"
        )
        
        val expectedTaskRequest = TaskRequestDTO(
            name = "Weekly Grocery Shopping",
            description = "Buy groceries for the week",
            currentValue = 0,
            startDate = java.time.LocalDate.now(),
            endDateInterval = 1,
            endDate = null,
            completed = false,
            customEndDateSelected = false,
            priority = br.com.taskstreamai.model.Priority.HIGH,
            tagId = 2L,
            link = null
        )

        Mockito.doReturn(listOf(expectedTaskRequest)).`when`(aiAssistantService).planAutomatedTaskCreation(automatedTaskDTO)

        // When & Then
        mockMvc.post("/api/ai-assistant/plan-automated-creation") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(automatedTaskDTO)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(1)
            jsonPath("$[0].name").value("Weekly Grocery Shopping")
            jsonPath("$[0].description").value("Buy groceries for the week")
            jsonPath("$[0].priority").value("HIGH")
            jsonPath("$[0].tagId").value(2)
        }
    }

    @Test
    fun `should return 400 when request body is invalid`() {
        // Given
        val invalidJson = """{"invalidField": "invalidValue"}"""

        // When & Then
        mockMvc.post("/api/ai-assistant/plan-automated-creation") {
            contentType = MediaType.APPLICATION_JSON
            content = invalidJson
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when input is empty`() {
        // Given
        val automatedTaskDTO = AutomatedTaskDTO(
            input = ""
        )

        Mockito.doReturn(emptyList<TaskRequestDTO>()).`when`(aiAssistantService).planAutomatedTaskCreation(automatedTaskDTO)

        // When & Then
        mockMvc.post("/api/ai-assistant/plan-automated-creation") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(automatedTaskDTO)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(0)
        }
    }

    @Test
    fun `should handle complex task creation with multiple priorities`() {
        // Given
        val automatedTaskDTO = AutomatedTaskDTO(
            input = "Create tasks for project management including planning, development, testing, and deployment"
        )
        
        val expectedTaskRequests = listOf(
            TaskRequestDTO(
                name = "Project Planning",
                description = "Create detailed project plan and timeline",
                currentValue = 0,
                startDate = java.time.LocalDate.now(),
                endDateInterval = 2,
                endDate = null,
                completed = false,
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.CRITICAL,
                tagId = 3L,
                link = null
            ),
            TaskRequestDTO(
                name = "Development Phase",
                description = "Implement core features and functionality",
                currentValue = 0,
                startDate = java.time.LocalDate.now().plusDays(1),
                endDateInterval = 3,
                endDate = null,
                completed = false,
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.HIGH,
                tagId = 3L,
                link = null
            ),
            TaskRequestDTO(
                name = "Testing Phase",
                description = "Perform comprehensive testing including unit and integration tests",
                currentValue = 0,
                startDate = java.time.LocalDate.now().plusDays(4),
                endDateInterval = 2,
                endDate = null,
                completed = false,
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.MEDIUM,
                tagId = 3L,
                link = null
            ),
            TaskRequestDTO(
                name = "Deployment",
                description = "Deploy application to production environment",
                currentValue = 0,
                startDate = java.time.LocalDate.now().plusDays(7),
                endDateInterval = 1,
                endDate = null,
                completed = false,
                customEndDateSelected = false,
                priority = br.com.taskstreamai.model.Priority.HIGH,
                tagId = 3L,
                link = null
            )
        )

        Mockito.doReturn(expectedTaskRequests).`when`(aiAssistantService).planAutomatedTaskCreation(automatedTaskDTO)

        // When & Then
        mockMvc.post("/api/ai-assistant/plan-automated-creation") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(automatedTaskDTO)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(4)
            jsonPath("$[0].priority").value("CRITICAL")
            jsonPath("$[1].priority").value("HIGH")
            jsonPath("$[2].priority").value("MEDIUM")
            jsonPath("$[3].priority").value("HIGH")
        }
    }
}
