package com.example.capstone_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.capstone_2.ui.*
//import com.example.capstone_2.ui.navigation.MainScreen
import com.example.capstone_2.ui.theme.CapstoneTheme
import com.example.capstone_2.ui.theme.RomanticBlue
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 시스템 UI 오버레이 설정
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CapstoneTheme {
                var selectedTab by remember { mutableStateOf(0) }

                // 상태바 색 적용 (로맨틱 블루)
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = RomanticBlue,
                        darkIcons = false
                    )
                }

                Scaffold(
                    modifier = Modifier.background(RomanticBlue),
                    containerColor = RomanticBlue,
                    bottomBar = {
                        BottomNavBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.White) // 본문만 흰색
                    ) {
                        when (selectedTab) {
                            0 -> AppUsageTrackerScreen(context = this@MainActivity)
                            1 -> HistoryScreen()
                            2 -> GroupScreen()
                            3 -> Text("마이페이지 (준비 중)", modifier = Modifier.padding(16.dp))
                        }
                    }
                }

//                MainScreen(context = this@MainActivity)
            }
        }
    }
}