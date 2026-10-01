package com.smsreminder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smsreminder.app.SmsReminderApplication
import com.smsreminder.app.service.NotificationHelper

class ReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderAlarmReceiver"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
        Log.d(TAG, "Received alarm for reminderId: $reminderId")

        val app = context.applicationContext as? SmsReminderApplication ?: return
        val settings = app.settingsRepository.getSettings()

        // Check if global reminders are active
        if (!settings.isGlobalEnabled) {
            Log.d(TAG, "Global reminders are OFF. Notification suppressed.")
            return
        }

        val reminder = app.reminderRepository.getReminderById(reminderId)
        if (reminder == null) {
            Log.w(TAG, "Reminder with id $reminderId not found.")
            return
        }

        if (!reminder.isEnabled) {
            Log.d(TAG, "Reminder '${reminder.title}' is disabled.")
            return
        }

        // Show Notification with Action buttons
        NotificationHelper.showReminderNotification(context, reminder)

        // Reschedule the next occurrence (e.g. next week or next selected day)
        app.alarmScheduler.scheduleReminder(reminder)
    }
}
