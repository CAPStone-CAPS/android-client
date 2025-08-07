package com.example.capstone_2.ui

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
    val groups = listOf(
        Group("유튜브 줄이기", 12, R.drawable.black),
        Group("그룹2", 8, R.drawable.black)
    )

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

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                groups.forEach { group ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGroupClick(group.name) }
                    ) {
                        Image(
                            painter = painterResource(id = group.imageRes),
                            contentDescription = "Group Image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(group.name, fontSize = 20.sp, color = Color.Black)
                            Text("${group.memberCount}명", fontSize = 14.sp, color = Color.Gray)
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
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = groupGoal,
                onValueChange = { groupGoal = it },
                label = { Text("그룹 목표") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    // TODO: 생성된 데이터를 서버로 전달하거나 저장
                    onBack()
                },
                enabled = isValid,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("완료")
            }
        }
    }
}