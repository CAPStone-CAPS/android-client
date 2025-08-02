package com.example.capstone_2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone_2.DB.*
import com.example.capstone_2.data.AppTimeBlock
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext

@Composable
fun MergedUsageGrid(
    appUsageBlocks: List<AppTimeBlock>,
    colorMap: Map<String, Color>,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
    val cellWidth = 24.dp
    val cellHeight = 24.dp
    val columns = 6
    val totalRows = 24
    val scrollState = rememberScrollState()

    // 시각 정렬: 06~23 → 00~05
    val visualToRealRow = List(24) { if (it < 18) it + 6 else it - 18 }

    // 시간 라벨 생성: "06", ..., "23", "00", ..., "05"
    val hourLabels = visualToRealRow.map { String.format("%02d", it) }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var memoText by remember { mutableStateOf("") }
    var showMemoDialog by remember { mutableStateOf(false) }
    val dateString = selectedDate.toString()

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    Row(
        modifier = modifier
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        // 왼쪽 시간 라벨 열
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = 4.dp),
            verticalArrangement = Arrangement.Top
        ) {
            for (hour in hourLabels) {
                Box(
                    modifier = Modifier
                        .height(cellHeight)
                        .width(28.dp), // 여유 공간
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = hour, fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }

        // 오른쪽 6x24 그리드
        Column {
            for (row in 0 until totalRows) {
                val realRow = visualToRealRow[row]
                Row {
                    for (col in 0 until columns) {
                        val realRow = visualToRealRow[row]
                        val realIndex = realRow * columns + col

                        val appsUsed = appUsageBlocks.filter {
                            it.usageBlocks.getOrNull(realIndex) == true
                        }

                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .height(cellHeight)
                                .clickable {
                                    selectedIndex = realIndex
                                    showMemoDialog = true
                                }
                                .drawBehind {
                                    if (appsUsed.isEmpty()) {
                                        drawRect(Color.Transparent)
                                    } else {
                                        val blockCount = appsUsed.size.coerceAtMost(4)
                                        val sectionWidth = size.width / blockCount

                                        appsUsed.take(4).forEachIndexed { i, app ->
                                            val color = colorMap[app.appName] ?: Color.Gray
                                            drawRect(
                                                color = color,
                                                topLeft = Offset(i * sectionWidth, 0f),
                                                size = androidx.compose.ui.geometry.Size(
                                                    width = sectionWidth,
                                                    height = size.height
                                                )
                                            )
                                        }
                                    }


                                    // 그리드 선
                                    val stroke = 1.dp.toPx()
                                    drawLine(Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f), stroke)
                                    drawLine(Color.LightGray, Offset(0f, 0f), Offset(0f, size.height), stroke)
                                    drawLine(Color.LightGray, Offset(size.width, 0f), Offset(size.width, size.height), stroke)
                                    drawLine(Color.LightGray, Offset(0f, size.height), Offset(size.width, size.height), stroke)
                                }
                        )
                    }
                }
            }
        }
    }
    if (showMemoDialog && selectedIndex != null) {
        AlertDialog(
            onDismissRequest = {
                showMemoDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        val memo = MemoEntity(
                            blockIndex = selectedIndex!!,
                            date = dateString,
                            content = memoText
                        )
                        db.memoDao().insertMemo(memo)
                        showMemoDialog = false
                        memoText = ""
                    }
                }) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMemoDialog = false
                    memoText = ""
                }) {
                    Text("취소")
                }
            },
            title = { Text("메모 작성") },
            text = {
                Column {
                    Text("해당 시간 블럭에 대한 메모를 입력하세요:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = memoText,
                        onValueChange = { memoText = it },
                        placeholder = { Text("예: 공부 집중 안 됨") },
                        maxLines = 3
                    )
                }
            }
        )
    }
}



