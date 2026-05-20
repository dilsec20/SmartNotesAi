package com.example.ainote.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.ainote.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFERENCES_NAME
)

class DataStoreManager(private val context: Context) {

    companion object {
        val USER_NAME = stringPreferencesKey(Constants.KEY_USER_NAME)
        val DARK_MODE = booleanPreferencesKey(Constants.KEY_DARK_MODE)
        val DAILY_REMINDER = booleanPreferencesKey(Constants.KEY_DAILY_REMINDER)
        val REMINDER_HOUR = intPreferencesKey(Constants.KEY_REMINDER_HOUR)
        val REMINDER_MINUTE = intPreferencesKey(Constants.KEY_REMINDER_MINUTE)
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME] ?: "Student"
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE] ?: false
    }

    val isDailyReminder: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DAILY_REMINDER] ?: false
    }

    val reminderHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_HOUR] ?: 9
    }

    val reminderMinute: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_MINUTE] ?: 0
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME] = name
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }

    suspend fun setDailyReminder(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_REMINDER] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[REMINDER_HOUR] = hour
            prefs[REMINDER_MINUTE] = minute
        }
    }
}
