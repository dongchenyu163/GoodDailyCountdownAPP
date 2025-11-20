package com.dlx.smartalarm.demo

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AppSettingsManager {
    private const val SETTINGS_FILE_NAME = "app_settings.json"

    private val json = Json { prettyPrint = true }

    fun loadSettings(): AppSettings {
        return try {
            val jsonString = readTextFile(SETTINGS_FILE_NAME)
            jsonString?.let {
                json.decodeFromString<AppSettings>(it)
            } ?: AppSettings()
        } catch (e: Exception) {
            println("Error loading app settings: ${e.message}")
            AppSettings() // Return default settings on error
        }
    }

    fun saveSettings(settings: AppSettings) {
        try {
            val jsonString = json.encodeToString(settings)
            writeTextFile(SETTINGS_FILE_NAME, jsonString)
        } catch (e: Exception) {
            println("Error saving app settings: ${e.message}")
        }
    }
}
