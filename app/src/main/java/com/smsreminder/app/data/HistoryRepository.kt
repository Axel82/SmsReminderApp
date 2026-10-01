package com.smsreminder.app.data

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.smsreminder.app.model.HistoryLog
import com.smsreminder.app.model.LogStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryRepository(context: Context) {

    private val storage = JsonStorageService(context)
    private val typeToken = object : TypeToken<List<HistoryLog>>() {}

    private val _logs = MutableStateFlow<List<HistoryLog>>(emptyList())
    val logs: StateFlow<List<HistoryLog>> = _logs.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        val loaded = storage.readFromFile(JsonStorageService.FILE_HISTORY, typeToken, emptyList())
        // Sort descending by timestamp
        _logs.value = loaded.sortedByDescending { it.timestamp }
    }

    fun getAllLogs(): List<HistoryLog> {
        return _logs.value
    }

    fun addLog(log: HistoryLog) {
        val updated = listOf(log) + _logs.value
        saveAll(updated)
    }

    fun logAction(
        reminderId: String?,
        reminderTitle: String,
        recipient: String,
        message: String,
        status: LogStatus,
        details: String? = null
    ) {
        val log = HistoryLog(
            reminderId = reminderId,
            reminderTitle = reminderTitle,
            recipient = recipient,
            message = message,
            status = status,
            details = details
        )
        addLog(log)
    }

    fun clearAllLogs() {
        saveAll(emptyList())
    }

    fun deleteLog(id: String) {
        val updated = _logs.value.filterNot { it.id == id }
        saveAll(updated)
    }

    private fun saveAll(list: List<HistoryLog>) {
        _logs.value = list
        storage.writeToFile(JsonStorageService.FILE_HISTORY, list)
    }
}
