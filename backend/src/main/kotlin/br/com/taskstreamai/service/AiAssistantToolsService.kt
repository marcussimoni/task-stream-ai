package br.com.taskstreamai.service

import br.com.taskstreamai.dto.TaskRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AiAssistantToolsService(
    private val taskService: TaskService
) {

    private val logger = LoggerFactory.getLogger(AiAssistantToolsService::class.java)

    @Tool(description = "Create a new task. Extract task details from the userInput and match tag names to tag IDs from the Reference Data.")
    fun createTaskTool(
        @ToolParam(description = "Task details extracted from user input. Map tag names to IDs, dates to YYYY-MM-DD format, and set appropriate priority.")
        requestDTO: TaskRequestDTO
    ) {

        logger.info("Creating task tool {}", requestDTO)
        taskService.createTask(requestDTO)
        logger.info("Task created: {}", requestDTO)
    }

    @Tool(description = "Log the reason why a task could not be created. Call this when user input is missing critical information.")
    fun logErrorCreateTaskTool(
        @ToolParam(description = "Explanation of what's missing or why the task cannot be created")
        reason: String
    ) {
        logger.error("Task creation failed: {}", reason)
    }

}