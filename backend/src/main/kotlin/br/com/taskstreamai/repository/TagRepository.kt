package br.com.taskstreamai.repository

import br.com.taskstreamai.model.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<Tag, Long> {

    fun findByNameContainingIgnoreCase(name: String): List<Tag>

    @Query("SELECT t FROM Tag t WHERE t.id IN :ids")
    fun findByIds(@Param("ids") ids: List<Long>): List<Tag>

    @Query("SELECT t FROM Tag t ORDER BY t.name ASC")
    fun findAllTags() : List<Tag>

}
