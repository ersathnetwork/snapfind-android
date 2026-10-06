package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.data.ScreenshotDao
import com.example.data.ScreenshotEntity
import com.example.data.ScreenshotRepository
import com.example.data.SnapFindDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption { NEWEST_FIRST, OLDEST_FIRST, RECENTLY_UPDATED }
enum class ViewMode { GRID, LIST }

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = SnapFindDatabase.getDatabase(application).screenshotDao()
    private val repository = ScreenshotRepository(dao, application)
    private val prefsManager = PreferencesManager(application)

    val onboardingCompleted: StateFlow<Boolean> = prefsManager.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<String> = prefsManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val cloudConsent: StateFlow<Boolean> = prefsManager.cloudConsentGranted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val defaultOrgMode: StateFlow<String> = prefsManager.defaultOrgMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "AI")

    val demoModeEnabled: StateFlow<Boolean> = prefsManager.demoModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val colorTheme: StateFlow<String> = prefsManager.colorTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "VIOLET_BLOOM")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _sortOption = MutableStateFlow(SortOption.NEWEST_FIRST)
    val sortOption: StateFlow<SortOption> = _sortOption

    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode

    private val realScreenshots: StateFlow<List<ScreenshotEntity>> = repository.allScreenshots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val demoScreenshotsList = listOf(
        ScreenshotEntity(
            id = "demo_1",
            localImagePath = "",
            contentHash = "demo_hash_1",
            title = "Design Workshop 2026",
            summary = "An upcoming workshop invitation with visible event details and venue.",
            category = "Events",
            tagsJson = "[\"workshop\", \"design\"]",
            extractedText = "Join us for the Annual Design System Workshop on Friday at 6 PM. Location: Tech Hub Hall A.",
            linksJson = "[]",
            datesJson = "[]",
            reviewNotesJson = "[]",
            isFavorite = true,
            importTimestamp = System.currentTimeMillis() - 3600000L,
            updateTimestamp = System.currentTimeMillis(),
            processingState = "READY",
            errorMessage = null,
            hasReminder = true,
            reminderTitle = "Design Workshop",
            reminderTime = System.currentTimeMillis() + 86400000L,
            reminderTimingOption = "1D_BEFORE",
            reminderStatus = "SCHEDULED"
        ),
        ScreenshotEntity(
            id = "demo_2",
            localImagePath = "",
            contentHash = "demo_hash_2",
            title = "Minimalist Coffee Table",
            summary = "Sleek oak wood coffee table from modern furniture store.",
            category = "Shopping",
            tagsJson = "[\"furniture\", \"oak\"]",
            extractedText = "Nordic Oak Coffee Table - $249.99. Free shipping on orders over $50.",
            linksJson = "[]",
            datesJson = "[]",
            reviewNotesJson = "[]",
            isFavorite = false,
            importTimestamp = System.currentTimeMillis() - 7200000L,
            updateTimestamp = System.currentTimeMillis(),
            processingState = "READY",
            errorMessage = null,
            hasReminder = false,
            reminderTitle = null,
            reminderTime = null,
            reminderTimingOption = null,
            reminderStatus = null
        ),
        ScreenshotEntity(
            id = "demo_3",
            localImagePath = "",
            contentHash = "demo_hash_3",
            title = "Kyoto Travel Itinerary",
            summary = "Flight booking confirmation and hotel details for Kyoto trip.",
            category = "Travel",
            tagsJson = "[\"japan\", \"flight\"]",
            extractedText = "Flight JL042 departs Nov 12 at 10:30 AM. Hotel Granvia Kyoto booking confirmed.",
            linksJson = "[]",
            datesJson = "[]",
            reviewNotesJson = "[]",
            isFavorite = true,
            importTimestamp = System.currentTimeMillis() - 14400000L,
            updateTimestamp = System.currentTimeMillis(),
            processingState = "READY",
            errorMessage = null,
            hasReminder = false,
            reminderTitle = null,
            reminderTime = null,
            reminderTimingOption = null,
            reminderStatus = null
        ),
        ScreenshotEntity(
            id = "demo_4",
            localImagePath = "",
            contentHash = "demo_hash_4",
            title = "Avocado Toast Recipe",
            summary = "Quick healthy breakfast recipe with poached eggs.",
            category = "Recipes",
            tagsJson = "[\"breakfast\", \"healthy\"]",
            extractedText = "Ingredients: Sourdough bread, ripe avocado, 2 eggs, lemon juice, chili flakes.",
            linksJson = "[]",
            datesJson = "[]",
            reviewNotesJson = "[]",
            isFavorite = false,
            importTimestamp = System.currentTimeMillis() - 28800000L,
            updateTimestamp = System.currentTimeMillis(),
            processingState = "READY",
            errorMessage = null,
            hasReminder = false,
            reminderTitle = null,
            reminderTime = null,
            reminderTimingOption = null,
            reminderStatus = null
        )
    )

    val allScreenshots: StateFlow<List<ScreenshotEntity>> = combine(
        realScreenshots,
        prefsManager.demoModeEnabled
    ) { real, demoEnabled ->
        if (demoEnabled) {
            // Merge real and demo, avoiding duplicate IDs
            val realIds = real.map { it.id }.toSet()
            real + demoScreenshotsList.filter { it.id !in realIds }
        } else {
            real
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteScreenshots: StateFlow<List<ScreenshotEntity>> = allScreenshots
        .combine(prefsManager.demoModeEnabled) { list, _ -> list.filter { it.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredScreenshots = combine(
        allScreenshots,
        _searchQuery,
        _selectedCategory,
        _sortOption
    ) { screenshots, query, category, sort ->
        var list = screenshots

        // Category filter
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Search query filter
        if (query.isNotBlank()) {
            val q = query.lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.summary.lowercase().contains(q) ||
                        it.extractedText.lowercase().contains(q) ||
                        it.tagsJson.lowercase().contains(q)
            }
        }

        // Sorting
        when (sort) {
            SortOption.NEWEST_FIRST -> list.sortedByDescending { it.importTimestamp }
            SortOption.OLDEST_FIRST -> list.sortedBy { it.importTimestamp }
            SortOption.RECENTLY_UPDATED -> list.sortedByDescending { it.updateTimestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
    }

    fun toggleFavorite(entity: ScreenshotEntity) {
        if (entity.id.startsWith("demo_")) return // demo items read-only
        viewModelScope.launch {
            repository.updateScreenshot(entity.copy(isFavorite = !entity.isFavorite))
        }
    }

    fun deleteScreenshot(entity: ScreenshotEntity) {
        if (entity.id.startsWith("demo_")) return
        viewModelScope.launch {
            repository.deleteScreenshot(entity)
        }
    }

    fun setDemoModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsManager.setDemoModeEnabled(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            prefsManager.setOnboardingCompleted(true)
        }
    }

    fun setCloudConsent(granted: Boolean) {
        viewModelScope.launch {
            prefsManager.setCloudConsentGranted(granted)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefsManager.setThemeMode(mode)
        }
    }

    fun setColorTheme(theme: String) {
        viewModelScope.launch {
            prefsManager.setColorTheme(theme)
        }
    }

    fun setDefaultOrgMode(mode: String) {
        viewModelScope.launch {
            prefsManager.setDefaultOrgMode(mode)
        }
    }

    suspend fun saveImageAndAnalyze(uri: android.net.Uri, mode: String): Pair<String, String>? {
        return repository.saveImportedImage(uri)
    }
}
