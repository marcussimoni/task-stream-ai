package br.com.taskstreamai.repository

import br.com.taskstreamai.model.WeekSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface WeekScheduleRepository : JpaRepository<WeekSchedule, Long> {
    @Query("SELECT w FROM WeekSchedule w WHERE w.weekStartDate >= :monday AND w.weekStartDate <= :sunday")
    fun findByWeekStartDate(@Param("monday") monday: LocalDate, @Param("sunday") sunday: LocalDate): List<WeekSchedule>
    fun findByWeekStartDateAndDayOfWeekAndHour(weekStartDate: LocalDate, dayOfWeek: Int, hour: Int): WeekSchedule?
    fun deleteByWeekStartDateAndDayOfWeekAndHour(weekStartDate: LocalDate, dayOfWeek: Int, hour: Int)

    @Query("""
        SELECT w FROM WeekSchedule w 
        WHERE w.dayOfWeek = :dayOfWeek 
        AND w.hour = :hour
    """)
    fun findSchedulesStartingAt(
        @Param("dayOfWeek") dayOfWeek: Int,
        @Param("hour") hour: Int
    ): List<WeekSchedule>
}
