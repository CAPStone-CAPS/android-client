package com.example.capstone_2.ui

import com.example.capstone_2.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
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
fun GroupDetailScreen(
    groupName: String = "그룹 이름 없음",
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("요약") }
    var showInviteDialog by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var isInviting by remember { mutableStateOf(false) }
    var inviteErrorMessage by remember { mutableStateOf<String?>(null) }

    // 그룹 멤버 상태 관리
    var groupMembers by remember { mutableStateOf<List<com.example.capstone_2.ui.GroupMember>>(emptyList()) }
    var isMembersLoading by remember { mutableStateOf(true) }
    var membersErrorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("그룹원", "요약", "MVP", "투표")

    // 친구 초대 처리 함수
    val handleInviteFriend = {
        showInviteDialog = true
        username = ""
        inviteErrorMessage = null
    }

    val memberSummaries = listOf(
        MemberSummary("김철수", R.drawable.black, false, "유튜브 시청 시간 50% 감소!"),
        MemberSummary("김동국", R.drawable.black, true, "3일 연속 목표 달성 및 그룹 격려 활동!"),
        MemberSummary("박재관", R.drawable.black, false, "요약 전부 보여주기..."),
        MemberSummary("김무경", R.drawable.black, false, "요약 전부 보여주기...")
    )

    val mvp = memberSummaries.find { it.isMvp }

    // 그룹 멤버 불러오기
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isMembersLoading = true
                membersErrorMessage = null

                // 먼저 그룹 목록을 가져와서 실제 존재하는 그룹 ID를 사용
                println("그룹 목록 조회 시도...")
                val groupsResponse = com.example.capstone_2.ui.RetrofitClient.instance.getGroups()
                val groups = groupsResponse.data.groups

                if (groups.isEmpty()) {
                    membersErrorMessage = "사용 가능한 그룹이 없습니다."
                    return@launch
                }

                // 첫 번째 그룹의 ID 사용
                val groupId = groups[0].id
                println("사용할 그룹 ID: $groupId (그룹명: ${groups[0].group_name})")
                println("그룹 멤버 조회 시도: groupId=$groupId")

                val response = com.example.capstone_2.ui.RetrofitClient.instance.getGroupMembers(groupId)
                groupMembers = response.data.members

                println("그룹 멤버 조회 성공: ${groupMembers.size}명")
                groupMembers.forEach { member ->
                    println("멤버: ${member.user.username} (ID: ${member.user.id})")
                }

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                println("=== 그룹 멤버 조회 HTTP 오류 ===")
                println("HTTP 상태 코드: ${e.code()}")
                println("오류 메시지: ${e.message()}")
                println("응답 본문: $errorBody")
                println("요청 URL: ${e.response()?.raw()?.request?.url}")

                membersErrorMessage = when (e.code()) {
                    401 -> "인증에 실패했습니다. (${e.code()})"
                    403 -> "권한이 없습니다. (${e.code()})"
                    404 -> "그룹을 찾을 수 없습니다. (${e.code()})"
                    else -> "그룹 멤버 정보를 불러오는데 실패했습니다. (${e.code()})"
                }
            } catch (e: Exception) {
                membersErrorMessage = "네트워크 오류가 발생했습니다."
                e.printStackTrace()
            } finally {
                isMembersLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = handleInviteFriend) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "친구 초대",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                "그룹원" -> ApiMemberList(
                    members = groupMembers,
                    isLoading = isMembersLoading,
                    errorMessage = membersErrorMessage
                )
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

    // 친구 초대 다이얼로그
    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = {
                showInviteDialog = false
                username = ""
                inviteErrorMessage = null
            },
            title = { Text("친구 초대") },
            text = {
                Column {
                    Text(
                        text = "$groupName 그룹에 초대할 친구의 사용자명을 입력하세요.",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("사용자명") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isInviting,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    // 에러 메시지 표시
                    inviteErrorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (username.isNotBlank()) {
                            coroutineScope.launch {
                                isInviting = true
                                inviteErrorMessage = null

                                try {
                                    // user_id가 username을 의미하므로 직접 사용 가능
                                    val groupId = 1 // TODO: 실제 groupId 가져오기 (그룹 목록에서 GroupData.id 사용)

                                    println("친구 초대 시도: groupId=$groupId, username=$username")

                                    // 실제 API 호출
                                    val response = com.example.capstone_2.ui.RetrofitClient.instance.inviteFriend(groupId, username)

                                    // 성공 메시지
                                    println("친구 초대 성공: ${response.message}")
                                    println("초대된 사용자: ${response.data?.username}")
                                    showInviteDialog = false
                                    username = ""

                                } catch (e: retrofit2.HttpException) {
                                    inviteErrorMessage = when (e.code()) {
                                        401 -> "인증에 실패했습니다."
                                        403 -> "권한이 없습니다."
                                        404 -> "사용자를 찾을 수 없습니다."
                                        else -> "초대에 실패했습니다."
                                    }
                                } catch (e: Exception) {
                                    inviteErrorMessage = "네트워크 오류가 발생했습니다."
                                    e.printStackTrace()
                                } finally {
                                    isInviting = false
                                }
                            }
                        }
                    },
                    enabled = username.isNotBlank() && !isInviting
                ) {
                    if (isInviting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("초대")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showInviteDialog = false
                        username = ""
                        inviteErrorMessage = null
                    },
                    enabled = !isInviting
                ) {
                    Text("취소")
                }
            }
        )
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
fun ApiMemberList(
    members: List<com.example.capstone_2.ui.GroupMember>,
    isLoading: Boolean,
    errorMessage: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            members.isEmpty() -> {
                Text(
                    text = "그룹 멤버가 없습니다.",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                members.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.user.username,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                    }
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
