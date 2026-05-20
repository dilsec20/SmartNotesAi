package com.example.ainote.utils

object Constants {
    // Subjects
    val SUBJECTS = listOf(
        "Programming",
        "Data Structures and Algorithms",
        "Computer Networks",
        "Computer Architecture",
        "Operating Systems",
        "Databases",
        "Artificial Intelligence",
        "Machine Learning"
    )

    // Notification
    const val CHANNEL_ID = "study_reminders"
    const val CHANNEL_NAME = "Study Reminders"
    const val NOTIFICATION_ID = 1001
    const val REMINDER_WORK_TAG = "daily_study_reminder"

    // DataStore
    const val PREFERENCES_NAME = "ainote_preferences"
    const val KEY_USER_NAME = "user_name"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_DAILY_REMINDER = "daily_reminder"
    const val KEY_REMINDER_HOUR = "reminder_hour"
    const val KEY_REMINDER_MINUTE = "reminder_minute"

    // AI
    const val GEMINI_API_KEY = ""
    const val GEMINI_MODEL = "gemini-3.1-flash-lite"

    // Note colors (indices into the color list)
    val NOTE_COLORS = listOf(
        0xFFFDCB6E, // Yellow
        0xFF74B9FF, // Blue
        0xFF55EFC4, // Green
        0xFFFF9FF3, // Pink
        0xFFFFA502, // Orange
        0xFFA29BFE, // Purple
        0xFFFF7675, // Red
        0xFFDFE6E9  // Grey
    )
}
