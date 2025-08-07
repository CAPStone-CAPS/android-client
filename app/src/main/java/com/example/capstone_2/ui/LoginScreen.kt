package com.example.capstone_2.ui

import android.content.Context
import android.os.Bundle
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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.capstone_2.R
import com.example.capstone_2.data.LoginSession
import com.example.capstone_2.data.User
import com.example.capstone_2.retrofit.LoginService
import com.example.capstone_2.retrofit.NullableUserRequest
import com.example.capstone_2.retrofit.RetrofitInstance
import com.example.capstone_2.retrofit.UserRequest
import com.example.capstone_2.ui.theme.CapstoneTheme
import kotlinx.coroutines.launch
import retrofit2.Response

// 앱별로 사용자가 지정한 카테고리를 Int로 저장한다.
// 카테고리: 0 = 놀기(기본), 1 = 공부.
data class AppCategorySettings (val username: String) {
    val SETTING_LEISURE = 0
    val SETTING_PRODUCTIVE = 1

    private var categoryMap : MutableMap<String, Int> = mutableMapOf<String, Int>()

    fun addApp(appName: String) {
        categoryMap.put(appName, 0)
    }

    fun setAppCategory(appName: String, newCategory: Int) {
        categoryMap.put(appName, newCategory)
    }

    fun getAppCategory(appName: String): Int {
        return categoryMap.getValue(appName)
    }

    fun getAllAppCategory(): Map<String, Int> {
        return categoryMap.toMap()
    }

    fun getAppNameSet(): Set<String> {
        return categoryMap.keys
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CapstoneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginMypageScreen(
                        context = baseContext,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// 현재 사용하고 있는 토큰. 구현 문제로 일단 전역변수로 설정.
var currentToken: String? = null

/*
* 랜딩 페이지가 없으므로, 로그인 상태라면 마이페이지, 그렇지 않으면 로그인 페이지가 나오도록 한다.
* */

@Composable
fun LoginMypageScreen(context: Context, modifier: Modifier) {
    var userLoggedIn by rememberSaveable { mutableStateOf(false) }
    var appSettingsPageOpen by rememberSaveable { mutableStateOf(false) }

    Surface(Modifier) {
        if(userLoggedIn) {
            if(appSettingsPageOpen) {
                AppSettingsScreen (
                    onAppSettingsClose = { appSettingsPageOpen = false }
                )
            } else {
                MyPageScreen(
                    onAppSettingsOpen = { appSettingsPageOpen = true },
                    onLogout = { userLoggedIn = false }
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
                    currentToken = response.body()!!.accessToken
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
                        currentToken = loginResponse.body()!!.accessToken
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
}

// API로 유저네임 변경 요청을 전송하고 성공 여부를 반환.
fun sendUsernameChangeRequest(username: String): Boolean {
    // TODO
    return true
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

    if (loginSuccess) {
        // 로그인 성공 시 콜백 호출
        LaunchedEffect(Unit) {
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
                val response = retrofitInstance.getUser(currentToken!!)
                if(response.isSuccessful) {
                    username = response.body()!!.username
                    if(response.body()!!.profile_image_url != null) {
                        profileimageExists = true
                        profileURL = response.body()!!.profile_image_url!!
                    } else {
                        profileimageExists = false
                    }
                }
            } catch (e: Exception) {
                errorMessage = "오류 발생: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun changeUserName() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = retrofitInstance.editUser(currentToken!!, NullableUserRequest(username, null))
                if(response.isSuccessful) {
                    username = response.body()!!.username
                } else {
                    errorMessage = "이름 변경에 실패하였습니다. (${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "오류 발생: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun MyPageScreen(onAppSettingsOpen: () -> Unit, onLogout: () -> Unit, viewModel: MyPageViewModel = viewModel()) {
    val username = viewModel.username
    var newUsername = viewModel.newUsername
    val profileimageExists = viewModel.profileimageExists
    val profileURL = viewModel.profileURL
    val errorMessage = viewModel.errorMessage
    val isLoading = viewModel.isLoading

    var usernameEditDialogOpen by remember { mutableStateOf(false) }
    var profileEditDialogOpen by remember { mutableStateOf(false) }

    val LoggedInUser = mutableMapOf<String, String>(
        "id" to "0", "username" to "testuser"
    )

    viewModel.getUser()

    Surface(Modifier) {
        Column(Modifier) {
            if(profileimageExists) {
                AsyncImage(
                    model = profileURL,
                    contentDescription = "프로필 사진",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(210.dp, 260.dp)
                        .padding(start = 30.dp, end = 30.dp, top = 100.dp, bottom = 10.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile_picture),
                    contentDescription = LoggedInUser.getValue("username") + "님의 프로필 사진",
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
                        // TODO 그룹 탭으로 이동..
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
                        currentToken = null
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

    if(usernameEditDialogOpen) {
        Dialog(
            onDismissRequest = { usernameEditDialogOpen = false }
        ) {
            Surface(shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("변경할 이름을 입력해주세요.")
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = {newUsername = it},
                        singleLine = true,
                        modifier = Modifier.padding(10.dp)
                    )
                    Row {
                        Button(onClick = {
                            viewModel.changeUserName()
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
    val LoggedInUser = mapOf<String, String>(
        "id" to "0", "username" to "testuser"
    )
    val currentappCategorySettings : AppCategorySettings = AppCategorySettings(LoggedInUser.getValue("username"))
    var currentAppNameList: Array<String>

    // 사용 기록에서 앱 이름만 가져와서 key로 사용 예정. 일단 임시로 테스트용 데이터를 넣어둠.
    currentappCategorySettings.addApp("App1")
    currentappCategorySettings.addApp("App2")
    currentappCategorySettings.addApp("App3")
    currentappCategorySettings.addApp("App4")
    currentappCategorySettings.addApp("App5")

    currentAppNameList = currentappCategorySettings.getAppNameSet().toTypedArray()
    var numApps = currentAppNameList.size

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

// TODO 레이아웃이 일자로 보이도록 수정. 앱 아이콘을 추가할 수 있을지 확인.
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
        ) {
            Row(){
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