package com.example.capstone_2.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
// import coil3.compose.AsyncImage // Coil 라이브러리를 추가하면 자꾸 오류가 발생해서 일단 비활성화.
import com.example.capstone_2.R
import com.example.capstone_2.data.AppCategorySettings
import com.example.capstone_2.retrofit.LoginService
import com.example.capstone_2.retrofit.NullableUserRequest
import com.example.capstone_2.retrofit.RetrofitInstance
import com.example.capstone_2.retrofit.UserRequest
import com.example.capstone_2.ui.theme.CapstoneTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CapstoneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginMypageScreen(
                        modifier = Modifier.padding(innerPadding),
                        onMoveToGroupScreen = {}
                    )
                }
            }
        }
    }
}

// 토큰 저장은 GroupScreen.kt의 AuthManager를 이용..
var currentRefresh: String? = null

val loggedInUser = mutableMapOf<String, String>(
    "id" to "0", "username" to "testuser"
)

/*
* 랜딩 페이지가 없으므로, 로그인 상태라면 마이페이지, 그렇지 않으면 로그인 페이지가 나오도록 한다.
* */

@Composable
fun LoginMypageScreen(modifier: Modifier = Modifier, onMoveToGroupScreen: () -> Unit) {
    var userLoggedIn by rememberSaveable { mutableStateOf(false) }
    var appSettingsPageOpen by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = modifier) {
        if(userLoggedIn) {
            if(appSettingsPageOpen) {
                AppSettingsScreen (
                    onAppSettingsClose = { appSettingsPageOpen = false }
                )
            } else {
                MyPageScreen(
                    onAppSettingsOpen = { appSettingsPageOpen = true },
                    onLogout = { userLoggedIn = false },
                    onMoveToGroupScreen = onMoveToGroupScreen
                )
            }
        } else {
            LoginScreen(
                onLogin = { userLoggedIn = true }
            )
        }
    }
}

class LoginViewModel : ViewModel() {
    var retrofitInstance = RetrofitInstance.getRetrofitInstance().create(LoginService::class.java)

    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var loginSuccess by mutableStateOf(false)
        private set

    fun login() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = retrofitInstance.login(UserRequest(username, password))
                if (response.isSuccessful) {
                    AuthManager.authToken = response.body()!!.data.accessToken
                    currentRefresh = response.body()!!.data.refresh
                    Log.d("LOGIN", "remote Token: ${response.body()!!.data.accessToken}")
                    Log.d("LOGIN", "currentToken: ${AuthManager.authToken}")
                    // 로그인 성공 시 전역 AuthManager에 토큰 저장 (Group API 인터셉터에서 사용)
                    loginSuccess = true
                } else {
                    errorMessage = "로그인 실패 (${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "오류 발생: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun signup() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = retrofitInstance.signup(UserRequest(username, password))
                if (response.isSuccessful) {
                    val loginResponse = retrofitInstance.login(UserRequest(username, password))
                    if (loginResponse.isSuccessful) {
                        AuthManager.authToken = loginResponse.body()!!.data.accessToken
                        currentRefresh = loginResponse.body()!!.data.refresh
                        // 회원가입 직후 자동 로그인 성공 시 토큰 저장
                        loginSuccess = true
                    } else {
                        errorMessage = "회원가입에 성공했으나 로그인에 실패하였습니다. (${loginResponse.code()})"
                    }
                } else {
                    errorMessage = "회원가입 실패 (${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "오류 발생: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun reset() {
        if(AuthManager.authToken == null){
            loginSuccess = false
        }
        Log.d("LOGIN", "로그인 페이지 리셋 완료...")
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit,
    viewModel: LoginViewModel = viewModel()
    ) {
    val username = viewModel.username
    val password = viewModel.password
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val loginSuccess = viewModel.loginSuccess

    var needReset = true

    // 로그아웃해서 다시 이 화면으로 돌아오면 리셋.... (토큰을 지우고 loginSuccess를 false로 설정.)
    if(needReset) {
        needReset = false
        viewModel.reset()
    }

    if (loginSuccess) {
        // 로그인 성공 시 콜백 호출
        LaunchedEffect(Unit) {
            viewModel.username = ""
            viewModel.password = ""
            onLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("로그인")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { viewModel.username = it },
            label = { Text("아이디") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.password = it },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(errorMessage)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.login() },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("로그인")
            }
        }

        Button(
            onClick = { viewModel.signup() },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("회원가입하고 로그인")
            }
        }
    }
}

class MyPageViewModel : ViewModel() {
    var retrofitInstance = RetrofitInstance.getRetrofitInstance().create(LoginService::class.java)

    var username by mutableStateOf("")
    var newUsername by mutableStateOf("")
    var profileimageExists by mutableStateOf(false)
    var profileURL by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun getUser() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                Log.d("GETUSER", "Token: ${AuthManager.authToken} 으로 요청...")
                val response = retrofitInstance.getUser("Bearer ${ AuthManager.authToken!! }")
                if(response.isSuccessful) {
                    Log.d("GETUSER", "Username: ${response.body()!!.data.username}")
                    username = response.body()!!.data.username
                    if(response.body()!!.data.profile_image_url != null) {
                        profileimageExists = true
                        profileURL = response.body()!!.data.profile_image_url!!
                    } else {
                        profileimageExists = false
                    }

                    loggedInUser.put("id", response.body()!!.data.id.toString())
                    loggedInUser.put("username", response.body()!!.data.username)

                } else {
                    Log.e("GETUSER", "요청 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "오류 발생: ${e.localizedMessage}"
                Log.e("GETUSER", "오류 발생: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }
/*
    fun changeUserName() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = retrofitInstance.editUser("Bearer ${ AuthManager.authToken!! }", NullableUserRequest(username, null))
                if(response.isSuccessful) {
                    username = response.body()!!.data.username
                } else {
                    errorMessage = "이름 변경에 실패하였습니다. (${response.code()})"
                    Log.e("EDITUSER", errorMessage!!)
                }
            } catch (e: Exception) {
                errorMessage = "오류 발생: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
*/

    fun changeUserName(changedUserName: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = retrofitInstance.editUser("Bearer ${ AuthManager.authToken!! }", NullableUserRequest(changedUserName, null))
                if(response.isSuccessful) {
                    username = response.body()!!.data.username
                    Log.d("EDITUSER", "유저네임 변경 성공. 새 유저네임: ${username}")
                } else {
                    errorMessage = "이름 변경에 실패하였습니다. (${response.code()})"
                    Log.e("EDITUSER", errorMessage!!)
                }
            } catch (e: Exception) {
                errorMessage = "오류 발생: ${e.localizedMessage}"
                Log.e("EDITUSER", errorMessage!!)
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun MyPageScreen(onAppSettingsOpen: () -> Unit, onLogout: () -> Unit, onMoveToGroupScreen: () -> Unit, viewModel: MyPageViewModel = viewModel()) {
    val username = viewModel.username
    var newUsername = viewModel.newUsername
    val profileimageExists = viewModel.profileimageExists
    val profileURL = viewModel.profileURL
    val errorMessage = viewModel.errorMessage
    val isLoading = viewModel.isLoading

    var userRetrieved by remember { mutableStateOf(false) }
    var usernameEditDialogOpen by remember { mutableStateOf(false) }
    var profileEditDialogOpen by remember { mutableStateOf(false) }
    if(userRetrieved == false) {
        Log.d("MYPAGE", "getUser 요청 송신...")
        viewModel.getUser()
        userRetrieved = true
    }


    Surface(Modifier.fillMaxSize()) {
        Column(Modifier) {
            if(profileimageExists) { // Coil 라이브러리를 추가하기만 하면 자꾸 원인 불명의 오류가 발생한다....
                /*
                AsyncImage(
                    model = profileURL,
                    contentDescription = "프로필 사진",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(210.dp, 260.dp)
                        .padding(start = 30.dp, end = 30.dp, top = 100.dp, bottom = 10.dp)
                )
                */
                Image(
                    painter = painterResource(id = R.drawable.profile_placeholder),
                    contentDescription = username + "님의 프로필 사진",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(210.dp, 260.dp)
                        .padding(start = 30.dp, end = 30.dp, top = 100.dp, bottom = 10.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile_placeholder),
                    contentDescription = username + "님의 프로필 사진",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(210.dp, 260.dp)
                        .padding(start = 30.dp, end = 30.dp, top = 100.dp, bottom = 10.dp)
                )
            }
            Row() {
                Text(
                    text = username,
                    style = TextStyle(
                        fontSize = 48.sp
                    ),
                    modifier = Modifier
                        .padding(start = 30.dp, bottom = 30.dp)
                        .clickable(
                            onClick = {
                                usernameEditDialogOpen = true
                            }
                        )
                )
            }
            /*
            HorizontalDivider(
                thickness = 2.dp
            )
            */
            Row(modifier = Modifier.padding(30.dp)) {
                ElevatedButton(
                    onClick = {
                        onMoveToGroupScreen()
                    }
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "사람 아이콘",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp)
                    )
                }
                Text(
                    text = "그룹",
                    modifier = Modifier
                        .padding(start = 50.dp)
                        .align(Alignment.CenterVertically),
                    style = TextStyle(
                        fontSize = 36.sp
                    )
                )
            }
            /*
            HorizontalDivider(
                thickness = 2.dp
            )
            */
            Row(modifier = Modifier.padding(30.dp)) {
                ElevatedButton(
                    onClick = {
                        onAppSettingsOpen()
                    }
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "설정 아이콘",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp)
                    )
                }
                Text(
                    text = "앱별 설정",
                    modifier = Modifier
                        .padding(start = 50.dp)
                        .align(Alignment.CenterVertically),
                    style = TextStyle(
                        fontSize = 36.sp
                    )
                )
            }
            /*
            HorizontalDivider(
                thickness = 2.dp
            )
            */
            Row(modifier = Modifier.padding(30.dp)) {
                ElevatedButton(
                    onClick = {
                        AuthManager.authToken = null
                        onLogout()
                    }
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = "로그아웃 아이콘",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp)
                    )
                }
                Text(
                    text = "로그아웃",
                    modifier = Modifier
                        .padding(start = 50.dp)
                        .align(Alignment.CenterVertically),
                    style = TextStyle(
                        fontSize = 36.sp
                    )
                )
            }
            /*
            HorizontalDivider(
                thickness = 2.dp
            )
            */
        }
    }


    var changedUserName by remember { mutableStateOf("") }
    if(usernameEditDialogOpen) {
        Dialog(
            onDismissRequest = { usernameEditDialogOpen = false }
        ) {
            Surface(shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("변경할 이름을 입력해주세요.")
                    OutlinedTextField(
                        value = changedUserName,
                        onValueChange = {changedUserName = it},
                        singleLine = true,
                        modifier = Modifier.padding(10.dp)
                    )
                    Row {
                        Button(onClick = {
                            Log.d("USERNAME-EDIT", "유저네임 변경 요청 송신... 새 유저네임: ${changedUserName}")
                            viewModel.changeUserName(changedUserName)
                            usernameEditDialogOpen = false
                        }) {
                            Text(text = "확인")
                        }
                        Button(onClick = {
                            usernameEditDialogOpen = false
                        }) {
                            Text(text = "취소")
                        }
                    }
                }
            }
        }
    }
}

// 앱별 설정은 추후 개선 예정으로 남겨둔다.
@Composable
fun AppSettingsScreen(onAppSettingsClose: () -> Unit) {

    val currentappCategorySettings = AppCategorySettings(loggedInUser.getValue("username"))
    var currentAppNameList: Array<String>

    // 사용 기록에서 앱 이름만 가져와서 key로 사용 예정. 일단 임시로 테스트용 데이터를 넣어둠.
    currentappCategorySettings.getAppListFromUsageStats(context = LocalContext.current)

    currentAppNameList = currentappCategorySettings.getAppNameSet().toTypedArray()
    var numApps = currentAppNameList.size
    Log.d("APP-CAT-SCREEN", "현재 인식된 앱 개수: ${numApps}")

    Column(modifier = Modifier){
        Row(modifier = Modifier){
            IconButton(
                onClick = onAppSettingsClose,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 20.dp),
                content = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
            )
            Text(
                text = "앱별 설정 (준비 중 기능)",
                modifier = Modifier.align(Alignment.CenterVertically),
                style = TextStyle(
                    fontSize = 24.sp
                )
            )
        }
        LazyColumn(modifier = Modifier) {
            items(numApps) { index ->
                SettingRow(
                    currentAppNameList[index],
                    currentappCategorySettings.getAppCategory(currentAppNameList[index]),
                    currentappCategorySettings::setAppCategory
                )
            }
        }
    }
}

// TODO 레이아웃이 일자로 보이도록 수정.
@Composable
fun SettingRow(appName: String, currentState: Int, onSettingUpdate: (String, Int) -> Unit) {
    val CATEGORY_NAMES: Array<String> = arrayOf("여가", "공부")
    var expanded by remember { mutableStateOf(false) }
    var currentCategory by remember { mutableStateOf(CATEGORY_NAMES[currentState]) }

    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text = appName)

        Box(
            modifier = Modifier.padding(16.dp)
                .align(Alignment.Top)
        ) {
            Row(modifier = Modifier.align(Alignment.TopEnd)){
                Text(text = currentCategory)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "카테고리 보기 버튼")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("여가") },
                        onClick = {
                            onSettingUpdate(appName, 0)
                            currentCategory = CATEGORY_NAMES[0]
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("공부") },
                        onClick = {
                            onSettingUpdate(appName, 1)
                            currentCategory = CATEGORY_NAMES[1]
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}