package com.daymark.app.util

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.daymark.app.data.AppDatabase
import androidx.room.withTransaction
import java.io.File
import java.io.RandomAccessFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

object DatabaseBackup {
    private const val DB_NAME = "daymark.db"
    private const val SQLITE_HEADER = "SQLite format 3"

    /** Copies a consistent snapshot of the database to the given SAF location. */
    suspend fun export(context: Context, db: AppDatabase, uri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) { runCatching { exportImpl(context, db, uri) } }

    /** Replaces all current data with the contents of the picked backup file. */
    suspend fun import(context: Context, db: AppDatabase, uri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) { runCatching { importImpl(context, db, uri) } }

    private fun exportImpl(context: Context, db: AppDatabase, uri: Uri) {
        // Fold the write-ahead log into the main file so the copy is complete.
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        val dbFile = context.getDatabasePath(DB_NAME)
        context.contentResolver.openOutputStream(uri)?.use { out ->
            dbFile.inputStream().use { it.copyTo(out) }
        } ?: error("Could not open destination")
    }

    private suspend fun importImpl(context: Context, db: AppDatabase, uri: Uri) {
        val temp = File(context.cacheDir, "import_tmp.db")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { input.copyTo(it) }
            } ?: error("Could not read backup file")

            RandomAccessFile(temp, "r").use { f ->
                val header = ByteArray(16)
                f.readFully(header)
                check(String(header, Charsets.US_ASCII).startsWith(SQLITE_HEADER)) {
                    "Not a valid DayMark backup file"
                }
            }

            // Open the backup through Room so the schema matches, read everything,
            // then replace live data via the live DAOs (keeps invalidation intact).
            val src = Room.databaseBuilder(context, AppDatabase::class.java, temp.absolutePath).build()
            try {
                val activities = src.activityDao().getAllOnce()
                val entries = src.entryDao().getAllOnce()
                db.withTransaction {
                    db.entryDao().deleteAll()
                    db.activityDao().deleteAll()
                    db.activityDao().insertAll(activities)
                    db.entryDao().insertAll(entries)
                }
            } finally {
                src.close()
            }
        } finally {
            temp.delete()
        }
    }
}
