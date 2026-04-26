package br.com.taskstreamai.service

import br.com.taskstreamai.config.SpringAiConfig
import br.com.taskstreamai.dto.AutomatedTaskDTO
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.TaskRequestDTO
import br.com.taskstreamai.model.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate

@Service
class AiAssistantService(
    @Qualifier("createTaskChatClient") private val createTaskChatClient: ChatClient,
    private val taskService: TaskService,
    private val tagService: TagService,
    private val jsonMapper: JsonMapper,
) {

    private val logger = LoggerFactory.getLogger(AiAssistantService::class.java)

    fun planAutomatedTaskCreation(automatedTaskDTO: AutomatedTaskDTO): List<TaskRequestDTO>? = startAutomatedTaskCreation(automatedTaskDTO)

    private fun startAutomatedTaskCreation(automatedTaskDTO: AutomatedTaskDTO): List<TaskRequestDTO>? {
        logger.info("Creating automated task plan")

        val tags = tagService.getAllTags();
        val tasks: List<TaskDTO> = taskService.getLastTasks(5)
        val responseType = object : ParameterizedTypeReference<List<TaskRequestDTO>>() {}
        val payload = BeanOutputConverter(responseType)
        val tagsTemplate = tags.joinToString(",") { "(${it.id} : ${it.name})" }
        val validResponse = jsonMapper.writeValueAsString(listOf(TaskRequestDTO()))

        logger.info("Creating model with default input data")
        val model = mutableMapOf<String, Any>(
            Pair("userInput", automatedTaskDTO.input),
            Pair("examples", tasks.toString()),
            Pair("tags_with_ids", tagsTemplate),
            Pair("priorities", Priority.entries.toString()),
            Pair("currentDate", LocalDate.now().toString()),
            Pair("requestTaskDTO", payload.format),
            Pair("validResponse", validResponse),
        )

        logger.info("Starting LLM integration")
        val taskRequests = createTaskChatClient
            .prompt(PromptTemplate(SpringAiConfig.PROMPT_TASK_PLANNER).create(model))
            .call()
            .entity(responseType)

        logger.info("Tasks extracted from LLM: ${taskRequests?.size ?: 0} tasks")

        return taskRequests
    }

}