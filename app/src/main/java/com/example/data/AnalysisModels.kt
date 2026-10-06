package com.example.data

data class LinkDto(
    val label: String,
    val url: String,
    val sourceText: String
)

data class DateDto(
    val sourceText: String,
    val label: String,
    val date: String?,
    val time: String?,
    val timezone: String?,
    val needsConfirmation: Boolean,
    val ambiguityReason: String?
)

data class AnalysisResultDto(
    val title: String,
    val summary: String,
    val category: String,
    val tags: List<String>,
    val extractedText: String,
    val links: List<LinkDto>,
    val dates: List<DateDto>,
    val reviewNotes: List<String>
)
