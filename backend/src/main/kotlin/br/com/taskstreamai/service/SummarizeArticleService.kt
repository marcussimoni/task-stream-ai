package br.com.taskstreamai.service

import br.com.taskstreamai.config.SpringAiConfig
import br.com.taskstreamai.dto.SiteDTO
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.repository.TaskRepository
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SummarizeArticleService(
    private val aiAssistantService: AiAssistantService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun createTaskSummary(task: TaskDTO, site: SiteDTO?): String? {
        return aiAssistantService.createTaskSummary(site!!.content)
    }
}