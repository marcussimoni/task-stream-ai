package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.MonthlyTaskMetricsDTO
import br.com.taskstreamai.service.MetricsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/metrics")
class MetricsController(
    private val metricsService: MetricsService
) {

    @GetMapping("/monthly/tasks")
    fun getMonthlyTasks(
        @RequestParam("month") month: Int,
        @RequestParam("year") year: Int
    ): ResponseEntity<MonthlyTaskMetricsDTO> {
        val metrics = metricsService.getMonthlyTaskMetrics(month, year)
        return ResponseEntity.ok(metrics)
    }
}
