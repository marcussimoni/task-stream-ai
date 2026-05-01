package br.com.taskstreamai.service

import br.com.taskstreamai.dto.SiteDTO
import br.com.taskstreamai.dto.TaskDTO
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WebScraperService {

    private val logger = LoggerFactory.getLogger(WebScraperService::class.java)

    fun loadSiteFromUrl(task: TaskDTO): SiteDTO? {
        if (isValidLink(task.link)) {

            logger.info("Informed link: ${task.link}")
            val site = loadSiteFromUrl(task.link!!)

            val title = site.title()
            val content = cleanHtmlForAi(site)

            logger.info("Site loaded. starting the summary task: $title")

            return SiteDTO(title, content)

        }
        return null;
    }

    fun loadContentSiteFromUrl(link: String): SiteDTO? {
        val site = loadSiteFromUrl(link)
        return SiteDTO(site.title(), cleanHtmlForAi(site))
    }

    fun loadSiteFromUrl(link: String): Document {

        require(link.startsWith("http://") || link.startsWith("https://")) {
            "Only HTTP/HTTPS URLs allowed"
        }

        // Basic private IP check
        val lower = link.lowercase()
        require(!lower.contains("localhost") &&
                !lower.contains("127.0.0.1") &&
                !lower.contains("192.168.") &&
                !lower.contains("10.") &&
                !lower.contains("172.16.")) {
            "Private addresses not allowed"
        }

        val baseDomain = extractDomainFromLink(link)
        return Jsoup
            .connect(link)
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:149.0) Gecko/20100101 Firefox/149.0")
            .referrer(baseDomain!!)
            .header("Accept-Language", "en-US,en;q=0.9")
            .timeout(10000)
            .maxBodySize(5 * 1024 * 1024)
            .followRedirects(true)
            .get()
    }

    private fun cleanHtmlForAi(html: Document): String {

        html.select("script, style, noscript, svg, header, footer, nav").remove()

        val cleanText = html.body().text()

        return cleanText.replace(Regex("\\s+"), " ").trim()
    }

    private fun isValidLink(link: String?): Boolean {
        return link != null && !StringUtil.isBlank(link) && (link.startsWith("http") || link.startsWith("https"))
    }

    private fun extractDomainFromLink(link: String): String? {
        val regex = Regex("""^(https?://[^/?#]+)""")
        return regex.find(link)?.value
    }

}