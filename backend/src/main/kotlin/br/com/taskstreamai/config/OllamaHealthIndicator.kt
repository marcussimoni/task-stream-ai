package br.com.taskstreamai.config

import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component

@Component("ollama")
class OllamaHealthIndicator(private val ollamaApi: OllamaApi) : HealthIndicator {

    override fun health(): Health = try {
        val response = ollamaApi.listModels()
        
        if (response.models().isNotEmpty()) {
            Health.up()
                .withDetail("model_count", response.models().size)
                .withDetail("active_models", response.models().map { it.name() })
                .build()
        } else {
            Health.down().withDetail("reason", "No models loaded in Ollama").build()
        }
    } catch (e: Exception) {
        Health.down(e)
            .withDetail("error", e.localizedMessage)
            .build()
    }
}