package com.smsreminder.app.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smsreminder.app.model.DayOfWeek
import com.smsreminder.app.model.Reminder
import com.smsreminder.app.service.NotificationHelper
import com.smsreminder.app.ui.components.DaySelector

@Composable
fun AddEditReminderDialog(
    reminderToEdit: Reminder? = null,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    onPickContactRequested: () -> Unit,
    pickedContactName: String? = null,
    pickedContactPhone: String? = null
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(reminderToEdit?.title ?: "") }
    var recipientName by remember { mutableStateOf(reminderToEdit?.recipientName ?: "") }
    var recipientPhone by remember { mutableStateOf(reminderToEdit?.recipientPhone ?: "") }
    var messageBody by remember { mutableStateOf(reminderToEdit?.messageBody ?: "") }
    var selectedDays by remember {
        mutableStateOf(reminderToEdit?.daysOfWeek ?: setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
    }
    var hour by remember { mutableIntStateOf(reminderToEdit?.hour ?: 9) }
    var minute by remember { mutableIntStateOf(reminderToEdit?.minute ?: 0) }

    // If contact was picked externally
    if (pickedContactPhone != null && pickedContactPhone.isNotBlank() && pickedContactPhone != recipientPhone) {
        recipientPhone = pickedContactPhone
        if (pickedContactName != null && pickedContactName.isNotBlank()) {
            recipientName = pickedContactName
        }
    }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val formattedTime = String.format("%02d:%02d", hour, minute)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Text(
                    text = if (reminderToEdit == null) "Nouveau rappel SMS" else "Modifier le rappel",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre du rappel (ex: Appel client)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time Picker Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val timePicker = TimePickerDialog(
                                context,
                                { _, selectedHour, selectedMinute ->
                                    hour = selectedHour
                                    minute = selectedMinute
                                },
                                hour,
                                minute,
                                true // 24h format
                            )
                            timePicker.show()
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Heure de déclenchement",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        TextButton(onClick = {
                            val timePicker = TimePickerDialog(
                                context,
                                { _, selectedHour, selectedMinute ->
                                    hour = selectedHour
                                    minute = selectedMinute
                                },
                                hour,
                                minute,
                                true
                            )
                            timePicker.show()
                        }) {
                            Text("Modifier")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day Selector
                DaySelector(
                    selectedDays = selectedDays,
                    onDaysChanged = { selectedDays = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Recipient Section
                Text(
                    text = "Destinataire",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("Numéro de téléphone") },
                        placeholder = { Text("ex: 0612345678") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = onPickContactRequested) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = "Choisir depuis les contacts",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Nom ou Libellé (optionnel)") },
                    placeholder = { Text("ex: Papa, Dr Dupont...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message Body
                Text(
                    text = "Message SMS",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = messageBody,
                    onValueChange = { messageBody = it },
                    label = { Text("Corps du message") },
                    placeholder = { Text("Tapez le texte du SMS à envoyer...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // SMS counter calculation
                val charCount = messageBody.length
                val smsCount = if (charCount <= 160) 1 else ((charCount - 1) / 153) + 1
                Text(
                    text = "$charCount caractères ($smsCount SMS)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp, end = 4.dp)
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Test Notification Button
                OutlinedButton(
                    onClick = {
                        val testReminder = Reminder(
                            id = reminderToEdit?.id ?: "test-id",
                            title = if (title.isBlank()) "Test de rappel" else title,
                            daysOfWeek = selectedDays,
                            hour = hour,
                            minute = minute,
                            messageBody = if (messageBody.isBlank()) "Ceci est un exemple de message SMS de test." else messageBody,
                            recipientName = recipientName,
                            recipientPhone = if (recipientPhone.isBlank()) "06 00 00 00 00" else recipientPhone,
                            isEnabled = true
                        )
                        NotificationHelper.showReminderNotification(context, testReminder)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tester la notification maintenant")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Annuler / Enregistrer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                showError = true
                                errorMessage = "Veuillez saisir un titre pour le rappel."
                                return@Button
                            }
                            if (recipientPhone.isBlank()) {
                                showError = true
                                errorMessage = "Veuillez saisir un numéro de destinataire."
                                return@Button
                            }
                            if (messageBody.isBlank()) {
                                showError = true
                                errorMessage = "Veuillez saisir le contenu du SMS."
                                return@Button
                            }
                            if (selectedDays.isEmpty()) {
                                showError = true
                                errorMessage = "Veuillez sélectionner au moins un jour de la semaine."
                                return@Button
                            }

                            val newReminder = (reminderToEdit ?: Reminder(
                                title = title.trim(),
                                daysOfWeek = selectedDays,
                                hour = hour,
                                minute = minute,
                                messageBody = messageBody.trim(),
                                recipientName = recipientName.trim(),
                                recipientPhone = recipientPhone.trim()
                            )).copy(
                                title = title.trim(),
                                daysOfWeek = selectedDays,
                                hour = hour,
                                minute = minute,
                                messageBody = messageBody.trim(),
                                recipientName = recipientName.trim(),
                                recipientPhone = recipientPhone.trim()
                            )

                            onSave(newReminder)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}
