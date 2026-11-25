package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_logs")
data class BlockedLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerSource: String, // e.g. "Chrome Browser", "UC Browser", "Regex Filter", "Terabox Link"
    val matchedPattern: String, // e.g. "terabox.com/s/...", "bokep viral", "18+ content"
    val category: String, // "ADULT_18+", "TERABOX", "UC_LINK", "CUSTOM_REGEX"
    val targetUrlOrText: String,
    val packageName: String = ""
)
