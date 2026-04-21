package br.com.taskstreamai.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class WeekScheduleDTO(
    val id: Long?,
    val dayOfWeek: Int,
    val hour: Int,
    val weekStartDate: String, // ISO format
    val tagId: Long,
    val tag: TagDTO?,
    val task: TaskDTO?
)

data class CreateWeekScheduleRequest(
    val dayOfWeek: Int,
    val hour: Int,
    val weekStartDate: String,
    val tagId: Long
)
