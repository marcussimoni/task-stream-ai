package br.com.taskstreamai.mapper

import br.com.taskstreamai.dto.TagDTO
import br.com.taskstreamai.dto.TagRequestDTO
import br.com.taskstreamai.model.Tag

object TagMapper {

    fun toEntity(dto: TagRequestDTO): Tag {
        return Tag(
            name = dto.name,
            description = dto.description,
            color = dto.color
        )
    }

    fun toDTO(entity: Tag): TagDTO {
        return TagDTO(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            color = entity.color,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toDTOList(entities: List<Tag>): List<TagDTO> {
        return entities.map { toDTO(it) }
    }
}
