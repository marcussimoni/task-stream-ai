package br.com.taskstreamai.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EstimatedTimeDTO(
    @JsonProperty("totalWordCount")
    val totalWordCount: Int,

    @JsonProperty("technicalDepth")
    val technicalDepth: String,

    @JsonProperty("estimatedReadingTimeMinutes")
    val estimatedReadingTimeMinutes: Int,

    @JsonProperty("depthJustification")
    val depthJustification: String,

    @JsonProperty("recommendedPace")
    val recommendedPace: String
)