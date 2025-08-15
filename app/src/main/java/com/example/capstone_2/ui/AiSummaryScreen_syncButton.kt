package com.example.capstone_2.ui
import java.time.ZoneId

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstone_2.ui.ai.AiSummaryCoordinatorViewModel
import com.example.capstone_2.ui.ai.AiSummaryUiState

/**
 * 기존 AiSummaryScreen과 병행 사용 가능.
 * - '동기화 후 요약' 버튼을 누르면: 오늘 세션 업로드 → /api/summary 호출
 */
@Composable
fun AiSummaryScreenSync() {
    val vm: AiSummaryCoordinatorViewModel = viewModel()
    val state by vm.ui.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.syncAndSummarize() }) {
                Text("동기화 후 요약")
            }
            Button(onClick = { vm.syncAndSummarize(java.time.LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1)) }) {
                Text("어제 요약")
            }
        }
        Spacer(Modifier.height(12.dp))

        when (state) {
            is AiSummaryUiState.Loading -> Text("불러오는 중...")
            is AiSummaryUiState.Success -> Text((state as AiSummaryUiState.Success).text)
            is AiSummaryUiState.Error -> Text((state as AiSummaryUiState.Error).fallback)
        }
    }

    // 첫 진입 시에도 한 번 시도하고 싶다면:
    LaunchedEffect(Unit) {
        if (state !is AiSummaryUiState.Success) vm.syncAndSummarize()
    }
}
