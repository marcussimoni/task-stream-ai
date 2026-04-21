package br.com.taskstreamai.mapper

import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.WeekScheduleDTO
import br.com.taskstreamai.model.WeekSchedule

object WeekScheduleMapper {

    fun toDTO(entity: WeekSchedule, task: TaskDTO? = null): WeekScheduleDTO {
        return WeekScheduleDTO(
            id = entity.id,
            dayOfWeek = entity.dayOfWeek,
            hour = entity.hour,
            weekStartDate = entity.weekStartDate.toString(),
            tagId = entity.tag.id,
            tag = TagMapper.toDTO(entity.tag),
            task = task
        )
    }

    fun toDTOList(entities: List<WeekSchedule>, taskMap: Map<Long, TaskDTO?> = emptyMap()): List<WeekScheduleDTO> {
        return entities.map { entity ->
            toDTO(entity, taskMap[entity.tag.id])
        }
    }
}
