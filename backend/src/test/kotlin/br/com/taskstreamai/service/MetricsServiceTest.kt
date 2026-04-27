package br.com.taskstreamai.service

import br.com.taskstreamai.dto.TaskMetricsDTO
import br.com.taskstreamai.repository.TaskRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import java.time.LocalDate

class MetricsServiceTest {

    @Mock
    private lateinit var taskRepository: TaskRepository
    private lateinit var metricsService: MetricsService

    @BeforeEach
    fun setup() {
        taskRepository = Mockito.mock(TaskRepository::class.java)
        metricsService = MetricsService(taskRepository)
    }

    @Test
    fun `should get monthly task metrics successfully`() {
        // Given
        val month = 1
        val year = 2024
        val firstDay = LocalDate.of(2024, 1, 1)
        val lastDay = LocalDate.of(2024, 1, 31)
        
        val monthlyTasks = listOf(
            TaskMetricsDTO(tag = "Work", total = 5L, status = "completed", date = LocalDate.of(2024, 1, 15), tagId = 1L),
            TaskMetricsDTO(tag = "Personal", total = 3L, status = "completed", date = LocalDate.of(2024, 1, 16), tagId = 2L),
            TaskMetricsDTO(tag = "Health", total = 2L, status = "incompleted", date = LocalDate.of(2024, 1, 17), tagId = 3L),
            TaskMetricsDTO(tag = "Learning", total = 1L, status = "incompleted", date = LocalDate.of(2024, 1, 18), tagId = 4L)
        )

        Mockito.doReturn(monthlyTasks).`when`(taskRepository).monthlyTasks(firstDay, lastDay)

        // When
        val result = metricsService.getMonthlyTaskMetrics(month, year)

        // Then
        assert(result.tasksMetrics.size == 4)
        assert(result.totalCompleted == 8L) // 5 + 3
        assert(result.totalIncomplete == 3L) // 2 + 1
    }

    @Test
    fun `should handle empty monthly tasks`() {
        // Given
        val month = 2
        val year = 2024
        val firstDay = LocalDate.of(2024, 2, 1)
        val lastDay = LocalDate.of(2024, 2, 29)
        
        val monthlyTasks = emptyList<TaskMetricsDTO>()

        Mockito.doReturn(monthlyTasks).`when`(taskRepository).monthlyTasks(firstDay, lastDay)

        // When
        val result = metricsService.getMonthlyTaskMetrics(month, year)

        // Then
        assert(result.tasksMetrics.isEmpty())
        assert(result.totalCompleted == 0L)
        assert(result.totalIncomplete == 0L)
    }

    @Test
    fun `should handle only completed tasks`() {
        // Given
        val month = 3
        val year = 2024
        val firstDay = LocalDate.of(2024, 3, 1)
        val lastDay = LocalDate.of(2024, 3, 31)
        
        val monthlyTasks = listOf(
            TaskMetricsDTO(tag = "Work", total = 10L, status = "completed", date = LocalDate.of(2024, 3, 15), tagId = 1L),
            TaskMetricsDTO(tag = "Personal", total = 7L, status = "completed", date = LocalDate.of(2024, 3, 16), tagId = 2L),
            TaskMetricsDTO(tag = "Health", total = 4L, status = "completed", date = LocalDate.of(2024, 3, 17), tagId = 3L)
        )

        Mockito.doReturn(monthlyTasks).`when`(taskRepository).monthlyTasks(firstDay, lastDay)

        // When
        val result = metricsService.getMonthlyTaskMetrics(month, year)

        // Then
        assert(result.tasksMetrics.size == 3)
        assert(result.totalCompleted == 21L) // 10 + 7 + 4
        assert(result.totalIncomplete == 0L)
    }

    @Test
    fun `should handle only incomplete tasks`() {
        // Given
        val month = 4
        val year = 2024
        val firstDay = LocalDate.of(2024, 4, 1)
        val lastDay = LocalDate.of(2024, 4, 30)
        
        val monthlyTasks = listOf(
            TaskMetricsDTO(tag = "Work", total = 3L, status = "incompleted", date = LocalDate.of(2024, 4, 15), tagId = 1L),
            TaskMetricsDTO(tag = "Personal", total = 2L, status = "incompleted", date = LocalDate.of(2024, 4, 16), tagId = 2L)
        )

        Mockito.doReturn(monthlyTasks).`when`(taskRepository).monthlyTasks(firstDay, lastDay)

        // When
        val result = metricsService.getMonthlyTaskMetrics(month, year)

        // Then
        assert(result.tasksMetrics.size == 2)
        assert(result.totalCompleted == 0L)
        assert(result.totalIncomplete == 5L) // 3 + 2
    }
}
