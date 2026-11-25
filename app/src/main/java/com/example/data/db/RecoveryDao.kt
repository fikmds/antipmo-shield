package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RecoveryProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface RecoveryDao {
    @Query("SELECT * FROM recovery_profile WHERE id = 1")
    fun getProfile(): Flow<RecoveryProfile?>

    @Query("SELECT * FROM recovery_profile WHERE id = 1")
    suspend fun getProfileOnce(): RecoveryProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: RecoveryProfile)

    @Update
    suspend fun updateProfile(profile: RecoveryProfile)
}
