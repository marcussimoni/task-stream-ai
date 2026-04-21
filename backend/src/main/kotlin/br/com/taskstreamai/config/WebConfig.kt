package br.com.taskstreamai.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    
    override fun addViewControllers(registry: ViewControllerRegistry) {
        // Forward all non-API routes to index.html for Angular routing
        registry.addViewController("/").setViewName("forward:/index.html")
        registry.addViewController("/tasks").setViewName("forward:/index.html")
        registry.addViewController("/task-types").setViewName("forward:/index.html")
        registry.addViewController("/monthly-overview").setViewName("forward:/index.html")
        registry.addViewController("/tags").setViewName("forward:/index.html")
        registry.addViewController("/metrics").setViewName("forward:/index.html")
        registry.addViewController("/weekly-calendar").setViewName("forward:/index.html")
        registry.addViewController("/database-backup").setViewName("forward:/index.html")
        registry.addViewController("/application-logs").setViewName("forward:/index.html")
    }

}
