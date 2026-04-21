package br.com.taskstreamai.service

import br.com.taskstreamai.dto.CreateWeekScheduleRequest
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.WeekScheduleDTO
import br.com.taskstreamai.exception.ResourceNotFoundException
import br.com.taskstreamai.mapper.TaskMapper
import br.com.taskstreamai.mapper.WeekScheduleMapper
import br.com.taskstreamai.model.Priority
import br.com.taskstreamai.model.Task
import br.com.taskstreamai.model.WeekSchedule
import br.com.taskstreamai.repository.TagRepository
import br.com.taskstreamai.repository.TaskRepository
import br.com.taskstreamai.repository.WeekScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.enums.enumEntries

@Service
@Transactional
class WeekScheduleService(
    private val scheduleRepository: WeekScheduleRepository,
    private val taskRepository: TaskRepository,
    private val tagRepository: TagRepository
) {

    fun getWeekSchedule(weekStartDate: LocalDate): List<WeekScheduleDTO> {

        val monday = weekStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val sunday = weekStartDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val schedules = scheduleRepository.findAll()

        // Find tasks for each tag in the schedule
        val taskMap = schedules.associate { schedule ->
            val task = findTaskForTag(schedule.tag.id)
            schedule.tag.id to task?.let { TaskMapper.toDTO(it) }
        }

        return WeekScheduleMapper.toDTOList(schedules, taskMap)
    }

    fun createOrUpdateSchedule(request: CreateWeekScheduleRequest): WeekScheduleDTO {
        val tag = tagRepository.findById(request.tagId)
            .orElseThrow { ResourceNotFoundException("Tag not found with id: ${request.tagId}") }

        val weekStartDate = LocalDate.parse(request.weekStartDate)

        // Check if a schedule already exists for this slot
        val existingSchedule = scheduleRepository.findByWeekStartDateAndDayOfWeekAndHour(
            weekStartDate, request.dayOfWeek, request.hour
        )

        val schedule = if (existingSchedule != null) {
            // Update existing schedule
            existingSchedule.tag = tag
            existingSchedule.updatedAt = LocalDateTime.now()
            scheduleRepository.save(existingSchedule)
        } else {
            // Create new schedule
            val newSchedule = WeekSchedule(
                dayOfWeek = request.dayOfWeek,
                hour = request.hour,
                weekStartDate = weekStartDate,
                tag = tag
            )
            scheduleRepository.save(newSchedule)
        }

        val task = findTaskForTag(tag.id)
        return WeekScheduleMapper.toDTO(schedule, task?.let { TaskMapper.toDTO(it) })
    }

    fun deleteSchedule(id: Long) {
        if (!scheduleRepository.existsById(id)) {
            throw ResourceNotFoundException("Schedule not found with id: $id")
        }
        scheduleRepository.deleteById(id)
    }

    fun deleteScheduleBySlot(weekStartDate: LocalDate, dayOfWeek: Int, hour: Int) {
        scheduleRepository.deleteByWeekStartDateAndDayOfWeekAndHour(weekStartDate, dayOfWeek, hour)
    }

    fun findTaskForTag(tagId: Long): Task? {

        var task: Task? = null
        val priorities = listOf(Priority.CRITICAL, Priority.HIGH, Priority.MEDIUM, Priority.LOW)
        for (priority in priorities) {
            val optional = taskRepository.findByTagAndPriority(tagId, priority.name)
            if (optional.isPresent) {
                task = optional.get();
                break;
            }
        }

        return task;
    }

    fun getTaskForTag(tagId: Long): TaskDTO? {
        val task = findTaskForTag(tagId)
        return task?.let { TaskMapper.toDTO(it) }
    }
}
