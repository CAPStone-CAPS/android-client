package com.example.capstone_2.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("과거 기록") },
                    navigationIcon = {
                        if (onNavigateBack != null) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showTopBar) paddingValues else PaddingValues(0.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
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
            CalendarCard(
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
                DayDetailsCard(dayData = dayData)
            } else {
                NoDataCard(selectedDate = selectedDate)
            }
        }
    }
}

@Composable
fun CalendarCard(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    usageData: Map<LocalDate, DayUsageData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    style = MaterialTheme.typography.titleMedium,
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

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                                    isSelected -> Color(0xFF00AFF0)  // Skype Blue
                                    isToday -> Color(0xFFBFDAF7)     // Sierra Blue
                                    hasData -> Color(0xFFA6DAF4)     // Light Baby Blue
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
                                isToday -> Color(0xFF00AFF0)      // Skype Blue
                                else -> Color.Black
                            },
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayDetailsCard(dayData: DayUsageData) {
    val isToday = dayData.date == LocalDate.now()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayData.date.format(DateTimeFormatter.ofPattern("M월 d일")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "총 사용시간",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayData.totalUsage,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00AFF0)  // Skype Blue
                )

                if (!isToday && dayData.satisfactionRating > 0) {
                    RatingBar(score = dayData.satisfactionRating)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isToday && dayData.aiSummary != null) {
                var showAi by remember { mutableStateOf(false) }
                ExpandCard(
                    title = "AI 요약과 피드백 보기",
                    expanded = showAi,
                    onToggle = { showAi = !showAi },
                    bg = Color(0xFFA6DAF4)  // Light Baby Blue
                ) {
                    Text(
                        dayData.aiSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAAD1E7)  // Romantic Blue
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!isToday && dayData.selfFeedback != null) {
                var showSelf by remember { mutableStateOf(false) }
                ExpandCard(
                    title = "자가 피드백",
                    expanded = showSelf,
                    onToggle = { showSelf = !showSelf },
                    bg = Color(0xFFBFDAF7)  // Sierra Blue
                ) {
                    Text(
                        dayData.selfFeedback,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00AFF0)  // Skype Blue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "앱별 사용시간",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            dayData.apps.forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(app.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = app.usageTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NoDataCard(selectedDate: LocalDate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "이 날짜의 데이터가 없습니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("M월 d일")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatisticsTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF00AFF0)  // Skype Blue
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "통계 화면",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "사용시간 통계 그래프가 여기에 표시됩니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

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
                    contentDescription = if (expanded) "숨기기" else "펼치기"
                )
            }
        }
        if (expanded) {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFFAAD1E7)  // Romantic Blue (연한 색상)
            ) {
                Box(Modifier.padding(12.dp)) { content() }
            }
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