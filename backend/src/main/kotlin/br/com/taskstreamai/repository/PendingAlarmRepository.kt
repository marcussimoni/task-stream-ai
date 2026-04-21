package br.com.taskstreamai.repository

import br.com.taskstreamai.model.AlarmType
import br.com.taskstreamai.model.PendingAlarm
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PendingAlarmRepository : JpaRepository<PendingAlarm, String> {

    @Query("SELECT p FROM PendingAlarm p WHERE p.acknowledgedAt IS NULL AND p.scheduledTime >= :since ORDER BY p.scheduledTime")
    fun findPendingAlarmsSince(@Param("since") since: LocalDateTime): List<PendingAlarm>

    @Query("SELECT p FROM PendingAlarm p WHERE p.scheduleId = :scheduleId AND p.type = :type AND p.acknowledgedAt IS NULL")
    fun findUnacknowledgedByScheduleAndType(
        @Param("scheduleId") scheduleId: Long,
        @Param("type") type: AlarmType
    ): PendingAlarm?

    @Query("SELECT p FROM PendingAlarm p WHERE p.acknowledgedAt IS NULL AND p.scheduledTime <= :before")
    fun findUnacknowledgedBefore(@Param("before") before: LocalDateTime): List<PendingAlarm>

    fun findByScheduleIdAndTypeAndAcknowledgedAtIsNull(scheduleId: Long, type: AlarmType): PendingAlarm?
}
