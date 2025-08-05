package com.example.capstone_2.ui

import android.R.attr.maxLines
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone_2.data.*
import com.example.capstone_2.util.generateAppColorMap
import java.util.*
import kotlin.math.absoluteValue
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import com.example.capstone_2.DB.AppDatabase
import com.example.capstone_2.DB.MemoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


fun extractAppNames(timeGrid: List<Set<String>>): List<String> {
    return timeGrid.flatten().toSet().sorted()
}
fun getAppColor(appName: String): Color {
    val colors = listOf(
        Color(0xFF64B5F6),
        Color(0xFF81C784),
        Color(0xFFFFB74D),
        Color(0xFFBA68C8),
        Color(0xFFFF8A65),
        Color(0xFFA1887F),
        Color(0xFFFFD54F),
        Color(0xFF4DD0E1),
        Color(0xFF7986CB),
        Color(0xFF90A4AE)
    )
    val index = (appName.hashCode().absoluteValue) % colors.size
    return colors[index]
}

// DetailsTab.kt

@Composable
fun DetailsScreen(
    appUsageInfoList: List<AppUsageInfo>,
    timeGrid: List<Set<String>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val appNames = appUsageInfoList
        .sortedByDescending { it.totalTimeInForeground }
        .map { it.appName }
    val colorMap = remember(appNames) { generateAppColorMap(appNames) }

    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    var showAppList by remember { mutableStateOf(false) }
    var showAllMemos by remember { mutableStateOf(false) }
    var memoList by remember { mutableStateOf(listOf<MemoEntity>()) }
    var selectedApp by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val dateString = selectedDate.toString()

    val appUsageBlocks = appUsageInfoList.map {
        AppTimeBlock(
            appName = it.appName,
            usageBlocks = BooleanArray(144) { i ->
                timeGrid.getOrNull(i)?.contains(it.appName) == true
            }.toList()
        )
    }

    val filteredBlocks = if (selectedApp == null) appUsageBlocks
    else appUsageBlocks.filter { it.appName == selectedApp }

    val memoMap = remember(memoList, selectedDate) {
        memoList.associateBy({ it.blockIndex }, { it.content })
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        DateNavigationHeader(selectedDate, onDateSelected)
        Divider()

        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, top = 8.dp)
                    .widthIn(min = 140.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Button(onClick = { showAppList = !showAppList }) {
                    Text(if (showAppList) "앱 리스트 숨기기" else "앱 리스트 보기")
                }

                if (showAppList) {
                    AppNameList(
                        appNames = appNames,
                        colorMap = colorMap,
                        selectedApp = selectedApp,
                        onAppClick = { clicked ->
                            selectedApp = if (selectedApp == clicked) null else clicked
                        },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .heightIn(max = 240.dp)
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val memos = withContext(Dispatchers.IO) {
                                db.memoDao().getMemosByDate(dateString)
                            }
                            memoList = memos
                            showAllMemos = true
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("전체 메모 보기")
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("전체 메모 초기화")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Divider(
                    color = Color.LightGray,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .fillMaxHeight()
                ) {
                    MergedUsageGrid(
                        appUsageBlocks = filteredBlocks,
                        colorMap = colorMap,
                        selectedDate = selectedDate,
                        memoMap = memoMap
                    )
                }
            }
        }

        if (showAllMemos) {
            AlertDialog(
                onDismissRequest = { showAllMemos = false },
                confirmButton = {
                    TextButton(onClick = { showAllMemos = false }) {
                        Text("닫기")
                    }
                },
                title = { Text("전체 메모 목록") },
                text = {
                    if (memoList.isEmpty()) {
                        Text("해당 날짜에 작성된 메모가 없습니다.")
                    } else {
                        Column(modifier = Modifier.heightIn(max = 300.dp)) {
                            memoList.sortedBy { it.blockIndex }.forEach { memo ->
                                Text(
                                    text = "⌚ ${memo.blockIndex}번 블럭 - ${memo.content}",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                db.memoDao().deleteMemosByDate(dateString)
                            }
                            memoList = emptyList()
                            showDeleteDialog = false
                        }
                    }) {
                        Text("삭제")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("취소")
                    }
                },
                title = { Text("메모 초기화") },
                text = { Text("해당 날짜의 모든 메모를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.") }
            )
        }


    }
}


@Composable
fun AppNameList(
    appNames: List<String>,
    colorMap: Map<String, Color>,
    selectedApp: String?,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(appNames) { app ->
            val color = colorMap[app] ?: Color.Gray
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = app,
                    fontSize = 16.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DateNavigationHeader(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()

    val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREA)

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = LocalDate.of(year, month + 1, dayOfMonth)
                if (picked <= today) onDateSelected(picked)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).apply {
            datePicker.maxDate = today.toEpochDay() * 24 * 60 * 60 * 1000
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //이전 버튼
        IconButton(
            onClick = { onDateSelected(selectedDate.minusDays(1)) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "이전 날짜"
            )
        }

        //가운데 날짜 텍스트 (클릭 시 다이얼로그)
        Text(
            text = selectedDate.format(dateFormatter),
            modifier = Modifier
                .weight(1f)
                .clickable { datePickerDialog.show() },
            style = MaterialTheme.typography.titleLarge,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        //다음 버튼 (오늘보다 크면 비활성)
        IconButton(
            onClick = {
                if (selectedDate < today) onDateSelected(selectedDate.plusDays(1))
            },
            enabled = selectedDate < today
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "다음 날짜"
            )
        }
    }
}

