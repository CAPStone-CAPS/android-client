package com.example.capstone_2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MergedUsageGrid(
    appUsageBlocks: List<AppTimeBlock>,
    colorMap: Map<String, Color>,
    selectedDate: LocalDate,
    memoMap: Map<Int, String>,
    modifier: Modifier = Modifier
) {
    val cellWidth = 24.dp
    val cellHeight = 24.dp
    val columns = 6
    val totalRows = 24
    val scrollState = rememberScrollState()

    val density = LocalDensity.current
    val cellWidthPx = with(density) { cellWidth.toPx() }
    val cellHeightPx = with(density) { cellHeight.toPx() }

    val visualToRealRow = List(24) { if (it < 18) it + 6 else it - 18 }
    val hourLabels = visualToRealRow.map { String.format("%02d", it) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val dateString = selectedDate.toString()

    var memoText by remember { mutableStateOf("") }
    var showMemoDialog by remember { mutableStateOf(false) }

    val selectedBlocks = remember { mutableStateListOf<Int>() }

    // Offset -> 블럭 인덱스 계산 함수
    fun Offset.toBlockIndex(): Int? {
        val col = (x / cellWidthPx).toInt()
        val row = (y / cellHeightPx).toInt()
        if (col in 0 until columns && row in 0 until totalRows) {
            val realRow = visualToRealRow[row]
            return realRow * columns + col
        }
        return null
    }

    Box(
        modifier = modifier
            .padding(8.dp)
            .verticalScroll(scrollState)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        offset.toBlockIndex()?.let {
                            if (!selectedBlocks.contains(it)) selectedBlocks.add(it)
                        }
                    },
                    onDrag = { change, _ ->
                        change.position.toBlockIndex()?.let {
                            if (!selectedBlocks.contains(it)) selectedBlocks.add(it)
                        }
                    },
                    onDragEnd = {
                        if (selectedBlocks.isNotEmpty()) showMemoDialog = true
                    },
                    onDragCancel = {}
                )
            }
    ) {
        Row {
            // 시간 라벨
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(end = 4.dp)
            ) {
                for (hour in hourLabels) {
                    Box(
                        modifier = Modifier
                            .height(cellHeight)
                            .width(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(hour, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }

            // 그리드
            Column {
                for (row in 0 until totalRows) {
                    val realRow = visualToRealRow[row]
                    Row {
                        for (col in 0 until columns) {
                            val realIndex = realRow * columns + col
                            val appsUsed = appUsageBlocks.filter {
                                it.usageBlocks.getOrNull(realIndex) == true
                            }

                            val hasMemo = memoMap.containsKey(realIndex)
                            val isSelected = selectedBlocks.contains(realIndex)
                            val sorted = selectedBlocks.sorted()
                            val isFirst = sorted.firstOrNull() == realIndex
                            val isLast = sorted.lastOrNull() == realIndex

                            Box(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .height(cellHeight)
                                    .clickable {
                                        selectedBlocks.clear()
                                        selectedBlocks.add(realIndex)
                                        showMemoDialog = true
                                    }
                                    .drawBehind {
                                        val stroke = 1.dp.toPx()
                                        drawLine(Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f), stroke)
                                        drawLine(Color.LightGray, Offset(0f, 0f), Offset(0f, size.height), stroke)
                                        drawLine(Color.LightGray, Offset(size.width, 0f), Offset(size.width, size.height), stroke)
                                        drawLine(Color.LightGray, Offset(0f, size.height), Offset(size.width, size.height), stroke)

                                        if (appsUsed.isNotEmpty()) {
                                            val blockCount = appsUsed.size.coerceAtMost(4)
                                            val sectionWidth = size.width / blockCount
                                            appsUsed.take(4).forEachIndexed { i, app ->
                                                val color = colorMap[app.appName] ?: Color.Gray
                                                drawRect(
                                                    color = color,
                                                    topLeft = Offset(i * sectionWidth, 0f),
                                                    size = androidx.compose.ui.geometry.Size(sectionWidth, size.height)
                                                )
                                            }
                                        }

                                        if (hasMemo) {
                                            drawRect(Color.Red, style = Stroke(width = 2.dp.toPx()))
                                        }

                                        when {
                                            isFirst -> drawRect(Color(0xFFFFA726).copy(alpha = 0.4f))
                                            isLast -> drawRect(Color(0xFF42A5F5).copy(alpha = 0.4f))
                                            isSelected -> drawRect(Color.Yellow.copy(alpha = 0.2f))
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMemoDialog && selectedBlocks.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                showMemoDialog = false
                selectedBlocks.clear()
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        selectedBlocks.forEach { idx ->
                            db.memoDao().insertMemo(
                                MemoEntity(
                                    blockIndex = idx,
                                    date = dateString,
                                    content = memoText
                                )
                            )
                        }
                        showMemoDialog = false
                        memoText = ""
                        selectedBlocks.clear()
                    }
                }) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMemoDialog = false
                    memoText = ""
                    selectedBlocks.clear()
                }) {
                    Text("취소")
                }
            },
            title = { Text("메모 작성 (${selectedBlocks.size}개 블럭)") },
            text = {
                Column {
                    Text("선택된 블럭에 대한 메모를 입력하세요:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = memoText,
                        onValueChange = { memoText = it },
                        placeholder = { Text("예: 유튜브, 멍때림, 산책") },
                        maxLines = 3
                    )
                }
            }
        )
    }
}
