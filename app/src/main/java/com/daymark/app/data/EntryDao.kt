package com.daymark.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class ActivityCount(val activityId: Long, val count: Int)

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry)

    @Query("DELETE FROM entries WHERE activityId = :activityId AND date = :date")
    suspend fun delete(activityId: Long, date: String)

    @Query("SELECT * FROM entries WHERE date = :date")
    suspend fun getForDateOnce(date: String): List<Entry>

    @Query("SELECT * FROM entries WHERE date = :date")
    fun getForDate(date: String): Flow<List<Entry>>

    @Query("SELECT * FROM entries WHERE date BETWEEN :start AND :end")
    fun getBetween(start: String, end: String): Flow<List<Entry>>

    @Query("SELECT * FROM entries")
    suspend fun getAllOnce(): List<Entry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<Entry>)

    @Query("DELETE FROM entries")
    suspend fun deleteAll()

    @Query("SELECT activityId, COUNT(*) as count FROM entries WHERE date BETWEEN :start AND :end GROUP BY activityId")
    suspend fun countsBetween(start: String, end: String): List<ActivityCount>
}
