package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.CreateWeekScheduleRequest
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.dto.WeekScheduleDTO
import br.com.taskstreamai.service.WeekScheduleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/week-calendar")
class WeekCalendarController(private val weekScheduleService: WeekScheduleService) {

    @GetMapping
    fun getWeekSchedule(@RequestParam weekStartDate: String): ResponseEntity<List<WeekScheduleDTO>> {
        val schedule = weekScheduleService.getWeekSchedule(LocalDate.parse(weekStartDate))
        return ResponseEntity.ok(schedule)
    }

    @PostMapping
    fun createSchedule(@RequestBody request: CreateWeekScheduleRequest): ResponseEntity<WeekScheduleDTO> {
        val schedule = weekScheduleService.createOrUpdateSchedule(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule)
    }

    @DeleteMapping("/{id}")
    fun deleteSchedule(@PathVariable id: Long): ResponseEntity<Void> {
        weekScheduleService.deleteSchedule(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/task-for-tag")
    fun getTaskForTag(@RequestParam tagId: Long): ResponseEntity<TaskDTO> {
        val task = weekScheduleService.getTaskForTag(tagId)
        return if (task != null) {
            ResponseEntity.ok(task)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
