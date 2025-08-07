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
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.capstone_2.DB.AppDatabase
import com.example.capstone_2.data.AppInfoHelper
import com.example.capstone_2.data.AppUsageInfo
import com.example.capstone_2.DB.UsageSessionEntity
import com.example.capstone_2.ui.theme.RomanticBlue
import com.example.capstone_2.util.convertEventsToBlockEntries
import com.example.capstone_2.util.convertSessionToTimeGridMatrix
import com.example.capstone_2.util.getAppUsageSessions
import com.example.capstone_2.util.getDetailedUsageInfoPerApp
import com.example.capstone_2.util.hasUsageStatsPermission
import com.example.capstone_2.data.getLifestyleDate
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstone_2.data.AppTimeBlock
import com.example.capstone_2.viewmodel.MemoViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@Composable
fun AppUsageTrackerScreen() {
    val context = LocalContext.current
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
    val selectedDate = remember { mutableStateOf(LocalDate.now()) }
    val timeGridState = remember { mutableStateOf(BooleanArray(144) { false }) }
    val appUsageBlocksState = remember { mutableStateOf<List<AppTimeBlock>>(emptyList()) }
    val memoViewModel: MemoViewModel = viewModel()

    val db: AppDatabase = remember(context) { AppDatabase.getDatabase(context) }
    val dao = remember(db) { db.usageSessionDao() }

    LaunchedEffect(Unit) {
        hasPermission = hasUsageStatsPermission(context)
        Log.d("APP_DEBUG", "권한 상태: $hasPermission")

        if (hasPermission) {
            val sessionList = getAppUsageSessions(context, LocalDate.now())

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

    LaunchedEffect(selectedDate.value, hasPermission) {
        if (hasPermission) {
            val usageList = getDetailedUsageInfoPerApp(context, selectedDate.value)
                .sortedByDescending { it.totalTimeInForeground }

            usageInfoState.value = usageList
            totalTimeState.value = usageList.sumOf { it.totalTimeInForeground }

            appUsageBlocksState.value = usageList.map {
                val blockMatrix = convertEventsToBlockEntries(it.usageEvents, selectedDate.value, context)

                AppTimeBlock(
                    appName = it.appName,
                    totalDurationMillis = it.totalTimeInForeground,
                    usageBlocks = BooleanArray(144) { index ->
                        blockMatrix.any { entry -> entry.blockIndex == index }
                    }
                )
            }

            val filteredSessions = dao.getAllSessions().filter {
                it.getLifestyleDate() == selectedDate.value
            }

            timeGridState.value = convertSessionToTimeGridMatrix(filteredSessions, selectedDate.value)
        }
    }

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
                    1 -> DetailsTab(
                        selectedDate = selectedDate.value,
                        appUsageBlocks = appUsageBlocksState.value,
                        viewModel = memoViewModel

                    )
                    2 -> AiSummaryScreen()
                }
            }
        }
    }
}
