package br.com.taskstreamai.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "pending_alarms")
data class PendingAlarm(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "schedule_id", nullable = false)
    val scheduleId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: AlarmType,

    @Column(name = "scheduled_time", nullable = false)
    val scheduledTime: LocalDateTime,

    @Column(name = "emitted_at")
    var emittedAt: LocalDateTime? = null,

    @Column(name = "acknowledged_at")
    var acknowledgedAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun isAcknowledged(): Boolean = acknowledgedAt != null

    fun isEmitted(): Boolean = emittedAt != null
}

enum class AlarmType {
    PRE_REMINDER,
    START_ALARM
}
