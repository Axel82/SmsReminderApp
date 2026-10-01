package com.smsreminder.app.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class LogStatus(val label: String) {
    SENT("Envoyé"),
    CANCELLED("Annulé"),
    FAILED("Échoué")
}

data class HistoryLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val reminderId: String? = null,
    val reminderTitle: String,
    val recipient: String,
    val message: String,
    val status: LogStatus = LogStatus.SENT,
    val details: String? = null
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE)
            return sdf.format(Date(timestamp))
        }

    val formattedShortDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM à HH:mm", Locale.FRANCE)
            return sdf.format(Date(timestamp))
        }
}
