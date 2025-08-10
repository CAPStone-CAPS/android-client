package com.example.capstone_2.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    val usageTime: String,   // "HH:mm"
    val usageMinutes: Int,
    val color: Color // = MaterialTheme.colorScheme.primary
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
    onNavigateBack: (() -> Unit)? = null  // 네비게이션 콜백 추가
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // 샘플 데이터 (시연용)
    val sampleData = remember {
        mapOf(
            LocalDate.of(2025, 8, 1) to DayUsageData(
                date = LocalDate.of(2025, 8, 1),
                totalUsage = "05:42",
                totalMinutes = 342,
                satisfactionRating = 3,
                aiSummary = "오늘은 소셜미디어 사용이 평소보다 높았습니다. 특히 Instagram과 YouTube 사용시간이 증가했으며, 업무 관련 앱은 상대적으로 적게 사용했습니다.",
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
                selfFeedback = "업무에 집중하며 건전하게 사용한 것 같다. 점심시간과 퇴근 후 유튜브로 스트레스를 잘 해소했다.",
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

    // Scaffold 추가로 TopAppBar 적용
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("과거 기록") },
                navigationIcon = {onNavigateBack?.let { callback ->
                        IconButton(onClick = callback) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { 
                    Text("일별", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { 
                    Text("통계", modifier = Modifier.padding(vertical = 12.dp))
                }
            }

            when (selectedTab) {
                0 -> DailyTab(
                    selectedDate, onDateSelected = { selectedDate = it },
                    currentMonth, onMonthChanged = { currentMonth = it },
                    usageData = sampleData
                )
                else -> StatisticsTab()
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 월 네비게이션 - 제한 로직 추가
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChanged(currentMonth.minusMonths(1)) },
                    enabled = currentMonth > YearMonth.of(2020, 1) // 최소 날짜 제한
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "이전 달")
                }
                
                Text(
                    text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { onMonthChanged(currentMonth.plusMonths(1)) },
                    enabled = currentMonth < YearMonth.now() // 미래 월 제한
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "다음 달")
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
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 달력 그리드
            val firstDayOfMonth = currentMonth.atDay(1)
            val daysInMonth = currentMonth.lengthOfMonth()
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
            val today = LocalDate.now()
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(240.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false // 스크롤 비활성화
            ) {
                // 빈 칸 추가 (월 첫날이 일요일이 아닌 경우)
                items(firstDayOfWeek) {
                    Box(modifier = Modifier.size(32.dp))
                }
                
                // 실제 날짜들
                items(daysInMonth) { day ->
                    val date = currentMonth.atDay(day + 1)
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val isFuture = date.isAfter(today)
                    val hasData = usageData.containsKey(date)
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    hasData -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable(enabled = !isFuture) {
                                onDateSelected(date)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${day + 1}",
                            color = when {
                                isSelected -> Color.White
                                isFuture -> Color.Gray
                                isToday -> MaterialTheme.colorScheme.primary
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 날짜와 총 사용시간
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
                    color = Color.Gray
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
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (!isToday && dayData.satisfactionRating > 0) {
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < dayData.satisfactionRating) {
                                    Icons.Default.Star
                                } else {
                                    Icons.Default.StarBorder
                                },
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // AI 요약 (과거 날짜만)
            if (!isToday && dayData.aiSummary != null) {
                var expanded by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI 요약 피드백 보기",
                                style = MaterialTheme.typography.bodyMedium,
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
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 자가 피드백 (과거 날짜만 + selfFeedback이 있는 경우)
            if (!isToday && dayData.selfFeedback != null) {
                var selfExpanded by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFBFDAF7))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selfExpanded = !selfExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "자가 피드백",
                                style = MaterialTheme.typography.bodyMedium,
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
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 앱별 사용시간
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        color = Color.Gray
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
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "선택한 날짜의 데이터가 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("M월 d일")),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatisticsTab() {
    // 통계 화면은 간단한 플레이스홀더로 구현
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
                    tint = MaterialTheme.colorScheme.primary
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
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}