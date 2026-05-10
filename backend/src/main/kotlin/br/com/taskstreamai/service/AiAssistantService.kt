package br.com.taskstreamai.service

import br.com.taskstreamai.config.SpringAiConfig
import br.com.taskstreamai.dto.AutomatedTaskDTO
import br.com.taskstreamai.dto.EstimatedTimeDTO
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.TaskRequestDTO
import br.com.taskstreamai.model.Priority
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.entity
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate
import kotlin.time.measureTime

private const val LLAMA3_2_3B = "llama3.2:3b"
private const val QWEN_2_5_CODER_7_B = "qwen2.5-coder:7b"

@Service
class AiAssistantService(
    private val chatClient: ChatClient,
    private val taskService: GetTaskService,
    private val tagService: GetTagService,
    private val jsonMapper: JsonMapper,
) {

    private val logger = LoggerFactory.getLogger(AiAssistantService::class.java)

    fun planAutomatedTaskCreation(automatedTaskDTO: AutomatedTaskDTO): List<TaskRequestDTO>? = startAutomatedTaskCreation(automatedTaskDTO)

    private fun startAutomatedTaskCreation(automatedTaskDTO: AutomatedTaskDTO): List<TaskRequestDTO>? {
        logger.info("Creating automated task plan")

        val tasks: List<TaskDTO> = taskService.getLastTasks(5)
        val responseType = object : ParameterizedTypeReference<List<TaskRequestDTO>>() {}
        val payload = BeanOutputConverter(responseType)
        val tagsTemplate = tagService.getAllTagsTemplate()
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
        val taskRequests = chatClient
            .prompt(PromptTemplate(SpringAiConfig.PROMPT_TASK_PLANNER).create(model))
            .options(OllamaChatOptions
                .builder()
                .model(QWEN_2_5_CODER_7_B)
                .temperature(0.0)
                .topP(1.0)
                .build())
            .call()
            .entity(responseType)

        logger.info("Tasks extracted from LLM: ${taskRequests?.size ?: 0} tasks")

        return taskRequests
    }

    fun createTaskSummary(site: String): String? {

        logger.info("Creating summary")

        var summary: String? = null
        val duration = measureTime {
            summary = chatClient
                .prompt()
                .system(SpringAiConfig.PROMPT_SUMMARY_ROLE)
                .user(site)
                .options(
                    OllamaChatOptions
                        .builder()
                        .model(LLAMA3_2_3B)
                        .build()
                )
                .call()
                .content()
        }

        logger.info("Summary completed in $duration ms.")

        return summary


    }

    fun calculateEstimatedTime(siteContent: String): EstimatedTimeDTO? {

        logger.info("Calculating estimated reading time")

        val payload = BeanOutputConverter(EstimatedTimeDTO::class.java)

        var estimatedTime: EstimatedTimeDTO? = null

        val duration = measureTime {
            try {

                val chatOptions = OllamaChatOptions
                    .builder()
                    .model(LLAMA3_2_3B)
                    .temperature(0.0)
                    .topP(1.0)
                    .build()

                estimatedTime = chatClient
                    .prompt()
                    .system(SpringAiConfig.PROMPT_READING_ESTIMATED_TIME)
                    .user {
                        it
                            .param("format", payload.format)
                            .param("content", siteContent)
                    }
                    .options(chatOptions)
                    .call()
                    .entity(payload)

            } catch (e: Exception) {
                logger.error("Error while calculating estimated reading time", e)
            }
        }

        logger.info("Estimated time calculation completed in: $duration ms")

        logger.info("Estimated read time: $estimatedTime")

        return estimatedTime

    }

}