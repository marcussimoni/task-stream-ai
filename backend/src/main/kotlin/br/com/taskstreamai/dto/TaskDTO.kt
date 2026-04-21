package br.com.taskstreamai.dto

import br.com.taskstreamai.model.Priority
import java.time.LocalDate
import java.time.LocalDateTime

data class TaskDTO(
    val id: Long,
    val name: String,
    val description: String,
    val currentValue: Int,
    val startDate: LocalDate?,
    val endDateInterval: Int?,
    val endDate: LocalDate?,
    val completed: Boolean = false,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val tag: TagDTO,
    val customEndDateSelected: Boolean = false,
    val priority: Priority,
    val link: String?,
    var summary: String?
)

data class TaskRequestDTO(
    val name: String,
    val description: String = "",
    val currentValue: Int = 0,
    val startDate: LocalDate = LocalDate.now(),
    val endDateInterval: Int? = null,
    var endDate: LocalDate? = null,
    val completed: Boolean = false,
    val tagId: Long,
    val customEndDateSelected: Boolean,
    val priority: Priority = Priority.MEDIUM,
    val link: String? = null
)

data class TaskAggregatedByTagsDTO(
    val tagId: Long,
    val tagName: String,
    val tasks: List<TaskDTO>,
    val completedCount: Int,
    val totalCount: Int
)

data class TaskQueryParamsDTO(
    val task: Long?
)

data class LinkContentDTO(
    val title: String
)