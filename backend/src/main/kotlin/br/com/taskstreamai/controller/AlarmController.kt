package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.AlarmDTO
import br.com.taskstreamai.service.AlarmEmitterService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/alarms")
class AlarmController(
    private val alarmEmitterService: AlarmEmitterService
) {

    /**
     * SSE endpoint for real-time alarm notifications.
     * Supports reconnection with Last-Event-ID header or lastEventId query parameter.
     */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamAlarms(
        @RequestHeader(value = "Last-Event-ID", required = false) lastEventIdHeader: String?,
        @RequestParam(value = "lastEventId", required = false) lastEventIdParam: String?
    ): SseEmitter {
        val lastEventId = lastEventIdHeader ?: lastEventIdParam
        return alarmEmitterService.createEmitter(lastEventId)
    }

    /**
     * Mark an alarm as acknowledged.
     */
    @PostMapping("/{id}/acknowledge")
    fun acknowledgeAlarm(@PathVariable id: String): ResponseEntity<Void> {
        val success = alarmEmitterService.acknowledgeAlarm(id)
        return if (success) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Get all pending (unacknowledged) alarms for reconnection sync.
     */
    @GetMapping("/pending")
    fun getPendingAlarms(): ResponseEntity<List<AlarmDTO>> {
        val alarms = alarmEmitterService.getPendingAlarms()
        return ResponseEntity.ok(alarms)
    }
}
