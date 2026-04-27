package br.com.taskstreamai.service

import br.com.taskstreamai.dto.TagDTO
import br.com.taskstreamai.dto.TagRequestDTO
import br.com.taskstreamai.exception.ResourceNotFoundException
import br.com.taskstreamai.mapper.TagMapper
import br.com.taskstreamai.model.Tag
import br.com.taskstreamai.repository.TagRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*

class TagServiceTest {

    @Mock
    private lateinit var tagRepository: TagRepository
    @Mock
    private lateinit var taskService: TaskService
    private lateinit var tagService: TagService

    private lateinit var testTag: Tag
    private lateinit var testTagDTO: TagDTO

    @BeforeEach
    fun setup() {
        tagRepository = Mockito.mock(TagRepository::class.java)
        taskService = Mockito.mock(TaskService::class.java)
        
        tagService = TagService(tagRepository, taskService)

        testTag = Tag(
            id = 1L,
            name = "Work",
            description = "Work tasks",
            color = "#FF0000"
        )

        testTagDTO = TagMapper.toDTO(testTag)
    }

    @Test
    fun `should get tag by id successfully`() {
        // Given
        Mockito.doReturn(Optional.of(testTag)).`when`(tagRepository).findById(1L)

        // When
        val result = tagService.getTagById(1L)

        // Then
        assert(result.id == 1L)
        assert(result.name == "Work")
        assert(result.description == "Work tasks")
        assert(result.color == "#FF0000")
    }

    @Test
    fun `should throw exception when getting non-existent tag`() {
        // Given
        Mockito.doReturn(Optional.empty<Tag>()).`when`(tagRepository).findById(999L)

        // When & Then
        try {
            tagService.getTagById(999L)
            assert(false) { "Should have thrown ResourceNotFoundException" }
        } catch (e: ResourceNotFoundException) {
            // Expected exception
        }
    }

    @Test
    fun `should get all tags successfully`() {
        // Given
        val tags = listOf(testTag)
        
        Mockito.doReturn(tags).`when`(tagRepository).findAllTags()

        // When
        val result = tagService.getAllTags()

        // Then
        assert(result.size == 1)
        assert(result[0].id == 1L)
        assert(result[0].name == "Work")
    }

    @Test
    fun `should delete tag successfully`() {
        // Given
        Mockito.doReturn(true).`when`(tagRepository).existsById(1L)
        Mockito.doNothing().`when`(tagRepository).deleteById(1L)

        // When & Then - Should not throw exception
        tagService.deleteTag(1L)
    }

    @Test
    fun `should throw exception when deleting non-existent tag`() {
        // Given
        Mockito.doReturn(false).`when`(tagRepository).existsById(999L)

        // When & Then
        assertThrows<ResourceNotFoundException> {
            tagService.deleteTag(999L)
        }
    }

    @Test
    fun `should search tags successfully`() {
        // Given
        val tags = listOf(testTag)
        val query = "Work"
        
        Mockito.doReturn(tags).`when`(tagRepository).findByNameContainingIgnoreCase(query)

        // When
        val result = tagService.searchTags(query)

        // Then
        assert(result.size == 1)
        assert(result[0].id == 1L)
        assert(result[0].name == "Work")
    }

    @Test
    fun `should return empty list when no tags match search query`() {
        // Given
        val query = "NonExistent"
        
        Mockito.doReturn(emptyList<Tag>()).`when`(tagRepository).findByNameContainingIgnoreCase(query)

        // When
        val result = tagService.searchTags(query)

        // Then
        assert(result.isEmpty())
    }
}
