package br.com.taskstreamai.service

import br.com.taskstreamai.dto.TagDTO
import br.com.taskstreamai.dto.TagRequestDTO
import br.com.taskstreamai.exception.ResourceNotFoundException
import br.com.taskstreamai.mapper.TagMapper
import br.com.taskstreamai.model.Tag
import br.com.taskstreamai.repository.TagRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class TagService(
    private val tagRepository: TagRepository,
    private val taskService: TaskService
) {

    fun createTag(requestDTO: TagRequestDTO): TagDTO {
        val tag = TagMapper.toEntity(requestDTO)
        val savedTag = tagRepository.save(tag)
        return TagMapper.toDTO(savedTag)
    }

    fun getTagById(id: Long): TagDTO {
        val tag = tagRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Tag not found with id: $id") }
        return TagMapper.toDTO(tag)
    }

    fun getAllTags(): List<TagDTO> {
        val tags = tagRepository.findAllTags()
        return tags.map { TagMapper.toDTO(it) }
    }

    fun updateTag(id: Long, requestDTO: TagRequestDTO): TagDTO {
        val existingTag = tagRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Tag not found with id: $id") }

        existingTag.name = requestDTO.name
        existingTag.description = requestDTO.description
        existingTag.color = requestDTO.color
        existingTag.updatedAt = LocalDateTime.now()

        val savedTag = tagRepository.save(existingTag)
        return TagMapper.toDTO(savedTag)
    }

    fun deleteTag(id: Long) {
        if (!tagRepository.existsById(id)) {
            throw ResourceNotFoundException("Tag not found with id: $id")
        }
        tagRepository.deleteById(id)
    }

    fun searchTags(query: String): List<TagDTO> {
        val tags = tagRepository.findByNameContainingIgnoreCase(query)
        return tags.map { TagMapper.toDTO(it) }
    }

}
