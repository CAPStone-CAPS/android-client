package com.example.capstone_2.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone_2.ui.theme.RomanticBlue
import com.example.capstone_2.ui.theme.SkypeBlue
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.util.*

@Composable
fun TopSection(totalTimeMillis: Long) {
    val context = LocalContext.current
    val hours = totalTimeMillis / 1000 / 60 / 60
    val minutes = (totalTimeMillis / 1000 / 60) % 60
    val seconds = (totalTimeMillis / 1000) % 60
    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val today = remember {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)
        String.format(
            "%02d-%02d %s",
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            dayOfWeek?.uppercase() ?: ""
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(RomanticBlue)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // 왼쪽 아이콘
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // 중앙 날짜 + 시간
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(today, fontSize = 20.sp, textAlign = TextAlign.Center, color = Color.White)
            Text(formattedTime, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // 오른쪽 설정 아이콘
        IconButton(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "설정")
        }
    }
}
