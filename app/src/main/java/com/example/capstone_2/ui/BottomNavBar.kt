package com.example.capstone_2.ui

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.capstone_2.ui.theme.SierraBlue

@Composable
fun BottomNavBar() {
    NavigationBar(
        //containerColor = SierraBlue,
        modifier = Modifier.height(96.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            //label = { Text("Home") },
            selected = true,
            onClick = { /* TODO: 선택 기능 구현 예정 */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            //label = { Text("History") },
            selected = false,
            onClick = { /* TODO */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Group, contentDescription = "Group") },
            //label = { Text("Group") },
            selected = false,
            onClick = { /* TODO */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "My Page") },
            //label = { Text("My Page") },
            selected = false,
            onClick = { /* TODO */ }
        )
    }
}
