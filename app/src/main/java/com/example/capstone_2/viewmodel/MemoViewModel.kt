package com.example.capstone_2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone_2.DB.AppDatabase
import com.example.capstone_2.DB.MemoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoViewModel(application: Application) : AndroidViewModel(application) {
    private val memoDao = AppDatabase.getDatabase(application).memoDao()

    fun insertMemo(memo: MemoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            memoDao.insertMemo(memo)
        }
    }

    fun getMemosByDate(date: String, onResult: (List<MemoEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val memos = memoDao.getMemosByDate(date)
            onResult(memos)
        }
    }

    fun deleteMemoById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            memoDao.deleteMemoById(id)
        }
    }

    fun deleteAllMemos() {
        viewModelScope.launch(Dispatchers.IO) {
            memoDao.deleteAllMemos()
        }
    }

    fun deleteAllMemosForDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val memos = memoDao.getMemosByDate(date)
            memos.forEach { memoDao.deleteMemoById(it.id) }
        }
    }
}
