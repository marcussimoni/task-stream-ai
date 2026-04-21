package br.com.taskstreamai.service

import br.com.taskstreamai.repository.WeekScheduleRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Service
class PreReminderScheduler(
    private val weekScheduleRepository: WeekScheduleRepository,
    private val alarmEmitterService: AlarmEmitterService
) {

    /**
     * Runs at minute 55 of every hour (e.g., 8:55, 9:55, 10:55).
     * Emits pre-reminders for tasks starting at the next hour (e.g., 9:00).
     * Fixed 5-minute pre-reminder.
     */
    @Scheduled(cron = "0 55 * * * *")
    fun emitPreReminders() {
        val now = LocalDateTime.now()
        val targetHour = now.plusMinutes(5)

        // Calculate which day of week the target hour falls on
        val targetDate = targetHour.toLocalDate()
        val dayOfWeek = targetDate.dayOfWeek.value % 7  // Convert to 0=Monday, 6=Sunday

        val preReminders = weekScheduleRepository.findSchedulesStartingAt(
            dayOfWeek = dayOfWeek,
            hour = targetHour.hour
        )

        preReminders.forEach { schedule ->
            alarmEmitterService.emitPreReminder(schedule)
        }
    }
}
