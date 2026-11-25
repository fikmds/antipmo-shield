package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recovery_profile")
data class RecoveryProfile(
    @PrimaryKey
    val id: Int = 1,
    val streakStartTimestamp: Long = System.currentTimeMillis(),
    val lastRelapseTimestamp: Long = 0L,
    val totalUrgesResisted: Int = 0,
    val targetDays: Int = 90,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val userPledge: String = "Saya berjanji menjaga masa depan, energi, dan fokus saya dari PMO."
)
