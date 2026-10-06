package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey
    val id: String,
    val localImagePath: String,
    val contentHash: String,
    val title: String,
    val summary: String,
    val category: String, // Shopping, Events, Travel, Study, Recipes, Work, Other
    val tagsJson: String, // JSON array string
    val extractedText: String,
    val linksJson: String, // JSON array string of LinkDto
    val datesJson: String, // JSON array string of DateDto
    val reviewNotesJson: String, // JSON array string
    val isFavorite: Boolean,
    val importTimestamp: Long,
    val updateTimestamp: Long,
    val processingState: String, // READY, PROCESSING, ERROR
    val errorMessage: String?,
    val hasReminder: Boolean,
    val reminderTitle: String?,
    val reminderTime: Long?,
    val reminderTimingOption: String?, // AT_TIME, 15M_BEFORE, 1H_BEFORE, 1D_BEFORE
    val reminderStatus: String? // SCHEDULED, DELIVERED, CANCELLED
)
