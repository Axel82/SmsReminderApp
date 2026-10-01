# Proguard rules for SmsReminderApp
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.smsreminder.app.model.** { *; }
