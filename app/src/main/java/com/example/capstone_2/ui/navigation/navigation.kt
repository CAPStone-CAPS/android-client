package com.example.capstone_2.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.capstone_2.ui.AppUsageTrackerScreen
import com.example.capstone_2.ui.LoginMypageScreen
import com.example.capstone_2.ui.history.HistoryScreen

@Composable
fun MainScreen(context: Context) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                AppUsageTrackerScreen(context = context)
            }
            composable("history") {
                HistoryScreen()
            }
            composable("group") {
                PlaceholderScreen("그룹 화면")
            }
            composable("profile") {
                LoginMypageScreen(context = context, modifier = Modifier)
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        modifier = Modifier.height(96.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            selected = currentRoute == "home",
            onClick = { 
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            selected = currentRoute == "history",
            onClick = { 
                navController.navigate("history") {
                    popUpTo("home")
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Group, contentDescription = "Group") },
            selected = currentRoute == "group",
            onClick = { 
                navController.navigate("group") {
                    popUpTo("home")
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "My Page") },
            selected = currentRoute == "profile",
            onClick = { 
                navController.navigate("profile") {
                    popUpTo("home")
                }
            }
        )
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}