package br.com.taskstreamai.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.ai.chat.client.ChatClient
import tools.jackson.databind.json.JsonMapper

class AiAssistantServiceTest {

    @Mock
    private lateinit var chatClient: ChatClient
    @Mock
    private lateinit var taskService: GetTaskService
    @Mock
    private lateinit var tagService: GetTagService
    @Mock
    private lateinit var jsonMapper: JsonMapper
    private lateinit var aiAssistantService: AiAssistantService

    @BeforeEach
    fun setup() {
        chatClient = Mockito.mock(ChatClient::class.java)
        taskService = Mockito.mock(GetTaskService::class.java)
        tagService = Mockito.mock(GetTagService::class.java)
        jsonMapper = Mockito.mock(JsonMapper::class.java)
        
        aiAssistantService = AiAssistantService(chatClient, taskService, tagService, jsonMapper)
    }

    @Test
    fun `should initialize service correctly`() {
        // Given - Service is initialized in setup
        
        // When
        val service = aiAssistantService

        // Then - Service should be properly initialized
        assert(service != null)
    }
}
