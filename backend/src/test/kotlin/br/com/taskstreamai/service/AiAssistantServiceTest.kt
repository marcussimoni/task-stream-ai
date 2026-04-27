package br.com.taskstreamai.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.ai.chat.client.ChatClient
import tools.jackson.databind.json.JsonMapper

class AiAssistantServiceTest {

    @Mock
    private lateinit var createTaskChatClient: ChatClient
    @Mock
    private lateinit var taskService: TaskService
    @Mock
    private lateinit var tagService: TagService
    @Mock
    private lateinit var jsonMapper: JsonMapper
    private lateinit var aiAssistantService: AiAssistantService

    @BeforeEach
    fun setup() {
        createTaskChatClient = Mockito.mock(ChatClient::class.java)
        taskService = Mockito.mock(TaskService::class.java)
        tagService = Mockito.mock(TagService::class.java)
        jsonMapper = Mockito.mock(JsonMapper::class.java)
        
        aiAssistantService = AiAssistantService(createTaskChatClient, taskService, tagService, jsonMapper)
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
