package com.smsreminder.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.smsreminder.app.model.DayOfWeek
import com.smsreminder.app.model.Reminder
import com.smsreminder.app.receiver.ReminderAlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    companion object {
        private const val TAG = "AlarmScheduler"
    }

    fun scheduleReminder(reminder: Reminder) {
        if (!reminder.isEnabled || reminder.daysOfWeek.isEmpty()) {
            cancelReminder(reminder)
            return
        }

        val nextTriggerTime = calculateNextTriggerTime(reminder)
        if (nextTriggerTime <= System.currentTimeMillis()) {
            Log.w(TAG, "Next trigger time is in the past for reminder ${reminder.id}")
            return
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is not available on this device.")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder '${reminder.title}' for epoch: $nextTriggerTime")
        } catch (e: Throwable) {
            Log.e(TAG, "Error while scheduling alarm for reminder ${reminder.id}: ${e.message}", e)
        }
    }

    fun cancelReminder(reminder: Reminder) {
        try {
            val intent = Intent(context, ReminderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled alarm for reminder ${reminder.id}")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error cancelling reminder ${reminder.id}: ${e.message}", e)
        }
    }

    fun rescheduleAllActiveReminders(reminders: List<Reminder>) {
        reminders.forEach { reminder ->
            if (reminder.isEnabled) {
                scheduleReminder(reminder)
            } else {
                cancelReminder(reminder)
            }
        }
    }

    fun cancelAll(reminders: List<Reminder>) {
        reminders.forEach { cancelReminder(it) }
    }

    fun calculateNextTriggerTime(reminder: Reminder): Long {
        val now = Calendar.getInstance()
        var closestTime: Calendar? = null

        // Check each selected day of the week
        for (day in reminder.daysOfWeek) {
            val candidate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminder.hour)
                set(Calendar.MINUTE, reminder.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.DAY_OF_WEEK, day.calendarDay)
            }

            // If candidate day/time is before or equal to now, advance 1 week (7 days)
            if (candidate.timeInMillis <= now.timeInMillis) {
                candidate.add(Calendar.DAY_OF_YEAR, 7)
            }

            if (closestTime == null || candidate.timeInMillis < closestTime.timeInMillis) {
                closestTime = candidate
            }
        }

        return closestTime?.timeInMillis ?: (System.currentTimeMillis() + 60_000)
    }
}
