package com.example.capstone_2.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionErrors
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.loginandmypage.ui.theme.LoginAndMypageTheme
import kotlin.math.exp

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
            LoginAndMypageTheme {
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
                    onAppSettingsClose = {appSettingsPageOpen = false}
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

// API로 로그인 요청을 전송하고 로그인 성공 여부를 반환.
fun sendLoginRequest(id: String, password: String): Boolean {
    // TODO 로그인 API를 호출. 로그인 성공 여부를 반환.
    return true
}

// 로그아웃. API에 연동해서 처리하는 게 아닌 경우에는 이름 변경 예정.
fun sendLogoutRequest(): Boolean {
    return true
}

// API로 유저네임 변경 요청을 전송하고 성공 여부를 반환.
fun sendUsernameChangeRequest(username: String): Boolean {
    // TODO
    return true
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier, onLogin: () -> Unit) {

    var inputId by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "로그인",
            style = TextStyle(
                fontSize = 48.sp
            ),
            modifier = Modifier.padding(36.dp)
        )
        OutlinedTextField(
            value = inputId,
            onValueChange = { inputId = it },
            label = { Text("ID") },
            modifier = Modifier
                .padding(40.dp, 8.dp)
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = inputPassword,
            onValueChange = {inputPassword = it},
            label = { Text("Password") },
            modifier = Modifier
                .padding(40.dp, 8.dp)
                .fillMaxWidth()
        )
        Button(
            onClick = {
                var loginSuccess = sendLoginRequest(inputId, inputPassword)
                if(loginSuccess) {
                    onLogin()
                } else {
                    // TODO Snackbar 또는 Dialog를 띄워서 로그인 실패를 알림.
                }
            },
            content = { Text("NEXT") },
            modifier = Modifier
                .padding(40.dp, 8.dp)
                .fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(onAppSettingsOpen: () -> Unit, onLogout: () -> Unit) {
    var usernameEditDialogOpen by remember { mutableStateOf(false) }
    var profileEditDialogOpen by remember { mutableStateOf(false) }

    val LoggedInUser = mapOf<String, String>(
        "id" to "0", "username" to "testuser"
    )

    Surface(Modifier) {
        Column(Modifier) {
            Image(
                painter = painterResource(id = R.drawable.profile_picture),
                contentDescription = LoggedInUser.getValue("username") + "님의 프로필 사진",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(210.dp, 260.dp)
                    .padding(start = 30.dp, end = 30.dp, top = 100.dp, bottom = 10.dp)
            )
            Row() {
                Text(
                    text = LoggedInUser.getValue("username"),
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
            HorizontalDivider(
                thickness = 2.dp
            )
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
            HorizontalDivider(
                thickness = 2.dp
            )
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
            HorizontalDivider(
                thickness = 2.dp
            )
            Row(modifier = Modifier.padding(30.dp)) {
                ElevatedButton(
                    onClick = {
                        sendLogoutRequest()
                        onLogout()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
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
            HorizontalDivider(
                thickness = 2.dp
            )
        }
    }

    var newUsername by rememberSaveable { mutableStateOf(LoggedInUser.getValue("username")) }

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
                            sendUsernameChangeRequest(newUsername)
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


@Composable
fun AppSettingsScreen(onAppSettingsClose: () -> Unit) {
    val LoggedInUser = mapOf<String, String>(
        "id" to "0", "username" to "testuser"
    )
    val currentappCategorySettings : AppCategorySettings = AppCategorySettings(LoggedInUser.getValue("username"))
    var currentAppNameList: Array<String>

    // 사용 기록에서 앱 이름만 가져와서 key로 사용. 일단 임시로 테스트용 데이터를 넣어둠.
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
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
            )
            Text(
                text = "앱별 설정",
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