package com.smsreminder.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.smsreminder.app.SmsReminderApplication
import com.smsreminder.app.model.Reminder
import com.smsreminder.app.ui.navigation.Screen
import com.smsreminder.app.ui.screens.AddEditReminderDialog
import com.smsreminder.app.ui.screens.HistoryScreen
import com.smsreminder.app.ui.screens.RemindersScreen
import com.smsreminder.app.ui.screens.SettingsScreen
import com.smsreminder.app.ui.theme.SmsReminderAppTheme

class MainActivity : ComponentActivity() {

    private val app by lazy { applicationContext as SmsReminderApplication }

    // State for contact picker callback
    private var onContactPickedCallback: ((name: String, phone: String) -> Unit)? = null

    // Contact Picker Launcher
    private val contactPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            extractContactDetails(contactUri)
        }
    }

    // Permission Launchers
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permission de notification accordée", Toast.LENGTH_SHORT).show()
        }
    }

    private val contactPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permission contacts accordée", Toast.LENGTH_SHORT).show()
            launchContactPickerInternal()
        }
    }

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permission SMS accordée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Request notifications permission on Android 13+ automatically if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SmsReminderAppTheme {
                MainAppContent()
            }
        }
    }

    private fun launchContactPickerInternal() {
        try {
            contactPickerLauncher.launch(null)
        } catch (e: Exception) {
            Toast.makeText(this, "Impossible d'ouvrir les contacts: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestContactPicker(onPicked: (name: String, phone: String) -> Unit) {
        onContactPickedCallback = onPicked
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchContactPickerInternal()
        } else {
            contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun extractContactDetails(contactUri: Uri) {
        var name = ""
        var phoneNumber = ""

        try {
            val cursor: Cursor? = contentResolver.query(contactUri, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val hasPhoneIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                    if (nameIndex != -1) {
                        name = c.getString(nameIndex) ?: ""
                    }

                    val id = if (idIndex != -1) c.getString(idIndex) else null
                    val hasPhoneNumber = if (hasPhoneIndex != -1) c.getInt(hasPhoneIndex) > 0 else false

                    if (hasPhoneNumber && id != null) {
                        val pCursor: Cursor? = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        pCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                val phoneIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (phoneIndex != -1) {
                                    phoneNumber = pc.getString(phoneIndex) ?: ""
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error extracting contact: ${e.message}", e)
        }

        if (phoneNumber.isNotBlank()) {
            onContactPickedCallback?.invoke(name, phoneNumber)
        } else if (name.isNotBlank()) {
            onContactPickedCallback?.invoke(name, "")
            Toast.makeText(this, "Aucun numéro trouvé pour ce contact", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun MainAppContent() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Reminders) }

        val reminders by app.reminderRepository.reminders.collectAsState()
        val historyLogs by app.historyRepository.logs.collectAsState()
        val settings by app.settingsRepository.settings.collectAsState()

        var showAddEditDialog by remember { mutableStateOf(false) }
        var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }
        var pickedName by remember { mutableStateOf<String?>(null) }
        var pickedPhone by remember { mutableStateOf<String?>(null) }

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val hasContactPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    Screen.items.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentScreen == Screen.Reminders) {
                    FloatingActionButton(
                        onClick = {
                            reminderToEdit = null
                            pickedName = null
                            pickedPhone = null
                            showAddEditDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter un rappel"
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    Screen.Reminders -> {
                        RemindersScreen(
                            reminders = reminders,
                            isGlobalEnabled = settings.isGlobalEnabled,
                            onToggleGlobal = { enabled ->
                                app.settingsRepository.setGlobalEnabled(enabled)
                                if (enabled) {
                                    app.alarmScheduler.rescheduleAllActiveReminders(reminders)
                                } else {
                                    app.alarmScheduler.cancelAll(reminders)
                                }
                            },
                            onToggleReminder = { id, isEnabled ->
                                app.reminderRepository.toggleReminder(id, isEnabled)
                                val updated = app.reminderRepository.getReminderById(id)
                                if (updated != null && settings.isGlobalEnabled) {
                                    if (isEnabled) {
                                        app.alarmScheduler.scheduleReminder(updated)
                                    } else {
                                        app.alarmScheduler.cancelReminder(updated)
                                    }
                                }
                            },
                            onAddReminderClicked = {
                                reminderToEdit = null
                                pickedName = null
                                pickedPhone = null
                                showAddEditDialog = true
                            },
                            onEditReminderClicked = { reminder ->
                                reminderToEdit = reminder
                                pickedName = null
                                pickedPhone = null
                                showAddEditDialog = true
                            },
                            onDeleteReminderClicked = { id ->
                                val reminder = app.reminderRepository.getReminderById(id)
                                if (reminder != null) {
                                    app.alarmScheduler.cancelReminder(reminder)
                                }
                                app.reminderRepository.deleteReminder(id)
                            }
                        )
                    }

                    Screen.History -> {
                        HistoryScreen(
                            logs = historyLogs,
                            onClearAllLogs = { app.historyRepository.clearAllLogs() },
                            onDeleteLog = { id -> app.historyRepository.deleteLog(id) }
                        )
                    }

                    Screen.Settings -> {
                        SettingsScreen(
                            settings = settings,
                            onUpdateSettings = { newSettings ->
                                val wasEnabled = settings.isGlobalEnabled
                                app.settingsRepository.updateSettings(newSettings)
                                if (newSettings.isGlobalEnabled && !wasEnabled) {
                                    app.alarmScheduler.rescheduleAllActiveReminders(reminders)
                                } else if (!newSettings.isGlobalEnabled && wasEnabled) {
                                    app.alarmScheduler.cancelAll(reminders)
                                }
                            },
                            hasSmsPermission = hasSmsPermission,
                            hasContactPermission = hasContactPermission,
                            hasNotificationPermission = hasNotificationPermission,
                            onRequestSmsPermission = {
                                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                            },
                            onRequestContactPermission = {
                                contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            },
                            onRequestNotificationPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showAddEditDialog) {
            AddEditReminderDialog(
                reminderToEdit = reminderToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { savedReminder ->
                    if (reminderToEdit == null) {
                        app.reminderRepository.addReminder(savedReminder)
                    } else {
                        app.reminderRepository.updateReminder(savedReminder)
                    }

                    if (settings.isGlobalEnabled && savedReminder.isEnabled) {
                        app.alarmScheduler.scheduleReminder(savedReminder)
                    }

                    showAddEditDialog = false
                    Toast.makeText(this, "Rappel enregistré", Toast.LENGTH_SHORT).show()
                },
                onPickContactRequested = {
                    requestContactPicker { name, phone ->
                        pickedName = name
                        pickedPhone = phone
                    }
                },
                pickedContactName = pickedName,
                pickedContactPhone = pickedPhone
            )
        }
    }
}
