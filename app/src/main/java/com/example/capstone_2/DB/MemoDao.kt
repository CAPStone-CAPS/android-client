package com.example.capstone_2.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.capstone_2.DB.MemoEntity

@Dao
interface MemoDao {

    @Insert
    suspend fun insertMemo(memo: MemoEntity)

    @Query("SELECT * FROM memo_sessions WHERE date = :date")
    suspend fun getMemosByDate(date: String): List<MemoEntity>

    @Query("DELETE FROM memo_sessions WHERE id = :id")
    suspend fun deleteMemoById(id: Int)

    @Query("DELETE FROM memo_sessions")
    suspend fun deleteAllMemos()

    @Query("SELECT * FROM memo_sessions")
    suspend fun getAllMemos(): List<MemoEntity>
}
