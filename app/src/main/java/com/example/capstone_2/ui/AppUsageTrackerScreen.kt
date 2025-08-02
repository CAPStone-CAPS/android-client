package com.example.capstone_2.ui

import android.content.Context
import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.capstone_2.DB.AppDatabase
import com.example.capstone_2.data.AppInfoHelper
import com.example.capstone_2.data.AppUsageInfo
import com.example.capstone_2.data.UsageSessionEntity
import com.example.capstone_2.ui.theme.RomanticBlue
import com.example.capstone_2.util.convertEventsToTimeGridMatrix
import com.example.capstone_2.util.getAppUsageSessions
import com.example.capstone_2.util.getDetailedUsageInfoPerApp
import com.example.capstone_2.util.hasUsageStatsPermission
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.time.LocalDate
import java.util.Date


@Composable
fun AppUsageTrackerScreen(context: Context) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = RomanticBlue,
            darkIcons = false
        )
    }

    var hasPermission by remember { mutableStateOf(false) }
    val usageInfoState = remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    val totalTimeState = remember { mutableStateOf(0L) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val timeGridState = remember { mutableStateOf<List<Set<String>>>(emptyList()) }

    val db = remember(context) { AppDatabase.getInstance(context) }
    val dao = remember(db) { db.usageSessionDao() }

    //STEP 1: 권한 확인 및 session 저장
    LaunchedEffect(Unit) {
        hasPermission = hasUsageStatsPermission(context)
        Log.d("APP_DEBUG", "권한 상태: $hasPermission")

        if (hasPermission) {
            val sessionList = getAppUsageSessions(context, LocalDate.now())

            // 중복 방지: 삭제 후 insert (또는 조건 insert)
            // dao.deleteSessionsForDate(...) 를 만들 수도 있음

            dao.insertSessions(
                sessionList.map {
                    UsageSessionEntity(
                        packageName = it.packageName,
                        appName = it.appName,
                        startTime = it.startTime,
                        endTime = it.endTime
                    )
                }
            )

            Log.d("SESSION", "앱 사용 세션 DB 저장 완료: ${sessionList.size}개")

            val allSessions = dao.getAllSessions()
            allSessions.forEach {
                Log.d("SESSION", "${it.appName} 사용됨: ${Date(it.startTime)} ~ ${Date(it.endTime)}")
            }
        }
    }

    //STEP 2: usage 통계 및 time grid 수집
    LaunchedEffect(selectedDate, hasPermission) {
        if (hasPermission) {
            val usageList = getDetailedUsageInfoPerApp(context, selectedDate)
                .sortedByDescending { it.totalTimeInForeground }

            usageInfoState.value = usageList
            totalTimeState.value = usageList.sumOf { it.totalTimeInForeground }

            val timeGrid = convertEventsToTimeGridMatrix(context, selectedDate)
            timeGridState.value = timeGrid

            Log.d("APP_DEBUG", "날짜 $selectedDate 의 데이터 새로 로드 완료")
        }
    }

    //UI 렌더링
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RomanticBlue)
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                TopSection(totalTimeMillis = totalTimeState.value)
            }
        },
        //bottomBar = { BottomNavBar() },
        containerColor = Color.White
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabSection(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            if (!hasPermission) {
                PermissionRequestButton(context)
            } else {
                when (selectedTab) {
                    0 -> AppUsageList(
                        usageStats = usageInfoState.value,
                        context = context
                    )
                    1 -> DetailsScreen(
                        appUsageInfoList = usageInfoState.value,
                        timeGrid = timeGridState.value,
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it }
                    )
                    2 -> Text("AI SUM 화면 (준비 중)", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
