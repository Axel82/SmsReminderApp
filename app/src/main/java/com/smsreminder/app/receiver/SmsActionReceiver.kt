package com.smsreminder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.smsreminder.app.SmsReminderApplication
import com.smsreminder.app.model.LogStatus
import com.smsreminder.app.service.NotificationHelper
import com.smsreminder.app.service.SmsSender

class SmsActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsActionReceiver"
        const val ACTION_SEND_SMS = "com.smsreminder.app.ACTION_SEND_SMS"
        const val ACTION_CANCEL_SMS = "com.smsreminder.app.ACTION_CANCEL_SMS"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_RECIPIENT_NAME = "extra_recipient_name"
        const val EXTRA_RECIPIENT_PHONE = "extra_recipient_phone"
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? SmsReminderApplication
        val historyRepo = app?.historyRepository
        val settingsRepo = app?.settingsRepository

        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Rappel SMS"
        val recipientName = intent.getStringExtra(EXTRA_RECIPIENT_NAME) ?: ""
        val recipientPhone = intent.getStringExtra(EXTRA_RECIPIENT_PHONE) ?: ""
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""

        val displayRecipient = if (recipientName.isNotBlank() && recipientName != recipientPhone) {
            "$recipientName ($recipientPhone)"
        } else {
            recipientPhone
        }

        // Dismiss notification
        if (notificationId != 0) {
            NotificationHelper.dismissNotification(context, notificationId)
        }

        when (intent.action) {
            ACTION_SEND_SMS -> {
                Log.d(TAG, "Action SEND triggered for $recipientPhone")
                val isDirectSend = settingsRepo?.getSettings()?.sendDirectlyViaSmsManager == true

                if (isDirectSend) {
                    val success = SmsSender.sendDirectSms(context, recipientPhone, message)
                    if (success) {
                        Toast.makeText(context, "SMS envoyé à $displayRecipient", Toast.LENGTH_LONG).show()
                        historyRepo?.logAction(
                            reminderId = reminderId,
                            reminderTitle = title,
                            recipient = displayRecipient,
                            message = message,
                            status = LogStatus.SENT,
                            details = "Envoyé directement en arrière-plan"
                        )
                    } else {
                        Toast.makeText(context, "Échec de l'envoi direct. Ouverture de l'app SMS...", Toast.LENGTH_SHORT).show()
                        val smsIntent = SmsSender.createSmsIntent(recipientPhone, message)
                        context.startActivity(smsIntent)
                        historyRepo?.logAction(
                            reminderId = reminderId,
                            reminderTitle = title,
                            recipient = displayRecipient,
                            message = message,
                            status = LogStatus.SENT,
                            details = "Transmis vers l'application SMS"
                        )
                    }
                } else {
                    // Open SMS app with prefilled text and recipient
                    val smsIntent = SmsSender.createSmsIntent(recipientPhone, message)
                    context.startActivity(smsIntent)

                    historyRepo?.logAction(
                        reminderId = reminderId,
                        reminderTitle = title,
                        recipient = displayRecipient,
                        message = message,
                        status = LogStatus.SENT,
                        details = "Transmis vers l'application SMS"
                    )
                }
            }

            ACTION_CANCEL_SMS -> {
                Log.d(TAG, "Action CANCEL triggered for $recipientPhone")
                historyRepo?.logAction(
                    reminderId = reminderId,
                    reminderTitle = title,
                    recipient = displayRecipient,
                    message = message,
                    status = LogStatus.CANCELLED,
                    details = "Notification fermée sans envoi"
                )
                Toast.makeText(context, "Rappel annulé", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
