package com.daymark.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insert(activity: Activity): Long

    @Update
    suspend fun update(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)

    @Query("SELECT * FROM activities ORDER BY id")
    fun getAll(): Flow<List<Activity>>

    @Query("SELECT * FROM activities ORDER BY id")
    suspend fun getAllOnce(): List<Activity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<Activity>)

    @Query("DELETE FROM activities")
    suspend fun deleteAll()
}
