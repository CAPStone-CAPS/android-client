package com.example.capstone_2.ui

import com.example.capstone_2.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                val navController = rememberNavController()
                NavGraph(navController)
        }
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "groupList") {
        composable("groupList") {
            Scaffold(
                bottomBar = { BottomNavBar(currentIndex = 2) },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navController.navigate("groupCreate") },
                        containerColor = Color(0xFF7A67EE),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Group", tint = Color.White)
                    }
                },
                containerColor = Color.White
            ) { innerPadding ->
                GroupScreen(
                    hasGroups = true,
                    innerPadding = innerPadding,
                    onSearchClick = { /* TODO: 그룹 검색 */ },
                    onAddGroupClick = { /* handled by navController */ }
                )
            }
        }

        composable("groupCreate") {
            GroupCreateScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { name, goal ->
                    // TODO: 서버에 그룹 생성 로직 수행
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun GroupScreen(
    hasGroups: Boolean = false,
    innerPadding: PaddingValues = PaddingValues(),
    onSearchClick: () -> Unit = {},
    onAddGroupClick: () -> Unit = {}
) {
    Scaffold(
        bottomBar = { BottomNavBar(currentIndex = 2) },
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

            if (hasGroups) {
                GroupListScreen()
            } else {
                GroupEmptyScreen(onSearchClick)
            }
        }
    }
}

@Composable
fun GroupListScreen() {
    // 임시로 예시 그룹 데이터 추가
    val groups = listOf(
        Group("유튜브 줄이기", 12, R.drawable.black),
        Group("그룹2", 8, R.drawable.black)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        groups.forEach { group ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = group.imageRes),
                    contentDescription = "Group Image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = group.name,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${group.memberCount}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun GroupEmptyScreen(onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("아직 그룹이 없네요..", fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("그룹에 참여해보세요!", fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Group",
                        tint = Color(0xFF7A67EE)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(currentIndex: Int = 0) {
    NavigationBar {
        val items = listOf("Home", "History", "Group", "My Page")
        val icons = listOf(
            Icons.Default.Home,
            Icons.Default.Home,
            Icons.Default.Home,
            Icons.Default.Person
        )

        items.forEachIndexed { index, label ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = label) },
                label = { Text(label) },
                selected = index == currentIndex,
                onClick = { /* TODO: 탭 전환 로직 */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreateScreen(
    onBack: () -> Unit,
    onSubmit: (groupName: String, groupGoal: String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var groupGoal by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("그룹생성") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("그룹 이름") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = groupGoal,
                onValueChange = { groupGoal = it },
                label = { Text("그룹 목표") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { onSubmit(groupName, groupGoal) }) {
                    Text("완료")
                }
            }
        }
    }
}

// 그룹 데이터 클래스
data class Group(
    val name: String,
    val memberCount: Int,
    val imageRes: Int
)