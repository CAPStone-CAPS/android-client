package com.example.capstone_2.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.navigation.compose.*
import com.example.capstone_2.R
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

// Data classes for API
data class CreateGroupRequest(
    val group_name: String,
    val description: String
)

data class CreateGroupResponse(
    val message: String,
    val data: GroupData?
)

data class GroupData(
    val id: Int,
    val group_name: String,
    val description: String,
    val create_date: String,
    val modify_date: String
)

data class ErrorResponse(
    val message: String,
    val data: Any?
)

// API Service
interface GroupApiService {
    @POST("/api/group")
    suspend fun createGroup(@Body request: CreateGroupRequest): CreateGroupResponse

    @GET("/api/group")
    suspend fun getGroups(): GetGroupsResponse

    @POST("/api/group/{group_id}/member/{user_id}")
    suspend fun inviteFriend(
        @retrofit2.http.Path("group_id") groupId: Int,
        @retrofit2.http.Path("user_id") username: String
    ): InviteFriendResponse

    @GET("/api/group/{group_id}/members")
    suspend fun getGroupMembers(
        @retrofit2.http.Path("group_id") groupId: Int
    ): GroupMembersResponse
}

// 그룹 목록 조회 응답
data class GetGroupsResponse(
    val message: String,
    val data: GroupsData
)

// 그룹 데이터 래퍼
data class GroupsData(
    val groups: List<GroupData>
)

// 친구 초대 응답
data class InviteFriendResponse(
    val message: String,
    val data: InvitedUserData?
)

// 초대된 사용자 데이터
data class InvitedUserData(
    val id: Int,
    val username: String
)

// 그룹 멤버 조회 응답
data class GroupMembersResponse(
    val message: String,
    val data: GroupMembersData
)

// 그룹 멤버 데이터 래퍼
data class GroupMembersData(
    val members: List<GroupMember>
)

// 그룹 멤버 정보 (API 응답 구조에 맞게 수정)
data class GroupMember(
    val user: GroupMemberUser,
    val summary: String,
    val profile_image_url: String?
)

// 그룹 멤버의 사용자 정보
data class GroupMemberUser(
    val id: Int,
    val username: String
)

// 토큰 저장을 위한 싱글톤 객체
object AuthManager {
    // TODO: JWT 토큰을 여기에 설정
    var authToken: String? = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzU0NDI0NTU1LCJpYXQiOjE3NTQ0MjQyNTUsImp0aSI6IjJlN2FjMjBkOWRkZjQxZDU5ZTlmZWM2MDhhYzBiMDg2IiwidXNlcl9pZCI6IjMifQ.SA4R3xQLXW6Xu2I7EJgzQqJn9Gn2a28wUAr3m4QnYOo"
    init {
        // 토큰이 제대로 설정되었는지 확인
        println("=== AuthManager Initialized ===")
        println("Token is set: ${authToken != null}")
        println("Token length: ${authToken?.length ?: 0}")
        if (authToken != null && authToken!!.isNotBlank()) {
            println("Token first 20 chars: ${authToken?.take(20)}...")
        }
    }
}

// Retrofit instance
object RetrofitClient {
    const val BASE_URL = "http://211.188.51.35/" // 실제 서버 URL

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        // 토큰이 있으면 헤더에 추가
        AuthManager.authToken?.let { token ->
            val authHeader = "Bearer $token"
            requestBuilder.header("Authorization", authHeader)

            // 디버깅: 실제로 추가되는 헤더 값 확인
            println("=== Adding Authorization Header ===")
            println("Token exists: true")
            println("Authorization header value: $authHeader")
            println("First 30 chars of header: ${authHeader.take(30)}...")
        } ?: run {
            println("=== WARNING: No Token Found ===")
            println("AuthManager.authToken is null!")
        }

        val request = requestBuilder.build()

        // 디버깅: 최종 요청의 모든 헤더 출력
        println("=== Final Request Headers ===")
        println("URL: ${request.url}")
        println("Method: ${request.method}")
        request.headers.forEach { (name, value) ->
            if (name == "Authorization") {
                println("$name: ${value.take(30)}...") // Authorization 헤더는 일부만 출력
            } else {
                println("$name: $value")
            }
        }

        chain.proceed(request)
    }

    val instance: GroupApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroupApiService::class.java)
    }
}

data class Group(val name: String, val memberCount: Int, val imageRes: Int)

@Composable
fun GroupScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "groupList") {
        composable("groupList") {
            GroupListScreen(
                onGroupClick = { groupName ->
                    navController.navigate("groupDetail/$groupName")
                },
                onAddGroupClick = {
                    navController.navigate("groupAdd")
                }
            )
        }

        composable("groupDetail/{groupName}") { backStackEntry ->
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "알 수 없음"
            GroupDetailScreen(
                groupName = groupName,
                onBack = { navController.popBackStack() }
            )
        }

        composable("groupAdd") {
            GroupAddScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun GroupListScreen(
    onGroupClick: (String) -> Unit,
    onAddGroupClick: () -> Unit
) {
    var groups by remember { mutableStateOf<List<GroupData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // 컴포넌트가 처음 로드될 때 그룹 목록 가져오기
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                println("=== Fetching Groups ===")
                val response = RetrofitClient.instance.getGroups()
                groups = response.data.groups
                println("Groups fetched: ${groups.size}")
                groups.forEach { group ->
                    println("Group: ${group.group_name}, ID: ${group.id}")
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                errorMessage = when (e.code()) {
                    401 -> "인증에 실패했습니다. 다시 로그인해주세요."
                    404 -> "API 경로를 찾을 수 없습니다. 서버 설정을 확인해주세요."
                    500 -> "서버 내부 오류가 발생했습니다."
                    else -> "HTTP 오류 (${e.code()}): $errorBody"
                }
                println("HTTP Error: ${e.code()}, Body: $errorBody")
            } catch (e: java.net.UnknownHostException) {
                errorMessage = "서버를 찾을 수 없습니다.\n서버 주소: ${RetrofitClient.BASE_URL}\n네트워크 연결을 확인해주세요."
                println("UnknownHostException: ${e.message}")
                e.printStackTrace()
            } catch (e: java.net.ConnectException) {
                errorMessage = "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해주세요.\nURL: ${RetrofitClient.BASE_URL}"
                println("ConnectException: ${e.message}")
                e.printStackTrace()
            } catch (e: java.net.SocketTimeoutException) {
                errorMessage = "서버 응답 시간이 초과되었습니다.\n네트워크 상태를 확인해주세요."
                println("SocketTimeoutException: ${e.message}")
                e.printStackTrace()
            } catch (e: javax.net.ssl.SSLException) {
                errorMessage = "SSL 연결 오류가 발생했습니다.\nHTTPS 설정을 확인해주세요."
                println("SSLException: ${e.message}")
                e.printStackTrace()
            } catch (e: Exception) {
                errorMessage = "네트워크 오류: ${e.javaClass.simpleName}\n${e.message}"
                println("Generic Exception: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGroupClick,
                containerColor = Color(0xFF7A67EE),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Group", tint = Color.White)
            }
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
            Text(
                text = "그룹",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 24.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                groups.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 가입한 그룹이 없습니다.\n새로운 그룹을 만들어보세요!",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        groups.forEach { group ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onGroupClick(group.group_name) }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.black),
                                    contentDescription = "Group Image",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(group.group_name, fontSize = 20.sp, color = Color.Black)
                                    Text(group.description, fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupAddScreen(onBack: () -> Unit) {
    var groupName by remember { mutableStateOf("") }
    var groupGoal by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val isValid = groupName.isNotBlank() && groupGoal.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("그룹 생성") },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("그룹 이름") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = groupGoal,
                onValueChange = { groupGoal = it },
                label = { Text("그룹 목표") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            // 에러 메시지 표시
            errorMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null

                        try {
                            // ===== 디버깅을 위한 로그 추가 =====
                            println("=== Group Creation Debug ===")
                            println("Auth Token exists: ${AuthManager.authToken != null}")
                            println("Auth Token: ${AuthManager.authToken?.take(20)}...") // 토큰의 처음 20자만 출력
                            println("Base URL: ${RetrofitClient.BASE_URL}")

                            // 토큰이 실제로 설정되었는지 확인
                            if (AuthManager.authToken == null || AuthManager.authToken == "여기에_받은_토큰을_붙여넣으세요") {
                                errorMessage = "토큰이 설정되지 않았습니다. AuthManager.authToken을 확인하세요."
                                isLoading = false
                                return@launch
                            }

                            val request = CreateGroupRequest(
                                group_name = groupName.trim(),
                                description = groupGoal.trim()
                            )

                            println("Request - Group Name: ${request.group_name}")
                            println("Request - Description: ${request.description}")
                            println("Full URL: ${RetrofitClient.BASE_URL}api/group")

                            // 실제 API 호출 부분
                            val response = RetrofitClient.instance.createGroup(request)

                            // 성공적으로 그룹이 생성되었을 때
                            println("=== Success ===")
                            println("Response: ${response.message}")
                            println("Created Group: ${response.data?.group_name}")

                            // 그룹 목록을 다시 불러오도록 첫 화면으로 이동
                            onBack()

                        } catch (e: retrofit2.HttpException) {
                            // HTTP 에러 처리 (401 Unauthorized 등)
                            val errorBody = e.response()?.errorBody()?.string()
                            errorMessage = when (e.code()) {
                                401 -> "인증에 실패했습니다. 다시 로그인해주세요."
                                404 -> "요청한 경로를 찾을 수 없습니다. API 경로를 확인해주세요."
                                500 -> "서버 오류가 발생했습니다."
                                else -> "그룹 생성에 실패했습니다. (${e.code()}: $errorBody)"
                            }
                            println("HTTP Error: ${e.code()}, $errorBody")
                        } catch (e: java.net.UnknownHostException) {
                            errorMessage = "서버를 찾을 수 없습니다. 서버 주소를 확인해주세요.\nURL: ${RetrofitClient.BASE_URL}"
                            e.printStackTrace()
                        } catch (e: java.net.SocketTimeoutException) {
                            errorMessage = "서버 응답 시간이 초과되었습니다. 네트워크 연결을 확인해주세요."
                            e.printStackTrace()
                        } catch (e: java.net.ConnectException) {
                            errorMessage = "서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요."
                            e.printStackTrace()
                        } catch (e: Exception) {
                            // 네트워크 에러 등 기타 에러 처리
                            errorMessage = "네트워크 오류: ${e.javaClass.simpleName}\n${e.message}"
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = isValid && !isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("완료")
                }
            }
        }
    }
}