package com.example.capstone_2.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import com.example.capstone_2.ui.theme.SkypeBlue

@Composable
fun TabSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = listOf("Home", "Details", "AI SUM")

    TabRow(
        selectedTabIndex = selectedTab
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    }
}
