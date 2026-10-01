package com.smsreminder.app.data

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.smsreminder.app.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val storage = JsonStorageService(context)
    private val typeToken = object : TypeToken<AppSettings>() {}

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _settings.value = storage.readFromFile(JsonStorageService.FILE_SETTINGS, typeToken, AppSettings())
    }

    fun getSettings(): AppSettings {
        return _settings.value
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings.copy(lastUpdated = System.currentTimeMillis())
        storage.writeToFile(JsonStorageService.FILE_SETTINGS, _settings.value)
    }

    fun setGlobalEnabled(enabled: Boolean) {
        updateSettings(_settings.value.copy(isGlobalEnabled = enabled))
    }

    fun setSendDirectly(sendDirectly: Boolean) {
        updateSettings(_settings.value.copy(sendDirectlyViaSmsManager = sendDirectly))
    }
}
