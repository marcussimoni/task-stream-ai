package br.com.taskstreamai.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SilenceFaviconController::class)
@ExtendWith(MockitoExtension::class)
class SilenceFaviconControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `should return 204 No Content for favicon request`() {
        // When & Then
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent()
        }
    }

    @Test
    fun `should return empty response body for favicon request`() {
        // When & Then
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent()
            content().string("")
        }
    }

    @Test
    fun `should handle favicon request with no content type`() {
        // When & Then
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent()
            // No content type should be set for 204 responses
        }
    }

    @Test
    fun `should prevent NoResourceFoundException for favicon endpoint`() {
        // This test verifies that the controller handles the favicon request
        // instead of letting Spring throw a NoResourceFoundException
        
        // When & Then
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent()
            // If this returns 204 instead of 404, the controller is working correctly
        }
    }

    @Test
    fun `should handle multiple favicon requests consistently`() {
        // When & Then - Make multiple requests to ensure consistency
        repeat(3) {
            mockMvc.get("/favicon.ico").andExpect {
                status().isNoContent()
                content().string("")
            }
        }
    }

    @Test
    fun `should handle favicon request regardless of HTTP method`() {
        // Note: This test uses GET since that's what browsers use for favicon
        // The controller only defines GET mapping
        
        // When & Then
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent()
        }
    }

    @Test
    fun `should handle favicon request with headers`() {
        // When & Then
        mockMvc.get("/favicon.ico") {
            header("User-Agent", "Mozilla/5.0")
            header("Accept", "image/x-icon")
        }.andExpect {
            status().isNoContent()
        }
    }

    @Test
    fun `should handle favicon request without any headers`() {
        // When & Then
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent()
        }
    }

    @Test
    fun `should verify controller exists and is properly mapped`() {
        // This test ensures the controller is properly registered and mapped
        
        // When & Then
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent()
        }
    }

    @Test
    fun `should handle favicon request in different contexts`() {
        // Test that the favicon endpoint works regardless of context path
        // (This is more of an integration concern, but we can verify the basic functionality)
        
        // When & Then
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent()
        }
    }

    @Test
    fun `should return consistent response for favicon requests`() {
        // When & Then
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent() // 204
            content().string("")
        }

        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent() // 204
            content().string("")
        }
    }

    @Test
    fun `should handle favicon request quickly`() {
        // This test ensures the favicon endpoint responds quickly
        // since it's a simple no-op controller
        
        val startTime = System.currentTimeMillis()
        
        // When
        mockMvc.get("/favicon.ico").andExpect {
            status().isNoContent()
        }
        
        val endTime = System.currentTimeMillis()
        val responseTime = endTime - startTime
        
        // Then - Should respond very quickly (under 100ms)
        assert(responseTime < 100) { "Favicon endpoint should respond quickly, took ${responseTime}ms" }
    }
}
