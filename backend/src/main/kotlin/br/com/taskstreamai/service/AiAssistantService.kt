package br.com.taskstreamai.service

import br.com.taskstreamai.config.SpringAiConfig
import br.com.taskstreamai.dto.AutomatedTaskDTO
import br.com.taskstreamai.dto.EstimatedTimeDTO
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.TaskRequestDTO
import br.com.taskstreamai.model.Priority
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate
import kotlin.time.measureTime

@Service
class AiAssistantService(
    @Qualifier("createTaskChatClient") private val createTaskChatClient: ChatClient,
    private val chatClient: ChatClient,
    @Qualifier("createEstimatedReadingTimeChatClient") private val createEstimatedReadingTimeChatClient: ChatClient,
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
        val taskRequests = createTaskChatClient
            .prompt(PromptTemplate(SpringAiConfig.PROMPT_TASK_PLANNER).create(model))
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
                .call()
                .content()
        }

        logger.info("Summary completed in $duration ms.")

        return summary


    }

    fun calculateEstimatedTime(siteContent: String): EstimatedTimeDTO? {

        logger.info("Calculating estimated reading time")

        val payload = BeanOutputConverter(EstimatedTimeDTO::class.java)

        val model = mutableMapOf<String, Any>(
            Pair("content", siteContent),
            Pair("format", payload.format)
        )

        var estimatedTime: EstimatedTimeDTO? = null

        val duration = measureTime {
            try {

                val prompt = PromptTemplate(SpringAiConfig.PROMPT_READING_ESTIMATED_TIME).create(model)

                val content = createEstimatedReadingTimeChatClient
                    .prompt(prompt)
                    .call()
                    .chatClientResponse().chatResponse()?.result?.output?.text

                content?.let {
                    estimatedTime = payload.convert(content)
                }

            } catch (e: Exception) {
                logger.error("Error while calculating estimated reading time", e)
            }
        }

        logger.info("Estimated time calculation completed in: $duration ms")

        logger.info("Estimated read time: $estimatedTime")

        return estimatedTime

    }

}