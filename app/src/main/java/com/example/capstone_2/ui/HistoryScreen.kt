package com.example.capstone_2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun HistoryScreen(
    onNavigateBack: (() -> Unit)? = null  // 네비게이션 콜백 추가
) {

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
            SmallTopAppBar(
                title = { Text("과거 기록 (일별)") },
                navigationIcon = onNavigateBack?.let { callback ->
                    {
                        IconButton(onClick = callback) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                        }
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            /* ---- 월 헤더 (제한 로직 추가) ---- */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) },
                    enabled = currentMonth > YearMonth.of(2020, 1) // 최소 날짜 제한
                ) {
                    Icon(Icons.Rounded.ArrowBackIos, contentDescription = "prev")
                }
                Text(
                    text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) },
                    enabled = currentMonth < YearMonth.now() // 미래 월 제한
                ) {
                    Icon(Icons.Rounded.ArrowForwardIos, contentDescription = "next")
                }
            }

            /* ---- 요일 ---- */
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                listOf("일","월","화","수","목","금","토").forEach {
                    Text(
                        it, 
                        Modifier.weight(1f), 
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            /* ---- 달력 그리드 ---- */
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(240.dp),
                userScrollEnabled = false
            ) {
                items(days) { date ->
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    val isFuture = date != null && date.isAfter(LocalDate.now())
                    val hasData = date != null && sampleData.containsKey(date.toString())
                    
                    Box(
                        Modifier
                            .aspectRatio(1f)
                            .clickable(
                                enabled = date != null && !isFuture
                            ) { date?.let { selectedDate = it } }
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    hasData -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    else -> Color.Transparent
                                },
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString() ?: "",
                            color = when {
                                isSelected -> Color.White
                                isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---- 샘플 데이터 바인딩 ---- */
            val key = selectedDate.toString()      // "2025-08-17"
            val data = remember { sampleData }[key]

            if (data != null) {
                Text(
                    "총 사용시간: ${data.totalTime}", 
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedDate != LocalDate.now()) {
                    RatingBar(score = data.satisfaction)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                /* AI 요약 */
                if (selectedDate != LocalDate.now()) {
                    ExpandCard(
                        title = "AI 요약과 피드백 보기",
                        expanded = showAi,
                        onToggle = { showAi = !showAi },
                        bg = Color(0xFFE8F5E8)
                    ) { 
                        Text(
                            data.aiSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        ) 
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    /* 자가 피드백 */
                    ExpandCard(
                        title = "자가 피드백",
                        expanded = showSelf,
                        onToggle = { showSelf = !showSelf },
                        bg = Color(0xFFBFDAF7)
                    ) { 
                        Text(
                            data.selfFeedback,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1565C0)
                        ) 
                    }
                }
            } else {
                Text(
                    "이 날짜의 데이터가 없습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* --------- 작은 컴포넌트 --------- */

@Composable
private fun RatingBar(score: Int) {
    Row {
        repeat(5) { idx ->
            Icon(
                imageVector = if (idx < score) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (idx < score) Color(0xFFFFD700) else Color.LightGray,
                modifier = Modifier.size(20.dp)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title, 
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "접기" else "펼치기"
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

/* --------- 샘플 데이터 객체 (확장) --------- */
private val sampleData = mapOf(
    "2025-08-01" to DayData("05:42", 3, 
        "오늘은 소셜미디어 사용이 평소보다 높았습니다. 특히 Instagram과 YouTube 사용시간이 증가했으며, 학업 관련 앱은 상대적으로 적게 사용했습니다.", 
        "휴일이라 좀 더 여유롭게 폰을 사용했다. 유익한 콘텐츠도 있었지만 무의식적으로 스크롤한 시간이 많았던 것 같다."
    ),
    "2025-08-02" to DayData("04:58", 4, 
        "균형잡힌 사용 패턴을 보였습니다. 업무 시간에는 생산성 앱을, 여가 시간에는 엔터테인먼트 앱을 적절히 사용했습니다.", 
        "학업에 집중하며 건전하게 사용한 것 같다. 점심시간과 일과 후 유튜브로 스트레스를 잘 해소했다."
    ),
    "2025-08-03" to DayData("02:36", 0, "", "")
)

private data class DayData(
    val totalTime: String,
    val satisfaction: Int,
    val aiSummary: String,
    val selfFeedback: String
)