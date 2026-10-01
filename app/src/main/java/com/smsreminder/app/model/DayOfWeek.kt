package com.smsreminder.app.model

import java.util.Calendar

enum class DayOfWeek(
    val calendarDay: Int,
    val shortLabel: String,
    val fullLabel: String
) {
    MONDAY(Calendar.MONDAY, "Lun", "Lundi"),
    TUESDAY(Calendar.TUESDAY, "Mar", "Mardi"),
    WEDNESDAY(Calendar.WEDNESDAY, "Mer", "Mercredi"),
    THURSDAY(Calendar.THURSDAY, "Jeu", "Jeudi"),
    FRIDAY(Calendar.FRIDAY, "Ven", "Vendredi"),
    SATURDAY(Calendar.SATURDAY, "Sam", "Samedi"),
    SUNDAY(Calendar.SUNDAY, "Dim", "Dimanche");

    companion object {
        fun fromCalendarDay(calendarDay: Int): DayOfWeek? {
            return entries.firstOrNull { it.calendarDay == calendarDay }
        }
    }
}
