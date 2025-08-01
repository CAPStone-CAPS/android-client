package com.example.capstone_2.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.capstone_2.data.AppInfoHelper
import com.example.capstone_2.data.AppUsageInfo
import com.example.capstone_2.ui.theme.RomanticBlue
import com.example.capstone_2.util.convertEventsToTimeGridMatrix
import com.example.capstone_2.util.getDetailedUsageInfoPerApp
import com.example.capstone_2.util.hasUsageStatsPermission
import java.time.LocalDate


@Composable
fun AppUsageTrackerScreen(context: Context) {
    var hasPermission by remember { mutableStateOf(false) }
    val usageInfoState = remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    val totalTimeState = remember { mutableStateOf(0L) }
    var selectedTab by remember { mutableStateOf(0) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val timeGridState = remember { mutableStateOf<List<Set<String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        hasPermission = hasUsageStatsPermission(context)
        Log.d("APP_DEBUG", "권한 상태: $hasPermission")

        if (hasPermission) {
            val usageList = getDetailedUsageInfoPerApp(context, selectedDate)
                .sortedByDescending { it.totalTimeInForeground }

            usageInfoState.value = usageList
            totalTimeState.value = usageList.sumOf { it.totalTimeInForeground }

            val timeGrid = convertEventsToTimeGridMatrix(context, selectedDate)
            timeGridState.value = timeGrid

            Log.d("APP_DEBUG", "날짜 ${selectedDate}의 데이터 새로 로드 완료")

        }
    }

    LaunchedEffect(selectedDate, hasPermission) {
        if (hasPermission) {
            val timeGrid = convertEventsToTimeGridMatrix(context, selectedDate)
            timeGridState.value = timeGrid
        }
    }

    Scaffold(
        topBar = { TopSection(totalTimeMillis = totalTimeState.value) },
        bottomBar = { BottomNavBar() }
        //containerColor = RomanticBlue
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

                    1 -> {
                        DetailsScreen(
                            appUsageInfoList = usageInfoState.value,
                            timeGrid = timeGridState.value,
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it }
                        )
                    }

                    2 -> Text("AI SUM 화면 (준비 중)", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
