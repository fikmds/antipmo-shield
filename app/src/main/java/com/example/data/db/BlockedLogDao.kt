package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BlockedLog
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedLogDao {
    @Query("SELECT * FROM blocked_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<BlockedLog>>

    @Query("SELECT COUNT(*) FROM blocked_logs")
    fun getBlockedCount(): Flow<Int>

    @Query("SELECT * FROM blocked_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentLogs(): Flow<List<BlockedLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BlockedLog): Long

    @Query("DELETE FROM blocked_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM blocked_logs")
    suspend fun clearAllLogs()
}
