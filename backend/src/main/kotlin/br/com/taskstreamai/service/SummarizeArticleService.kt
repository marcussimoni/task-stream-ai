package br.com.taskstreamai.service

import br.com.taskstreamai.config.SpringAiConfig
import br.com.taskstreamai.dto.TaskDTO
import br.com.taskstreamai.repository.TaskRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SummarizeArticleService(
    private val chatClient: ChatClient,
    private val taskRepository: TaskRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createTaskSummary(task: TaskDTO) {

        if (task.link != null) {

            logger.info("Informed link: ${task.link}")
            val site = loadSiteFromUrl(task.link)

            val title = site.title()
            val content = cleanHtmlForAi(site)

            logger.info("Site loaded. starting the summary task: $title")

            val summary = chatClient
                .prompt()
                .system(SpringAiConfig.PROMPT)
                .user(content)
                .call().content()

            logger.info("Summary completed. Updating task: ${task.id}")

            taskRepository.findById(task.id).ifPresent {
                it.summary = summary
                taskRepository.save(it)
                logger.info("Task ${task.id} updated successfully.")
            }

            logger.info("Summary completed exiting method.")

        }

    }

    fun loadSiteFromUrl(link: String) : Document {
        val baseDomain = extractDomainFromLink(link)
        return Jsoup
            .connect(link)
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:149.0) Gecko/20100101 Firefox/149.0")
            .referrer(baseDomain!!)
            .header("Accept-Language", "en-US,en;q=0.9")
            .followRedirects(true)
            .get();
    }

    private fun extractDomainFromLink(link: String): String? {
        val regex = Regex("""^(https?://[^/?#]+)""")
        return regex.find(link)?.value
    }

    fun cleanHtmlForAi(html: Document): String {

        // 2. Remove non-visible elements that contain code/metadata
        // This targets <script>, <style>, <noscript>, <svg>, etc.
        html.select("script, style, noscript, svg, header, footer, nav").remove()

        // 3. Get the text content
        // Jsoup's .text() method naturally ignores tags and returns
        // what a human would actually see on the page.
        val cleanText = html.body().text()

        // 4. Optional: Basic whitespace normalization
        return cleanText.replace(Regex("\\s+"), " ").trim()
    }
}