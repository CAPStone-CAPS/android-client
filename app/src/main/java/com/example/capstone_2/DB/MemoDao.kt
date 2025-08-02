package com.example.capstone_2.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MemoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: MemoEntity)

    @Query("SELECT * FROM memo_sessions WHERE date = :date")
    suspend fun getMemosByDate(date: String): List<MemoEntity>

    @Query("SELECT * FROM memo_sessions WHERE date = :date AND blockIndex = :index")
    suspend fun getMemoByBlock(date: String, index: Int): MemoEntity?
}
