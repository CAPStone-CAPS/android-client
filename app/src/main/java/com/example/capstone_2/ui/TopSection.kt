package com.example.capstone_2.ui

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
import java.util.*

@Composable
fun TopSection(totalTimeMillis: Long) {
    // 사용 시간 형식으로 변환 (HH:mm:ss)
    val hours = totalTimeMillis / 1000 / 60 / 60
    val minutes = (totalTimeMillis / 1000 / 60) % 60
    val seconds = (totalTimeMillis / 1000) % 60
    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    // 오늘 날짜 형식 (e.g. 07-29 MON)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            //.background(RomanticBlue)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(today, fontSize = 20.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(formattedTime, fontSize = 32.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(24.dp))

//        Box {
//            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
//            Box(
//                modifier = Modifier
//                    .offset(x = 10.dp, y = (-4).dp)
//                    .background(Color.Magenta, CircleShape)
//                    .size(18.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("4", color = Color.White, fontSize = 10.sp)
//            }
//        }
    }
}
