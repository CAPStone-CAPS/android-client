package com.example.capstone_2.ui
import java.time.ZoneId

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstone_2.ui.ai.AiSummaryUiState
import com.example.capstone_2.ui.ai.AiSummaryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.format.TextStyle

@Composable
fun AiSummaryScreen(vm: AiSummaryViewModel = viewModel()) {
    val state by vm.ui.collectAsState()

    val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
    val dayName = today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
    val dateStr = today.format(DateTimeFormatter.ofPattern("M월 d일")) + " (" + dayName + ")"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "AI 요약",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { vm.refresh() }) { Text("새로고침") }
        }

        Spacer(Modifier.height(8.dp))

        Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFEAEFFF)) {
            Text(
                text = dateStr,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                fontSize = 13.sp,
                color = Color(0xFF3D5AFE)
            )
        }

        Spacer(Modifier.height(16.dp))

        when (state) {
            is AiSummaryUiState.Loading -> SummarySkeleton()
            is AiSummaryUiState.Success -> SummaryCard((state as AiSummaryUiState.Success).text)
            is AiSummaryUiState.Error -> {
                val s = state as AiSummaryUiState.Error
                SummaryCard(s.fallback, errorBanner = s.reason)
            }
        }
    }
}

@Composable
private fun SummarySkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .padding(vertical = 8.dp)
                    .background(Color(0xFFE9ECF7), RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun SummaryCard(message: String, errorBanner: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        if (!errorBanner.isNullOrBlank()) {
            Surface(color = Color(0xFFFFF3F0), shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = "서버 오류로 임시 요약을 보여줘요: " + errorBanner,
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFFCC3A2B),
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        Text(text = message, fontSize = 16.sp, color = Color(0xFF2B2B2B), lineHeight = 22.sp)
    }
}
