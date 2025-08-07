package com.example.capstone_2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone_2.DB.AppDatabase
import com.example.capstone_2.data.AppInfoHelper
import com.example.capstone_2.data.UsageRecordDto
import com.example.capstone_2.DB.UsageSessionEntity
import com.example.capstone_2.data.toUsageRecordDto
import com.example.capstone_2.network.RetrofitClient
import kotlinx.coroutines.launch

class UsageUploadViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = AppDatabase.getDatabase(context)
    private val appInfoHelper = AppInfoHelper(context)

    fun uploadAllUsageSessions() {
        viewModelScope.launch {
            try {
                val sessions = db.usageSessionDao().getAllSessions()
                val records = sessions.toUsageRecordDtos(appInfoHelper)

                Log.d("USAGE_UPLOAD", "전송할 데이터 개수: ${records.size}")

                records.forEach { dto ->
                    val response = RetrofitClient.usageApiService.sendUsageRecord(dto)
                    if (response.isSuccessful) {
                        Log.d("USAGE_UPLOAD", "전송 성공: ${dto.package_name}")
                    } else {
                        Log.e("USAGE_UPLOAD", "전송 실패: ${dto.package_name}")
                        Log.e("USAGE_UPLOAD", "Status: ${response.code()}, Error: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("USAGE_UPLOAD", "예외 발생", e)
            }
        }
    }
}

fun List<UsageSessionEntity>.toUsageRecordDtos(appInfoHelper: AppInfoHelper): List<UsageRecordDto> {
    return this.map { session ->
        val appName = appInfoHelper.getAppName(session.packageName)
        session.toUsageRecordDto(appName)
    }
}
