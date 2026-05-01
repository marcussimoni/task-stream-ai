package br.com.taskstreamai.service

import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.TaskRequestDTO
import br.com.taskstreamai.exception.ResourceNotFoundException
import br.com.taskstreamai.mapper.TaskMapper
import br.com.taskstreamai.model.Priority
import br.com.taskstreamai.model.Tag
import br.com.taskstreamai.model.Task
import br.com.taskstreamai.repository.TagRepository
import br.com.taskstreamai.repository.TaskRepository
import br.com.taskstreamai.service.AiAssistantService
import br.com.taskstreamai.service.WebScraperService
import org.jsoup.nodes.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class TaskServiceTest {

    @Mock
    private lateinit var taskRepository: TaskRepository
    @Mock
    private lateinit var tagRepository: TagRepository
    @Mock
    private lateinit var aiAssistantService: AiAssistantService
    @Mock
    private lateinit var webScraperService: WebScraperService
    private lateinit var taskService: TaskService

    private lateinit var testTag: Tag
    private lateinit var testTask: Task
    private lateinit var testTaskDTO: TaskDTO

    @BeforeEach
    fun setup() {
        taskRepository = Mockito.mock(TaskRepository::class.java)
        tagRepository = Mockito.mock(TagRepository::class.java)
        aiAssistantService = Mockito.mock(AiAssistantService::class.java)
        webScraperService = Mockito.mock(WebScraperService::class.java)
        
        taskService = TaskService(taskRepository, tagRepository, aiAssistantService, webScraperService)

        testTag = Tag(
            id = 1L,
            name = "Work",
            description = "Work tasks",
            color = "#FF0000"
        )

        testTask = Task(
            id = 1L,
            name = "Test Task",
            description = "Test Description",
            currentValue = 50,
            startDate = LocalDate.now(),
            endDateInterval = 1,
            endDate = LocalDate.now().plusDays(7),
            completed = false,
            customEndDateSelected = false,
            priority = Priority.MEDIUM,
            tag = testTag,
            link = null
        )

        testTaskDTO = TaskMapper.toDTO(testTask)
    }

    @Test
    fun `should get task by id successfully`() {
        // Given
        Mockito.doReturn(Optional.of(testTask)).`when`(taskRepository).findById(1L)

        // When
        val result = taskService.getTaskById(1L)

        // Then
        assert(result.id == 1L)
        assert(result.name == "Test Task")
        assert(result.tag.id == 1L)
    }

    @Test
    fun `should throw exception when getting non-existent task`() {
        // Given
        Mockito.doReturn(Optional.empty<Task>()).`when`(taskRepository).findById(999L)

        // When & Then
        try {
            taskService.getTaskById(999L)
            assert(false) { "Should have thrown ResourceNotFoundException" }
        } catch (e: ResourceNotFoundException) {
            // Expected exception
        }
    }

    @Test
    fun `should delete task successfully`() {
        // Given
        Mockito.doReturn(true).`when`(taskRepository).existsById(1L)
        Mockito.doNothing().`when`(taskRepository).deleteById(1L)

        // When & Then - Should not throw exception
        taskService.deleteTask(1L)
    }

    @Test
    fun `should throw exception when deleting non-existent task`() {
        // Given
        Mockito.doReturn(false).`when`(taskRepository).existsById(999L)

        // When & Then
        assertThrows<ResourceNotFoundException> {
            taskService.deleteTask(999L)
        }
    }

    @Test
    fun `should complete task successfully`() {
        // Given
        Mockito.doReturn(Optional.of(testTask)).`when`(taskRepository).findById(1L)
        Mockito.doReturn(testTask).`when`(taskRepository).save(testTask)

        // When
        val result = taskService.completeTask(1L)

        // Then
        assert(result.completed == true)
        assert(result.currentValue == 100)
    }

    @Test
    fun `should get current value correctly when less than 100`() {
        // Given
        val requestDTO = TaskRequestDTO(
            name = "Test",
            currentValue = 50,
            startDate = LocalDate.now(),
            endDateInterval = 1,
            tagId = 1L
        )

        // When
        val result = taskService.getCurrentValue(requestDTO)

        // Then
        assert(result == 50)
    }

    @Test
    fun `should cap current value at 100`() {
        // Given
        val requestDTO = TaskRequestDTO(
            name = "Test",
            currentValue = 150,
            startDate = LocalDate.now(),
            endDateInterval = 1,
            tagId = 1L
        )

        // When
        val result = taskService.getCurrentValue(requestDTO)

        // Then
        assert(result == 100)
    }

    @Test
    fun `should load link content successfully`() {
        // Given
        val url = "https://example.com"
        val mockDocument = Mockito.mock(org.jsoup.nodes.Document::class.java)
        
        // Mock the document behavior
        Mockito.doReturn("Example Title").`when`(mockDocument).title()
        
        // Mock the service to return a Document
        Mockito.doReturn(mockDocument).`when`(webScraperService).loadSiteFromUrl(url)

        // When
        val result = taskService.loadLinkContent(url)

        // Then
        assert(result != null)
        assert(result.title == "Example Title")
    }

    @Test
    fun `should get last tasks successfully`() {
        // Given
        val tasks = listOf(testTask)
        
        Mockito.doReturn(tasks).`when`(taskRepository).findTaskByDates(5)

        // When
        val result = taskService.getLastTasks(5)

        // Then
        assert(result.size == 1)
        assert(result[0].id == 1L)
    }

    @Test
    fun `should get tasks grouped by tag successfully`() {
        // Given
        val month = LocalDate.of(2024, 1, 1)
        val startDate = month.withDayOfMonth(1)
        val endDate = month.withDayOfMonth(month.lengthOfMonth())
        
        // Mock the repository calls to return empty lists for simplicity
        Mockito.doReturn(emptyList<Any>()).`when`(taskRepository).countTasksByTag(startDate, endDate)

        // When
        val result = taskService.getTasksGroupedByTag(month)

        // Then
        assert(result.isEmpty())
    }
}
