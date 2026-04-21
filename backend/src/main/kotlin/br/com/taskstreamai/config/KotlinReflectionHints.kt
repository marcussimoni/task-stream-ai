package br.com.taskstreamai.config

import org.slf4j.LoggerFactory
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints


class KotlinReflectionHints : RuntimeHintsRegistrar {

    private val logger = LoggerFactory.getLogger(KotlinReflectionHints::class.java)

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        // Register the missing internal Kotlin class
        try {
            val emptyListClass = Class.forName("kotlin.collections.EmptyList")
            hints.reflection().registerType(emptyListClass)

            val deserializerClass = Class.forName("org.springframework.ai.ollama.api.ThinkOption\$ThinkOptionDeserializer")
            hints.reflection().registerType(deserializerClass) { builder ->
                builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS
                )
            }
        } catch (e: ClassNotFoundException) {
            logger.error("Class could not be found ", e)
        }

    }
}

@Configuration
@ImportRuntimeHints(KotlinReflectionHints::class)
class NativeConfig {
    // This tells Spring to apply the hints during the native compile phase
}