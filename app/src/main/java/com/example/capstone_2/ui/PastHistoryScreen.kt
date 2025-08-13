package com.example.capstone_2.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview

// 🍀 데이터 모델 --------------------------------------------------

data class AppUsage(
    val appName: String,
    val usageTime: String,
    val usageMinutes: Int,
    val color: Color
)

data class DayUsageData(
    val date: LocalDate,
    val totalUsage: String,
    val totalMinutes: Int,
    val apps: List<AppUsage>,
    val aiSummary: String? = null,
    val selfFeedback: String? = null,
    val satisfactionRating: Int = 0
)

// 🍀 화면 ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: (() -> Unit)? = null,
    showTopBar: Boolean = false
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val sampleData = remember { getSampleData() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 상단 제목 (showTopBar가 false일 때만 표시)
        if (!showTopBar) {
            Text(
                text = "과거 기록",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        // 탭 섹션
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            ) {
                Text("일별", modifier = Modifier.padding(vertical = 12.dp))
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            ) {
                Text("통계", modifier = Modifier.padding(vertical = 12.dp))
            }
        }

        // 탭 내용
        when (selectedTab) {
            0 -> DailyTab(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                currentMonth = currentMonth,
                onMonthChanged = { currentMonth = it },
                usageData = sampleData
            )
            1 -> StatisticsTab()
        }
    }
}

@Composable
fun DailyTab(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    usageData: Map<LocalDate, DayUsageData>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CalendarSection(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                currentMonth = currentMonth,
                onMonthChanged = onMonthChanged,
                usageData = usageData
            )
        }

        item {
            val dayData = usageData[selectedDate]
            if (dayData != null) {
                DayDetailsSection(dayData = dayData)
            } else {
                NoDataSection(selectedDate = selectedDate)
            }
        }
    }
}

@Composable
fun CalendarSection(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    usageData: Map<LocalDate, DayUsageData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 월 네비게이션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChanged(currentMonth.minusMonths(1)) },
                    enabled = currentMonth > YearMonth.of(2020, 1)
                ) {
                    Icon(Icons.Rounded.ArrowBackIos, contentDescription = "이전 달")
                }

                Text(
                    text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { onMonthChanged(currentMonth.plusMonths(1)) },
                    enabled = currentMonth < YearMonth.now()
                ) {
                    Icon(Icons.Rounded.ArrowForwardIos, contentDescription = "다음 달")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 요일 헤더
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 달력 그리드
            val days = remember(currentMonth) {
                val first = currentMonth.atDay(1)
                val lastDay = currentMonth.lengthOfMonth()
                val offset = first.dayOfWeek.value % 7
                List(offset) { null } + (1..lastDay).map { currentMonth.atDay(it) }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(240.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false
            ) {
                items(days) { date ->
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    val isFuture = date != null && date.isAfter(LocalDate.now())
                    val hasData = date != null && usageData.containsKey(date)

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> Color(0xFF4285F4)  // 파란색
                                    isToday -> Color(0xFFE8F5E8)     // 연한 초록색
                                    hasData -> Color(0xFFE3F2FD)     // 연한 파란색
                                    else -> Color.Transparent
                                }
                            )
                            .clickable(enabled = date != null && !isFuture) {
                                date?.let { onDateSelected(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString() ?: "",
                            color = when {
                                isSelected -> Color.White
                                isFuture -> Color.Gray
                                isToday -> Color(0xFF4285F4)
                                else -> Color.Black
                            },
                            fontSize = 14.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayDetailsSection(dayData: DayUsageData) {
    val isToday = dayData.date == LocalDate.now()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 날짜와 총 사용시간
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dayData.date.monthValue}월 ${dayData.date.dayOfMonth}일",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "총 사용시간",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 사용시간과 만족도
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayData.totalUsage,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4285F4)
                )

                if (!isToday && dayData.satisfactionRating > 0) {
                    RatingStars(score = dayData.satisfactionRating)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI 요약 (과거 날짜만)
            if (!isToday && dayData.aiSummary != null) {
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI 요약 피드백 보기",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dayData.aiSummary,
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 자가 피드백 (과거 날짜만)
            if (!isToday && dayData.selfFeedback != null) {
                var selfExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selfExpanded = !selfExpanded },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "자가 피드백",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (selfExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        if (selfExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dayData.selfFeedback,
                                fontSize = 13.sp,
                                color = Color(0xFF1565C0),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 앱별 사용시간
            Text(
                text = "앱별 사용시간",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 앱 리스트
            dayData.apps.forEach { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = app.color,
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = app.usageTime,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoDataSection(selectedDate: LocalDate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "선택한 날짜의 데이터가 없습니다",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("M월 d일")),
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 기존 StatisticsTab() 함수를 아래 코드로 완전히 교체하세요
// 그리고 맨 아래 헬퍼 함수들도 추가해주세요

@Composable
fun StatisticsTab() {
    var selectedPeriod by remember { mutableStateOf("주간") }
    val sampleData = remember { getSampleData() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 기간 선택
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("주간", "월간", "3개월").forEach { period ->
                        FilterChip(
                            onClick = { selectedPeriod = period },
                            label = { Text(period) },
                            selected = selectedPeriod == period
                        )
                    }
                }
            }
        }

        item {
            // 총 사용시간 요약
            val totalMinutes = sampleData.values.sumOf { it.totalMinutes }
            val avgDailyMinutes = if (sampleData.isNotEmpty()) totalMinutes / sampleData.size else 0

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "$selectedPeriod 사용 요약",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 총 사용시간
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatMinutesToTime(totalMinutes),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4)
                            )
                            Text(
                                text = "총 사용시간",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }

                        // 일평균 사용시간
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatMinutesToTime(avgDailyMinutes),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34A853)
                            )
                            Text(
                                text = "일평균",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }

                        // 기록된 일수
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${sampleData.size}일",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA4335)
                            )
                            Text(
                                text = "기록 일수",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }

        item {
            // 앱별 사용시간 차트
            val appUsageMap = mutableMapOf<String, Int>()
            sampleData.values.forEach { dayData ->
                dayData.apps.forEach { app ->
                    appUsageMap[app.appName] = (appUsageMap[app.appName] ?: 0) + app.usageMinutes
                }
            }

            val topApps = appUsageMap.entries
                .sortedByDescending { it.value }
                .take(5)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "앱별 사용시간 순위",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    topApps.forEachIndexed { index, (appName, minutes) ->
                        val percentage = if (appUsageMap.values.sum() > 0) {
                            (minutes.toFloat() / appUsageMap.values.sum() * 100).toInt()
                        } else 0

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(
                                                color = getAppColor(appName),
                                                shape = CircleShape
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = appName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = formatMinutesToTime(minutes),
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // 프로그레스 바
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(
                                        color = Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(percentage / 100f)
                                        .height(8.dp)
                                        .background(
                                            color = getAppColor(appName),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                            }

                            if (index < topApps.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            // 일별 사용시간 추세
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "일별 사용시간 추세",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val sortedData = sampleData.entries.sortedBy { it.key }
                    val maxMinutes = sortedData.maxOfOrNull { it.value.totalMinutes } ?: 1

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sortedData.forEach { (date, dayData) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${date.monthValue}/${date.dayOfMonth}",
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(40.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(20.dp)
                                        .background(
                                            color = Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(dayData.totalMinutes.toFloat() / maxMinutes)
                                            .fillMaxHeight()
                                            .background(
                                                color = Color(0xFF4285F4),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = dayData.totalUsage,
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(50.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // 사용 패턴 분석
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "사용 패턴 분석",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 평균 만족도
                    val satisfactionData = sampleData.values.filter { it.satisfactionRating > 0 }
                    if (satisfactionData.isNotEmpty()) {
                        val avgSatisfaction = satisfactionData.map { it.satisfactionRating }.average()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "평균 만족도",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = if (index < avgSatisfaction.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (index < avgSatisfaction.toInt()) Color(0xFFFFD700) else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 주요 인사이트
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "💡 인사이트",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "YouTube가 가장 많이 사용된 앱입니다. 평균적으로 하루 1시간 20분 정도 사용하고 있어요.",
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 아래 헬퍼 함수들을 파일 맨 아래에 추가해주세요

private fun formatMinutesToTime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        String.format("%02d:%02d", hours, mins)
    } else {
        String.format("00:%02d", mins)
    }
}

private fun getAppColor(appName: String): Color {
    return when (appName) {
        "Instagram" -> Color(0xFFE4405F)
        "YouTube" -> Color(0xFFFF0000)
        "KakaoTalk" -> Color(0xFFFEE500)
        "Chrome" -> Color(0xFF4285F4)
        "Spotify" -> Color(0xFF1DB954)
        "Slack" -> Color(0xFF4A154B)
        "Notion" -> Color(0xFF000000)
        "Gmail" -> Color(0xFFEA4335)
        else -> Color(0xFF8E8E93)
    }
}

@Composable
private fun RatingStars(score: Int) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < score) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (index < score) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun getSampleData(): Map<LocalDate, DayUsageData> {
    return mapOf(
        LocalDate.of(2025, 8, 1) to DayUsageData(
            date = LocalDate.of(2025, 8, 1),
            totalUsage = "05:42",
            totalMinutes = 342,
            satisfactionRating = 3,
            aiSummary = "오늘은 소셜미디어 사용이 평소보다 높았습니다. 특히 Instagram과 YouTube 사용시간이 증가했으며, 학업 관련 앱은 상대적으로 적게 사용했습니다.",
            selfFeedback = "휴일이라 좀 더 여유롭게 폰을 사용했다. 유익한 콘텐츠도 있었지만 무의식적으로 스크롤한 시간이 많았던 것 같다.",
            apps = listOf(
                AppUsage("Instagram", "02:00", 120, Color(0xFFE4405F)),
                AppUsage("YouTube", "01:30", 90, Color(0xFFFF0000)),
                AppUsage("KakaoTalk", "00:45", 45, Color(0xFFFEE500)),
                AppUsage("Chrome", "00:30", 30, Color(0xFF4285F4)),
                AppUsage("Spotify", "00:25", 25, Color(0xFF1DB954)),
                AppUsage("Settings", "00:12", 12, Color(0xFF8E8E93))
            )
        ),
        LocalDate.of(2025, 8, 2) to DayUsageData(
            date = LocalDate.of(2025, 8, 2),
            totalUsage = "04:58",
            totalMinutes = 298,
            satisfactionRating = 4,
            aiSummary = "균형잡힌 사용 패턴을 보였습니다. 업무 시간에는 생산성 앱을, 여가 시간에는 엔터테인먼트 앱을 적절히 사용했습니다.",
            selfFeedback = "학업에 집중하며 건전하게 사용한 것 같다. 점심시간과 일과 후 유튜브로 스트레스를 잘 해소했다.",
            apps = listOf(
                AppUsage("YouTube", "01:15", 75, Color(0xFFFF0000)),
                AppUsage("Slack", "01:00", 60, Color(0xFF4A154B)),
                AppUsage("KakaoTalk", "00:52", 52, Color(0xFFFEE500)),
                AppUsage("Chrome", "00:48", 48, Color(0xFF4285F4)),
                AppUsage("Instagram", "00:39", 39, Color(0xFFE4405F)),
                AppUsage("Notion", "00:24", 24, Color(0xFF000000))
            )
        ),
        LocalDate.of(2025, 8, 3) to DayUsageData(
            date = LocalDate.of(2025, 8, 3),
            totalUsage = "02:36",
            totalMinutes = 156,
            apps = listOf(
                AppUsage("KakaoTalk", "00:42", 42, Color(0xFFFEE500)),
                AppUsage("Chrome", "00:38", 38, Color(0xFF4285F4)),
                AppUsage("YouTube", "00:31", 31, Color(0xFFFF0000)),
                AppUsage("Instagram", "00:25", 25, Color(0xFFE4405F)),
                AppUsage("Settings", "00:12", 12, Color(0xFF8E8E93)),
                AppUsage("Gmail", "00:08", 8, Color(0xFFEA4335))
            )
        )
    )
}

// 🍀 Preview -------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    HistoryScreen()
}

@Preview(showBackground = true)
@Composable
fun DayDetailsSectionPreview() {
    DayDetailsSection(
        dayData = DayUsageData(
            date = LocalDate.of(2025, 8, 2),
            totalUsage = "04:58",
            totalMinutes = 298,
            satisfactionRating = 4,
            aiSummary = "균형잡힌 사용 패턴을 보였습니다. 업무 시간에는 생산성 앱을, 여가 시간에는 엔터테인먼트 앱을 적절히 사용했습니다.",
            selfFeedback = "학업에 집중하며 건전하게 사용한 것 같다. 점심시간과 일과 후 유튜브로 스트레스를 잘 해소했다.",
            apps = listOf(
                AppUsage("YouTube", "01:15", 75, Color(0xFFFF0000)),
                AppUsage("KakaoTalk", "00:52", 52, Color(0xFFFEE500)),
                AppUsage("Chrome", "00:48", 48, Color(0xFF4285F4)),
                AppUsage("Instagram", "00:39", 39, Color(0xFFE4405F))
            )
        )
    )
}