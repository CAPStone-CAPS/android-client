package com.example.capstone_2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone_2.DB.MemoEntity
import com.example.capstone_2.data.AppTimeBlock
import com.example.capstone_2.util.generateAppColorMap
import com.example.capstone_2.viewmodel.MemoViewModel
import java.time.LocalDate

@Composable
fun DetailsTab(
    selectedDate: LocalDate,
    appUsageBlocks: List<AppTimeBlock>,
    viewModel: MemoViewModel
) {
    val colorMap = remember(appUsageBlocks) { generateAppColorMap(appUsageBlocks.map { it.appName }) }

    val blockMatrix = Array(24) { Array(6) { mutableListOf<String>() } }
    for (app in appUsageBlocks) {
        for ((index, used) in app.usageBlocks.withIndex()) {
            if (used) {
                val row = index / 6
                val col = index % 6
                if (row in 0 until 24 && col in 0 until 6) {
                    blockMatrix[row][col].add(app.appName)
                }
            }
        }
    }

    val memoList = remember { mutableStateListOf<MemoEntity>() }
    var memoText by remember { mutableStateOf("") }
    var memoPosition by remember { mutableStateOf<Offset?>(null) }

    var showMemoOptions by remember { mutableStateOf(false) }
    var showMemoList by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDate) {
        viewModel.getMemosByDate(selectedDate.toString()) {
            memoList.clear()
            memoList.addAll(it)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    memoPosition = offset
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                ) {
                    items(appUsageBlocks) { appBlock ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = colorMap[appBlock.appName] ?: Color.Gray,
                                            shape = MaterialTheme.shapes.small
                                        )
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(appBlock.appName, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = millisToTimeString(appBlock.totalDurationMillis),
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Top)
            ) {
                for (row in 0 until 24) {
                    Row {
                        for (col in 0 until 6) {
                            val appNames = blockMatrix[row][col]
                            val color = if (appNames.isNotEmpty()) {
                                colorMap[appNames.last()] ?: Color.Gray
                            } else Color.White

                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(20.dp)
                                    .border(0.5.dp, Color.LightGray)
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showMemoOptions = !showMemoOptions },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.List, contentDescription = "메모 옵션 열기")
        }

        if (showMemoOptions) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 96.dp),
                horizontalAlignment = Alignment.End
            ) {
                Button(onClick = { showMemoList = true }) {
                    Text("전체 메모 보기")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    viewModel.deleteAllMemosForDate(selectedDate.toString())
                    memoList.clear()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("전체 삭제")
                }
            }
        }

        if (showMemoList) {
            AlertDialog(
                onDismissRequest = { showMemoList = false },
                title = { Text("전체 메모") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        memoList.forEach { memo ->
                            Text("${memo.appName}: ${memo.memo} [${memo.blockIndices}]")
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showMemoList = false }) {
                        Text("닫기")
                    }
                }
            )
        }

        memoPosition?.let { position ->
            AlertDialog(
                onDismissRequest = { memoPosition = null },
                confirmButton = {
                    TextButton(onClick = {
                        if (memoText.isNotBlank()) {
                            viewModel.insertMemo(
                                MemoEntity(
                                    date = selectedDate.toString(),
                                    appName = "UnknownApp",
                                    blockIndices = "",
                                    memo = memoText
                                )
                            )
                            memoText = ""
                            memoPosition = null
                        }
                    }) {
                        Text("저장")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        memoText = ""
                        memoPosition = null
                    }) {
                        Text("취소")
                    }
                },
                title = { Text("메모 추가") },
                text = {
                    OutlinedTextField(
                        value = memoText,
                        onValueChange = { memoText = it },
                        label = { Text("메모 내용을 입력하세요") }
                    )
                }
            )
        }
    }
}

fun millisToTimeString(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
