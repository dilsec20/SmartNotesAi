package com.example.ainote.ui.screens.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.example.ainote.notifications.AlarmReceiver
import com.example.ainote.notifications.NotificationHelper
import com.example.ainote.notifications.ReminderWorker
import com.example.ainote.ui.components.GradientButton
import com.example.ainote.utils.Constants
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var reminderEnabled by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(9) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var examDate by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val timePickerState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(Modifier.padding(16.dp)) { Text(snackbarMessage) }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Daily reminder card
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Daily Study Reminder", fontWeight = FontWeight.Bold)
                                Text("Get notified to study every day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        Switch(checked = reminderEnabled, onCheckedChange = { enabled ->
                            reminderEnabled = enabled
                            if (enabled) {
                                scheduleDaily(context, selectedHour, selectedMinute)
                                snackbarMessage = "Daily reminder set for ${formatTime(selectedHour, selectedMinute)}"
                            } else {
                                cancelDaily(context)
                                snackbarMessage = "Daily reminder cancelled"
                            }
                            showSnackbar = true
                        })
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { showTimePicker = true }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.AccessTime, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reminder Time: ${formatTime(selectedHour, selectedMinute)}")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Exam date card
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(12.dp))
                        Text("Exam Schedule", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { showDatePicker = true }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.DateRange, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (examDate.isBlank()) "Set Exam Date" else "Exam: $examDate")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick alarm
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Alarm, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Text("Quick Study Alarm", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 60).forEach { min ->
                            OutlinedButton(onClick = {
                                setAlarm(context, min)
                                snackbarMessage = "Alarm set for $min minutes from now"
                                showSnackbar = true
                            }, Modifier.weight(1f)) { Text("${min}m") }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            GradientButton("Test Notification", onClick = {
                NotificationHelper.showStudyReminder(context)
                snackbarMessage = "Notification sent!"
                showSnackbar = true
            }, modifier = Modifier.fillMaxWidth(), icon = Icons.Default.NotificationsActive)
        }

        // Time picker dialog
        if (showTimePicker) {
            AlertDialog(onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                        if (reminderEnabled) scheduleDaily(context, selectedHour, selectedMinute)
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
                text = { TimePicker(state = timePickerState) }
            )
        }

        // Date picker dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val cal = Calendar.getInstance().apply { timeInMillis = it }
                            examDate = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }
}

private fun formatTime(h: Int, m: Int): String {
    val amPm = if (h < 12) "AM" else "PM"
    val hour = if (h == 0) 12 else if (h > 12) h - 12 else h
    return String.format("%d:%02d %s", hour, m, amPm)
}

private fun scheduleDaily(ctx: Context, hour: Int, minute: Int) {
    val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateDelay(hour, minute), TimeUnit.MILLISECONDS)
        .addTag(Constants.REMINDER_WORK_TAG)
        .build()
    WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
        Constants.REMINDER_WORK_TAG, ExistingPeriodicWorkPolicy.UPDATE, request
    )
}

private fun cancelDaily(ctx: Context) {
    WorkManager.getInstance(ctx).cancelUniqueWork(Constants.REMINDER_WORK_TAG)
}

private fun calculateDelay(hour: Int, minute: Int): Long {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0)
    }
    if (target.before(now)) target.add(Calendar.DAY_OF_MONTH, 1)
    return target.timeInMillis - now.timeInMillis
}

private fun setAlarm(ctx: Context, minutes: Int) {
    val intent = Intent(ctx, AlarmReceiver::class.java)
    val pi = PendingIntent.getBroadcast(ctx, minutes, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minutes * 60000L, pi)
}
