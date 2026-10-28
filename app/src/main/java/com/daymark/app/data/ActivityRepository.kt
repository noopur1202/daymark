package com.daymark.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val entryDao: EntryDao,
) {
    fun activities(): Flow<List<Activity>> = activityDao.getAll()

    fun entriesForDate(date: String): Flow<List<Entry>> = entryDao.getForDate(date)

    fun entriesForDateRange(start: String, end: String): Flow<List<Entry>> =
        entryDao.getBetween(start, end)

    fun countsFlow(start: String, end: String): Flow<Map<Long, Int>> =
        entryDao.getBetween(start, end).map { entries ->
            entries.groupingBy { it.activityId }.eachCount()
        }

    suspend fun addActivity(name: String, color: String) {
        activityDao.insert(Activity(name = name, color = color))
    }

    suspend fun deleteActivity(activity: Activity) = activityDao.delete(activity)

    suspend fun updateActivity(activity: Activity) = activityDao.update(activity)

    suspend fun toggleEntry(activityId: Long, date: String) {
        val exists = entryDao.getForDateOnce(date).any { it.activityId == activityId }
        if (exists) entryDao.delete(activityId, date)
        else entryDao.insert(Entry(activityId = activityId, date = date))
    }

    suspend fun counts(start: String, end: String): Map<Long, Int> =
        entryDao.countsBetween(start, end).associate { it.activityId to it.count }
}
