package com.daymark.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insert(activity: Activity): Long

    @Delete
    suspend fun delete(activity: Activity)

    @Query("SELECT * FROM activities ORDER BY id")
    fun getAll(): Flow<List<Activity>>
}
