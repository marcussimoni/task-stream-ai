package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.LogDTO
import br.com.taskstreamai.service.ApplicationLogsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/application-logs")
class ApplicationLogController(
    private val applicationLogsService: ApplicationLogsService
) {

    @GetMapping
    fun getLog(@RequestParam(name = "lines", required = false) lines: Int = ApplicationLogsService.LOG_LIMIT_LINES): LogDTO {
        return applicationLogsService.retrieveLogs(lines)
    }

}