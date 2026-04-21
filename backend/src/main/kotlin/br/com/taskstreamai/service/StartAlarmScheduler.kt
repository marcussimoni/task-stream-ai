package br.com.taskstreamai.service

import br.com.taskstreamai.repository.WeekScheduleRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Service
class StartAlarmScheduler(
    private val weekScheduleRepository: WeekScheduleRepository,
    private val alarmEmitterService: AlarmEmitterService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Runs at minute 0 of every hour (e.g., 9:00, 10:00, 11:00).
     * Emits start alarms for tasks beginning at the current hour.
     */
    @Scheduled(cron = $$"${application.start-alarm-scheduler}")
    fun emitStartAlarms() {

        logger.debug("Starting hour alarms schedule")

        val now = LocalDateTime.now()

        // Calculate which day of week the current hour falls on
        val currentDate = now.toLocalDate()
        val dayOfWeek = currentDate.dayOfWeek.value % 7  // Convert to 0=Monday, 6=Sunday

        logger.debug("Starting for day $dayOfWeek")
        logger.debug("Starting for hour ${now.hour}")

        val startAlarms = weekScheduleRepository.findSchedulesStartingAt(
            dayOfWeek = dayOfWeek,
            hour = now.hour,
        )

        logger.debug("Notifications found ${startAlarms.size}")
        logger.debug("Starting alarms for tags $startAlarms")

        startAlarms.forEach { schedule ->
            logger.debug("Emitting event for $schedule")
            alarmEmitterService.emitStartAlarm(schedule)
        }

        logger.debug("Ending alarms")
    }
}
