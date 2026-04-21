package br.com.taskstreamai.mapper

import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.TaskRequestDTO
import br.com.taskstreamai.model.Tag
import br.com.taskstreamai.model.Task

object TaskMapper {
    
    fun toEntity(dto: TaskRequestDTO, tag: Tag): Task {
        return Task(
            name = dto.name,
            description = dto.description,
            currentValue = dto.currentValue,
            startDate = dto.startDate ?: java.time.LocalDate.now(),
            endDateInterval = dto.endDateInterval,
            endDate = dto.endDate,
            completed = dto.completed,
            tag = tag,
            customEndDateSelected = dto.customEndDateSelected,
            priority = dto.priority,
            link = dto.link
        )
    }

    fun toDTO(entity: Task): TaskDTO {
        return TaskDTO(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            currentValue = entity.currentValue,
            startDate = entity.startDate,
            endDateInterval = entity.endDateInterval,
            endDate = entity.endDate,
            completed = entity.completed,
            createdAt = null,
            updatedAt = null,
            TagMapper.toDTO(entity.tag),
            customEndDateSelected = entity.customEndDateSelected,
            priority = entity.priority,
            link = entity.link,
            summary = entity.summary
        )
    }
    
    fun toDTOList(entities: List<Task>): List<TaskDTO> {
        return entities.map { toDTO(it) }
    }
}
