package com.daymark.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: ActivityRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = ActivityRepository(db.activityDao(), db.entryDao())
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun addActivityThenToggleOn() = runTest {
        repo.addActivity("Gym", "#E91E63")
        val gym = repo.activities().first().single()
        repo.toggleEntry(gym.id, "2026-10-27")

        val entries = repo.entriesForDate("2026-10-27").first()
        assertEquals(listOf(gym.id), entries.map { it.activityId })
    }

    @Test
    fun toggleTwiceRemovesEntry() = runTest {
        repo.addActivity("Gym", "#E91E63")
        val gym = repo.activities().first().single()
        repo.toggleEntry(gym.id, "2026-10-27")
        repo.toggleEntry(gym.id, "2026-10-27")
        assertTrue(repo.entriesForDate("2026-10-27").first().isEmpty())
    }

    @Test
    fun deleteActivityRemovesItsEntries() = runTest {
        repo.addActivity("Gym", "#E91E63")
        val gym = repo.activities().first().single()
        repo.toggleEntry(gym.id, "2026-10-27")
        repo.deleteActivity(gym)
        assertNull(repo.activities().first().find { it.id == gym.id })
        assertTrue(repo.entriesForDate("2026-10-27").first().isEmpty())
    }

    @Test
    fun countsForRangeMapsActivityIdToCount() = runTest {
        repo.addActivity("Gym", "#E91E63")
        repo.addActivity("Read", "#4CAF50")
        val all = repo.activities().first()
        val gym = all.first { it.name == "Gym" }
        val read = all.first { it.name == "Read" }
        listOf("2026-10-01", "2026-10-02", "2026-10-03").forEach { repo.toggleEntry(gym.id, it) }
        repo.toggleEntry(read.id, "2026-10-02")

        val counts = repo.counts("2026-10-01", "2026-10-31")
        assertEquals(3, counts[gym.id])
        assertEquals(1, counts[read.id])
    }
}
