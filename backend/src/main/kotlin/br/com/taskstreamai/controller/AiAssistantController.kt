package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.AutomatedTaskDTO
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.TaskRequestDTO
import br.com.taskstreamai.service.AiAssistantService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai-assistant")
class AiAssistantController(
    private val aiAssistantService: AiAssistantService
) {

    @PostMapping("/plan-automated-creation")
    fun planAutomatedTaskCreation(@RequestBody automatedTaskDTO: AutomatedTaskDTO): List<TaskRequestDTO>? = aiAssistantService.planAutomatedTaskCreation(automatedTaskDTO)

}