package br.com.taskstreamai.repository

import br.com.taskstreamai.dto.TaskGroupedByTagDTO
import br.com.taskstreamai.dto.TaskMetricsDTO
import br.com.taskstreamai.dto.TasksByTagDTO
import br.com.taskstreamai.model.Tag
import br.com.taskstreamai.model.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    
    fun findByTag(tag: Tag): List<Task>

    fun findByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate: LocalDate, startDate: LocalDate): List<Task>

    @Query("""
            SELECT t FROM Task t 
            INNER JOIN t.tag tag
            WHERE t.completed = :completed
            AND (:tag IS NULL OR tag.id = :tag)
            order by t.name ASC
        """)
    fun findAllTasks(
        @Param("completed") completed: Boolean,
        @Param("tag") tag: Long?
    ): List<Task>

    @Query(value = """
        select * from tasks t 
        where t.tag_id = :id 
        and t.completed = false
        and t.priority = :priority
        order by t.start_date asc
        limit 1;
        """, nativeQuery = true)
    fun findByTagAndPriority(@Param("id") id: Long, @Param("priority") priority: String): Optional<Task>

    @Query(value ="""
        select tag.id as tagId, tag.name as tag, count(t.id) as total, 'completed' as status, t.start_date as date  from tasks t
        inner join tags tag on tag.id = t.tag_id 
        where t.completed = true 
        and (t.start_date between :startDate and :endDate or t.end_date between :startDate and :endDate)
        group by tag.name, t.start_date, tag.id 
        union
        select tag.id as tagId, tag.name as tag, count(t.id) as total, 'incompleted' as status, t.start_date as date  from tasks t
        inner join tags tag on tag.id = t.tag_id 
        where t.completed = false 
        and (t.start_date between :startDate and :endDate or t.end_date between :startDate and :endDate)
        group by tag.name, t.start_date, tag.id 
        order by tag
        """, nativeQuery = true)
    fun monthlyTasks(@Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): List<TaskMetricsDTO>

    @Query(value = """
        select t.* from tasks t
        where t.summary is not null
        and t.description is not null
        limit :total;
    """, nativeQuery = true)
    fun findTaskByDates(@Param("total") total: Int): List<Task>

    @Query("""
        select tag.id, tag.name as tag, count(task.id) as total from TASKS task inner join TAGS tag on task.tag_id = tag.id
        where (task.start_date between :startDate and :endDate)
        and (task.end_date between :startDate and :endDate)
        group by tag.name
        order by total desc
        """, nativeQuery = true)
    fun countTasksByTag(@Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): List<TaskGroupedByTagDTO>

    @Query("""
        select task.id, task.name as task, task.description, task.start_date as startDate, task.end_date as endDate, tag.name as tag, task.priority, task.completed, task.current_value as currentvalue from TASKS task inner join TAGS tag on task.tag_id = tag.id
        where (task.start_date between :startDate and :endDate)
        and (task.end_date between :startDate and :endDate)
        and tag.id = :tagId
        order by task.start_date asc
    """, nativeQuery = true)
    fun getTasksByTagAndDate(@Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate, @Param("tagId") tagId: Long): List<TasksByTagDTO>

}
