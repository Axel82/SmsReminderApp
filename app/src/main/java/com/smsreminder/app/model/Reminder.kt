package com.smsreminder.app.model

import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val daysOfWeek: Set<DayOfWeek>,
    val hour: Int,
    val minute: Int,
    val messageBody: String,
    val recipientName: String,
    val recipientPhone: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() = String.format("%02d:%02d", hour, minute)

    val formattedDays: String
        get() {
            if (daysOfWeek.size == 7) return "Tous les jours"
            if (daysOfWeek.size == 5 &&
                daysOfWeek.containsAll(listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
            ) return "En semaine (Lun - Ven)"
            if (daysOfWeek.size == 2 &&
                daysOfWeek.containsAll(listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
            ) return "Week-end (Sam - Dim)"

            return DayOfWeek.entries
                .filter { daysOfWeek.contains(it) }
                .joinToString(", ") { it.shortLabel }
        }

    val displayRecipient: String
        get() = if (recipientName.isNotBlank() && recipientName != recipientPhone) {
            "$recipientName ($recipientPhone)"
        } else {
            recipientPhone
        }
}
