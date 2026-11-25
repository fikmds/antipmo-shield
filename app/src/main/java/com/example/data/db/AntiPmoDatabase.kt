package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.JournalMode
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BlockedLog
import com.example.data.model.CustomRule
import com.example.data.model.RecoveryProfile

@Database(
    entities = [BlockedLog::class, CustomRule::class, RecoveryProfile::class],
    version = 1,
    exportSchema = false
)
abstract class AntiPmoDatabase : RoomDatabase() {
    abstract fun blockedLogDao(): BlockedLogDao
    abstract fun customRuleDao(): CustomRuleDao
    abstract fun recoveryDao(): RecoveryDao

    companion object {
        @Volatile
        private var INSTANCE: AntiPmoDatabase? = null

        // Bump version only alongside a Migration(old, new) here; no destructive fallback exists.
        private val MIGRATIONS: Array<Migration> = emptyArray()

        fun getInstance(context: Context): AntiPmoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AntiPmoDatabase::class.java,
                    "anti_pmo_shield.db"
                )
                    .addMigrations(*MIGRATIONS)
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            // SQLite leaves FK off by default; WAL opens multiple connections.
                            db.execSQL("PRAGMA foreign_keys = ON")
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
