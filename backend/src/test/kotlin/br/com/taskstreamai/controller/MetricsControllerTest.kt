package br.com.taskstreamai.controller

import br.com.taskstreamai.dto.MonthlyTaskMetricsDTO
import br.com.taskstreamai.dto.TaskMetricsDTO
import br.com.taskstreamai.service.MetricsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@WebMvcTest(MetricsController::class)
@ExtendWith(MockitoExtension::class)
class MetricsControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    private lateinit var metricsService: MetricsService

    @Test
    fun `should get monthly tasks metrics successfully`() {
        // Given
        val month = 1
        val year = 2024
        val expectedMetrics = MonthlyTaskMetricsDTO(
            tasksMetrics = listOf(
                TaskMetricsDTO(
                    tagId = 1L,
                    tag = "Work",
                    total = 15L,
                    status = "completed",
                    date = LocalDate.of(2024, 1, 15)
                ),
                TaskMetricsDTO(
                    tagId = 2L,
                    tag = "Personal",
                    total = 8L,
                    status = "incompleted",
                    date = LocalDate.of(2024, 1, 10)
                ),
                TaskMetricsDTO(
                    tagId = 3L,
                    tag = "Health",
                    total = 12L,
                    status = "completed",
                    date = LocalDate.of(2024, 1, 20)
                )
            ),
            totalCompleted = 27L,
            totalIncomplete = 8L
        )
        
        Mockito.`when`(metricsService.getMonthlyTaskMetrics(month, year)).thenReturn(expectedMetrics)

        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", month.toString())
            param("year", year.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.tasksMetrics.length()").value(3)
            jsonPath("$.totalCompleted").value(27)
            jsonPath("$.totalIncomplete").value(8)
            jsonPath("$.tasksMetrics[0].tagId").value(1)
            jsonPath("$.tasksMetrics[0].tag").value("Work")
            jsonPath("$.tasksMetrics[0].total").value(15)
            jsonPath("$.tasksMetrics[0].status").value("completed")
            jsonPath("$.tasksMetrics[0].date").value("2024-01-15")
            jsonPath("$.tasksMetrics[1].tag").value("Personal")
            jsonPath("$.tasksMetrics[1].status").value("incompleted")
            jsonPath("$.tasksMetrics[2].tag").value("Health")
        }
    }

    @Test
    fun `should get monthly tasks metrics with empty data`() {
        // Given
        val month = 2
        val year = 2024
        val expectedMetrics = MonthlyTaskMetricsDTO(
            tasksMetrics = emptyList(),
            totalCompleted = 0L,
            totalIncomplete = 0L
        )
        
        Mockito.`when`(metricsService.getMonthlyTaskMetrics(month, year)).thenReturn(expectedMetrics)

        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", month.toString())
            param("year", year.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.tasksMetrics.length()").value(0)
            jsonPath("$.totalCompleted").value(0)
            jsonPath("$.totalIncomplete").value(0)
        }
    }

    @Test
    fun `should get monthly tasks metrics for different months`() {
        // Given
        val testCases = listOf(
            Triple(1, 2024, "January 2024"),
            Triple(6, 2024, "June 2024"),
            Triple(12, 2024, "December 2024"),
            Triple(3, 2023, "March 2023")
        )
        
        testCases.forEach { (month, year, description) ->
            val expectedMetrics = MonthlyTaskMetricsDTO(
                tasksMetrics = listOf(
                    TaskMetricsDTO(
                        tagId = 1L,
                        tag = "Test",
                        total = 5L,
                        status = "completed",
                        date = LocalDate.of(year, month, 15)
                    )
                ),
                totalCompleted = 5L,
                totalIncomplete = 0L
            )
            
            Mockito.`when`(metricsService.getMonthlyTaskMetrics(month, year)).thenReturn(expectedMetrics)

            // When & Then
            mockMvc.get("/api/metrics/monthly/tasks") {
                param("month", month.toString())
                param("year", year.toString())
            }.andExpect {
                status().isOk()
                content().contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.tasksMetrics.length()").value(1)
                jsonPath("$.totalCompleted").value(5)
                jsonPath("$.totalIncomplete").value(0)
            }
        }
    }

    @Test
    fun `should return 400 when month parameter is missing`() {
        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("year", "2024")
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when year parameter is missing`() {
        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", "1")
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should return 400 when both parameters are missing`() {
        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks").andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle invalid month parameter`() {
        // Given
        val invalidMonth = 13
        val year = 2024
        
        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", invalidMonth.toString())
            param("year", year.toString())
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle invalid year parameter`() {
        // Given
        val month = 1
        val invalidYear = 1800
        
        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", month.toString())
            param("year", invalidYear.toString())
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle negative month parameter`() {
        // Given
        val negativeMonth = -1
        val year = 2024
        
        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", negativeMonth.toString())
            param("year", year.toString())
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle negative year parameter`() {
        // Given
        val month = 1
        val negativeYear = -2024
        
        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", month.toString())
            param("year", negativeYear.toString())
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle zero month parameter`() {
        // Given
        val zeroMonth = 0
        val year = 2024
        
        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", zeroMonth.toString())
            param("year", year.toString())
        }.andExpect {
            status().isBadRequest()
        }
    }

    @Test
    fun `should handle service errors gracefully`() {
        // Given
        val month = 1
        val year = 2024
        
        Mockito.`when`(metricsService.getMonthlyTaskMetrics(month, year)).thenThrow(RuntimeException("Database error"))

        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", month.toString())
            param("year", year.toString())
        }.andExpect {
            status().is5xxServerError()
        }
    }

    @Test
    fun `should handle large metrics data`() {
        // Given
        val month = 1
        val year = 2024
        val largeTasksMetricsList = (1..100).map { index ->
            TaskMetricsDTO(
                tagId = index.toLong(),
                tag = "Tag $index",
                total = (index * 2).toLong(),
                status = if (index % 2 == 0) "completed" else "incompleted",
                date = LocalDate.of(year, month, (index % 28) + 1)
            )
        }
        val expectedMetrics = MonthlyTaskMetricsDTO(
            tasksMetrics = largeTasksMetricsList,
            totalCompleted = 1050L,
            totalIncomplete = 950L
        )
        
        Mockito.`when`(metricsService.getMonthlyTaskMetrics(month, year)).thenReturn(expectedMetrics)

        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", month.toString())
            param("year", year.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.tasksMetrics.length()").value(100)
            jsonPath("$.totalCompleted").value(1050)
            jsonPath("$.totalIncomplete").value(950)
            jsonPath("$.tasksMetrics[0].tag").value("Tag 1")
            jsonPath("$.tasksMetrics[99].tag").value("Tag 100")
        }
    }

    @Test
    fun `should handle edge case months`() {
        // Given
        val edgeCases = listOf(
            Pair(1, 2024), // January
            Pair(12, 2024), // December
            Pair(2, 2024), // February (shortest month)
            Pair(7, 2024)  // July (middle of year)
        )
        
        edgeCases.forEach { (month, year) ->
            val expectedMetrics = MonthlyTaskMetricsDTO(
                tasksMetrics = listOf(
                    TaskMetricsDTO(
                        tagId = 1L,
                        tag = "Edge Case Tag",
                        total = 10L,
                        status = "completed",
                        date = LocalDate.of(year, month, 15)
                    )
                ),
                totalCompleted = 10L,
                totalIncomplete = 0L
            )
            
            Mockito.`when`(metricsService.getMonthlyTaskMetrics(month, year)).thenReturn(expectedMetrics)

            // When & Then
            mockMvc.get("/api/metrics/monthly/tasks") {
                param("month", month.toString())
                param("year", year.toString())
            }.andExpect {
                status().isOk()
                content().contentType(MediaType.APPLICATION_JSON)
                jsonPath("$.tasksMetrics.length()").value(1)
                jsonPath("$.totalCompleted").value(10)
            }
        }
    }

    @Test
    fun `should handle leap year February`() {
        // Given
        val month = 2
        val year = 2024 // Leap year
        val expectedMetrics = MonthlyTaskMetricsDTO(
            tasksMetrics = listOf(
                TaskMetricsDTO(
                    tagId = 1L,
                    tag = "Leap Year Tag",
                    total = 29L, // 29 days in February
                    status = "completed",
                    date = LocalDate.of(year, month, 29)
                )
            ),
            totalCompleted = 29L,
            totalIncomplete = 0L
        )
        
        Mockito.`when`(metricsService.getMonthlyTaskMetrics(month, year)).thenReturn(expectedMetrics)

        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", month.toString())
            param("year", year.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.tasksMetrics[0].total").value(29)
            jsonPath("$.tasksMetrics[0].date").value("2024-02-29")
        }
    }

    @Test
    fun `should verify correct content type and response structure`() {
        // Given
        val month = 1
        val year = 2024
        val expectedMetrics = MonthlyTaskMetricsDTO(
            tasksMetrics = listOf(
                TaskMetricsDTO(
                    tagId = 1L,
                    tag = "Test",
                    total = 5L,
                    status = "completed",
                    date = LocalDate.of(2024, 1, 15)
                )
            ),
            totalCompleted = 5L,
            totalIncomplete = 0L
        )
        
        Mockito.`when`(metricsService.getMonthlyTaskMetrics(month, year)).thenReturn(expectedMetrics)

        // When & Then
        mockMvc.get("/api/metrics/monthly/tasks") {
            param("month", month.toString())
            param("year", year.toString())
        }.andExpect {
            status().isOk()
            content().contentType(MediaType.APPLICATION_JSON)
            jsonPath("$.tasksMetrics").exists()
            jsonPath("$.totalCompleted").exists()
            jsonPath("$.totalIncomplete").exists()
            jsonPath("$.tasksMetrics").isArray()
        }
    }
}
