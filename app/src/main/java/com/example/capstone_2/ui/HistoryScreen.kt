package com.example.capstone_2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {

    /* ---------- 상태 ---------- */
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAi by remember { mutableStateOf(false) }
    var showSelf by remember { mutableStateOf(false) }

    /* ---------- 달력 계산 ---------- */
    val days = remember(currentMonth) {
        val first = currentMonth.atDay(1)
        val lastDay = currentMonth.lengthOfMonth()
        val offset = first.dayOfWeek.value % 7      // 일요일=0
        List(offset) { null } +                     // 이전 달 빈칸
                (1..lastDay).map { currentMonth.atDay(it) }
    }

    /* ---------- UI ---------- */
    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("과거 기록 (일별)") })
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            /* ---- 월 헤더 ---- */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton({ currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Rounded.ArrowBackIos, contentDescription = "prev")
                }
                Text(
                    text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton({ currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Rounded.ArrowForwardIos, contentDescription = "next")
                }
            }

            /* ---- 요일 ---- */
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                listOf("일","월","화","수","목","금","토").forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }

            /* ---- 달력 그리드 ---- */
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(240.dp),
                userScrollEnabled = false
            ) {
                items(days) { date ->
                    Box(
                        Modifier
                            .aspectRatio(1f)
                            .clickable(
                                enabled = date != null && !date.isAfter(LocalDate.now())
                            ) { date?.let { selectedDate = it } }
                            .background(
                                if (date == selectedDate) Color(0xFF00AFF0)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString() ?: "",
                            color = if (date == selectedDate) Color.White
                            else if (date == null || date.isAfter(LocalDate.now())) Color.Gray
                            else Color.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---- 샘플 데이터 바인딩 ---- */
            val key = selectedDate.toString()      // "2025-08-17"
            val data = remember { sampleData }[key]

            if (data != null) {
                Text("총 사용시간: ${data.totalTime}", style = MaterialTheme.typography.headlineSmall)
                if (selectedDate != LocalDate.now()) {
                    RatingBar(score = data.satisfaction)
                }

                /* AI 요약 */
                if (selectedDate != LocalDate.now()) {
                    ExpandCard(
                        title = "AI 요약과 피드백 보기",
                        expanded = showAi,
                        onToggle = { showAi = !showAi },
                        bg = Color(0xFFA6DAF4)
                    ) { Text(data.aiSummary) }

                    /* 자가 피드백 */
                    ExpandCard(
                        title = "자가 피드백",
                        expanded = showSelf,
                        onToggle = { showSelf = !showSelf },
                        bg = Color(0xFFBFDAF7)
                    ) { Text(data.selfFeedback) }
                }
            } else {
                Text("이 날짜의 데이터가 없습니다.")
            }
        }
    }
}

/* --------- 작은 컴포넌트 --------- */

@Composable
private fun RatingBar(score: Int) {
    Row {
        repeat(5) { idx ->
            Text("★",
                color = if (idx < score) Color(0xFFFFC107) else Color.LightGray,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ExpandCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    bg: Color,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Surface(
            color = bg,
            shape = MaterialTheme.shapes.medium,
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontWeight = FontWeight.Medium)
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }
        if (expanded) {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFFF6F9FC)
            ) {
                Box(Modifier.padding(12.dp)) { content() }
            }
        }
    }
}

/* --------- 샘플 데이터 객체 (간략화) --------- */
private val sampleData = mapOf(
    "2025-08-17" to DayData("05:00:00", 4, "AI 요약 ...", "자가 피드백 ..."),
    "2025-08-16" to DayData("04:30:00", 3, "AI 요약 ...", "자가 피드백 ...")
)

private data class DayData(
    val totalTime: String,
    val satisfaction: Int,
    val aiSummary: String,
    val selfFeedback: String
)