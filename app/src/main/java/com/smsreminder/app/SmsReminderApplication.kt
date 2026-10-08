package com.smsreminder.app

import android.app.Application
import android.util.Log
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

    @Volatile
    private var backgroundWorkReady = false

    override fun onCreate() {
        super.onCreate()
        instance = this

        reminderRepository = ReminderRepository(this)
        historyRepository = HistoryRepository(this)
        settingsRepository = SettingsRepository(this)
        alarmScheduler = AlarmScheduler(this)
    }

    /**
     * Channel + alarms must not run before the UI is visible.
     * Creating a HIGH notification channel or scheduling exact alarms at process
     * start can open a system screen and send MainActivity to the background.
     */
    fun initializeBackgroundWorkIfNeeded() {
        if (backgroundWorkReady) return
        synchronized(this) {
            if (backgroundWorkReady) return
            try {
                NotificationHelper.createNotificationChannel(this)
                if (settingsRepository.getSettings().isGlobalEnabled) {
                    alarmScheduler.rescheduleAllActiveReminders(reminderRepository.getAllReminders())
                }
            } catch (e: Throwable) {
                Log.e("SmsReminderApp", "Error initializing background work: ${e.message}", e)
            }
            backgroundWorkReady = true
        }
    }

    companion object {
        lateinit var instance: SmsReminderApplication
            private set
    }
}
