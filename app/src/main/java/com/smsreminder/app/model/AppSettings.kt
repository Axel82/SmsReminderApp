package com.smsreminder.app.model

data class AppSettings(
    val isGlobalEnabled: Boolean = true,
    val sendDirectlyViaSmsManager: Boolean = false, // If false, opens default SMS app; if true, tries direct SmsManager send
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
