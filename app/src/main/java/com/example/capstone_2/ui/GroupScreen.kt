package com.example.capstone_2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone_2.data.Group

@Composable
fun GroupScreen() {
    val groupList = listOf(
        Group("유튜브 줄이기", 12, android.R.drawable.ic_menu_camera),
        Group("인스타 1시간 이하", 8, android.R.drawable.ic_menu_gallery)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text("참여 중인 그룹", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(12.dp))

        groupList.forEach { group ->
            GroupCard(group)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun GroupCard(group: Group) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: 상세 페이지 이동 */ },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = group.imageResId),
                contentDescription = group.title,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(group.title, fontSize = 18.sp)
                Text("멤버 수: ${group.memberCount}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
