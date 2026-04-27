package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.TagDTO
import br.com.taskstreamai.dto.TagRequestDTO
import br.com.taskstreamai.service.TagService
import tools.jackson.databind.json.JsonMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(TagController::class)
@ExtendWith(MockitoExtension::class)
class TagControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jsonMapper: JsonMapper = JsonMapper.builder().build()
) {

    @MockitoBean
    private lateinit var tagService: TagService

    @Test
    fun `should create tag successfully`() {
        // Given
        val requestDTO = TagRequestDTO(
            name = "Work",
            description = "Work related tasks",
            color = "#FF0000"
        )
        
        val createdTag = TagDTO(
            id = 1L,
            name = "Work",
            description = "Work related tasks",
            color = "#FF0000",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        Mockito.`when`(tagService.createTag(requestDTO)).thenReturn(createdTag)

        // When & Then
        mockMvc.post("/api/tags") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status().isCreated()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.id").value(1)
            jsonPath("$.name").value("Work")
            jsonPath("$.description").value("Work related tasks")
            jsonPath("$.color").value("#FF0000")
        }
    }

    @Test
    fun `should get all tags successfully`() {
        // Given
        val tags = listOf(
            TagDTO(
                id = 1L,
                name = "Work",
                description = "Work related tasks",
                color = "#FF0000",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            TagDTO(
                id = 2L,
                name = "Personal",
                description = "Personal tasks",
                color = "#00FF00",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        Mockito.`when`(tagService.getAllTags()).thenReturn(tags)

        // When & Then
        mockMvc.get("/api/tags").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].id").value(1)
            jsonPath("$[0].name").value("Work")
            jsonPath("$[1].id").value(2)
            jsonPath("$[1].name").value("Personal")
        }
    }

    @Test
    fun `should get tag by id successfully`() {
        // Given
        val tagId = 1L
        val tag = TagDTO(
            id = tagId,
            name = "Work",
            description = "Work related tasks",
            color = "#FF0000",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        Mockito.`when`(tagService.getTagById(tagId)).thenReturn(tag)

        // When & Then
        mockMvc.get("/api/tags/$tagId").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.id").value(tagId)
            jsonPath("$.name").value("Work")
            jsonPath("$.description").value("Work related tasks")
            jsonPath("$.color").value("#FF0000")
        }
    }

    @Test
    fun `should update tag successfully`() {
        // Given
        val tagId = 1L
        val requestDTO = TagRequestDTO(
            name = "Updated Work",
            description = "Updated work related tasks",
            color = "#0000FF"
        )
        
        val updatedTag = TagDTO(
            id = tagId,
            name = "Updated Work",
            description = "Updated work related tasks",
            color = "#0000FF",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now()
        )
        
        Mockito.`when`(tagService.updateTag(tagId, requestDTO)).thenReturn(updatedTag)

        // When & Then
        mockMvc.put("/api/tags/$tagId") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.id").value(tagId)
            jsonPath("$.name").value("Updated Work")
            jsonPath("$.description").value("Updated work related tasks")
            jsonPath("$.color").value("#0000FF")
        }
    }

    @Test
    fun `should delete tag successfully`() {
        // Given
        val tagId = 1L
        
        Mockito.doNothing().`when`(tagService).deleteTag(tagId)

        // When & Then
        mockMvc.delete("/api/tags/$tagId").andExpect {
            status().isNoContent()
        }
    }

    @Test
    fun `should search tags successfully`() {
        // Given
        val query = "work"
        val searchResults = listOf(
            TagDTO(
                id = 1L,
                name = "Work",
                description = "Work related tasks",
                color = "#FF0000",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            TagDTO(
                id = 3L,
                name = "Workout",
                description = "Exercise routines",
                color = "#00FF00",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        Mockito.`when`(tagService.searchTags(query)).thenReturn(searchResults)

        // When & Then
        mockMvc.get("/api/tags/search") {
            param("q", query)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].name").value("Work")
            jsonPath("$[1].name").value("Workout")
        }
    }

    @Test
    fun `should return empty list when no tags exist`() {
        // Given
        Mockito.`when`(tagService.getAllTags()).thenReturn(emptyList())

        // When & Then
        mockMvc.get("/api/tags").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(0)
        }
    }

    @Test
    fun `should return empty list when search yields no results`() {
        // Given
        val query = "nonexistent"
        Mockito.`when`(tagService.searchTags(query)).thenReturn(emptyList())

        // When & Then
        mockMvc.get("/api/tags/search") {
            param("q", query)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(0)
        }
    }

    @Test
    fun `should return 400 when creating tag with invalid data`() {
        // Given
        val invalidRequestDTO = TagRequestDTO(
            name = "", // Empty name should be invalid
            description = "Invalid tag",
            color = "#FF0000"
        )

        // When & Then
        mockMvc.post("/api/tags") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(invalidRequestDTO)
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when updating tag with invalid data`() {
        // Given
        val tagId = 1L
        val invalidRequestDTO = TagRequestDTO(
            name = "", // Empty name should be invalid
            description = "Invalid tag",
            color = "#FF0000"
        )

        // When & Then
        mockMvc.put("/api/tags/$tagId") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(invalidRequestDTO)
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when search query is missing`() {
        // When & Then
        mockMvc.get("/api/tags/search").andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when search query is empty`() {
        // When & Then
        mockMvc.get("/api/tags/search") {
            param("q", "")
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle tag creation with default color`() {
        // Given
        val requestDTO = TagRequestDTO(
            name = "New Tag",
            description = "A new tag",
            color = "#3B82F6" // Default color
        )
        
        val createdTag = TagDTO(
            id = 1L,
            name = "New Tag",
            description = "A new tag",
            color = "#3B82F6",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        Mockito.`when`(tagService.createTag(requestDTO)).thenReturn(createdTag)

        // When & Then
        mockMvc.post("/api/tags") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status().isCreated()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.color").value("#3B82F6")
        }
    }

    @Test
    fun `should handle tag creation with custom color`() {
        // Given
        val requestDTO = TagRequestDTO(
            name = "Custom Tag",
            description = "Tag with custom color",
            color = "#FF5733"
        )
        
        val createdTag = TagDTO(
            id = 1L,
            name = "Custom Tag",
            description = "Tag with custom color",
            color = "#FF5733",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        Mockito.`when`(tagService.createTag(requestDTO)).thenReturn(createdTag)

        // When & Then
        mockMvc.post("/api/tags") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status().isCreated()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.color").value("#FF5733")
        }
    }

    @Test
    fun `should handle case-insensitive search`() {
        // Given
        val query = "WORK"
        val searchResults = listOf(
            TagDTO(
                id = 1L,
                name = "Work",
                description = "Work related tasks",
                color = "#FF0000",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        Mockito.`when`(tagService.searchTags(query)).thenReturn(searchResults)

        // When & Then
        mockMvc.get("/api/tags/search") {
            param("q", query)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(1)
            jsonPath("$[0].name").value("Work")
        }
    }

    @Test
    fun `should handle partial search matches`() {
        // Given
        val query = "per"
        val searchResults = listOf(
            TagDTO(
                id = 2L,
                name = "Personal",
                description = "Personal tasks",
                color = "#00FF00",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            TagDTO(
                id = 4L,
                name = "Performance",
                description = "Performance metrics",
                color = "#0000FF",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        Mockito.`when`(tagService.searchTags(query)).thenReturn(searchResults)

        // When & Then
        mockMvc.get("/api/tags/search") {
            param("q", query)
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.length()").value(2)
            jsonPath("$[0].name").value("Personal")
            jsonPath("$[1].name").value("Performance")
        }
    }

    @Test
    fun `should handle service errors gracefully`() {
        // Given
        Mockito.`when`(tagService.getAllTags()).thenThrow(RuntimeException("Database error"))

        // When & Then
        mockMvc.get("/api/tags").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle creation service errors gracefully`() {
        // Given
        val requestDTO = TagRequestDTO(
            name = "Error Tag",
            description = "This will cause an error",
            color = "#FF0000"
        )
        
        Mockito.doThrow(RuntimeException("Creation failed")).`when`(tagService).createTag(requestDTO)

        // When & Then
        mockMvc.post("/api/tags") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(requestDTO)
        }.andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle deletion service errors gracefully`() {
        // Given
        val tagId = 1L
        Mockito.`when`(tagService.deleteTag(tagId)).thenThrow(RuntimeException("Deletion failed"))

        // When & Then
        mockMvc.delete("/api/tags/$tagId").andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should verify correct content type for all endpoints`() {
        // Given
        val tag = TagDTO(
            id = 1L,
            name = "Test",
            description = "Test tag",
            color = "#FF0000",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        Mockito.`when`(tagService.getAllTags()).thenReturn(listOf(tag))
        Mockito.`when`(tagService.getTagById(1L)).thenReturn(tag)
        Mockito.`when`(tagService.searchTags("test")).thenReturn(listOf(tag))

        // When & Then - All endpoints should return JSON
        mockMvc.get("/api/tags").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        mockMvc.get("/api/tags/1").andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }

        mockMvc.get("/api/tags/search") {
            param("q", "test")
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
        }
    }
}
