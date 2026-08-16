package com.example.myai.modules.tools

import java.util.Date

data class ReminderItem(
    val id: String,
    val title: String,
    val timestamp: Long,
    val isCompleted: Boolean = false
)

/**
 * Modular interface for device reminders and alarms scheduling.
 */
interface RemindersModule {
    suspend fun scheduleReminder(title: String, time: Date): Boolean
    suspend fun setAlarm(hour: Int, minute: Int, title: String): Boolean
    suspend fun getUpcomingReminders(): List<ReminderItem>
}

class DefaultRemindersModule : RemindersModule {
    private val reminders = mutableListOf<ReminderItem>()

    override suspend fun scheduleReminder(title: String, time: Date): Boolean {
        reminders.add(
            ReminderItem(
                id = System.currentTimeMillis().toString(),
                title = title,
                timestamp = time.time
            )
        )
        return true
    }

    override suspend fun setAlarm(hour: Int, minute: Int, title: String): Boolean {
        return true
    }

    override suspend fun getUpcomingReminders(): List<ReminderItem> {
        return reminders.filter { !it.isCompleted }
    }
}
