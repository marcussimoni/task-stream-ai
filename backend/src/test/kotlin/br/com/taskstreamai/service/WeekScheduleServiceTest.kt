package br.com.taskstreamai.service

import br.com.taskstreamai.dto.CreateWeekScheduleRequest
import br.com.taskstreamai.dto.WeekScheduleDTO
import br.com.taskstreamai.exception.ResourceNotFoundException
import br.com.taskstreamai.model.Priority
import br.com.taskstreamai.model.Tag
import br.com.taskstreamai.model.Task
import br.com.taskstreamai.model.WeekSchedule
import br.com.taskstreamai.repository.TagRepository
import br.com.taskstreamai.repository.TaskRepository
import br.com.taskstreamai.repository.WeekScheduleRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

class WeekScheduleServiceTest {

    @Mock
    private lateinit var scheduleRepository: WeekScheduleRepository
    @Mock
    private lateinit var taskRepository: TaskRepository
    @Mock
    private lateinit var tagRepository: TagRepository
    private lateinit var weekScheduleService: WeekScheduleService

    private lateinit var testTag: Tag
    private lateinit var testTask: Task
    private lateinit var testSchedule: WeekSchedule

    @BeforeEach
    fun setup() {
        scheduleRepository = Mockito.mock(WeekScheduleRepository::class.java)
        taskRepository = Mockito.mock(TaskRepository::class.java)
        tagRepository = Mockito.mock(TagRepository::class.java)
        
        weekScheduleService = WeekScheduleService(scheduleRepository, taskRepository, tagRepository)

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
            priority = Priority.HIGH,
            tag = testTag,
            link = null
        )

        testSchedule = WeekSchedule(
            dayOfWeek = DayOfWeek.MONDAY.value,
            hour = 10,
            weekStartDate = LocalDate.of(2024, 1, 15),
            tag = testTag
        )
    }

    @Test
    fun `should get week schedule successfully`() {
        // Given
        val weekStartDate = LocalDate.of(2024, 1, 15)
        val schedules = listOf(testSchedule)
        
        Mockito.doReturn(schedules).`when`(scheduleRepository).findAll()
        Mockito.doReturn(Optional.of(testTask)).`when`(taskRepository).findByTagAndPriority(1L, Priority.CRITICAL.name)

        // When
        val result = weekScheduleService.getWeekSchedule(weekStartDate)

        // Then
        assert(result.isNotEmpty())
        assert(result[0].dayOfWeek == DayOfWeek.MONDAY.value)
        assert(result[0].hour == 10)
        assert(result[0].tag?.id == 1L)
    }

    @Test
    fun `should create new schedule successfully`() {
        // Given
        val request = CreateWeekScheduleRequest(
            dayOfWeek = DayOfWeek.TUESDAY.value,
            hour = 14,
            weekStartDate = "2024-01-15",
            tagId = 1L
        )

        Mockito.doReturn(Optional.of(testTag)).`when`(tagRepository).findById(1L)
        Mockito.doReturn(null).`when`(scheduleRepository)
            .findByWeekStartDateAndDayOfWeekAndHour(LocalDate.of(2024, 1, 15), DayOfWeek.TUESDAY.value, 14)
        Mockito.doReturn(testSchedule).`when`(scheduleRepository).save(org.mockito.ArgumentMatchers.any(WeekSchedule::class.java))
        Mockito.doReturn(Optional.of(testTask)).`when`(taskRepository).findByTagAndPriority(1L, Priority.CRITICAL.name)

        // When & Then - Should not throw exception
        weekScheduleService.createOrUpdateSchedule(request)
    }

    @Test
    fun `should throw exception when creating schedule with non-existent tag`() {
        // Given
        val request = CreateWeekScheduleRequest(
            dayOfWeek = DayOfWeek.TUESDAY.value,
            hour = 14,
            weekStartDate = "2024-01-15",
            tagId = 999L
        )

        Mockito.doReturn(Optional.empty<Tag>()).`when`(tagRepository).findById(999L)

        // When & Then
        try {
            weekScheduleService.createOrUpdateSchedule(request)
            assert(false) { "Should have thrown ResourceNotFoundException" }
        } catch (e: ResourceNotFoundException) {
            // Expected exception
        }
    }

    @Test
    fun `should delete schedule successfully`() {
        // Given
        Mockito.doReturn(true).`when`(scheduleRepository).existsById(1L)
        Mockito.doNothing().`when`(scheduleRepository).deleteById(1L)

        // When & Then - Should not throw exception
        weekScheduleService.deleteSchedule(1L)
    }

    @Test
    fun `should throw exception when deleting non-existent schedule`() {
        // Given
        Mockito.doReturn(false).`when`(scheduleRepository).existsById(999L)

        // When & Then
        assertThrows<ResourceNotFoundException> {
            weekScheduleService.deleteSchedule(999L)
        }
    }

    @Test
    fun `should delete schedule by slot successfully`() {
        // Given
        val weekStartDate = LocalDate.of(2024, 1, 15)
        val dayOfWeek = DayOfWeek.MONDAY.value
        val hour = 10

        Mockito.doNothing().`when`(scheduleRepository)
            .deleteByWeekStartDateAndDayOfWeekAndHour(weekStartDate, dayOfWeek, hour)

        // When & Then - Should not throw exception
        weekScheduleService.deleteScheduleBySlot(weekStartDate, dayOfWeek, hour)
    }

    @Test
    fun `should find task for tag successfully`() {
        // Given
        Mockito.doReturn(Optional.of(testTask)).`when`(taskRepository).findByTagAndPriority(1L, Priority.CRITICAL.name)

        // When
        val result = weekScheduleService.findTaskForTag(1L)

        // Then
        assert(result != null)
        assert(result!!.id == 1L)
        assert(result.name == "Test Task")
    }

    @Test
    fun `should return null when no task found for tag`() {
        // Given
        Mockito.doReturn(Optional.empty<Task>()).`when`(taskRepository).findByTagAndPriority(1L, Priority.CRITICAL.name)
        Mockito.doReturn(Optional.empty<Task>()).`when`(taskRepository).findByTagAndPriority(1L, Priority.HIGH.name)
        Mockito.doReturn(Optional.empty<Task>()).`when`(taskRepository).findByTagAndPriority(1L, Priority.MEDIUM.name)
        Mockito.doReturn(Optional.empty<Task>()).`when`(taskRepository).findByTagAndPriority(1L, Priority.LOW.name)

        // When
        val result = weekScheduleService.findTaskForTag(1L)

        // Then
        assert(result == null)
    }

    @Test
    fun `should get task for tag as DTO successfully`() {
        // Given
        Mockito.doReturn(Optional.of(testTask)).`when`(taskRepository).findByTagAndPriority(1L, Priority.CRITICAL.name)

        // When
        val result = weekScheduleService.getTaskForTag(1L)

        // Then
        assert(result != null)
        assert(result!!.id == 1L)
        assert(result.name == "Test Task")
        assert(result.tag.id == 1L)
    }

    @Test
    fun `should return null when getting task DTO for non-existent tag`() {
        // Given
        Mockito.doReturn(Optional.empty<Task>()).`when`(taskRepository).findByTagAndPriority(1L, Priority.CRITICAL.name)
        Mockito.doReturn(Optional.empty<Task>()).`when`(taskRepository).findByTagAndPriority(1L, Priority.HIGH.name)
        Mockito.doReturn(Optional.empty<Task>()).`when`(taskRepository).findByTagAndPriority(1L, Priority.MEDIUM.name)
        Mockito.doReturn(Optional.empty<Task>()).`when`(taskRepository).findByTagAndPriority(1L, Priority.LOW.name)

        // When
        val result = weekScheduleService.getTaskForTag(1L)

        // Then
        assert(result == null)
    }
}
