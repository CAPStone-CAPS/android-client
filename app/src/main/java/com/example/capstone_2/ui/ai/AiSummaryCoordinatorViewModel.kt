package com.example.capstone_2.ui.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone_2.data.AiSummarySyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class AiSummaryCoordinatorViewModel(app: Application): AndroidViewModel(app) {
    private val repo = AiSummarySyncRepository(app.applicationContext)

    private val _ui = MutableStateFlow<AiSummaryUiState>(AiSummaryUiState.Loading)
    val ui: StateFlow<AiSummaryUiState> = _ui

    private val fallback = """
오늘 데이터가 충분하지 않아 임시 요약을 보여줘요.
- 팁: 앱을 몇 분 사용한 뒤 '동기화 후 요약' 버튼을 눌러보세요.
""".trimIndent()

    fun syncAndSummarize(date: LocalDate? = null) {
        _ui.value = AiSummaryUiState.Loading
        viewModelScope.launch {
            val r = repo.syncAndGetSummary(date)
            _ui.value = r.fold(
                onSuccess = { AiSummaryUiState.Success(it) },
                onFailure = { AiSummaryUiState.Error(it.message ?: "요약 실패", fallback) }
            )
        }
    }
}
