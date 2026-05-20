package com.example.ainote.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showStudyReminder(
            context,
            title = intent.getStringExtra("title") ?: "Study Alarm! ⏰",
            message = intent.getStringExtra("message") ?: "Time for your scheduled study session!"
        )
    }
}
