package br.com.taskstreamai.dto

import java.time.LocalDateTime

data class TagDTO(
    val id: Long,
    val name: String,
    val description: String,
    val color: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val tasks: List<TaskDTO> = emptyList(),
) {
    var totalUsed: Int = 0

}

data class TagRequestDTO(
    val name: String,
    val description: String,
    val color: String = "#3B82F6"
)

data class TagUsageDTO(
    val tag: TagDTO,
    val taskCount: Int
)
