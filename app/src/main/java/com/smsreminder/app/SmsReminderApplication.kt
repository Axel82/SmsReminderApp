package com.smsreminder.app

import android.app.Application
import com.smsreminder.app.data.HistoryRepository
import com.smsreminder.app.data.ReminderRepository
import com.smsreminder.app.data.SettingsRepository
import com.smsreminder.app.service.AlarmScheduler
import com.smsreminder.app.service.NotificationHelper

class SmsReminderApplication : Application() {

    lateinit var reminderRepository: ReminderRepository
        private set
    lateinit var historyRepository: HistoryRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var alarmScheduler: AlarmScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        // Initialize repositories
        reminderRepository = ReminderRepository(this)
        historyRepository = HistoryRepository(this)
        settingsRepository = SettingsRepository(this)
        alarmScheduler = AlarmScheduler(this)

        // Reschedule alarms if global service is enabled
        if (settingsRepository.getSettings().isGlobalEnabled) {
            alarmScheduler.rescheduleAllActiveReminders(reminderRepository.getAllReminders())
        }
    }

    companion object {
        lateinit var instance: SmsReminderApplication
            private set
    }
}
