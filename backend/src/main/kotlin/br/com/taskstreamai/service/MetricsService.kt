package br.com.taskstreamai.service

import br.com.taskstreamai.dto.MonthlyTaskMetricsDTO
import br.com.taskstreamai.dto.TaskMetricsDTO
import br.com.taskstreamai.repository.TaskRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.stream.Collectors

@Service
class MetricsService(
    private val taskRepository: TaskRepository
) {

    fun getMonthlyTaskMetrics(month: Int, year: Int): MonthlyTaskMetricsDTO {
        val firstDay = LocalDate.of(year, month, 1)
        val lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth())
        val monthlyTasks = taskRepository.monthlyTasks(firstDay, lastDay)

        val totalCompleted = getTotalTaskMetrics(monthlyTasks, "completed")
        val totalIncomplete = getTotalTaskMetrics(monthlyTasks, "incompleted")

        return MonthlyTaskMetricsDTO(
            monthlyTasks, totalCompleted, totalIncomplete
        )
    }

    private fun getTotalTaskMetrics(monthlyTasks: List<TaskMetricsDTO>, status: String): Long {
        return monthlyTasks
            .stream()
            .filter { it.status.equals(status, true) }
            .map { it.total }
            .reduce { a, b -> a.plus(b) }
            .orElseGet { 0L }
    }
}
