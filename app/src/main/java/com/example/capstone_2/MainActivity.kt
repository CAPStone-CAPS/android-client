package com.example.capstone_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.capstone_2.ui.navigation.MainScreen
import com.example.capstone_2.ui.theme.CapstoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CapstoneTheme {
                MainScreen(context = this@MainActivity)
            }
        }
    }
}