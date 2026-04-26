package br.com.taskstreamai.dto

import br.com.taskstreamai.model.Priority
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import java.time.LocalDate
import java.time.LocalDateTime

data class TaskDTO(
    val id: Long,
    val name: String,
    val description: String,
    val currentValue: Int = 0,
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
    @JsonProperty("name")
    @JsonPropertyDescription("The concise name or title of the task. THIS IS MANDATORY.")
    val name: String = "New Task", // 1. Added default value to prevent the crash
    @JsonProperty("description")
    @JsonPropertyDescription("Detailed description of the task")
    val description: String = "",
    @JsonProperty("currentValue")
    @JsonPropertyDescription("Numeric value representing the task importance or current progress")
    val currentValue: Int = 0,
    @JsonProperty("startDate")
    @JsonPropertyDescription("The start date in YYYY-MM-DD format")
    val startDate: LocalDate = LocalDate.now(),
    @JsonProperty("endDateInterval")
    @JsonPropertyDescription("The number of days from start until deadline")
    val endDateInterval: Int = 1,
    @JsonProperty("endDate")
    @JsonPropertyDescription("The specific deadline date in YYYY-MM-DD format")
    var endDate: LocalDate? = null,
    @JsonProperty("completed")
    val completed: Boolean = false,
    @JsonProperty("tagId")
    @JsonPropertyDescription("The numeric database ID for the selected tag")
    val tagId: Long = 0L, // 2. Added default to avoid null issues
    @JsonProperty("customEndDateSelected")
    @JsonPropertyDescription("Set to true if a specific end date was provided by the user")
    val customEndDateSelected: Boolean = false,
    @JsonProperty("priority")
    @JsonPropertyDescription("The priority level: LOW, MEDIUM, or HIGH")
    val priority: Priority = Priority.MEDIUM,
    @JsonProperty("link")
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

data class AutomatedTaskDTO(
    val input: String
)

data class TaskGroupedByTagDTO(
    val id: Long,
    val tag: String,
    val total: Long
)

data class TasksByTagDTO(
    val id: Long,
    val task: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val tag: String,
    val priority: String,
    val completed: Boolean,
    val currentValue: Int,
)

data class TasksGroupedDTO(
    val tag: String,
    val total: Long,
    val tasks: List<TasksByTagDTO>
)