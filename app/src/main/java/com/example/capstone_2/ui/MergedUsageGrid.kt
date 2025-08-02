package com.example.capstone_2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone_2.data.AppTimeBlock

@Composable
fun MergedUsageGrid(
    appUsageBlocks: List<AppTimeBlock>,
    colorMap: Map<String, Color>,
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
                        val index = realRow * columns + col

                        val appsUsed = appUsageBlocks.filter {
                            it.usageBlocks.getOrNull(index) == true
                        }

                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .height(cellHeight)
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
}

