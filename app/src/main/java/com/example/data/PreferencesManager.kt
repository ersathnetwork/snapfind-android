package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "snapfind_preferences")

class PreferencesManager(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, LIGHT, DARK
        val DEFAULT_ORG_MODE = stringPreferencesKey("default_org_mode") // AI, MANUAL, TEXT_ONLY
        val CLOUD_CONSENT_GRANTED = booleanPreferencesKey("cloud_consent_granted")
        val DEMO_MODE_ENABLED = booleanPreferencesKey("demo_mode_enabled")
        val COLOR_THEME = stringPreferencesKey("color_theme")
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }

    val themeMode: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM" }

    val defaultOrgMode: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DEFAULT_ORG_MODE] ?: "AI" }

    val cloudConsentGranted: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CLOUD_CONSENT_GRANTED] ?: false }

    val demoModeEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DEMO_MODE_ENABLED] ?: false }

    val colorTheme: Flow<String> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.COLOR_THEME] ?: "VIOLET_BLOOM" }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setDefaultOrgMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_ORG_MODE] = mode
        }
    }

    suspend fun setCloudConsentGranted(granted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_CONSENT_GRANTED] = granted
        }
    }

    suspend fun setDemoModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEMO_MODE_ENABLED] = enabled
        }
    }

    suspend fun setColorTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_THEME] = theme
        }
    }
}
