package com.example.capstone_2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone_2.data.AiSummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AiSummaryUiState {
    object Loading: AiSummaryUiState
    data class Success(val text: String): AiSummaryUiState
    data class Error(val reason: String, val fallback: String): AiSummaryUiState
}

class AiSummaryViewModel(app: Application): AndroidViewModel(app) {
    private val repo = AiSummaryRepository(app.applicationContext)

    private val _uiState = MutableStateFlow<AiSummaryUiState>(AiSummaryUiState.Loading)
    val uiState: StateFlow<AiSummaryUiState> = _uiState

    private val fallback = """
오늘 하루 사용 패턴을 분석했어요.
- 집중 구간: 10:20~11:40, 21:10~22:00
- 방해 요소: SNS 34분, 동영상 51분
- 추천: 14시에 25분 타이머, 20시에 25분 타이머
키워드: #집중회복 #야간루틴 #SNS절제
""".trimIndent()

    init { refresh() }

    fun refresh() {
        _uiState.value = AiSummaryUiState.Loading
        viewModelScope.launch {
            val r = repo.fetch()
            _uiState.value = r.fold(
                onSuccess = { AiSummaryUiState.Success(it) },
                onFailure = { AiSummaryUiState.Error(it.message ?: "실패", fallback) }
            )
        }
    }
}
