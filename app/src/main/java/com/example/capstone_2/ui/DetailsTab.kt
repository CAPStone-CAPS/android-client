package com.example.capstone_2.ui

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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

    Column(
        modifier = Modifier
            .fillMaxSize() // ← 전체 화면 기준
    ) {
        DateNavigationHeader(selectedDate, onDateSelected)

        Divider()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
        ) {
            AppNameList(
                appNames = appNames,
                colorMap = colorMap,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            MergedUsageGrid(
                appUsageBlocks = appUsageInfoList.map {
                    AppTimeBlock(
                        appName = it.appName,
                        usageBlocks = BooleanArray(144) { i ->
                            timeGrid.getOrNull(i)?.contains(it.appName) == true
                        }.toList()
                    )
                },
                colorMap = colorMap,
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun AppNameList(
    appNames: List<String>,
    colorMap: Map<String, Color>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(start = 8.dp)) {
        for (app in appNames) {
            val color = colorMap[app] ?: Color.Gray
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = app,
                    fontSize = 18.sp
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
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ◀ 이전 버튼
        IconButton(
            onClick = { onDateSelected(selectedDate.minusDays(1)) }
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "이전 날짜")
        }

        //가운데 날짜 텍스트 (클릭 시 다이얼로그)
        Text(
            text = selectedDate.format(dateFormatter),
            modifier = Modifier
                .weight(1f)
                .clickable { datePickerDialog.show() },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        //다음 버튼 (오늘보다 크면 비활성)
        IconButton(
            onClick = {
                if (selectedDate < today) onDateSelected(selectedDate.plusDays(1))
            },
            enabled = selectedDate < today
        ) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "다음 날짜")
        }
    }
}
