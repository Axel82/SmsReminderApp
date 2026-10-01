package com.smsreminder.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smsreminder.app.R
import com.smsreminder.app.model.Reminder
import com.smsreminder.app.receiver.SmsActionReceiver
import com.smsreminder.app.ui.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "sms_reminders_channel"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(context: Context, reminder: Reminder) {
        val notificationId = reminder.id.hashCode()

        // Intent to open Main Activity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "Envoyer"
        val sendIntent = Intent(context, SmsActionReceiver::class.java).apply {
            action = SmsActionReceiver.ACTION_SEND_SMS
            putExtra(SmsActionReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(SmsActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(SmsActionReceiver.EXTRA_TITLE, reminder.title)
            putExtra(SmsActionReceiver.EXTRA_RECIPIENT_NAME, reminder.recipientName)
            putExtra(SmsActionReceiver.EXTRA_RECIPIENT_PHONE, reminder.recipientPhone)
            putExtra(SmsActionReceiver.EXTRA_MESSAGE, reminder.messageBody)
        }
        val sendPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            sendIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "Annuler"
        val cancelIntent = Intent(context, SmsActionReceiver::class.java).apply {
            action = SmsActionReceiver.ACTION_CANCEL_SMS
            putExtra(SmsActionReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(SmsActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(SmsActionReceiver.EXTRA_TITLE, reminder.title)
            putExtra(SmsActionReceiver.EXTRA_RECIPIENT_NAME, reminder.recipientName)
            putExtra(SmsActionReceiver.EXTRA_RECIPIENT_PHONE, reminder.recipientPhone)
            putExtra(SmsActionReceiver.EXTRA_MESSAGE, reminder.messageBody)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val recipientLabel = if (reminder.recipientName.isNotBlank() && reminder.recipientName != reminder.recipientPhone) {
            "${reminder.recipientName} (${reminder.recipientPhone})"
        } else {
            reminder.recipientPhone
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("SMS pour $recipientLabel")
            .setContentText(reminder.messageBody)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Destinataire : $recipientLabel\n\nMessage :\n${reminder.messageBody}")
                    .setSummaryText(reminder.title)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_notification, context.getString(R.string.action_send), sendPendingIntent)
            .addAction(R.drawable.ic_notification, context.getString(R.string.action_cancel), cancelPendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationHelper", "Notification permission missing: ${e.message}")
        }
    }

    fun dismissNotification(context: Context, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }
}
