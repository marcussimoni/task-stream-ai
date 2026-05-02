package br.com.taskstreamai.service

import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.mapper.TaskMapper
import br.com.taskstreamai.repository.TaskRepository
import org.springframework.stereotype.Service

@Service
class GetTaskService(private val taskRepository: TaskRepository) {
    fun getLastTasks(total: Int): List<TaskDTO> {
        val tasks = taskRepository.findTaskByDates(total)
        val tasksTemplate = tasks.map { TaskMapper.toDTO(it) }
        return tasksTemplate
    }
}