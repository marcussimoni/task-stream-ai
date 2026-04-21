package br.com.taskstreamai.service

import br.com.taskstreamai.dto.LinkContentDTO
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.TaskQueryParamsDTO
import br.com.taskstreamai.dto.TaskRequestDTO
import br.com.taskstreamai.exception.ResourceNotFoundException
import br.com.taskstreamai.mapper.TaskMapper
import br.com.taskstreamai.model.Task
import br.com.taskstreamai.repository.TagRepository
import br.com.taskstreamai.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private const val PERCENT_COMPLETED = 100

@Service
@Transactional
class TaskService(
    private val taskRepository: TaskRepository,
    private val tagRepository: TagRepository,
    private val summarizeArticleService: SummarizeArticleService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun createTask(requestDTO: TaskRequestDTO): TaskDTO {
        val tag = tagRepository.findById(requestDTO.tagId)
            .orElseThrow { ResourceNotFoundException("Tag not found with id: ${requestDTO.tagId}") }

        if(!requestDTO.customEndDateSelected){
            requestDTO.endDate = requestDTO.startDate.plusWeeks(requestDTO.endDateInterval!!.toLong())
        }

        val task = TaskMapper.toEntity(requestDTO, tag)
        val savedTask = taskRepository.save(task)

        val taskDto = TaskMapper.toDTO(savedTask)

        serviceScope.launch {
            summarizeArticleService.createTaskSummary(taskDto)
        }

        return taskDto
    }

    
    fun getTaskById(id: Long): TaskDTO {
        val task = findById(id);
        return TaskMapper.toDTO(task)
    }
    
    fun getAllTasks(queryParamsDTO: TaskQueryParamsDTO): List<TaskDTO> {

        val tasks = ArrayList<Task>()
        tasks.addAll(taskRepository.findAllTasks(false, queryParamsDTO.task))
        tasks.addAll(taskRepository.findAllTasks(true, queryParamsDTO.task))
        return TaskMapper.toDTOList(tasks)
    }
    
    fun updateTask(id: Long, requestDTO: TaskRequestDTO): TaskDTO {
        val existingTask = taskRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Task not found with id: $id") }
        
        val tag = tagRepository.findById(requestDTO.tagId)
            .orElseThrow { ResourceNotFoundException("Tag not found with id: ${requestDTO.tagId}") }
        
        if(!requestDTO.customEndDateSelected){
            requestDTO.endDate = requestDTO.startDate.plusWeeks(requestDTO.endDateInterval!!.toLong())
        }
        
        existingTask.name = requestDTO.name
        existingTask.description = requestDTO.description
        existingTask.currentValue = getCurrentValue(requestDTO)
        existingTask.startDate = requestDTO.startDate ?: existingTask.startDate
        existingTask.endDateInterval = requestDTO.endDateInterval
        existingTask.endDate = requestDTO.endDate
        existingTask.tag = tag
        existingTask.customEndDateSelected = requestDTO.customEndDateSelected
        existingTask.completed = requestDTO.currentValue >= 100
        existingTask.priority = requestDTO.priority
        existingTask.link = requestDTO.link
        
        val savedTask = taskRepository.save(existingTask)
        val taskDto = TaskMapper.toDTO(savedTask)

        serviceScope.launch {
            summarizeArticleService.createTaskSummary(taskDto)
        }

        return taskDto
    }
    
    fun deleteTask(id: Long) {
        if (!taskRepository.existsById(id)) {
            throw ResourceNotFoundException("Task not found with id: $id")
        }
        taskRepository.deleteById(id)
    }
    
    fun getTasksByTag(tagId: Long): List<TaskDTO> {
        val tag = tagRepository.findById(tagId)
            .orElseThrow { ResourceNotFoundException("Tag not found with id: $tagId") }

        val tasks = taskRepository.findByTag(tag)
        return TaskMapper.toDTOList(tasks)
    }

    @Transactional
    fun completeTask(id: Long): TaskDTO {
        val task = findById(id)
        task.completed = true
        task.currentValue = 100
        val savedTask = taskRepository.save(task)
        return TaskMapper.toDTO(savedTask)
    }

    private fun findById(id: Long): Task {
        val task = taskRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Task not found with id: $id") }
        return task;
    }

    fun getTasksForMonthRange(monthStart: LocalDate, monthEnd: LocalDate): List<TaskDTO> {
        val tasks = taskRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(monthEnd, monthStart)
        return TaskMapper.toDTOList(tasks)
    }

    fun getCurrentValue(requestDTO: TaskRequestDTO): Int {
        return if (requestDTO.currentValue >= PERCENT_COMPLETED) {
            PERCENT_COMPLETED
        } else {
            requestDTO.currentValue
        }
    }

    fun loadLinkContent(url: String): LinkContentDTO {
        logger.info("Loading link content for url: $url")
        val site = summarizeArticleService.loadSiteFromUrl(url)
        logger.info("Link content for url: ${site.title()}")
        return LinkContentDTO(site.title())
    }

}
