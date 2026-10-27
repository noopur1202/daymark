package com.daymark.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DaoTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insertAndReadActivities() = runTest {
        db.activityDao().insert(Activity(name = "Gym", color = "#E91E63"))
        db.activityDao().insert(Activity(name = "Read", color = "#4CAF50"))
        val all = db.activityDao().getAll().first()
        assertEquals(listOf("Gym", "Read"), all.map { it.name })
    }

    @Test
    fun deleteActivity_cascadesEntries() = runTest {
        val id = db.activityDao().insert(Activity(name = "Gym", color = "#E91E63"))
        db.entryDao().insert(Entry(activityId = id, date = "2026-10-27"))
        db.activityDao().delete(Activity(id = id, name = "Gym", color = "#E91E63"))
        assertTrue(db.entryDao().getForDateOnce("2026-10-27").isEmpty())
    }

    @Test
    fun duplicateEntryForSameActivityAndDateIsIgnored() = runTest {
        val id = db.activityDao().insert(Activity(name = "Gym", color = "#E91E63"))
        db.entryDao().insert(Entry(activityId = id, date = "2026-10-27"))
        db.entryDao().insert(Entry(activityId = id, date = "2026-10-27"))
        assertEquals(1, db.entryDao().getForDateOnce("2026-10-27").size)
    }

    @Test
    fun countsBetweenGroupsByActivity() = runTest {
        val gym = db.activityDao().insert(Activity(name = "Gym", color = "#E91E63"))
        val read = db.activityDao().insert(Activity(name = "Read", color = "#4CAF50"))
        listOf("2026-10-01", "2026-10-05", "2026-10-20").forEach {
            db.entryDao().insert(Entry(activityId = gym, date = it))
        }
        db.entryDao().insert(Entry(activityId = read, date = "2026-10-10"))
        // Out of range:
        db.entryDao().insert(Entry(activityId = gym, date = "2026-09-30"))
        db.entryDao().insert(Entry(activityId = gym, date = "2026-11-01"))

        val counts = db.entryDao().countsBetween("2026-10-01", "2026-10-31")
            .associate { it.activityId to it.count }
        assertEquals(3, counts[gym])
        assertEquals(1, counts[read])
    }
}
