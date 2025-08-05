package com.example.capstone_2.ui

import com.example.capstone_2.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone_2.ui.theme.SkypeBlue

data class MemberSummary(
    val name: String,
    val profileImageRes: Int,
    val isMvp: Boolean,
    val summary: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(groupName: String = "그룹 이름 없음", onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf("요약") }

    val tabs = listOf("그룹원", "요약", "MVP", "투표")

    val memberSummaries = listOf(
        MemberSummary("김철수", R.drawable.black, false, "유튜브 시청 시간 50% 감소!"),
        MemberSummary("김동국", R.drawable.black, true, "3일 연속 목표 달성 및 그룹 격려 활동!"),
        MemberSummary("박재관", R.drawable.black, false, "요약 전부 보여주기..."),
        MemberSummary("김무경", R.drawable.black, false, "요약 전부 보여주기...")
    )

    val mvp = memberSummaries.find { it.isMvp }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("이번 주의 MVP", fontSize = 14.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = mvp?.profileImageRes ?: R.drawable.black),
                    contentDescription = "MVP Image",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                )
            }
            Text(mvp?.name ?: "", fontSize = 14.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEach { tab ->
                    Text(
                        text = tab,
                        fontSize = 16.sp,
                        color = if (selectedTab == tab) SkypeBlue else Color.Gray,
                        modifier = Modifier.clickable { selectedTab = tab }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                "그룹원" -> MemberList(members = memberSummaries)
                "요약" -> LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(memberSummaries.size) { index ->
                        MVPCard(summary = memberSummaries[index])
                    }
                }
            }
        }
    }
}

@Composable
fun MemberList(members: List<MemberSummary>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        members.forEach { member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(member.name, fontSize = 18.sp, color = Color.Black)
                if (member.isMvp) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🌟 MVP", color = Color(0xFF7A67EE), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun MVPCard(summary: MemberSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = summary.profileImageRes),
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = summary.name + if (summary.isMvp) " 🌟 MVP" else "",
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summary.summary,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
