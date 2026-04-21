package br.com.taskstreamai.dto

import br.com.taskstreamai.model.AlarmType
import br.com.taskstreamai.model.PendingAlarm
import br.com.taskstreamai.model.WeekSchedule
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AlarmDTO(
    val id: String,
    val type: String,
    val scheduleId: Long,
    val tagName: String,
    val tagColor: String,
    val taskName: String?,
    val scheduledTime: String,
    val message: String
) {
    companion object {
        private val ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME

        fun fromPendingAlarm(
            alarm: PendingAlarm,
            schedule: WeekSchedule,
            taskName: String? = null
        ): AlarmDTO {
            val message = when (alarm.type) {
                AlarmType.PRE_REMINDER ->
                    "Your ${schedule.tag.name} session starts in 5 minutes (${formatTime(alarm.scheduledTime)})"
                AlarmType.START_ALARM ->
                    "START NOW: ${schedule.tag.name} session at ${formatTime(alarm.scheduledTime)}"
            }

            return AlarmDTO(
                id = alarm.id,
                type = alarm.type.name,
                scheduleId = alarm.scheduleId,
                tagName = schedule.tag.name,
                tagColor = schedule.tag.color,
                taskName = taskName,
                scheduledTime = alarm.scheduledTime.format(ISO_FORMATTER),
                message = message
            )
        }

        private fun formatTime(dateTime: LocalDateTime): String {
            return dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
        }
    }
}
