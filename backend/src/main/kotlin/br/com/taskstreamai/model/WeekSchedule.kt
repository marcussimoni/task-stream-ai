package br.com.taskstreamai.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "week_schedules")
data class WeekSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val dayOfWeek: Int, // 0-6 (Monday-Sunday)

    @Column(nullable = false, name = "task_hour")
    val hour: Int, // 8-22

    @Column(nullable = false)
    val weekStartDate: LocalDate, // First day of the week (Monday)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id", nullable = false)
    var tag: Tag,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = null,
)
