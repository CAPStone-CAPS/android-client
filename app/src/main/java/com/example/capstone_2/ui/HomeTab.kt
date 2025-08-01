package com.example.capstone_2.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.capstone_2.data.AppUsageInfo
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip



@Composable
fun AppUsageList(usageStats: List<AppUsageInfo>, context: Context) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(usageStats) { stat ->
            val totalSeconds = stat.totalTimeInForeground / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            val formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            AppUsageItem(
                appName = stat.appName,
                usageTime = formatted,
                icon = stat.appIcon
            )
        }
    }
}

@Composable
fun AppUsageItem(appName: String, usageTime: String, icon: Drawable?) {
    val bitmap = remember(icon) { icon?.toBitmap()?.asImageBitmap() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "$appName icon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = appName,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = usageTime,
            fontSize = 14.sp
        )
    }
}
