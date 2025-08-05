package com.example.capstone_2.DB

import com.example.capstone_2.data.UsageSessionEntity
import androidx.room.*

@Dao
interface UsageSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UsageSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<UsageSessionEntity>)

    @Query("SELECT * FROM usage_sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<UsageSessionEntity>

    @Query("SELECT * FROM usage_sessions WHERE startTime BETWEEN :start AND :end")
    suspend fun getSessionsByDate(start: Long, end: Long): List<UsageSessionEntity>
}
