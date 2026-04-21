package br.com.taskstreamai.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tags")
data class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    var name: String,
    
    @Column(nullable = false)
    var description: String,
    
    @Column(nullable = false)
    var color: String = "#3B82F6",
    
    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    var updatedAt: LocalDateTime? = null,

)
