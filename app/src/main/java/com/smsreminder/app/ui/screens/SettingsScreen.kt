package com.smsreminder.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smsreminder.app.model.AppSettings
import com.smsreminder.app.model.DayOfWeek
import com.smsreminder.app.model.Reminder
import com.smsreminder.app.service.NotificationHelper
import com.smsreminder.app.ui.components.MasterSwitchCard
import com.smsreminder.app.ui.theme.StatusError
import com.smsreminder.app.ui.theme.StatusSuccess
import com.smsreminder.app.ui.theme.StatusWarning

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    hasSmsPermission: Boolean,
    hasContactPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestSmsPermission: () -> Unit,
    onRequestContactPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    val context = LocalContext.current

    // Check battery optimization status
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val isIgnoringBatteryOptimizations = remember(settings) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Réglages & Système",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Global Master Switch
            MasterSwitchCard(
                isEnabled = settings.isGlobalEnabled,
                onToggle = { isEnabled ->
                    onUpdateSettings(settings.copy(isGlobalEnabled = isEnabled))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Battery Optimization Exemption Card
            BatteryOptimizationCard(
                isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
                onRequestExemption = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions Status Section
            Text(
                text = "Autorisations requises",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PermissionRow(
                        title = "Notifications",
                        description = "Pour afficher les alertes de rappel interactives",
                        icon = Icons.Default.Notifications,
                        isGranted = hasNotificationPermission,
                        onRequest = onRequestNotificationPermission
                    )

                    PermissionRow(
                        title = "Contacts",
                        description = "Pour sélectionner un numéro dans votre répertoire",
                        icon = Icons.Default.Contacts,
                        isGranted = hasContactPermission,
                        onRequest = onRequestContactPermission
                    )

                    PermissionRow(
                        title = "SMS",
                        description = "Pour l'envoi direct de SMS si activé",
                        icon = Icons.Default.Sms,
                        isGranted = hasSmsPermission,
                        onRequest = onRequestSmsPermission
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options d'envoi SMS
            Text(
                text = "Options d'envoi des SMS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Envoi direct en arrière-plan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (settings.sendDirectlyViaSmsManager)
                                    "Le SMS partira immédiatement lors du clic 'Envoyer' sans ouvrir l'application SMS."
                                else
                                    "Le clic 'Envoyer' ouvre l'application SMS avec destinataire et texte préremplis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.sendDirectlyViaSmsManager,
                            onCheckedChange = { isChecked ->
                                onUpdateSettings(settings.copy(sendDirectlyViaSmsManager = isChecked))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test Notification Button
            Button(
                onClick = {
                    val sampleReminder = Reminder(
                        title = "Rappel de démonstration",
                        daysOfWeek = setOf(DayOfWeek.MONDAY),
                        hour = 12,
                        minute = 0,
                        messageBody = "Bonjour ! N'oubliez pas notre rendez-vous demain.",
                        recipientName = "Alice Dupont",
                        recipientPhone = "06 12 34 56 78"
                    )
                    NotificationHelper.showReminderNotification(context, sampleReminder)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Envoyer une notification test")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Local JSON Storage Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stockage 100 % local (JSON)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Fichiers : reminders.json, history_logs.json, settings.json\nAucune donnée n'est transmise vers des serveurs tiers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BatteryOptimizationCard(
    isIgnoringBatteryOptimizations: Boolean,
    onRequestExemption: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isIgnoringBatteryOptimizations) {
                MaterialTheme.colorScheme.surface
            } else {
                StatusWarning.copy(alpha = 0.12f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = if (isIgnoringBatteryOptimizations) StatusSuccess else StatusWarning,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Optimisation de batterie",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isIgnoringBatteryOptimizations) {
                    "✅ L'application est exemptée d'optimisation de batterie. Les alarmes et rappels en arrière-plan s'exécuteront à la seconde exacte même en veille prolongée."
                } else {
                    "⚠️ L'optimisation de batterie Android est active. Elle peut retarder ou tuer les alertes en arrière-plan lorsque le téléphone est en veille."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isIgnoringBatteryOptimizations) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onRequestExemption,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Désactiver l'optimisation (Recommandé)")
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) StatusSuccess.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                    contentDescription = null,
                    tint = if (isGranted) StatusSuccess else StatusError,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!isGranted) {
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onRequest,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Autoriser", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
