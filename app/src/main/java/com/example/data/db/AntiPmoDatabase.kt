package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BlockedLog
import com.example.data.model.CustomRule
import com.example.data.model.RecoveryProfile

@Database(
    entities = [BlockedLog::class, CustomRule::class, RecoveryProfile::class],
    version = 1,
    exportSchema = true
)
abstract class AntiPmoDatabase : RoomDatabase() {
    abstract fun blockedLogDao(): BlockedLogDao
    abstract fun customRuleDao(): CustomRuleDao
    abstract fun recoveryDao(): RecoveryDao

    companion object {
        @Volatile
        private var INSTANCE: AntiPmoDatabase? = null

        fun getInstance(context: Context): AntiPmoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AntiPmoDatabase::class.java,
                    "anti_pmo_shield.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            db.execSQL("PRAGMA journal_mode=WAL")
                            db.execSQL("PRAGMA foreign_keys=ON")
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
