package br.com.taskstreamai.model

import jakarta.persistence.*
import java.time.LocalDate

enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    var description: String,

    var targetValue: Int = 100,

    @Column(nullable = false)
    var currentValue: Int = 0,

    var startDate: LocalDate,

    var endDateInterval: Int? = null,

    var endDate: LocalDate? = null,

    var completed: Boolean = false,

    var customEndDateSelected: Boolean = false,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var priority: Priority = Priority.LOW,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id", nullable = false)
    var tag: Tag,

    @Column(length = 300)
    var link: String? = null,

    @Column(columnDefinition = "TEXT")
    var summary: String? = null
)
