package br.com.taskstreamai.service

import br.com.taskstreamai.repository.TagRepository
import org.springframework.stereotype.Service

@Service
class GetTagService(private val tagRepository: TagRepository) {

    fun getAllTagsTemplate(): String {
        val tags = tagRepository.findAllTags()
        return tags.joinToString(",") { "(${it.id} : ${it.name})" }
    }


}