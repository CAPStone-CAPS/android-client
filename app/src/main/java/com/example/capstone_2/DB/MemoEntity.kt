package com.example.capstone_2.DB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo_sessions")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockIndex: Int, // 0~143 (10분 단위)
    val date: String,    // YYYY-MM-DD
    val content: String  // 사용자가 작성한 메모
)
