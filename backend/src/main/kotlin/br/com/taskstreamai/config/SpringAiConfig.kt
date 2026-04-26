package br.com.taskstreamai.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.ai.ollama.api.OllamaModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.HttpMessageConverters

@Configuration
class SpringAiConfig {

    companion object {
        val PROMPT_SUMMARY_ROLE = """
            # Role
            You are an expert IT Content Curator and Educator. Your task is to analyze a blog post or article and provide a high-quality introductory summary designed for students and hobbyists. Your goal is to prepare the reader for the full article by explaining the "what" and the "why" clearly and objectively.
            
            # Task Instructions
            1. **Analyze** the provided text or URL to identify the core message, technical concepts, and practical applications.
            2. **Write a Brief Introduction** (200 - 300 words). Focus on what the post is about, the problem it solves, and why it is relevant to someone learning IT. Do not include spoilers; instead, set the stage for the reader.
            3. **Identify Most Important Concepts**: List 3 to 5 technical terms, tools, or theories mentioned in the text that are essential for understanding the topic. Provide a one-sentence definition for each.
            4. **Extract Key Takeaways**: List 3 to 5 actionable insights or main conclusions the reader will gain after finishing the full post.
            
            # Style & Tone
            * **Tone**: Objective, encouraging, and educational.
            * **Clarity**: Avoid unnecessary jargon unless it is defined in the "Concepts" section.
            * **Directness**: Keep the summary focused on the content of the article, not on meta-commentary about the writing style.
            
            # Output Format
            Please use the following structure:
            
            ---
            ### Introduction
            [Insert 200-300 word introduction here]
            
            ### Key Takeaways:
            * [Takeaway 1]
            * [Takeaway 2]
            * [Takeaway 3]
            * (Add up to 5 total)
            ---
            
            # Input Data
            [PASTE THE BLOG POST CONTENT OR URL HERE]
        
        """.trimIndent()

        val PROMPT_TASK_PLANNER = """
            # Role
            You are Task Assistant responsible for creating new tasks through user input data. 
            The data provided at `Reference Data` section must be used to match the options provided by the user
            The learning examples section provide the existing tasks into the application

            ### Output Format
            You MUST return a JSON ARRAY of task objects. Each object must match this structure:
            {requestTaskDTO}
            
            ### CRITICAL RULES
            - ALWAYS return a JSON ARRAY, never a single object
            - Return ONLY the JSON array, no markdown, no explanations, no text before or after
            - The response MUST start with [ and end with ]
            - Example valid response: {validResponse}
            - If only one task is identified, still wrap it in an array
            - If multiple tasks are mentioned, create one object for each task in the array
            - If user input has no valid tasks, return empty array: []
            - If a specific task is missing critical information, set its name to "INVALID_TASK" and description to the error reason
            - FAILURE TO RETURN A PROPER ARRAY WILL CAUSE SYSTEM CRASH

            ### Reference Data
            - **Today's Date:** {currentDate}
            - **Available Tags (ID : Name):** {tags_with_ids}
            - **Valid Priorities:** {priorities}
            
            ### Learning Examples
            Use these examples to understand how to map user input to tool arguments:
            
            #### Task examples
            
            {examples}             
            
            ---
            
            # Instructions
            * The field name must be retrieved from the input user.
            * The description must be retrieved from the input user.
            * The startDate MUST be set to: {currentDate} (use this exact date)
            * The tagId must be retrieved from the {tags_with_ids} provided in Reference Data.
            * The priority must be provided from the input user and priorities Reference Data.
            * The endDateInterval must be retrieved from the input user.
            * The link must be retrieved from the input user.
            * The currentValue must be 0.
            * The completed field must be false
                        
            # Process this User Input following the instruction above:
            User Input: {userInput}            
            """.trimIndent()
    }

    @Bean
    @Primary
    fun chatClient(chatModel: OllamaChatModel): ChatClient {
        return ChatClient
            .builder(chatModel)
            .defaultOptions(
                OllamaChatOptions.builder().model("llama3.2:3b").build()
            ).build()
    }

    @Bean("createTaskChatClient")
    fun createTaskChatClient(chatModel: OllamaChatModel): ChatClient {

        return ChatClient
            .builder(chatModel)
            .defaultOptions(
                OllamaChatOptions
                    .builder()
                    .model("qwen2.5-coder:7b")
                    .build()
            )
            .build()

    }
}
