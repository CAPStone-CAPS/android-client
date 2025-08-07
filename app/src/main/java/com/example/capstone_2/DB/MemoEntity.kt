package com.example.capstone_2.DB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo_sessions")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,    // YYYY-MM-DD
    val appName: String, // 앱 이름
    val blockIndices: String, // "1,2,3" 형태로 10분 단위 블록 인덱스들
    val memo: String     // 사용자가 작성한 메모
)
