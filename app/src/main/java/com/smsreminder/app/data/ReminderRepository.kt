package com.smsreminder.app.data

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.smsreminder.app.model.DayOfWeek
import com.smsreminder.app.model.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReminderRepository(context: Context) {

    private val storage = JsonStorageService(context)
    private val typeToken = object : TypeToken<List<Reminder>>() {}

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        loadReminders()
        // Provide sample reminders if empty on first launch
        if (_reminders.value.isEmpty()) {
            val sampleReminders = listOf(
                Reminder(
                    title = "Rappel Réunion d'équipe",
                    daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                    hour = 9,
                    minute = 0,
                    messageBody = "Bonjour, n'oubliez pas le point d'équipe ce matin à 9h30.",
                    recipientName = "Équipe Projet",
                    recipientPhone = "0600000001",
                    isEnabled = true
                ),
                Reminder(
                    title = "Course & Pain",
                    daysOfWeek = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                    hour = 10,
                    minute = 30,
                    messageBody = "Coucou ! Est-ce que tu as besoin de quelque chose à la boulangerie ?",
                    recipientName = "Marie",
                    recipientPhone = "0600000002",
                    isEnabled = true
                )
            )
            saveAll(sampleReminders)
        }
    }

    private fun loadReminders() {
        _reminders.value = storage.readFromFile(JsonStorageService.FILE_REMINDERS, typeToken, emptyList())
    }

    fun getAllReminders(): List<Reminder> {
        return _reminders.value
    }

    fun getReminderById(id: String): Reminder? {
        return _reminders.value.firstOrNull { it.id == id }
    }

    fun addReminder(reminder: Reminder) {
        val updated = _reminders.value + reminder
        saveAll(updated)
    }

    fun updateReminder(reminder: Reminder) {
        val updated = _reminders.value.map {
            if (it.id == reminder.id) reminder else it
        }
        saveAll(updated)
    }

    fun toggleReminder(id: String, enabled: Boolean) {
        val updated = _reminders.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
        saveAll(updated)
    }

    fun deleteReminder(id: String) {
        val updated = _reminders.value.filterNot { it.id == id }
        saveAll(updated)
    }

    private fun saveAll(list: List<Reminder>) {
        _reminders.value = list
        storage.writeToFile(JsonStorageService.FILE_REMINDERS, list)
    }
}
