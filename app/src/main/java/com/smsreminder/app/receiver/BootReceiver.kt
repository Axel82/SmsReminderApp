package com.smsreminder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smsreminder.app.SmsReminderApplication

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "BootReceiver received action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val app = context.applicationContext as? SmsReminderApplication ?: return
            val settings = app.settingsRepository.getSettings()

            if (settings.isGlobalEnabled) {
                val activeReminders = app.reminderRepository.getAllReminders().filter { it.isEnabled }
                app.alarmScheduler.rescheduleAllActiveReminders(activeReminders)
                Log.d(TAG, "Successfully rescheduled ${activeReminders.size} active reminders after boot.")
            } else {
                Log.d(TAG, "Global reminders are disabled; no alarms scheduled after boot.")
            }
        }
    }
}
