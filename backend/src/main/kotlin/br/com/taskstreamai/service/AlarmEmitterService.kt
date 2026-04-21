package br.com.taskstreamai.service

import br.com.taskstreamai.dto.AlarmDTO
import br.com.taskstreamai.model.AlarmType
import br.com.taskstreamai.model.PendingAlarm
import br.com.taskstreamai.model.WeekSchedule
import br.com.taskstreamai.repository.PendingAlarmRepository
import br.com.taskstreamai.repository.WeekScheduleRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class AlarmEmitterService(
    private val pendingAlarmRepository: PendingAlarmRepository,
    private val weekScheduleRepository: WeekScheduleRepository,
    private val weekScheduleService: WeekScheduleService
) {
    private val emitters = CopyOnWriteArrayList<SseEmitter>()
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val logger = LoggerFactory.getLogger(AlarmEmitterService::class.java)

    init {
        startHeartbeat()
    }

    fun createEmitter(lastEventId: String?): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)

        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        emitters.add(emitter)

        // Send initial heartbeat
        try {
            emitter.send(SseEmitter.event()
                .name("heartbeat")
                .data("connected"))
        } catch (e: Exception) {
            emitters.remove(emitter)
        }

        // If reconnecting with lastEventId, send missed alarms
        if (lastEventId != null) {
            sendMissedAlarms(emitter, lastEventId)
        }

        return emitter
    }

    fun emitPreReminder(schedule: WeekSchedule) {
        // Check if already emitted and not acknowledged
        val existingAlarm = pendingAlarmRepository
            .findUnacknowledgedByScheduleAndType(schedule.id!!, AlarmType.PRE_REMINDER)

        if (existingAlarm != null) {
            return // Already pending
        }

        val alarmTime = LocalDateTime.now()
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val pendingAlarm = PendingAlarm(
            scheduleId = schedule.id!!,
            type = AlarmType.PRE_REMINDER,
            scheduledTime = alarmTime,
            emittedAt = LocalDateTime.now()
        )

        val savedAlarm = pendingAlarmRepository.save(pendingAlarm)
        val taskName = weekScheduleService.findTaskForTag(schedule.tag.id)?.name

        val alarmDTO = AlarmDTO.fromPendingAlarm(savedAlarm, schedule, taskName)
        broadcastAlarm(alarmDTO)
    }

    fun emitStartAlarm(schedule: WeekSchedule) {
        // Check if already emitted and not acknowledged
        val existingAlarm = pendingAlarmRepository
            .findUnacknowledgedByScheduleAndType(schedule.id!!, AlarmType.START_ALARM)
        logger.debug("Emitting event for {}", schedule)
        if (existingAlarm != null) {
            logger.debug("Existing alarm not found.")
//            return // Already pending
        }

        logger.debug("Starting alarm time")
        val alarmTime = LocalDateTime.now()
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        logger.debug("Emitting event for {}", alarmTime)
        val pendingAlarm = PendingAlarm(
            scheduleId = schedule.id!!,
            type = AlarmType.START_ALARM,
            scheduledTime = alarmTime,
            emittedAt = LocalDateTime.now()
        )

        val savedAlarm = pendingAlarmRepository.save(pendingAlarm)
        val taskName = weekScheduleService.findTaskForTag(schedule.tag.id)?.name

        val alarmDTO = AlarmDTO.fromPendingAlarm(savedAlarm, schedule, taskName)
        broadcastAlarm(alarmDTO)
    }

    fun acknowledgeAlarm(alarmId: String): Boolean {
        val alarm = pendingAlarmRepository.findById(alarmId)
        if (alarm.isPresent) {
            val pendingAlarm = alarm.get()
            pendingAlarm.acknowledgedAt = LocalDateTime.now()
            pendingAlarmRepository.save(pendingAlarm)
            return true
        }
        return false
    }

    fun getPendingAlarms(): List<AlarmDTO> {
        val since = LocalDateTime.now().minusHours(1)
        val pendingAlarms = pendingAlarmRepository.findPendingAlarmsSince(since)

        return pendingAlarms.mapNotNull { alarm ->
            weekScheduleRepository.findById(alarm.scheduleId).map { schedule ->
                val taskName = weekScheduleService.findTaskForTag(schedule.tag.id)?.name
                AlarmDTO.fromPendingAlarm(alarm, schedule, taskName)
            }.orElse(null)
        }
    }

    private fun broadcastAlarm(alarmDTO: AlarmDTO) {
        try {
            logger.debug("Broadcasting alarm: {}", alarmDTO.id)
            val deadEmitters = mutableListOf<SseEmitter>()

            emitters.forEach { emitter ->
                try {
                    emitter.send(SseEmitter.event()
                        .id(alarmDTO.id)
                        .name("alarm")
                        .data(alarmDTO))
                } catch (e: Throwable) {
                    logger.debug("Failed to send alarm to emitter, marking as dead: {}", e.message)
                    deadEmitters.add(emitter)
                }
            }

            // Clean up dead emitters
            deadEmitters.forEach { emitters.remove(it) }
        } catch (e: Throwable) {
            logger.error("Unexpected error in broadcastAlarm", e)
        }
    }

    private fun sendMissedAlarms(emitter: SseEmitter, lastEventId: String) {
        try {
            val missedAlarms = getPendingAlarms()
            missedAlarms.forEach { alarm ->
                if (alarm.id > lastEventId) {
                    try {
                        emitter.send(SseEmitter.event()
                            .id(alarm.id)
                            .name("alarm")
                            .data(alarm))
                    } catch (e: Exception) {
                        emitters.remove(emitter)
                        return
                    }
                }
            }
        } catch (e: Exception) {
            emitters.remove(emitter)
        }
    }

    private fun startHeartbeat() {
        scheduler.scheduleAtFixedRate({
            try {
                val deadEmitters = mutableListOf<SseEmitter>()

                emitters.forEach { emitter ->
                    try {
                        emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data("ping"))
                    } catch (e: Throwable) {
                        deadEmitters.add(emitter)
                    }
                }

                deadEmitters.forEach { emitters.remove(it) }
            } catch (e: Throwable) {
                logger.error("Unexpected error in heartbeat", e)
            }
        }, 30, 30, TimeUnit.SECONDS)
    }
}
