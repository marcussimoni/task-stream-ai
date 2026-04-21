package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.LinkContentDTO
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.TaskQueryParamsDTO
import br.com.taskstreamai.dto.TaskRequestDTO
import br.com.taskstreamai.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {
    
    @PostMapping
    fun createTask(@Valid @RequestBody requestDTO: TaskRequestDTO): ResponseEntity<TaskDTO> {
        val createdTask = taskService.createTask(requestDTO)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask)
    }
    
    @GetMapping
    fun getAllTasks(
        @RequestParam(value = "tag", required = false) tag: Long?,
    ): ResponseEntity<List<TaskDTO>> {
        val queryParams = TaskQueryParamsDTO(tag)
        val tasks = taskService.getAllTasks(queryParams)
        return ResponseEntity.ok(tasks)
    }
    
    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: Long): ResponseEntity<TaskDTO> {
        val task = taskService.getTaskById(id)
        return ResponseEntity.ok(task)
    }
    
    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @Valid @RequestBody requestDTO: TaskRequestDTO
    ): ResponseEntity<TaskDTO> {
        val updatedTask = taskService.updateTask(id, requestDTO)
        return ResponseEntity.ok(updatedTask)
    }
    
    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        taskService.deleteTask(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/tag/{tagId}")
    fun getTasksByTag(@PathVariable tagId: Long): ResponseEntity<List<TaskDTO>> {
        val tasks = taskService.getTasksByTag(tagId)
        return ResponseEntity.ok(tasks)
    }

    @PatchMapping("/{id}/completed")
    fun completeTask(
        @PathVariable id: Long
    ): ResponseEntity<TaskDTO> {
        val updatedTask = taskService.completeTask(id)
        return ResponseEntity.ok(updatedTask)
    }

    @GetMapping("/month")
    fun getTasksForMonth(
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate
    ): ResponseEntity<List<TaskDTO>> {
        val tasks = taskService.getTasksForMonthRange(startDate, endDate)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("link-content")
    fun getLinkContent(@RequestParam url: String): ResponseEntity<LinkContentDTO> {
        return ResponseEntity.ok(taskService.loadLinkContent(url))
    }

}
