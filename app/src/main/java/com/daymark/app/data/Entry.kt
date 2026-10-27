package com.daymark.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entries",
    foreignKeys = [ForeignKey(
        entity = Activity::class,
        parentColumns = ["id"],
        childColumns = ["activityId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index(value = ["activityId", "date"], unique = true)],
)
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: Long,
    val date: String, // ISO yyyy-MM-dd
    val note: String? = null,
)
