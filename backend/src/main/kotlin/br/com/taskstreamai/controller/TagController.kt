package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.TagDTO
import br.com.taskstreamai.dto.TagRequestDTO
import br.com.taskstreamai.service.TagService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tags")
class TagController(
    private val tagService: TagService
) {

    @PostMapping
    fun createTag(@Valid @RequestBody requestDTO: TagRequestDTO): ResponseEntity<TagDTO> {
        val createdTag = tagService.createTag(requestDTO)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag)
    }

    @GetMapping
    fun getAllTags(): ResponseEntity<List<TagDTO>> {
        val tags = tagService.getAllTags()
        return ResponseEntity.ok(tags)
    }

    @GetMapping("/{id}")
    fun getTagById(@PathVariable id: Long): ResponseEntity<TagDTO> {
        val tag = tagService.getTagById(id)
        return ResponseEntity.ok(tag)
    }

    @PutMapping("/{id}")
    fun updateTag(
        @PathVariable id: Long,
        @Valid @RequestBody requestDTO: TagRequestDTO
    ): ResponseEntity<TagDTO> {
        val updatedTag = tagService.updateTag(id, requestDTO)
        return ResponseEntity.ok(updatedTag)
    }

    @DeleteMapping("/{id}")
    fun deleteTag(@PathVariable id: Long): ResponseEntity<Void> {
        tagService.deleteTag(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/search")
    fun searchTags(@RequestParam("q") query: String): ResponseEntity<List<TagDTO>> {
        val tags = tagService.searchTags(query)
        return ResponseEntity.ok(tags)
    }

}
