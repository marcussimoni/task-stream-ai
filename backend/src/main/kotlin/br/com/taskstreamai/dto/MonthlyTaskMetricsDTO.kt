package br.com.taskstreamai.dto

import java.time.LocalDate

data class TaskMetricsDTO(
    val tagId: Long,
    val tag: String,
    val total: Long,
    val status: String,
    val date: LocalDate
) {
}

data class MonthlyTaskMetricsDTO(
    val tasksMetrics: List<TaskMetricsDTO>,
    val totalCompleted: Long,
    val totalIncomplete: Long,
)