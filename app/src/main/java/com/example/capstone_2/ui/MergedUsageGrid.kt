package com.example.capstone_2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MergedUsageGrid(
    timeBlocks: List<Set<String>>,
    colorMap: Map<String, Color>,
    modifier: Modifier = Modifier
) {
    val columns = 6
    val rows = 24
    val cellWidth = 24.dp  //  칸 하나의 너비 지정 (좁게)

    Column(
        modifier = modifier
            .padding(8.dp)
            .wrapContentWidth()
            .fillMaxHeight()
    ) {
        for (hour in 0 until 24) {
            Row(modifier = Modifier.wrapContentWidth()) {
                for (tenMin in 0 until 6) {
                    val index = hour * 6 + tenMin
                    val appsInBlock = timeBlocks.getOrNull(index).orEmpty()

                    val color = when {
                        appsInBlock.isEmpty() -> Color.LightGray.copy(alpha = 0.1f)
                        appsInBlock.size == 1 -> colorMap[appsInBlock.first()] ?: Color.Blue
                        else -> Color.Magenta
                    }

                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .height(24.dp)
                            //.border(0.5.dp, Color.DarkGray)
                            .background(color)
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                val borderColor = Color.Black

                                // 바깥쪽 선도 포함한 완전한 그리드
                                // top
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = strokeWidth
                                )
                                // left
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = strokeWidth
                                )
                                // right
                                drawLine(
                                    color = borderColor,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                                // bottom
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                    )
                }
            }
        }
    }

}
