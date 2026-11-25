package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_rules")
data class CustomRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val pattern: String,
    val isRegex: Boolean = false,
    val category: String = "CUSTOM", // "ADULT_18+", "TERABOX", "UC_LINK", "CUSTOM"
    val description: String = "",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
