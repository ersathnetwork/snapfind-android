package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.UUID

class ScreenshotRepository(private val dao: ScreenshotDao, private val context: Context) {

    val allScreenshots: Flow<List<ScreenshotEntity>> = dao.getAllScreenshots()
    val favoriteScreenshots: Flow<List<ScreenshotEntity>> = dao.getFavoriteScreenshots()

    suspend fun getScreenshotById(id: String): ScreenshotEntity? = dao.getScreenshotById(id)

    suspend fun findByHash(hash: String): ScreenshotEntity? = dao.getByHash(hash)

    suspend fun saveImportedImage(uri: Uri): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            // Compute SHA-256 hash
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            val hash = hashBytes.joinToString("") { "%02x".format(it) }

            // Check duplicate
            val existing = dao.getByHash(hash)
            if (existing != null) {
                return@withContext Pair(existing.localImagePath, hash)
            }

            val screenshotsDir = File(context.filesDir, "screenshots").apply { mkdirs() }
            val fileName = "screenshot_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val file = File(screenshotsDir, fileName)
            file.writeBytes(bytes)

            Pair(file.absolutePath, hash)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveScreenshotRecord(
        id: String,
        localImagePath: String,
        contentHash: String,
        analysis: AnalysisResultDto,
        isFavorite: Boolean = false,
        hasReminder: Boolean = false,
        reminderTitle: String? = null,
        reminderTime: Long? = null,
        reminderTimingOption: String? = null,
        reminderStatus: String? = null
    ) = withContext(Dispatchers.IO) {
        val tagsJson = JSONArray(analysis.tags).toString()

        val linksJson = JSONArray().apply {
            analysis.links.forEach { link ->
                put(JSONObject().apply {
                    put("label", link.label)
                    put("url", link.url)
                    put("sourceText", link.sourceText)
                })
            }
        }.toString()

        val datesJson = JSONArray().apply {
            analysis.dates.forEach { date ->
                put(JSONObject().apply {
                    put("sourceText", date.sourceText)
                    put("label", date.label)
                    put("date", date.date ?: JSONObject.NULL)
                    put("time", date.time ?: JSONObject.NULL)
                    put("timezone", date.timezone ?: JSONObject.NULL)
                    put("needsConfirmation", date.needsConfirmation)
                    put("ambiguityReason", date.ambiguityReason ?: JSONObject.NULL)
                })
            }
        }.toString()

        val reviewNotesJson = JSONArray(analysis.reviewNotes).toString()

        val entity = ScreenshotEntity(
            id = id,
            localImagePath = localImagePath,
            contentHash = contentHash,
            title = analysis.title,
            summary = analysis.summary,
            category = analysis.category,
            tagsJson = tagsJson,
            extractedText = analysis.extractedText,
            linksJson = linksJson,
            datesJson = datesJson,
            reviewNotesJson = reviewNotesJson,
            isFavorite = isFavorite,
            importTimestamp = System.currentTimeMillis(),
            updateTimestamp = System.currentTimeMillis(),
            processingState = "READY",
            errorMessage = null,
            hasReminder = hasReminder,
            reminderTitle = reminderTitle,
            reminderTime = reminderTime,
            reminderTimingOption = reminderTimingOption,
            reminderStatus = reminderStatus
        )
        dao.insertScreenshot(entity)
    }

    suspend fun updateScreenshot(entity: ScreenshotEntity) = withContext(Dispatchers.IO) {
        dao.updateScreenshot(entity.copy(updateTimestamp = System.currentTimeMillis()))
    }

    suspend fun deleteScreenshot(entity: ScreenshotEntity) = withContext(Dispatchers.IO) {
        try {
            val file = File(entity.localImagePath)
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
        dao.deleteScreenshot(entity)
    }

    suspend fun insertDemoRecords() = withContext(Dispatchers.IO) {
        // Create demo sample images or empty placeholder paths if images don't exist
        val dummyPath = ""
        val demoList = listOf(
            AnalysisResultDto(
                title = "Design Workshop 2026",
                summary = "An upcoming workshop invitation with visible event details and venue.",
                category = "Events",
                tags = listOf("workshop", "design", "ux"),
                extractedText = "Join us for the Annual Design System Workshop on Friday at 6 PM. Location: Tech Hub Hall A, 500 Market St.",
                links = listOf(LinkDto("Registration", "https://example.com/register", "https://example.com/register")),
                dates = listOf(DateDto("Friday at 6 PM", "Design Workshop", "2026-10-09", "18:00", null, true, "Verify year if needed.")),
                reviewNotes = listOf("Confirm event time before scheduling reminder.")
            ),
            AnalysisResultDto(
                title = "Minimalist Coffee Table",
                summary = "Sleek oak wood coffee table from modern furniture store.",
                category = "Shopping",
                tags = listOf("furniture", "livingroom", "oak"),
                extractedText = "Nordic Oak Coffee Table - $249.99. Free shipping on orders over $50. In stock at Modern Living.",
                links = listOf(LinkDto("Store Page", "https://example.com/shop", "https://example.com/shop")),
                dates = emptyList(),
                reviewNotes = listOf("Price verified from screenshot.")
            ),
            AnalysisResultDto(
                title = "Kyoto Travel Itinerary",
                summary = "Flight booking confirmation and hotel details for Kyoto trip.",
                category = "Travel",
                tags = listOf("japan", "flight", "hotel"),
                extractedText = "Flight JL042 departs Nov 12 at 10:30 AM. Hotel Granvia Kyoto booking confirmed for 4 nights.",
                links = listOf(LinkDto("Airline", "https://example.com/flight", "https://example.com/flight")),
                dates = listOf(DateDto("Nov 12 at 10:30 AM", "Flight JL042", "2026-11-12", "10:30", "JST", false, null)),
                reviewNotes = listOf("Check baggage allowance.")
            ),
            AnalysisResultDto(
                title = "Avocado Toast Recipe",
                summary = "Quick healthy breakfast recipe with poached eggs.",
                category = "Recipes",
                tags = listOf("breakfast", "healthy", "avocado"),
                extractedText = "Ingredients: Sourdough bread, ripe avocado, 2 eggs, lemon juice, chili flakes, salt and pepper. Toast bread, mash avocado with lemon juice, top with poached egg.",
                links = emptyList(),
                dates = emptyList(),
                reviewNotes = listOf("Delicious and quick to make.")
            ),
            AnalysisResultDto(
                title = "Machine Learning Study Notes",
                summary = "Key concepts on transformer attention mechanisms.",
                category = "Study",
                tags = listOf("ai", "ml", "transformers"),
                extractedText = "Self-attention computes dot products of queries and keys, divided by square root of d_k, followed by softmax.",
                links = emptyList(),
                dates = emptyList(),
                reviewNotes = usefulNotes()
            )
        )

        demoList.forEachIndexed { index, analysis ->
            val id = "demo_${index}_${UUID.randomUUID().toString().take(6)}"
            val entity = ScreenshotEntity(
                id = id,
                localImagePath = dummyPath,
                contentHash = "hash_$id",
                title = analysis.title,
                summary = analysis.summary,
                category = analysis.category,
                tagsJson = JSONArray(analysis.tags).toString(),
                extractedText = analysis.extractedText,
                linksJson = "[]",
                datesJson = "[]",
                reviewNotesJson = "[]",
                isFavorite = index == 0,
                importTimestamp = System.currentTimeMillis() - (index * 3600000L),
                updateTimestamp = System.currentTimeMillis(),
                processingState = "READY",
                errorMessage = null,
                hasReminder = false,
                reminderTitle = null,
                reminderTime = null,
                reminderTimingOption = null,
                reminderStatus = null
            )
            dao.insertScreenshot(entity)
        }
    }

    private fun usefulNotes(): List<String> = listOf("Review before exam.")
}
